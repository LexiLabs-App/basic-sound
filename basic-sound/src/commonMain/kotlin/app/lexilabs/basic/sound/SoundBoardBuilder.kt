package app.lexilabs.basic.sound

public interface SoundBoardBuilder {
    public val soundBytes: MutableList<SoundByte>
    public val mixer: MixerChannel
    public fun powerUp()
    public fun powerDown()
    public fun load(soundName: String, localPath: String) {
        this.soundBytes.add(SoundByte(soundName, localPath))
    }
    public fun load(vararg soundByte: SoundByte) {
        this.soundBytes.addAll(soundByte)
    }
    public fun load(soundBytes: List<SoundByte>) {
        this.soundBytes.addAll(soundBytes)
    }
    public fun load(soundByte: Map<String, String>) {
        this.soundBytes.addAll(soundByte.map { SoundByte(it.key, it.value) })
    }
    public fun load(soundByte: Pair<String, String>) {
        this.soundBytes.add(SoundByte(soundByte.first, soundByte.second))
    }
}