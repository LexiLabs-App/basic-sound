package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.net.URI
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException

public actual class SoundBoard actual constructor (context: Any?): SoundBoardBuilder {

    private val tag: String = "SoundBoard"
    private val audioInputStreams: MutableMap<String, AudioInputStream> =
        mutableMapOf<String, AudioInputStream>()

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf<SoundByte>()
    public actual override val mixer: MixerChannel = Channel<String>()

    public actual override fun powerUp() {
        Log.i(tag, "launch:starting to load sounds")
        soundBytes.forEach { soundByte ->
            Log.d(tag, "${soundByte.name} -> PATH=${soundByte.localPath}")
            // Check for generated.resources
            if (soundByte.localPath.contains(".generated.resources")) {
                val path = soundByte.localPath.substringAfter(".generated.resources/")
                val stream = object {}.javaClass.getResourceAsStream(path)
                audioInputStreams[soundByte.name] = AudioSystem.getAudioInputStream(stream)
            } else if (
                // Check for Local or absolute path resources
                soundByte.localPath.startsWith("file:", true) ||
                soundByte.localPath.startsWith("/", true)) {
                val stream = object {}.javaClass.getResourceAsStream(soundByte.localPath)
                    ?: throw Exception("Resource not found: ${soundByte.localPath}")
                audioInputStreams[soundByte.name] = AudioSystem.getAudioInputStream(stream)
            // Check for website
            } else if (
                    soundByte.localPath.startsWith("https:", true) ||
                    soundByte.localPath.startsWith("http:", true)
                ) {
                audioInputStreams[soundByte.name] = AudioSystem.getAudioInputStream(
                    URI(soundByte.localPath).toURL()
                )
            // Check if links to file inside the JAR
            } else if (soundByte.localPath.startsWith("jar:", true)) {
                val path = soundByte.localPath.substringAfter("!/")
                val stream = object {}.javaClass.getResourceAsStream("/$path")
                        ?: throw Exception("Resource not found: $path")
                audioInputStreams[soundByte.name] = AudioSystem.getAudioInputStream(stream)
            }
            else {
                throw UnsupportedAudioFileException("Unclear if this is a file or link")
            }
        }
        startMixer()
        Log.i(tag, "launch:complete")
    }

    private fun startMixer() {
        Log.i(tag, "startMixer:starting")
        val clip = AudioSystem.getClip()
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val result = mixer.receiveCatching()
                when (val name = result.getOrNull()) {
                    null -> break
                    else -> {
                        audioInputStreams[name]?.let {
                            clip.open(it)
                            clip.start()
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
}