@file:Suppress("ConvertSecondaryConstructorToPrimary")

package app.lexilabs.basic.sound

import kotlinx.coroutines.flow.StateFlow

/**
 * Play audio from a url ([String]).
 *
 * Example:
 * ```
 * val audioUrl = "https://dare.wisc.edu/wp-content/uploads/sites/1051/2008/11/MS072.mp3"
 * val audio = Audio(audioUrl, true) // AutoPlay is marked "true"
 * ```
 */
@ExperimentalBasicSound
public expect class Audio() {

    /**
     * Play audio from a url or path ([String]).
     *
     * @param resource provides the link to the audio file online
     * @param autoPlay [play] after [AudioState.READY] is reached (defaults to "false").
     *
     * Example:
     * ```
     * val audioUrl = "https://dare.wisc.edu/wp-content/uploads/sites/1051/2008/11/MS072.mp3"
     * val audio = Audio(audioUrl, true) // AutoPlay is marked "true"
     * ```
     */
    public constructor(resource: String, autoPlay: Boolean = false)

    /**
     * Play audio from a url or path ([String]).
     *
     * @param context provide the Platform Context (this is mostly to accommodate Android)
     * @param composeResource provides the link to the audio file online
     * @param autoPlay [play] after [AudioState.READY] is reached (defaults to "false").
     *
     * Example:
     * ```
     * val audioUrl = "https://dare.wisc.edu/wp-content/uploads/sites/1051/2008/11/MS072.mp3"
     * val audio = Audio(audioUrl, true) // AutoPlay is marked "true"
     * ```
     */
    public constructor(context: Any?, composeResource: String, autoPlay: Boolean = false)

    /**
     * Provides the state of [Audio] after initialization
     *
     * @property[NONE] indicates merely an initial state where no audio has loaded
     * @property[LOADING] an audio file is being loaded
     * @property[READY] sound is ready to be played
     * @property[PLAYING] player is currently playing audio, even if not audible
     * @property[PAUSED] player has stopped but retained its progress / timecode
     * @property[ERROR] Something unexpected occurred. Error will be printed to logs
    */
    public val audioState: StateFlow<AudioState>

    /**
     * Used to load an [Audio] file when [AudioState.NONE].
     *
     * Sets the [audioState] to [AudioState.READY]
     *
     * Example:
     * ```
     * val audio = Audio(audioUrl) // AutoPlay defaults to "false"
     * audio.play() // plays the sound immediately
     * ```
     */
    public fun load(context: Any? = null)

    /**
     * Used after [Audio] is initialized with [AudioState.READY] to play the sound immediately.
     *
     * Sets the [audioState] to [AudioState.PLAYING]
     *
     * Example:
     * ```
     * val audio = Audio(audioUrl) // AutoPlay defaults to "false"
     * audio.play() // plays the sound immediately
     * ```
     */
    public fun play()

    /**
     * Used when [Audio] is [AudioState.PLAYING] to pause the sound to be continued later.
     *
     * Sets the [audioState] to [AudioState.PAUSED]
     *
     * Example:
     * ```
     * val audio = Audio(audioUrl) // AutoPlay defaults to "false"
     * audio.play() // plays the sound immediately
     * // more code
     * audio.pause() // paused until play is called again
     * // more code
     * audio.play() // sound resumes
     * ```
     */
    public fun pause()

    /**
     * Used to reset the [Audio] without reloading from the sound file.
     *
     * Sets the [audioState] to [AudioState.READY]
     *
     * Example:
     * ```
     * val audio = Audio(audioUrl) // AutoPlay defaults to "false"
     * audio.play() // plays the sound immediately
     * // more code
     * audio.stop() // now ready to play from the beginning again
     * ```
     */
    public fun stop()

    /**
     * Used when done to clear the [Audio] object from memory.
     **
     * Example:
     * ```
     * val audio = Audio(audioUrl) // AutoPlay defaults to "false"
     * audio.play() // plays the sound immediately
     * // more code
     * audio.release() // audio can no longer be called
     * ```
     */
    public fun release()

    /**
     * A url link as a string:
     **
     * Example:
     * ```
     * url = "https://dare.wisc.edu/wp-content/uploads/sites/1051/2008/11/MS072.mp3"
     * ```
     */
    public var resource: String

    /**
     * If true, [Audio] runs [play] immediately after reaching [AudioState.READY]
     *
     * Example:
     * ```
     * autoPlay = true // AutoPlay defaults to "false"
     * ```
     */
    public var autoPlay: Boolean

    /**
     * Playback volume of the [Audio] from `0.0f` (silence) to `1.0f` (full volume).
     *
     * Example:
     * ```
     * audio.volume = 0.5f // Set to 50% volume
     * ```
     */
    public var volume: Float

    /**
     * If true, [Audio] loops continuously until stopped or paused.
     *
     * Example:
     * ```
     * audio.isLooping = true // loops indefinitely
     * ```
     */
    public var isLooping: Boolean

    /**
     * Total duration of the audio in milliseconds, or `0L` if unknown or not loaded.
     */
    public val duration: Long

    /**
     * Current playback position of the audio in milliseconds, or `0L` if not loaded.
     */
    public val currentPosition: Long

    /**
     * Seeks playback to the specified position in milliseconds.
     *
     * @param positionMs target position in milliseconds
     *
     * Example:
     * ```
     * audio.seekTo(5000L) // seek to 5 seconds
     * ```
     */
    public fun seekTo(positionMs: Long)
}