# Assets Alert (Android prototype)

A working Kotlin + Jetpack Compose implementation of the app described in
the product doc — custom price alerts, an escalating audible alarm, custom
alert sounds, and a dark UI. Web3/NFT and beta-tester features were left
out, as removed from the source document. App icon and splash screen use
your "Alert! Blind Art" mouse mascot.

## ⚠️ Compile status

**This has not been compiled or run anywhere.** The environment that
generated this code has no Android SDK, no Gradle, and no network access,
so there's no way to verify it builds cleanly. It's been carefully
hand-reviewed for consistency (matching imports, correct references between
files, manifest entries for every component), but the first real compiler
check will be whenever you run `./gradlew assembleDebug`. If it throws
errors, paste them here and I'll fix them.

## Screens

- **Splash screen** — native Android 12+ style splash (via `androidx.core:core-splashscreen`, works back to API 26) showing the mouse mascot on a dark background while the app decides where to send you next.
- **Onboarding** (first launch only) — a 3-page swipeable intro covering price alerts, the escalating alarm, and customization. Skippable. Shown once, tracked in local settings.
- **Alert list** — your watched assets, live status, toggle/remove.
- **Add alert** — crypto search (CoinGecko) or stock ticker entry, target price, direction.
- **Settings** — dark mode, custom alert sound picker, stock API key.
- **Alarm ringing screen** (new) — a full-screen "Target hit!" screen that launches automatically — even over the lock screen — when an alert fires, with a big Stop button. Previously a trigger only produced a notification; this closes that gap so it matches the "impossible to sleep through" alarm behavior from the doc.

## App icon

Generated from your uploaded logo: the mouse mascot cropped out (without
the "ALERT! BLIND ART" wordmark, since that's hard to read at launcher-icon
size) and rendered at every required density as both a legacy square icon
and an adaptive icon (foreground mascot + solid dark background), so it
displays correctly whether the device masks icons as circles, squircles, or
squares.

## What's real vs. simplified

**Real / working:**
- Live crypto prices from the [CoinGecko](https://www.coingecko.com/en/api) public API — no API key needed.
- Live stock prices from [Twelve Data](https://twelvedata.com) — free tier, you supply your own API key in Settings.
- A foreground `Service` that polls prices on an interval (default 30s) so alerts fire close to real time while the app is running or backgrounded.
- An escalating audible alarm: starts at low volume, ramps to full volume over ~25 minutes, and auto-stops at the 30-minute mark if you don't dismiss it — matching the "5 to 30 minutes" behavior in the product doc.
- A full-screen alarm activity that launches automatically (including over the lock screen) when an alert fires.
- Custom alert sounds via the system file picker (persists a permanent URI permission so it survives reboots).
- Dark mode (default) built with Material 3, toggleable in Settings.
- Alerts and settings persist locally via Room + DataStore.

**Simplified for a prototype:**
- Polling every 30s (configurable in code) rather than a live push/websocket feed — CoinGecko's free tier doesn't offer push updates, so this is the practical ceiling without a paid data source.
- No iOS/Web client — Android only, as scoped.
- No user accounts, referral system, or cloud sync.

## Requirements to build

- Android Studio (Koala or newer) with JDK 17, **or** the command-line SDK tools + Gradle if you're building from a terminal (see below).
- Internet access for Gradle to download dependencies the first time.
- A physical device or emulator running Android 8.0 (API 26) or newer.

## Building via GitHub Actions (recommended if building on a phone/tablet)

This repo includes `.github/workflows/build.yml`, which builds the debug
APK on GitHub's servers every time you push to `main`/`master` (or you can
trigger it manually from the Actions tab). This sidesteps on-device build
limits entirely — no local Android SDK, no waiting on constrained
hardware.

1. Push this project to a GitHub repo (root of the repo should be this
   `AssetsAlert` folder's contents — i.e. `build.gradle.kts` sits at the
   repo root, not nested inside another folder).
2. Go to the repo's **Actions** tab — a "Build APK" run should already be
   in progress (or trigger it manually with the "Run workflow" button).
3. Wait for it to finish (usually 3–8 minutes).
4. Open the completed run, scroll to **Artifacts**, and download
   `app-debug` — unzip it to get `app-debug.apk`.
5. Transfer that APK to your device and tap it to install (allow
   "install from unknown sources" if prompted).

Note: this project doesn't include a Gradle wrapper (`gradlew`), since
generating one requires downloading `gradle-wrapper.jar` from the network,
which wasn't available when this project was created. The workflow
installs Gradle directly instead, so this doesn't block the cloud build —
but if you later work with this project locally via a terminal with
network access, run `gradle wrapper --gradle-version 8.7` once to add a
wrapper for convenience.

## Building from the terminal (no Android Studio)

```bash
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

cd AssetsAlert
gradle wrapper --gradle-version 8.7   # generates gradlew (only needed once)
./gradlew assembleDebug

adb install app/build/outputs/apk/debug/app-debug.apk
```

## First run

1. Open the `AssetsAlert/` folder in Android Studio (or build via terminal above) and let Gradle sync.
2. Run on a device/emulator. Grant the notification permission when prompted.
3. You'll see the splash screen, then a one-time onboarding intro.
4. Tap **+** to add an alert:
   - **Crypto**: search by name (e.g. "bitcoin"), pick a result, set a target price.
   - **Stock**: enter a ticker (e.g. "AAPL") — you'll need a free API key from twelvedata.com, added in Settings, for stock alerts to resolve.
5. Saving an alert starts the background monitoring service automatically.
6. When a target is hit, the full-screen alarm activity launches with an escalating alarm; tap **Stop alarm** to silence it and re-arm the alert from the list.

## Project layout

```
app/src/main/java/com/assetsalert/app/
  data/       Room entities/DAO, CoinGecko + Twelve Data API clients, repository, settings
  service/    Foreground monitoring service, escalating alarm player, boot receiver
  notification/  Notification channels + builders
  ui/         Compose screens (onboarding, list, add, settings), navigation, theme, view model
  MainActivity.kt       Splash screen install + onboarding routing
  AlarmRingActivity.kt  Full-screen "target hit" alarm screen
```

## Known follow-ups if you take this further

- Swap CoinGecko's simple endpoint for a websocket/push provider if you need true real-time (sub-second) updates.
- Add WorkManager as a battery-friendlier fallback for when the app is force-stopped (foreground services get killed if the user swipes the app away on some OEM skins).
