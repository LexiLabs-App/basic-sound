package app.lexilabs.basic.sound

import androidx.compose.runtime.Composable
import kotlinx.coroutines.channels.Channel

/**
 * Creates a standard [SoundBoard] format to replicate across all platforms
 * @property soundBytes a list of [SoundByte] values loaded by the user
 * @property mixer the [Channel] used to pass [MixerChannel.play] commands
 *
 * ```
 * // Creates SoundBoard instance
 * val soundBoard = SoundBoard(context)
 * // Creates SoundByte
 * val click = SoundByte("click", Res.getUri("files/click.mp3"))
 * // Loads SoundByte
 * soundBoard.load(click)
 * // Prepare to play sounds
 * soundBoard.powerUp()
 * // Play sounds
 * soundBoard.mixer.play("click")
 * soundBoard.mixer.play(click)
 * ```
 *
 * @see SoundBoardBuilder.PowerUp
 * @see SoundBoardBuilder.PowerDown
 * @see SoundBoardBuilder.load
 *
 */
public expect class SoundBoard: SoundBoardBuilder {

    /** a list of [SoundByte] values loaded by the user */
    override val soundBytes: MutableList<SoundByte>

    /** the [Channel] used to pass [MixerChannel.play] commands */
    override val mixer: MixerChannel

    /**
     * Used to load the [soundBytes] into memory and begin the listener for the [mixer] channel
     */
    @Composable
    override fun PowerUp()

    /**
     * Used to release resources used by [soundBytes], [mixer],
     * and other platform-specific resources
     */
    @Composable
    override fun PowerDown()
}
