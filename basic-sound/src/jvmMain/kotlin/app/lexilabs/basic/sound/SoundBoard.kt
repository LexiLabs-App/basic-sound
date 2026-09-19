package app.lexilabs.basic.sound

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.Collections
import java.util.jar.JarFile
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import kotlin.math.log10

public actual class SoundBoard actual constructor(context: Any?) : SoundBoardBuilder {

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf()

    public actual override val mixer: MixerChannel = Channel(Channel.UNLIMITED)

    private val activeClips: MutableList<Clip> = Collections.synchronizedList(mutableListOf())

    private var _volume: Float = 1.0f
    public actual override var volume: Float
        get() = _volume
        set(value) {
            _volume = value.coerceIn(0.0f, 1.0f)
            synchronized(activeClips) {
                activeClips.forEach { clip ->
                    applyVolume(clip, _volume)
                }
            }
        }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    public actual override fun powerUp() {
        scope.launch {
            for (soundName in mixer) {
                soundBytes.find { it.name == soundName }?.let { soundByte ->
                    try {
                        val clip = AudioSystem.getClip()
                        val audioInputStream = AudioSystem.getAudioInputStream(getJarResourceAsInputStream(soundByte.localPath))
                        val stream = if (soundByte.localPath.lowercase().endsWith(".mp3")) {
                            AudioSystem.getAudioInputStream(getMp3Format(audioInputStream.format), audioInputStream)
                        } else { audioInputStream }
                        clip.open(stream)
                        applyVolume(clip, _volume)
                        activeClips.add(clip)
                        clip.addLineListener { event ->
                            if (event.type == LineEvent.Type.STOP) {
                                activeClips.remove(clip)
                                clip.close()
                            }
                        }
                        clip.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    public actual override fun stopAll() {
        synchronized(activeClips) {
            activeClips.forEach { clip ->
                try {
                    if (clip.isRunning) {
                        clip.stop()
                    }
                    clip.close()
                } catch (e: Exception) {
                    // ignore
                }
            }
            activeClips.clear()
        }
    }

    public actual override fun powerDown() {
        stopAll()
        job.cancel()
        mixer.close()
    }

    private fun applyVolume(clip: Clip, vol: Float) {
        try {
            if (clip.isOpen && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                if (vol <= 0.0001f) {
                    gainControl.value = gainControl.minimum
                } else {
                    val dB = (20f * log10(vol)).coerceIn(gainControl.minimum, gainControl.maximum)
                    gainControl.value = dB
                }
            }
        } catch (e: Exception) {
            // ignore
        }
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
}
