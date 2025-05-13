package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

@Deprecated(
    "AudioByte greedily eats up memory. Switch to the newer channel-based implementation, SoundBoard(context = Any?).",
    replaceWith = ReplaceWith("SoundBoard(context = null)"),
    level = DeprecationLevel.WARNING
)
public actual class AudioByte actual constructor() : AudioByteBuilder {

    private val tag = "BasicSound AudioByte"

    actual override fun load(context: Any, localPath: String): Any {
        return AudioSystem.getAudioInputStream(File(localPath).absoluteFile)
            ?: throw IllegalStateException("load:The path provided was invalid")
    }

    actual override fun play(item: Any) {
        try {
            val clip = AudioSystem.getClip()
            clip.open(item as AudioInputStream)
            clip.start()
        } catch (e: Exception) {
            Log.e(tag, "play:failure:$e")
        }
    }

    actual override fun release() {
        // Do nothing
    }
}