package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.w3c.dom.Audio

public actual class SoundBoard actual constructor(context: Any?): SoundBoardBuilder {

    private val tag: String = "SoundBoard"
    private val audioObjects: MutableMap<String, Audio> = mutableMapOf<String, Audio>()

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf<SoundByte>()
    public actual override val mixer: MixerChannel = Channel<String>()

    public actual override fun powerUp() {
        Log.i(tag, "launch:starting to load sounds")
        soundBytes.forEachIndexed { index, soundByte ->
            Log.i(tag, "launch:adding ${soundByte.name}")
            audioObjects[soundByte.name] = Audio(soundByte.localPath)
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
                        audioObjects[name]?.play()
                    }
                }
            }
        }
    }

    public actual override fun powerDown() {
        mixer.close()
        audioObjects.clear()
        soundBytes.clear()
    }
}