package app.lexilabs.basic.sound

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioSystem

public actual class SoundBoard actual constructor(context: Any?) : SoundBoardBuilder {

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf()

    public actual override val mixer: MixerChannel = Channel(Channel.UNLIMITED)

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    public actual override fun powerUp() {
        scope.launch {
            for (soundName in mixer) {
                soundBytes.find { it.name == soundName }?.let { soundByte ->
                    try {
                        val clip = AudioSystem.getClip()
                        val audioInputStream = AudioSystem.getAudioInputStream(File(soundByte.localPath))
                        clip.open(audioInputStream)
                        clip.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    public actual override fun powerDown() {
        job.cancel()
        mixer.close()
    }
}
