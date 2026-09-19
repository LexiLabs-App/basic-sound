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

    private val activeAudios: MutableList<Audio> = mutableListOf<Audio>()

    private var _volume: Float = 1.0f
    public actual override var volume: Float
        get() = _volume
        set(value) {
            _volume = value.coerceIn(0.0f, 1.0f)
            activeAudios.forEach { audio ->
                try {
                    audio.volume = _volume.toDouble()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }

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
                            audio.volume = _volume.toDouble()
                            activeAudios.add(audio)
                            audio.onended = {
                                activeAudios.remove(audio)
                                releaseAudio(audio)
                            }
                            audio.play()
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

    public actual override fun stopAll() {
        activeAudios.forEach { audio ->
            releaseAudio(audio)
        }
        activeAudios.clear()
    }

    public actual override fun powerDown() {
        mixer.close()
        stopAll()
        audioPaths.clear()
        soundBytes.clear()
    }
}