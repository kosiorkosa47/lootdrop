<h1 align="center">LootDrop PWA</h1>

<p align="center">
  <strong>Mobile-optimized Progressive Web App for Solana dApp Store</strong><br/>
  A reference PWA sample + Bubblewrap template for Solana Mobile developers.
</p>

<p align="center">
  <a href="#about">About</a> •
  <a href="#pwa-features">PWA Features</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#getting-started">Getting Started</a> •
  <a href="#bubblewrap">Bubblewrap</a> •
  <a href="#license">License</a>
</p>

---

## About

LootDrop is a **location-based crypto rewards** PWA built as a reference implementation for publishing mobile-optimized web apps to the **Solana dApp Store** via [Bubblewrap CLI](https://github.com/GoogleChromeLabs/bubblewrap).

It serves two purposes:
1. A **functional dApp** — businesses drop SOL/USDC rewards at physical locations, users collect via QR scan
2. A **developer sample** — showcasing every PWA optimization needed for the Solana dApp Store

Built for the [Solana Mobile PWA Improved Template RFP](https://solanamobile.com/grants).

## PWA Features

### Mobile Optimizations (RFP Deliverables)

| Feature | Implementation |
|---------|---------------|
| **Splash Screen** | Custom branded loading with skeleton states |
| **Chrome Default** | TWA configured for Chrome Custom Tabs with WebView fallback |
| **Bottom Navigation** | 4-tab nav: Map, Scan, Rewards, Profile |
| **Pull-to-Refresh** | Native-feeling refresh on feed |
| **Offline Support** | Service worker with cache-first strategy |
| **Dark Mode** | System-preference auto-detect + manual toggle |
| **Touch Targets** | Min 44px tap areas, haptic-style feedback |
| **Safe Areas** | Notch and bottom bar handling |

### Solana Integration

- Mobile Wallet Adapter (MWA) within PWA/TWA context
- Seed Vault compatible signing
- Solana Pay deep links for merchant QR codes
- On-chain reward escrow via Anchor smart contracts

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | SvelteKit 5 + TypeScript |
| Styling | Tailwind CSS |
| Solana | @solana/web3.js + wallet-adapter |
| PWA | Web App Manifest + Service Worker |
| Android Wrap | Bubblewrap CLI (TWA) |
| Smart Contract | Anchor (Rust) |
| Backend | FastAPI (Python) |

## Getting Started

### PWA (Web App)

```bash
cd web
npm install
npm run dev
# Opens at http://localhost:5173
```

### Build for Production

```bash
cd web
npm run build
npm run preview
```

### Smart Contract

```bash
cd programs/lootdrop
anchor build
anchor test
```

### Backend

```bash
cd backend
pip install -r requirements.txt
uvicorn main:app --reload
```

## Bubblewrap

Wrap the PWA as an Android app for the Solana dApp Store:

```bash
# Install Bubblewrap CLI
npm i -g @bubblewrap/cli

# Initialize from twa-manifest.json
cd web
bubblewrap init --manifest twa-manifest.json

# Build APK
bubblewrap build

# Output: app-release-signed.apk — ready for dApp Store
```

### TWA Configuration

See `web/twa-manifest.json` for the full Trusted Web Activity config:
- Chrome Custom Tabs as default browser
- Fallback to system WebView
- Custom splash screen with fade
- Digital Asset Links placeholder

## Project Structure

```
lootdrop/
├── web/                       # SvelteKit PWA (main deliverable)
│   ├── src/
│   │   ├── routes/            # Pages: /, /scan, /rewards, /profile
│   │   └── lib/
│   │       ├── components/    # SplashScreen, BottomNav, DropCard, etc.
│   │       ├── stores/        # Svelte 5 reactive state
│   │       ├── data/          # Mock data (10 businesses, rewards)
│   │       └── types/         # TypeScript interfaces
│   ├── static/
│   │   ├── manifest.json      # Web App Manifest
│   │   ├── sw.js              # Service worker (offline cache)
│   │   └── icons/             # PWA icons (192/512, maskable)
│   ├── twa-manifest.json      # Bubblewrap TWA config
│   └── package.json
├── programs/lootdrop/         # Anchor smart contract (escrow + claims)
│   └── src/lib.rs
└── backend/                   # FastAPI merchant API
    └── main.py
```

## Roadmap

- [x] **v0.1** — Project scaffold, smart contract, QR SDK
- [x] **v0.2** — SvelteKit PWA with mobile UI
- [ ] **v0.3** — Bubblewrap wrapping + dApp Store submission
- [ ] **v0.4** — MWA integration in TWA context
- [ ] **v0.5** — Merchant dashboard
- [ ] **v1.0** — Mainnet launch

## License

[MIT](LICENSE) © 2026 Michal Kosiorek
