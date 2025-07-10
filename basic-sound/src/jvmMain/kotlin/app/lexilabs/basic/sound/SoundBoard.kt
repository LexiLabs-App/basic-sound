package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.jar.JarFile
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine.Info
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineUnavailableException

public actual class SoundBoard actual constructor (context: Any?): SoundBoardBuilder {

    private val tag: String = "SoundBoard"
    private val audioInputStreams: MutableMap<String, Pair<AudioFormat, ByteArray>> =
        mutableMapOf()

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf()
    public actual override val mixer: MixerChannel = Channel()

    public actual override fun powerUp() {
        Log.i(tag, "launch:starting to load sounds")
        soundBytes.forEachIndexed { _, soundByte ->
            val originalStream = AudioSystem.getAudioInputStream(getJarResourceAsInputStream(soundByte.localPath))
            // If MP3, convert to MP3 Format, otherwise, use standard format
            val newFormat = if (soundByte.localPath.lowercase().endsWith(".mp3")) {
                getMp3Format(originalStream.format)
            } else { originalStream.format }
            // Create new stream using updated format.
            val newStream = AudioSystem.getAudioInputStream(newFormat, originalStream)
            audioInputStreams[soundByte.name] =
                Pair(newFormat, newStream.readAllBytes())
        }
        startMixer()
        Log.i(tag, "launch:complete")
    }

    private fun startMixer() {
        Log.i(tag, "startMixer:starting")
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val result = mixer.receiveCatching()
                when (val name = result.getOrNull()) {
                    null -> break
                    else -> {
                        audioInputStreams[name]?.let { (format, byteArray) ->
                            val clip = getClip(format)
                            clip.open(format, byteArray, 0, byteArray.size)
                            Log.d(tag, "Mixer: clip `$name` starting")
                            clip.start()
                            clip.addLineListener { event ->
                                if (event.type == LineEvent.Type.STOP) {
                                    Log.d(tag, "Mixer: clip `$name` closing")
                                    clip.close()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public actual override fun powerDown() {
        audioInputStreams.clear()
        soundBytes.clear()
        mixer.close()
    }

    private fun getJarResourceAsInputStream(path: String): InputStream {
        // Split path for jar location from the path for jar interior
        val parts = path.split("!")
        // All Jar paths to resources have two parts
        if (parts.size != 2) throw IllegalArgumentException("Invalid JAR file path format: $path")
        // First part of jar path always starts with "jar:file:"
        val jarFilePath = parts[0].substringAfter("jar:file:")
        // Once you're inside the jar, you don't need to use "/" before the first folder
        val entryPath = parts[1].removePrefix("/")
        // Get Jar file and extract the local file from the jar
        val jarFile = JarFile(jarFilePath)
        val jarEntry = jarFile.getJarEntry(entryPath)
            ?: throw IllegalStateException("File not found in JAR: $entryPath")
        // Convert to input stream and return
        return BufferedInputStream(jarFile.getInputStream(jarEntry))
    }

    private fun getMp3Format(inFormat: AudioFormat): AudioFormat {
        val ch = inFormat.channels
        val rate = inFormat.sampleRate
        return AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false)
    }

    private fun createClipInfo(format: AudioFormat): Info {
        return Info(Clip::class.java, format)
    }

    private fun getClip(format: AudioFormat): Clip {
        try {
            val info = createClipInfo(format)
            return AudioSystem.getLine(info) as Clip
        } catch (e: LineUnavailableException) {
            e.printStackTrace()
            throw LineUnavailableException("Clip not available: ${e.message}")
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            throw IllegalArgumentException("Illegal argument for Clip: ${e.message}")
        }
    }
}