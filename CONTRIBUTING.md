# Contributing to Fresh Launcher

Thanks for helping! Contributions in **English or Spanish** are welcome. Code comments are in Spanish, but you don't need to match that in your PR.

## Before you start

- **Bugs** → open an [issue](https://github.com/JuanFer-NL/fresh-launcher/issues) with your device, Android version and steps to reproduce.
- **Questions and ideas** → [Discussions](https://github.com/JuanFer-NL/fresh-launcher/discussions).
- **Security problems** → please report them privately, see [SECURITY.md](SECURITY.md).
- New to the project? Look for issues labeled [good first issue](https://github.com/JuanFer-NL/fresh-launcher/labels/good%20first%20issue).

For bigger changes, open an issue first so we can agree on the approach before you invest time.

## Build and test

You need JDK 17+ and the Android SDK with platform 37.

```bash
./gradlew assembleDebug                 # installs as com.fresh.launcher.debug
./gradlew testDebugUnitTest lintDebug   # the same checks CI runs
```

To check the light theme or a larger font without changing your phone's settings:

```bash
adb shell am start -n com.fresh.launcher.debug/com.fresh.launcher.MainActivity --es theme light --ef font_scale 1.3
```

## Project principles

Please keep these in mind. A PR that breaks them will be asked to change:

1. **No ads, analytics, trackers or accounts.** No new SDKs that phone home.
2. **Minimal permissions.** Explain any new permission in the README's privacy table.
3. **Minimal UI.** Text over icons, one accent color (lime), generous spacing. See [DESIGN.md](DESIGN.md).
4. **Accessible.** 48 dp touch targets, content descriptions for TalkBack, and respect for *Remove animations* and the system font size.

## Common contributions

### Add a translation

1. Copy `app/src/main/res/values/strings.xml` to `values-<lang>/strings.xml` (for example `values-pt`) and translate it.
2. Add `<locale android:name="<lang>" />` to `app/src/main/res/xml/locales_config.xml`.
3. Add the language to `AppLanguage` in `Settings.kt` and to the choices in `ui/SettingsScreen.kt`, written in its own language (e.g. "Português").

### Add a wallpaper

Add a `MeshWallpaperStyle` to `MeshWallpapers` in `ui/Wallpapers.kt`, with a light palette (bright base, dark text) and a dark one (pure black base), plus its name in every `strings.xml`. Check that the clock stays readable on both.

## Pull requests

- Keep each PR focused on one change.
- Run `./gradlew testDebugUnitTest lintDebug` before pushing; add a unit test when you touch logic.
- Include screenshots for UI changes, in light and dark themes.
- By contributing you agree that your work is released under the [MIT License](LICENSE).
