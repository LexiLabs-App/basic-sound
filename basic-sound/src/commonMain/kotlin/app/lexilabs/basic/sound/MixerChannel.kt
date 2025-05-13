package app.lexilabs.basic.sound

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * A version of [Channel] used to play sounds on a [SoundBoard]
 * @see MixerChannel.play
 */
public typealias MixerChannel = Channel<String>

/**
 * Used to play a [SoundByte] on a [SoundBoard],
 * but requires a [SoundBoard.powerUp] first
 * @param soundName a string matching the name of a [SoundByte] you wish to play
 * @see SoundBoard.mixer
 */
public fun MixerChannel.play(soundName: String){
    CoroutineScope(Dispatchers.Default).launch {
        this@play.send(soundName)
    }
}

/**
 * Used to play a [SoundByte] on a [SoundBoard],
 * but requires a [SoundBoard.powerUp] first
 * @param sound the [SoundByte] you wish to play (must already be loaded onto your [SoundBoard])
 * @see SoundBoard.mixer
 */
public fun MixerChannel.play(sound: SoundByte) {
    CoroutineScope(Dispatchers.Default).launch {
        this@play.send(sound.name)
    }
}
