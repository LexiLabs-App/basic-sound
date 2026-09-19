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

    private val activeStreams: MutableSet<Int> = mutableSetOf()

    private var _volume: Float = 1.0f
    public actual override var volume: Float
        get() = _volume
        set(value) {
            _volume = value.coerceIn(0.0f, 1.0f)
            activeStreams.forEach { streamId ->
                soundPool.setVolume(streamId, _volume, _volume)
            }
        }

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
                        audioIds[name]?.let { soundId ->
                            val streamId = soundPool.play(soundId, _volume, _volume, 1, 0, 1f)
                            if (streamId != 0) {
                                activeStreams.add(streamId)
                            }
                        }
                    }
                }
            }
        }
    }

    public actual override fun stopAll() {
        activeStreams.forEach { streamId ->
            soundPool.stop(streamId)
        }
        activeStreams.clear()
        soundPool.autoPause()
    }

    public actual override fun powerDown() {
        mixer.close()
        stopAll()
        soundPool.release()
        audioIds.clear()
        soundBytes.clear()
    }
}