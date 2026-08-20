@file:OptIn(ExperimentalBasicSound::class)

package app.lexilabs.basic.sound

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidHostTest {
    @Test
    fun audioStartsInNoneState() {
        assertEquals(AudioState.NONE, Audio().audioState.value)
    }
}