package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

public actual class SoundBoard actual constructor (context: Any?): SoundBoardBuilder {

    private val tag: String = "SoundBoard"
    private val audioInputStreams: MutableMap<String, AudioInputStream> =
        mutableMapOf<String, AudioInputStream>()

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf<SoundByte>()
    public actual override val mixer: MixerChannel = Channel<String>()

    public actual override fun powerUp() {
        Log.i(tag, "launch:starting to load sounds")
        soundBytes.forEachIndexed { index, soundByte ->
            audioInputStreams[soundByte.name] =
                AudioSystem.getAudioInputStream(File(soundByte.localPath).absoluteFile)
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