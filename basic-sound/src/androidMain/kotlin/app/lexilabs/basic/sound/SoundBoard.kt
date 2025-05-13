package app.lexilabs.basic.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import app.lexilabs.basic.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

public actual class SoundBoard actual constructor(context: Any?): SoundBoardBuilder {

    private val tag: String = "SoundBoard"
    private val ctx: Context = context as Context
    private val soundPool: SoundPool
    private val audioIds: MutableMap<String, Int> = mutableMapOf<String, Int>()

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf<SoundByte>()
    public actual override val mixer: MixerChannel = Channel<String>()

    init {
        require(context != null) {
            "context for Soundboard cannot be null in Android"
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    public actual override fun powerUp() {
        Log.i(tag, "launch:starting to load sounds")
        soundBytes.forEachIndexed { index, soundByte ->
            Log.i(tag, "launch:adding ${soundByte.name}")
            val path = soundByte.localPath.removePrefix("file:///android_asset/")
            val fd = ctx.assets.openFd(path)
            audioIds[soundByte.name] = soundPool.load(fd, 1)
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
                        audioIds[name]?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) }
                    }
                }
            }
        }
    }

    public actual override fun powerDown() {
        mixer.close()
        soundPool.release()
        audioIds.clear()
        soundBytes.clear()
    }
}