<h1 align="center">
  <br/>
  LootDrop
  <br/>
</h1>

<h4 align="center">Mobile-optimized PWA template for the Solana dApp Store</h4>

<p align="center">
  <a href="https://web-rho-tawny-30.vercel.app">Live Demo</a> &nbsp;&bull;&nbsp;
  <a href="#screenshots">Screenshots</a> &nbsp;&bull;&nbsp;
  <a href="#quick-start">Quick Start</a> &nbsp;&bull;&nbsp;
  <a href="#bubblewrap--twa">Bubblewrap</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/SvelteKit_5-FF3E00?logo=svelte&logoColor=white" alt="SvelteKit" />
  <img src="https://img.shields.io/badge/Tailwind_CSS_4-06B6D4?logo=tailwindcss&logoColor=white" alt="Tailwind" />
  <img src="https://img.shields.io/badge/Material_Design_3-6750A4?logo=materialdesign&logoColor=white" alt="MD3" />
  <img src="https://img.shields.io/badge/Solana-9945FF?logo=solana&logoColor=white" alt="Solana" />
  <img src="https://img.shields.io/badge/TWA-3DDC84?logo=android&logoColor=white" alt="TWA" />
  <img src="https://img.shields.io/badge/license-MIT-blue" alt="License" />
</p>

---

## Screenshots

Fullscreen TWA running on **Solana Mobile Seeker** (no browser chrome):

<p align="center">
  <img src="docs/screenshots/home.png" width="180" alt="Home — Nearby Drops" />
  &nbsp;
  <img src="docs/screenshots/scan.png" width="180" alt="Scan QR Code" />
  &nbsp;
  <img src="docs/screenshots/rewards.png" width="180" alt="My Rewards" />
  &nbsp;
  <img src="docs/screenshots/profile.png" width="180" alt="Profile & Settings" />
</p>

<p align="center"><sub>Home &nbsp;&bull;&nbsp; Scan &nbsp;&bull;&nbsp; Rewards &nbsp;&bull;&nbsp; Profile</sub></p>

## About

LootDrop is a **reference PWA template** for developers who want to publish mobile-optimized web apps to the **Solana dApp Store** via [Bubblewrap CLI](https://github.com/GoogleChromeLabs/bubblewrap).

It showcases every mobile optimization needed for a production-quality Progressive Web App on Solana — from splash screens and offline caching to Material Design 3 theming and fullscreen Trusted Web Activity wrapping — using a location-based crypto rewards concept as a real-world demonstration.

Built for the [Solana Mobile PWA Improved Template RFP](https://solanamobile.com/grants).

## Key Features

### RFP Deliverables

| Deliverable | Status | Implementation |
|---|---|---|
| Sample PWA with Bubblewrap | Done | SvelteKit 5 + TWA via `twa-manifest.json` |
| Improved splash screen | Done | Branded loading with MD3 circular progress |
| Chrome default + WebView fallback | Done | Chrome Custom Tabs primary, WebView fallback |
| Mobile-intuitive navigation | Done | MD3 bottom nav bar (Map, Scan, Rewards, Profile) |

### Mobile Optimizations

- **Offline-first** — Service worker with cache-first for static assets, network-first for navigation
- **Pull-to-refresh** — Native-feeling gesture with MD3 refresh indicator
- **Dark mode** — System-preference detection + manual toggle, MD3 color tokens
- **Touch optimized** — 44px+ tap targets, ripple feedback, `touch-manipulation`
- **Safe areas** — Notch / punch-hole / gesture bar handling via `env(safe-area-inset-*)`
- **Responsive** — `clamp()` typography, fluid layouts, tested on 320px–800px+ screens
- **Fullscreen TWA** — Digital Asset Links verified, no browser chrome on Android

### Solana Integration (Template)

- Mobile Wallet Adapter (MWA) connection flow within TWA context
- Seed Vault compatible signing interface
- Solana Pay deep link placeholders for merchant QR codes
- On-chain reward escrow via Anchor smart contract scaffold

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | **SvelteKit 5** (Svelte 5 runes) + TypeScript |
| Styling | **Tailwind CSS 4** + Material Design 3 tokens |
| PWA | Web App Manifest + Service Worker (cache v2) |
| Android | **Bubblewrap** / PWABuilder TWA |
| Domain Verify | Digital Asset Links (`.well-known/assetlinks.json`) |
| Deployment | Vercel (adapter-static) |
| Smart Contract | Anchor (Rust) — scaffold |
| Backend | FastAPI (Python) — scaffold |

## Quick Start

### Run the PWA locally

```bash
cd web
npm install
npm run dev
# → http://localhost:5173
```

### Build for production

```bash
cd web
npm run build    # outputs to build/
npm run preview  # preview at http://localhost:4173
```

### Type check

```bash
cd web
npm run check    # svelte-check + TypeScript
```

## Bubblewrap / TWA

Wrap the PWA as an Android APK for the Solana dApp Store:

```bash
# 1. Install Bubblewrap
npm i -g @bubblewrap/cli

# 2. Initialize from the TWA manifest
cd web
bubblewrap init --manifest twa-manifest.json

# 3. Build signed APK
bubblewrap build
# → app-release-signed.apk
```

Alternatively, use [PWABuilder](https://www.pwabuilder.com/) for a zero-config Android package (APK + AAB).

### Digital Asset Links

The `/.well-known/assetlinks.json` is already configured for the production domain. This enables **fullscreen mode** — no Chrome URL bar in the TWA.

To use your own signing key, update the `sha256_cert_fingerprints` in:
- `web/static/.well-known/assetlinks.json`
- `web/twa-manifest.json`

Get your fingerprint: `keytool -list -v -keystore your.keystore | grep SHA256`

## Project Structure

```
lootdrop/
├── web/                          # SvelteKit PWA (main deliverable)
│   ├── src/
│   │   ├── routes/               # /, /scan, /rewards, /profile
│   │   └── lib/
│   │       ├── components/       # BottomNav, DropCard, SplashScreen, ...
│   │       ├── stores/           # Svelte 5 runes ($state, $derived)
│   │       ├── data/             # Mock businesses & rewards
│   │       └── types/            # TypeScript interfaces
│   ├── static/
│   │   ├── .well-known/          # Digital Asset Links
│   │   ├── icons/                # PWA icons (192/512 PNG + maskable)
│   │   ├── manifest.json         # Web App Manifest
│   │   └── sw.js                 # Service worker
│   ├── twa-manifest.json         # Bubblewrap TWA config
│   ├── vercel.json               # Vercel deployment config
│   └── package.json
├── programs/lootdrop/            # Anchor smart contract scaffold
│   └── src/lib.rs
├── backend/                      # FastAPI merchant API scaffold
│   └── main.py
└── docs/screenshots/             # Device screenshots (Seeker)
```

## Roadmap

- [x] **v0.1** — Project scaffold, smart contract, QR SDK
- [x] **v0.2** — SvelteKit PWA with mobile UI
- [x] **v0.3** — TWA wrapping + Material Design 3 + fullscreen DAL
- [ ] **v0.4** — MWA / Seed Vault integration in TWA context
- [ ] **v0.5** — Real camera QR scanning (zxing-js)
- [ ] **v0.6** — Merchant dashboard + backend API
- [ ] **v1.0** — Mainnet launch

## License

[MIT](LICENSE) — Michal Kosiorek
