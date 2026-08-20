@file:OptIn(ExperimentalBasicSound::class)

package app.lexilabs.basic.sound

import kotlin.test.Test
import kotlin.test.assertEquals

class ApplePlatformTest {
    @Test
    fun audioStartsAndReleasesInNoneState() {
        val audio = Audio()
        assertEquals(AudioState.NONE, audio.audioState.value)
        audio.release()
        assertEquals(AudioState.NONE, audio.audioState.value)
    }
}