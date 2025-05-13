package app.lexilabs.basic.sound

public expect class SoundBoard(context: Any?): SoundBoardBuilder {
    override val soundBytes: MutableList<SoundByte>
    override val mixer: MixerChannel
    override fun powerUp()
    override fun powerDown()
}
