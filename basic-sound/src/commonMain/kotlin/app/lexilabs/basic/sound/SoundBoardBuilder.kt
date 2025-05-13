package app.lexilabs.basic.sound

import kotlinx.coroutines.channels.Channel

/**
 * Creates a standard [SoundBoard] format to replicate across all platforms
 * @property soundBytes a list of [SoundByte] values loaded by the user
 * @property mixer the [Channel] used to pass [MixerChannel.play] commands
 * @see SoundBoardBuilder.powerUp
 * @see SoundBoardBuilder.powerDown
 * @see SoundBoardBuilder.load
 */
public interface SoundBoardBuilder {
    /** a list of [SoundByte] values loaded by the user */
    public val soundBytes: MutableList<SoundByte>
    /** the [Channel] used to pass [MixerChannel.play] commands */
    public val mixer: MixerChannel

    /**
     * Used to load the [soundBytes] into memory and begin the listener for the [mixer] channel
     */
    public fun powerUp()

    /**
     * Used to release resources used by [soundBytes], [mixer],
     * and other platform-specific resources
     */
    public fun powerDown()

    /**
     * Used to load a [SoundByte] onto a [SoundBoard]
     * @param soundName the name of the sound, used to play the sound later
     * @param localPath the URI [String] of the file location
     */
    public fun load(soundName: String, localPath: String) {
        this.soundBytes.add(SoundByte(soundName, localPath))
    }

    /**
     * Used to load one or many [SoundByte]s onto a [SoundBoard]
     * @param soundByte one or many [SoundByte] objects
     */
    public fun load(vararg soundByte: SoundByte) {
        this.soundBytes.addAll(soundByte)
    }

    /**
     * Used to load a [List] of [SoundByte]s onto a [SoundBoard]
     * @param soundBytes a [List] of [SoundByte] objects
     */
    public fun load(soundBytes: List<SoundByte>) {
        this.soundBytes.addAll(soundBytes)
    }

    /**
     * Used to load a [Map] of names and URI [String] values onto a [SoundBoard]
     * @param soundBytes a [Map] of [SoundByte] names and localPath [String]s
     */
    public fun load(soundByte: Map<String, String>) {
        this.soundBytes.addAll(soundByte.map { SoundByte(it.key, it.value) })
    }

    /**
     * Used to load a [Pair] for name and URI [String] values onto a [SoundBoard]
     * @param soundBytes a [Pair] of a [SoundByte] name and localPath [String]
     */
    public fun load(soundByte: Pair<String, String>) {
        this.soundBytes.add(SoundByte(soundByte.first, soundByte.second))
    }
}