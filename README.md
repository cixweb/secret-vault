# Android app template

Starting point of a Jetpack Compose app: Material 3, unit tests, screenshot
tests and CI. A repository created from it gets its own README when the app
is implemented.

## Look

Every screen uses one theme and one set of components, so all apps built from
this template look alike:

- `ui/theme/`: `AppTheme` with the Signal palette (indigo on cool greys, light
  and dark), Inter for text and JetBrains Mono for code, rounded shapes.
  Dynamic color is off. Status colors and monospace styles are in
  `AppTheme.colors` and `AppTheme.typography`.
- `ui/components/`: `AppScaffold`, `AppTopBar`, `ActionChip`, `SectionCard`,
  `LabeledValue`, `CodeBlock`, `StatusBanner`, `AppTextField`,
  `PrimaryButton`, `SecondaryButton` and `SwitchRow`.
- `@ScreenPreviews`: light and dark previews on one fixed phone size; the
  screenshot tests use it, so every image has the same frame.

`ComponentsScreenshot` shows the whole kit, and `ThemeContrastTest` checks that
text on every container reaches 4.5:1.

## Build

```bash
./gradlew assembleDebug lint testDebugUnitTest validateDebugScreenshotTest
```

JDK 17 and the Android SDK are required.

## Fonts

Inter and JetBrains Mono are bundled in `app/src/main/res/font/` under the SIL
Open Font License 1.1; see `licenses/`.
