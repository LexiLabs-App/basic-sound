package app.lexilabs.basic.sound

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.delay
import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class SoundBoardTest {

    private lateinit var soundBoard: SoundBoard
    private val testSoundName = "test_sound"
    private lateinit var testSoundFile: File

    @BeforeTest
    fun setUp() {
        soundBoard = SoundBoard()

        // Create a dummy audio file for testing
        testSoundFile = File.createTempFile("test_sound", ".wav")
        val audioFormat = javax.sound.sampled.AudioFormat(44100f, 16, 1, true, false)
        val audioInputStream = javax.sound.sampled.AudioInputStream(java.io.ByteArrayInputStream(ByteArray(0)), audioFormat, 0)
        AudioSystem.write(audioInputStream, javax.sound.sampled.AudioFileFormat.Type.WAVE, testSoundFile)

        soundBoard.soundBytes.add(SoundByte(testSoundName, testSoundFile.absolutePath))
    }

    @AfterTest
    fun tearDown() {
        testSoundFile.delete()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `mixer plays sound when name is received`() = runComposeUiTest {
        setContent {
            soundBoard.PowerUp()
        }
        // When
        soundBoard.mixer.send(testSoundName)

        // Then
        delay(1000.milliseconds)

        setContent {
            soundBoard.PowerDown()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `unknown sound name is ignored`() = runComposeUiTest {
        setContent {
            soundBoard.PowerUp()
        }
        soundBoard.mixer.send("missing_sound")
        delay(100.milliseconds)

        assertTrue(soundBoard.mixer.trySend(testSoundName).isSuccess)

        setContent {
            soundBoard.PowerDown()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `power down closes mixer`() = runComposeUiTest {
        setContent {
            soundBoard.PowerUp()
        }
        setContent {
            soundBoard.PowerDown()
        }

        assertFalse(soundBoard.mixer.trySend(testSoundName).isSuccess)
    }
}
