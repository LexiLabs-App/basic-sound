package app.lexilabs.basic.sound

@Deprecated(
    "Interface is obsolete. AudioByteBuilder supported AudioByte, which greedily eats up memory.",
    level = DeprecationLevel.WARNING
)
public interface AudioByteBuilder {
    public fun load(context: Any, localPath: String): Any
    public fun play(item: Any)
    public fun release()
}