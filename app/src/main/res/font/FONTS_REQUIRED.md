# Fonts Required — Atkinson Hyperlegible

The NeuroPulse typography system (NeuroPulseTypography.kt) uses **Atkinson Hyperlegible**,
a font designed by the Braille Institute specifically for low-vision and reading-difficulty users.

## Why this font (ADR-005)
Character disambiguation (b/d, p/q, 1/l/I) is significantly improved over standard
sans-serif fonts, directly reducing visual processing effort for ADHD users.

## Files needed in this folder

| Filename                                  | Weight        |
|-------------------------------------------|---------------|
| `atkinson_hyperlegible_regular.ttf`       | Regular (400) |
| `atkinson_hyperlegible_bold.ttf`          | Bold (700)    |
| `atkinson_hyperlegible_italic.ttf`        | Regular Italic|
| `atkinson_hyperlegible_bold_italic.ttf`   | Bold Italic   |

## Download
Free download from the Braille Institute:
https://brailleinstitute.org/freefont

1. Download the ZIP
2. Extract the `.ttf` files
3. Rename them to match the filenames above (lowercase, underscores)
4. Place them in this folder (`app/src/main/res/font/`)
5. Run `./gradlew assembleDebug` — the build will pick them up automatically

## Note
These font files are gitignored via the standard Android res exclusion pattern.
Each developer must download and place them locally.
