# LootDrop QR Proof-of-Visit SDK

Rust library for creating and verifying QR-based proof-of-visit attestations used by the LootDrop protocol.

## Overview

The SDK handles:

- **Tag provisioning** — Write campaign data + Ed25519 keypair to QR codes
- **Proof generation** — Create signed proof-of-visit messages on the tag's secure element
- **Proof verification** — Verify proofs on-chain or off-chain before reward distribution

## Architecture

```
┌────────────────────┐
│   QR Code (NTAG)   │
│  ┌──────────────┐  │
│  │ Ed25519 Key  │  │
│  │ Campaign ID  │  │
│  │ NDEF Records │  │
│  └──────────────┘  │
└─────────┬──────────┘
          │ tap
┌─────────▼──────────┐
│   Seeker Device    │
│  ┌──────────────┐  │
│  │ Read NDEF    │  │
│  │ Challenge    │──┼──▶ Tag signs [wallet|ts|campaign]
│  │ Build Proof  │  │
│  └──────────────┘  │
└─────────┬──────────┘
          │ submit
┌─────────▼──────────┐
│  Solana Program    │
│  ┌──────────────┐  │
│  │ Verify sig   │  │
│  │ Release SOL  │  │
│  └──────────────┘  │
└────────────────────┘
```

## Proof Message Format

48-byte message signed by the QR code:

| Offset | Size | Field |
|--------|------|-------|
| 0 | 32 | Claimer wallet public key |
| 32 | 8 | Unix timestamp (LE) |
| 40 | 8 | Campaign ID (LE) |

## Usage

### Tag Provisioning

```rust
use lootdrop_sdk::{TagProvisioner, CampaignConfig};

let config = CampaignConfig {
    campaign_id: 42,
    program_id: "LooTDr0p...",
    merchant_pubkey: "7xKX...",
};

let provisioner = TagProvisioner::new()?;
let tag_keypair = provisioner.provision_tag(&config)?;

println!("Tag pubkey: {}", tag_keypair.public_key_hex());
```

### Proof Verification

```rust
use lootdrop_sdk::{ProofVerifier, ProofMessage};

let verifier = ProofVerifier::new();
let proof = ProofMessage {
    claimer: claimer_pubkey,
    timestamp: 1706000000,
    campaign_id: 42,
};

let valid = verifier.verify(
    &proof.to_bytes(),
    &qr_signature,
    &tag_public_key,
)?;
```

## QR Code Requirements

- **Type**: NTAG 424 DNA or equivalent with crypto coprocessor
- **Memory**: Minimum 256 bytes user memory
- **Crypto**: Ed25519 key generation + signing
- **NDEF**: Must support NDEF message formatting
- **Provisioning**: One-time write with permanent key lock

## Security Model

1. Each QR code has a unique Ed25519 keypair burned at provisioning
2. The public key is registered on-chain when the campaign is created
3. At claim time, the tag signs a challenge containing the claimer's wallet
4. The signature is verified on-chain before releasing rewards
5. Tags cannot be cloned — the private key never leaves the secure element
6. Replay protection: each (campaign, wallet) pair can only claim once

## Building

```bash
cargo build --release
cargo test
cargo doc --open
```

## License

MIT
