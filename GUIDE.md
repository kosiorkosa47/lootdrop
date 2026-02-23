# Bubblewrap TWA Guide

Step-by-step guide to wrapping a web app as a Trusted Web Activity (TWA) for the Solana dApp Store.

## Prerequisites

- **Node.js 18+**
- **Java JDK 11+** (for signing APKs)
- **Android SDK** (auto-downloaded by Bubblewrap on first run)

## 1. Install Bubblewrap CLI

```bash
npm install -g @bubblewrap/cli
```

Verify:
```bash
bubblewrap --version
```

## 2. Prepare Your Web App

Your web app must be a valid PWA with:

- **`manifest.json`** — Web App Manifest with `name`, `icons`, `start_url`, `display: standalone`
- **HTTPS** — deployed on a secure origin (Vercel, Netlify, GitHub Pages, etc.)
- **Service Worker** — for offline support (required by TWA)

Check your manifest:
```bash
curl -s https://your-app.vercel.app/manifest.json | jq .
```

## 3. Initialize TWA Project

```bash
mkdir android-twa && cd android-twa
bubblewrap init --manifest https://your-app.vercel.app/manifest.json
```

Bubblewrap will prompt for:

| Setting | Description | Example |
|---|---|---|
| **Package name** | Reverse domain (Android convention) | `com.yourname.appname.twa` |
| **App name** | Display name | `LootDrop` |
| **Launcher URL** | Your deployed URL | `https://your-app.vercel.app` |
| **Icon** | 512x512 PNG (auto-downloaded from manifest) | — |
| **Signing key** | Generate new or use existing | Generate new |

On first run, it downloads Android SDK + build tools (~500MB).

## 4. Build APK

```bash
bubblewrap build
```

Outputs:
- `app-release-signed.apk` — install directly on device
- `app-release-bundle.aab` — upload to Google Play / Solana dApp Store

## 5. Test on Device

```bash
# Install via ADB
adb install app-release-signed.apk

# Launch
adb shell am start -a android.intent.action.VIEW -d "https://your-app.vercel.app"
```

## 6. Digital Asset Links (Fullscreen Mode)

Without Digital Asset Links, the TWA shows a Chrome URL bar at the top. To remove it:

### Get your signing key fingerprint

```bash
keytool -list -v -keystore your.keystore | grep SHA256
```

Output:
```
SHA256: 7B:16:82:F7:3A:87:45:CB:5F:0D:8C:D5:10:90:79:B3:...
```

### Create `.well-known/assetlinks.json`

Place this at `your-app.vercel.app/.well-known/assetlinks.json`:

```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.yourname.appname.twa",
    "sha256_cert_fingerprints": ["YOUR:SHA256:FINGERPRINT:HERE"]
  }
}]
```

### Verify DAL

```bash
# Google's verification tool
curl -s "https://digitalassetlinks.googleapis.com/v1/statements:list?source.web.site=https://your-app.vercel.app&relation=delegate_permission/common.handle_all_urls" | jq .
```

After deploying `assetlinks.json`, uninstall and reinstall the APK:
```bash
adb uninstall com.yourname.appname.twa
adb install app-release-signed.apk
```

The app now runs **fullscreen** — no browser chrome.

## 7. Solana dApp Store Submission

The Solana dApp Store accepts APK and AAB files. Requirements:

1. **Signed APK/AAB** from step 4
2. **App listing** — name, description, screenshots, icon
3. **DAL verified** — fullscreen mode working
4. **Content policy** compliance

Submit at: [solanamobile.com/dapp-store](https://solanamobile.com/dapp-store)

## Alternative: PWABuilder

[PWABuilder](https://www.pwabuilder.com/) provides a GUI alternative to Bubblewrap:

1. Enter your URL at pwabuilder.com
2. Click **Android** → **Generate Package**
3. Download ZIP with APK + AAB + signing key + assetlinks.json

PWABuilder uses the same TWA technology under the hood.

## Troubleshooting

### Chrome URL bar still showing
- DAL not deployed or wrong fingerprint
- Clear Chrome cache: `adb shell pm clear com.android.chrome`
- Reinstall APK after deploying assetlinks.json

### INSTALL_FAILED_UPDATE_INCOMPATIBLE
- Old APK with different signing key exists
- Fix: `adb uninstall <package_name>` then install again

### App opens in browser instead of TWA
- Chrome not installed or not default browser
- TWA falls back to Custom Chrome Tab → WebView

### Service worker not caching
- Check `sw.js` is at root scope (`/sw.js`, not `/js/sw.js`)
- Verify in Chrome DevTools → Application → Service Workers
