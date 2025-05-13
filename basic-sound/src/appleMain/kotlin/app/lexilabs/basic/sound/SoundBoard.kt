package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioMixerNode
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioNodeBus
import platform.AVFAudio.AVAudioPlayerNode
import platform.Foundation.NSURL
import kotlin.collections.set

@OptIn(ExperimentalForeignApi::class)
public actual class SoundBoard actual constructor(context: Any?): SoundBoardBuilder {

    // TODO: Sounds only play one after another, but stay in the buffer while waiting

    private val tag = "SoundBoard"
    private val engine: AVAudioEngine = AVAudioEngine()
    private val mixerNode: AVAudioMixerNode = engine.mainMixerNode
    private val audioFiles: MutableMap<String, AVAudioFile> = mutableMapOf<String, AVAudioFile>()

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf<SoundByte>()
    public actual override val mixer: MixerChannel = Channel<String>()

    init {
        engine.startAndReturnError(null)
    }

    public actual override fun powerUp(){
        Log.i(tag, "launch:starting to load sounds")
        soundBytes.forEachIndexed { index, soundByte ->
            Log.i(tag, "launch:adding ${soundByte.name}")
            audioFiles[soundByte.name] = AVAudioFile(NSURL(string = soundByte.localPath), null)
        }
        startMixer()
        Log.i(tag, "launch:complete")
    }

    @OptIn(UnsafeNumber::class)
    private fun startMixer(){
        Log.i(tag, "startMixer:starting")
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val result = mixer.receiveCatching()
                when (val name = result.getOrNull()) {
                    null -> break
                    else -> {
                        audioFiles[name]?.let {
                            val playerNode = AVAudioPlayerNode()
                            engine.attachNode(playerNode)
                            engine.connect(playerNode, mixerNode, mixerNode.outputFormatForBus(AVAudioNodeBus.MIN_VALUE))
                            playerNode.scheduleFile(file = it, atTime = null, null)
                            playerNode.play()
                        }
                    }
                }
            }
        }
    }

    public actual override fun powerDown() {
        mixer.close()
        audioFiles.clear()
        soundBytes.clear()
        engine.stop()
    }
}