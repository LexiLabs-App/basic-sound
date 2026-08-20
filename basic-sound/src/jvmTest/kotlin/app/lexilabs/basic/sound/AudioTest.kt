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
    fun validWaveSupportsAudioStateTransitions() {
        val file = createWaveFile()
        val audio = Audio(file.absolutePath)

        audio.load()
        assertEquals(AudioState.READY, audio.audioState.value)
        audio.play()
        assertEquals(AudioState.PLAYING, audio.audioState.value)
        audio.pause()
        assertEquals(AudioState.PAUSED, audio.audioState.value)
        audio.play()
        assertEquals(AudioState.PLAYING, audio.audioState.value)
        audio.stop()
        assertEquals(AudioState.READY, audio.audioState.value)
        audio.release()
    }


    private fun createWaveFile(): File {
        val file = File.createTempFile("basic-sound", ".wav")
        temporaryFiles += file
        val format = AudioFormat(8_000f, 8, 1, true, false)
        AudioInputStream(ByteArray(8).inputStream(), format, 8L).use { stream ->
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file)
        }
        return file
    }
}