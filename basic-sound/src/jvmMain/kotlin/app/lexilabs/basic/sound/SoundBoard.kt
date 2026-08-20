package app.lexilabs.basic.sound

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.jar.JarFile
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

public actual class SoundBoard : SoundBoardBuilder {

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf()

    public actual override val mixer: MixerChannel = Channel(Channel.UNLIMITED)

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Composable
    public actual override fun PowerUp() {
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
                        clip.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @Composable
    public actual override fun PowerDown() {
        job.cancel()
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
}
