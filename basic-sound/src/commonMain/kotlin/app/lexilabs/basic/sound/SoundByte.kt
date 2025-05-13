package app.lexilabs.basic.sound

/**
 * Used to associate a name with a file resource in preparation for [SoundBoard]
 * @param name the title of the audio content
 * @param localPath the location of the audio file, which can also be a Composable Resource
 */
public class SoundByte(public val name: String, public val localPath: String)