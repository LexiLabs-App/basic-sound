@file:OptIn(ExperimentalBasicSound::class, androidx.compose.ui.test.ExperimentalTestApi::class)

package app.lexilabs.basic.sound

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonApiTest {

    @Test
    fun audioStatesHaveExpectedValues() {
        assertEquals(AudioState.NONE, AudioState.NONE)
        assertEquals(AudioState.READY, AudioState.READY)
        assertEquals(AudioState.PLAYING, AudioState.PLAYING)
        assertEquals(AudioState.PAUSED, AudioState.PAUSED)
        assertEquals(AudioState.LOADING, AudioState.LOADING)

        val error = AudioState.ERROR("failed")
        assertEquals(AudioState.ERROR("failed"), error)
        assertEquals("failed", error.message)
    }

    @Test
    fun soundByteExposesNameAndPath() {
        val sound = SoundByte("click", "files/click.wav")

        assertEquals("click", sound.name)
        assertEquals("files/click.wav", sound.localPath)
    }

    @Test
    fun soundBoardBuilderLoadsAllSupportedInputShapes() {
        val board = FakeSoundBoard()
        val first = SoundByte("first", "first.wav")
        val second = SoundByte("second", "second.wav")

        board.load("named", "named.wav")
        board.load(first, second)
        board.load(listOf(SoundByte("list", "list.wav")))
        board.load(mapOf("map" to "map.wav"))
        board.load("pair" to "pair.wav")

        assertEquals(
            listOf("named", "first", "second", "list", "map", "pair"),
            board.soundBytes.map { it.name },
        )
        assertEquals(
            listOf("named.wav", "first.wav", "second.wav", "list.wav", "map.wav", "pair.wav"),
            board.soundBytes.map { it.localPath },
        )
    }

    @Test
    fun mixerPlayExtensionsSendSoundNames() {
        runTest {
            val mixer = Channel<String>(Channel.UNLIMITED)
            val sound = SoundByte("click", "click.wav")

            mixer.play("beep")
            mixer.play(sound)

            assertEquals("beep", mixer.receive())
            assertEquals("click", mixer.receive())
            mixer.close()
        }
    }

    @Test
    fun soundBoardLifecycleCanBeImplementedByPlatform() = runComposeUiTest {
        val board = FakeSoundBoard()

        setContent {
            board.PowerUp()
        }
        setContent {
            board.PowerDown()
        }

        assertTrue(board.poweredUp)
        assertTrue(board.poweredDown)
    }

    private class FakeSoundBoard : SoundBoardBuilder {
        override val soundBytes = mutableListOf<SoundByte>()
        override val mixer: MixerChannel = Channel(Channel.UNLIMITED)
        var poweredUp = false
        var poweredDown = false

        @Composable
        override fun PowerUp() {
            poweredUp = true
        }

        @Composable
        override fun PowerDown() {
            poweredDown = true
            mixer.close()
        }
    }
}
