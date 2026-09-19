package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.Audio

@OptIn(ExperimentalBasicSound::class)
public actual class Audio public actual constructor() : AudioBuilder {

    private val tag = "Basic-Sound Audio"
    public actual var resource: String = ""
    public actual var autoPlay: Boolean = false

    private val _audioState = MutableStateFlow<AudioState>(AudioState.NONE)
    public actual override val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private var player: Audio? = null

    private var _volume: Float = 1.0f
    public actual override var volume: Float
        get() = _volume
        set(value) {
            _volume = value.coerceIn(0.0f, 1.0f)
            try {
                player?.volume = _volume.toDouble()
            } catch (e: Exception) {
                Log.e(tag, "setVolume:failure: $e")
            }
        }

    private var _isLooping: Boolean = false
    public actual override var isLooping: Boolean
        get() = _isLooping
        set(value) {
            _isLooping = value
            try {
                player?.loop = value
            } catch (e: Exception) {
                Log.e(tag, "setLooping:failure: $e")
            }
        }

    public actual override val duration: Long
        get() = try {
            player?.duration?.let { d ->
                if (d.isFinite() && !d.isNaN()) (d * 1000.0).toLong().coerceAtLeast(0L) else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }

    public actual override val currentPosition: Long
        get() = try {
            player?.currentTime?.let { t ->
                if (t.isFinite() && !t.isNaN()) (t * 1000.0).toLong().coerceAtLeast(0L) else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }

    public actual constructor(resource: String, autoPlay: Boolean) : this() {
        this.resource = resource
        this.autoPlay = autoPlay
        load()
    }

    public actual constructor(context: Any?, composeResource: String, autoPlay: Boolean) : this() {
        this.resource = composeResource
        this.autoPlay = autoPlay
        load()
    }

    public actual override fun seekTo(positionMs: Long) {
        try {
            player?.currentTime = positionMs.coerceAtLeast(0L) / 1000.0
        } catch (e: Exception) {
            Log.e(tag, "seekTo:failure: $e")
        }
    }

    actual override fun load(context: Any?) {
        try {
            _audioState.value = AudioState.LOADING
            player = Audio(resource).apply {
                volume = _volume.toDouble()
                loop = _isLooping
            }
            player?.oncanplaythrough?.let { _ ->
                {
                    _audioState.value = AudioState.READY
                    if (autoPlay) {
                        play()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "load:failure: $e")
            _audioState.value = AudioState.ERROR("load:failure: $e")
        }
    }

    actual override fun play() {
        try {
            player?.let {
                if (audioState.value !is AudioState.PLAYING) {
                    when (audioState.value) {
                        is AudioState.ERROR -> {
                            throw Exception("play:AudioState.ERROR: ${(audioState.value as AudioState.ERROR).message}")
                        }
                        is AudioState.LOADING -> { /** DO NOTHING **/ }
                        is AudioState.NONE -> {
                            throw IllegalStateException("play:AudioState.NONE: mediaPlayer not initialized")
                        }
                        is AudioState.PAUSED -> {
                            it.play()
                            _audioState.value = AudioState.PLAYING
                        }
                        is AudioState.PLAYING -> { /** DO NOTHING **/ }
                        is AudioState.READY -> {
                            it.play()
                            _audioState.value = AudioState.PLAYING
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "play:failure: $e")
            _audioState.value = AudioState.ERROR("play:failure: $e")
        }
    }

    actual override fun pause() {
        try {
            player?.let {
                if (audioState.value is AudioState.PLAYING) {
                    it.pause()
                    _audioState.value = AudioState.PAUSED
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "pause:failure: $e")
            _audioState.value = AudioState.ERROR("pause:failure: $e")
        }
    }

    actual override fun stop() {
        try {
            player?.let {
                if (audioState.value is AudioState.PLAYING) {
                    it.pause()
                    it.currentTime = 0.0
                    _audioState.value = AudioState.READY
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "stop:failure: $e")
            _audioState.value = AudioState.ERROR("stop:failure: $e")
        }
    }

    actual override fun release() {
        try {
            _audioState.value = AudioState.NONE
            player?.let {
                it.pause()
                it.src = ""
                it.load()
            }
            player = null
        } catch (e: Exception) {
            Log.e(tag, "release:failure: $e")
            _audioState.value = AudioState.ERROR("release:failure: $e")
        }
    }
}