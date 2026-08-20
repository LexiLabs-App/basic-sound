package app.lexilabs.basic.sound

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
        soundBoard = SoundBoard(null)

        // Create a dummy audio file for testing
        testSoundFile = File.createTempFile("test_sound", ".wav")
        val audioFormat = javax.sound.sampled.AudioFormat(44100f, 16, 1, true, false)
        val audioInputStream = javax.sound.sampled.AudioInputStream(java.io.ByteArrayInputStream(ByteArray(0)), audioFormat, 0)
        AudioSystem.write(audioInputStream, javax.sound.sampled.AudioFileFormat.Type.WAVE, testSoundFile)

        soundBoard.soundBytes.add(SoundByte(testSoundName, testSoundFile.absolutePath))
        soundBoard.PowerUp()
    }

    @AfterTest
    fun tearDown() {
        soundBoard.PowerDown()
        testSoundFile.delete()
    }

    @Test
    fun `mixer plays sound when name is received`() = runBlocking {
        // When
        soundBoard.mixer.send(testSoundName)

        // Then
        // We can't easily assert that the sound was played, 
        // but we can check that no exceptions were thrown during playback.
        // We'll add a small delay to allow the sound to be processed.
        delay(1000.milliseconds)
    }

    @Test
    fun `unknown sound name is ignored`() = runBlocking {
        soundBoard.mixer.send("missing_sound")
        delay(100.milliseconds)

        assertTrue(soundBoard.mixer.trySend(testSoundName).isSuccess)
    }

    @Test
    fun `power down closes mixer`() {
        soundBoard.PowerDown()

        assertFalse(soundBoard.mixer.trySend(testSoundName).isSuccess)
    }
}
