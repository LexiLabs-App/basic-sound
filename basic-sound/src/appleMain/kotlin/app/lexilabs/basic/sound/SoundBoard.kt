package app.lexilabs.basic.sound

import app.lexilabs.basic.logging.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioMixerNode
import platform.AVFAudio.AVAudioNodeBus
import platform.AVFAudio.AVAudioPlayerNode
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
public actual class SoundBoard actual constructor(context: Any?): SoundBoardBuilder {

    private val tag = "SoundBoard"
    private val engine: AVAudioEngine = AVAudioEngine()
    private val mixerNode: AVAudioMixerNode = engine.mainMixerNode
    private val audioFiles: MutableMap<String, AVAudioFile> = mutableMapOf<String, AVAudioFile>()

    public actual override val soundBytes: MutableList<SoundByte> = mutableListOf<SoundByte>()
    public actual override val mixer: MixerChannel = Channel<String>()

    private val activePlayerNodes: MutableList<AVAudioPlayerNode> = mutableListOf()

    private var _volume: Float = 1.0f
    public actual override var volume: Float
        get() = _volume
        set(value) {
            _volume = value.coerceIn(0.0f, 1.0f)
            mixerNode.outputVolume = _volume
        }

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
                            activePlayerNodes.add(playerNode)
                            playerNode.scheduleFile(file = it, atTime = null, null)
                            playerNode.play()
                        }
                    }
                }
            }
        }
    }

    public actual override fun stopAll() {
        activePlayerNodes.forEach { playerNode ->
            try {
                playerNode.stop()
                engine.detachNode(playerNode)
            } catch (e: Exception) {
                // ignore
            }
        }
        activePlayerNodes.clear()
    }

    public actual override fun powerDown() {
        mixer.close()
        stopAll()
        audioFiles.clear()
        soundBytes.clear()
        engine.stop()
    }
}