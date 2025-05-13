package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.w3c.dom.Audio

public actual class SoundBoard actual constructor(context: Any?): SoundBoardBuilder {

    private val tag: String = "SoundBoard"
    private val audioPaths: MutableMap<String, String> = mutableMapOf<String, String>()

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf<SoundByte>()
    public actual override val mixer: MixerChannel = Channel<String>()

    public actual override fun powerUp() {
        Log.i(tag, "launch:starting to load sounds")
        soundBytes.forEachIndexed { index, soundByte ->
            Log.i(tag, "launch:adding ${soundByte.name}")
            audioPaths[soundByte.name] = soundByte.localPath
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
                        audioPaths[name]?.let {
                            val audio = Audio(it)
                            audio.play()
                            when (audio.paused) {
                                true -> releaseAudio(audio)
                                false -> { /* DO NOTHING */}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun releaseAudio(audio: Audio) {
        Log.i(tag, "releaseAudio")
        audio.pause()
        audio.src = ""
        audio.load()
        audio.srcObject = null
    }

    public actual override fun powerDown() {
        mixer.close()
        audioPaths.clear()
        soundBytes.clear()
    }
}