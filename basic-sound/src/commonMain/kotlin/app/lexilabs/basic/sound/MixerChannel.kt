package app.lexilabs.basic.sound

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

public typealias MixerChannel = Channel<String>

public fun MixerChannel.play(soundName: String){
    CoroutineScope(Dispatchers.Default).launch {
        this@play.send(soundName)
    }
}

public fun MixerChannel.play(sound: SoundByte) {
    CoroutineScope(Dispatchers.Default).launch {
        this@play.send(sound.name)
    }
}
