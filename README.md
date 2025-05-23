# Basic-Sound
<img src="images/logo-icon.svg" alt="basic" height="240" align="right"/> 

[![Basic-Sound](https://img.shields.io/maven-central/v/app.lexilabs.basic/basic-sound?color=blue)](https://central.sonatype.com/artifact/app.lexilabs.basic/basic-sound)

A Kotlin Multiplatform library to rapidly integrate audio across all your Kotlin Multiplatform apps.
Currently, this library only ingests URLs and local paths. Composable Resources are also possible, but may be finicky.

![badge-android](http://img.shields.io/badge/android-full_support-65c663.svg?style=flat)
![badge-ios](http://img.shields.io/badge/ios-full_support-65c663.svg?style=flat)
![badge-mac](http://img.shields.io/badge/macos-full_support-65c663.svg?style=flat)
![badge-watchos](http://img.shields.io/badge/watchos-full_support-65c663.svg?style=flat)
![badge-tvos](http://img.shields.io/badge/tvos-full_support-65c663.svg?style=flat)
![badge-nodejs](https://img.shields.io/badge/jsNode-full_support-65c663.svg?style=flat)
![badge-jsBrowser](https://img.shields.io/badge/jsBrowser-full_support-65c663.svg?style=flat)
![badge-wasmJsBrowser](https://img.shields.io/badge/wasmJsBrowser-full_support-65c663.svg?style=flat)
![badge-jvm](http://img.shields.io/badge/jvm-full_support-65c663.svg?style=flat)
![badge-linux](http://img.shields.io/badge/linux-no_support-red.svg?style=flat)
![badge-windows](http://img.shields.io/badge/windows-no_support-red.svg?style=flat)

## Supported Filetypes
| Format    |      Android       |        iOS         | javascript / wasm  |        JVM*        | File / Container Types                                                                                                                                            |
|:----------|:------------------:|:------------------:|:------------------:|:------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AAC LC    | :white_check_mark: | :white_check_mark: |        :x:         |     :question:     | 3GPP (.3gp) MPEG-4 (.mp4, .m4a) ADTS raw AAC (.aac, decode in Android 3.1+, encode in Android 4.0+, ADIF not supported) MPEG-TS (.ts, not seekable, Android 3.0+) |
| AMR-NB    | :white_check_mark: |        :x:         |        :x:         |     :question:     | 3GPP (.3gp) AMR (.amr)                                                                                                                                            |
| FLAC      | :white_check_mark: |        :x:         |        :x:         |     :question:     | FLAC (.flac) MPEG-4 (.mp4, .m4a, Android 10+)                                                                                                                     |
| MIDI      | :white_check_mark: |        :x:         |        :x:         |     :question:     | Type 0 and 1 (.mid, .xmf, .mxmf) RTTTL/RTX (.rtttl, .rtx) OTA (.ota) iMelody (.imy)                                                                               |
| MP3       | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: | MP3 (.mp3) MPEG-4 (.mp4, .m4a, Android 10+) Matroska (.mkv, Android 10+)                                                                                          |
| Opus      | :white_check_mark: |        :x:         |     :question:     |     :question:     | Ogg (.ogg) Matroska (.mkv)                                                                                                                                        |
| PCM/WAVE  | :white_check_mark: |        :x:         | :white_check_mark: |     :question:     | WAVE (.wav)                                                                                                                                                       |
| Vorbis    | :white_check_mark: |        :x:         |     :question:     |     :question:     | Ogg (.ogg) Matroska (.mkv, Android 4.0+) MPEG-4 (.mp4, .m4a, Android 10+)                                                                                         |

* __NOTE: JVM file formats are dependent on the underlying operating system the app is run on.__
## Installation
You'll need to add your maven dependency list
```toml
# in your 'libs.versions.toml' file
[versions]
kotlin = "+" # gets the latest version
lexilabs-basic = "+" # gets the latest version

[libraries]
lexilabs-basic-sound = { module = "app.lexilabs.basic:basic-sound", version.ref = "lexilabs-basic" }
```
then include the library in your gradle build
```kotlin
// in your 'shared/build.gradle.kts' file
sourceSets {
    commonMain.dependencies {
        implementation(libs.lexilabs.basic.sound)
    }
}
```

## `Audio()` Usage
You can initialize an `Audio` object with a URL
```kotlin
val resource = "https://dare.wisc.edu/wp-content/uploads/sites/1051/2008/11/MS072.mp3"
val audio = Audio(resource, true) // AutoPlay is marked "true"
```

You can play the audio separately from initializing the `Audio` object.
```kotlin
val resource = "https://dare.wisc.edu/wp-content/uploads/sites/1051/2008/11/MS072.mp3"
val audio = Audio(resource) // loads the audio file
DoSomethingElse()
audio.play() // plays the audio immediately upon execution
```

You can also pause and stop the audio:
```kotlin
/** PAUSING **/
audio.pause() // remembers where it paused
audio.play() // and resumes once executed again
/** STOPPING **/
audio.stop() // resets to the beginning of the file
audio.play() // and replays it again upon execution
```

You should release your audio when done to preserve memory:
```kotlin
audio.release() // converts the audio instance to null
```

There are lots of options to load larger files asynchronously:

```kotlin
// Create empty Audio instance
val audio: Audio = Audio()
audio.resource = "https://dare.wisc.edu/wp-content/uploads/sites/1051/2008/11/MS072.mp3"
// Begin collecting the state of audio
val audioState by audioPlayer.audioState.collectAsState()
// Begin loading the audio async
lifecycleScope.launch {
    withContext(Dispatchers.IO) {
        audio.load()
    }
}

DoSomethingElse() // do other stuff in the meantime

Button(
    onClick = {
        when (audioState) {
            is AudioState.NONE -> audioPlayer.load()
            is AudioState.READY -> audioPlayer.play()
            is AudioState.ERROR -> println((audioState as AudioState.ERROR).message)
            is AudioState.PAUSED -> audioPlayer.play()
            is AudioState.PLAYING -> audioPlayer.pause()
            else -> {
                /** DO NOTHING **/
            }
        }
    }
) {
    when (audioState) {
        is AudioState.ERROR -> Text("Error")
        is AudioState.LOADING -> Text("Loading")
        is AudioState.NONE -> Text("None")
        is AudioState.READY -> Text("Ready")
        is AudioState.PAUSED -> Text("Paused")
        is AudioState.PLAYING -> Text("Playing")
        else -> { Text("Error") }
    }
}
```
If you need to load a Compose Resource, you need to use a constructor that includes `Context`.
Make sure you safely [pass your `Context` without memory leaks.](https://medium.com/hakz/contain-your-apps-memory-please-0c62819f8d7f).
```kotlin
val resource = Res.getUri("files/ringtone.wav")
// You can pass your Context
val audio = Audio(context, resource) // loads the audio file
audio.play() // plays the audio immediately upon execution
```

## `SoundBoard` Usage
SoundBoard allows you to load audio to memory to play multiple times later without reloading -- sort of like an actual soundboard.
The primary steps include:
1. Create a SoundBoard instance
2. Load SoundBytes onto the SoundBoard
3. PowerUp the SoundBoard
4. Play Sounds via the mixer
   If you need help creating a `Context` for the Android implementation, [you're welcome to steal my method.]("https://medium.com/@robert.jamison/passing-android-context-in-kmp-jetpack-compose-8de5b5de7bdd")
```kotlin
// commonMain

/* Create a SoundBoard Instance */
val soundBoard = SoundBoard(context)

/* Create a SoundByte */
val click = SoundByte(
    name = "click",
    localPath = Res.getUri("files/click.mp3")
)

/* Load the SoundByte onto the SoundBoard */
soundBoard.load(click)

/* PowerUp the SoundBoard */
soundBoard.powerUp()

/* Play sounds via the mixer */
soundBoard.mixer.play("click") // Use a String
soundBoard.mixer.play(click) // Use the original SoundByte value
/* Repeat as much as you like */

/* When you're done, PowerDown the SoundBoard to release resources */
soundBoard.powerDown()
```

## [Deprecated] ~~`AudioByte`~~ Usage
AudioByte allows you to load audio to memory to play multiple times later without reloading -- sort of like a soundboard.
You could make a callable class that is passed throughout the app so the sounds could be access in any context.
If you need help creating a platformContext, [you're welcome to steal my method.]("https://medium.com/@robert.jamison/passing-android-context-in-kmp-jetpack-compose-8de5b5de7bdd")
```kotlin
// Your custom class built in commonMain
class AudioByte(platformContext: Any) {
    // 
    private val audioByte: AudioByte = AudioByte()
    private val click: Any = audioByte.load(platformContext, Res.getUri("files/click.mp3"))
    private val fanfare: Any = audioByte.load(platformContext, Res.getUri("files/fanfare.mp3"))

    fun click() = audioByte.play(click)
    fun fanfare() = audioByte.play(solveId)
    fun release() = audioByte.release()
}

// create your class later
val audioByte = AudioByte(myPlatformContext)
// generate the sound whenever you like after
audioByte.click()
// remember to release when you won't need the audio board anymore.  
// If you use the sound everywhere, you won't need to do this
audioByte.release()
```