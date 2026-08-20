@file:OptIn(ExperimentalBasicSound::class)

package app.lexilabs.basic.sound

import java.io.File
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AudioTest {

    private val temporaryFiles = mutableListOf<File>()

    @AfterTest
    fun tearDown() {
        temporaryFiles.forEach(File::delete)
    }

    @Test
    fun invalidResourceProducesErrorState() {
        val audio = Audio("/does/not/exist.wav")

        audio.load()

        val state = audio.audioState.value
        assertIs<AudioState.ERROR>(state)
        assertTrue(state.message.startsWith("load:failure:"))
    }

    @Test
    fun releaseResetsAudioState() {
        val audio = Audio("/does/not/exist.wav")
        audio.load()

        audio.release()

        assertEquals(AudioState.NONE, audio.audioState.value)
    }

    @Test
    fun audioByteLoadsWaveInput() {
        val file = createWaveFile()
        @Suppress("DEPRECATION")
        val audioByte = AudioByte()

        @Suppress("DEPRECATION")
        val loaded = audioByte.load(Any(), file.absolutePath)

        assertIs<AudioInputStream>(loaded).close()
        audioByte.release()
    }

    private fun createWaveFile(): File {
        val file = File.createTempFile("basic-sound", ".wav")
        temporaryFiles += file
        val format = AudioFormat(8_000f, 8, 1, true, false)
        AudioInputStream(ByteArray(8).inputStream(), format, 8).use { stream ->
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file)
        }
        return file
    }
}