<p align="center">
  <img src="docs/logo-placeholder.png" alt="LootDrop" width="200"/>
</p>

<h1 align="center">LootDrop</h1>

<p align="center">
  <strong>Location-based crypto rewards for the real world.</strong><br/>
  Walk in. Tap. Earn. Powered by Solana.
</p>

<p align="center">
  <a href="#how-it-works">How It Works</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#getting-started">Getting Started</a> •
  <a href="#roadmap">Roadmap</a> •
  <a href="#license">License</a>
</p>

---

## What is LootDrop?

LootDrop is a location-based crypto rewards network built for **Solana Seeker**. Merchants and brands create geo-fenced reward campaigns. Users physically visit locations, tap an NFC tag with their Seeker device, and instantly receive token rewards via on-chain escrow.

No QR codes. No check-ins. No trust assumptions. Just cryptographic proof-of-visit powered by NFC + Solana.

### Why?

- **For merchants**: Drive real foot traffic with measurable on-chain analytics
- **For users**: Earn crypto rewards just by visiting places you already go
- **For Solana**: A killer use case for Seeker's NFC hardware

## How It Works

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│   Merchant   │────▶│  LootDrop    │────▶│  Solana Program  │
│   Dashboard  │     │  Backend     │     │  (Anchor)        │
└─────────────┘     └──────┬───────┘     └────────┬────────┘
                           │                       │
                           │  Campaign created     │ Escrow funded
                           │                       │
┌─────────────┐     ┌──────▼───────┐     ┌────────▼────────┐
│  NFC Tag     │◀───│  Seeker App  │────▶│  Claim Reward    │
│  @ Location  │───▶│  (Kotlin)    │     │  (on-chain)      │
└─────────────┘     └──────────────┘     └─────────────────┘

1. Merchant creates campaign → funds escrowed on-chain
2. NFC tags deployed at physical locations
3. User taps NFC tag with Solana Seeker
4. App reads tag, signs proof-of-visit
5. Smart contract verifies + releases reward
6. Merchant sees analytics in real-time
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Smart Contract | Anchor (Rust) on Solana |
| Mobile App | Kotlin + Solana Mobile SDK + Seed Vault |
| Backend | FastAPI (Python) |
| NFC SDK | Rust library for tag verification |
| Database | PostgreSQL + Redis |

## Getting Started

### Prerequisites

- Rust 1.75+ & Anchor CLI 0.30+
- Android Studio Hedgehog+
- Python 3.11+
- Solana CLI 1.18+

### Smart Contract

```bash
cd programs/lootdrop
anchor build
anchor test
anchor deploy --provider.cluster devnet
```

### Backend

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload
```

### Android App

Open `app/` in Android Studio, connect a Solana Seeker device (or emulator with NFC support), and run.

### NFC SDK

```bash
cd sdk
cargo build
cargo test
```

## Project Structure

```
lootdrop/
├── programs/lootdrop/     # Anchor smart contract
│   ├── src/lib.rs         # Program instructions & accounts
│   ├── Cargo.toml
│   └── Anchor.toml
├── app/                   # Android/Kotlin Seeker app
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/lootdrop/
│           ├── MainActivity.kt
│           └── NfcVerifier.kt
├── backend/               # FastAPI merchant backend
│   ├── main.py
│   └── requirements.txt
├── sdk/                   # NFC proof-of-visit SDK
│   ├── src/lib.rs
│   └── README.md
└── docs/                  # Documentation
```

## Roadmap

- [x] **v0.1** — Project scaffold, smart contract skeleton, NFC SDK design
- [ ] **v0.2** — On-chain escrow + claim flow (devnet)
- [ ] **v0.3** — Android app with NFC tap-to-claim
- [ ] **v0.4** — Merchant dashboard (web)
- [ ] **v0.5** — Multi-token campaigns (SPL, compressed NFTs)
- [ ] **v0.6** — Geofence verification layer
- [ ] **v0.7** — Analytics API + webhook integrations
- [ ] **v1.0** — Mainnet launch

## Contributing

We're building in the open. Issues and PRs welcome. See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for guidelines.

## License

[MIT](LICENSE) © 2026 Michal Kosiorek
