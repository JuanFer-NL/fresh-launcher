# Store metadata

`metadata/android/` contains English and Spanish descriptions and images in the
[Fastlane layout supported by F-Droid](https://f-droid.org/docs/All_About_Descriptions_Graphics_and_Screenshots/).
No Fastlane installation or store credentials are needed to edit these files.

The descriptions follow the English and Spanish READMEs. The images reuse the
existing artwork in `docs/`:

- Each `images/icon.png` is `docs/icon.svg` rendered at 512 × 512 pixels.
- Screenshots `1.png` through `4.png` are PNG conversions of the matching
  `en-` or `es-` files in `docs/screenshots/`, in this order: `home-dark`,
  `home-light`, `drawer`, `settings`. Their original dimensions are preserved.

Changelog filenames use the integer `versionCode` in `app/build.gradle.kts`.
`changelogs/6.txt` describes the published `v0.6.0` release (`versionName` 0.6.0,
`versionCode` 6). For each future release, add the matching changelog in both
locales. Keep titles within 30 characters, short descriptions within 80, full
descriptions within 4,000 and changelogs within 500.

This metadata prepares the store listing. Inclusion requests to IzzyOnDroid and
F-Droid, signing and publishing releases are separate maintainer steps. F-Droid
reads metadata from the release revision it builds, so these files must be
included in a future release before that process can pick them up.
