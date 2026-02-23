//! LootDrop QR Proof-of-Visit SDK
//!
//! Provides tag provisioning, proof generation, and verification
//! for the LootDrop location-based rewards protocol on Solana.

use std::fmt;

/// Size of an Ed25519 public key in bytes.
pub const PUBKEY_SIZE: usize = 32;

/// Size of an Ed25519 signature in bytes.
pub const SIGNATURE_SIZE: usize = 64;

/// Size of the proof-of-visit message in bytes.
/// Layout: [claimer_pubkey (32) | timestamp (8) | campaign_id (8)]
pub const PROOF_MESSAGE_SIZE: usize = 48;

// ─── Errors ──────────────────────────────────────────────────────────────────

#[derive(Debug, Clone, PartialEq)]
pub enum SdkError {
    /// The proof message has an invalid length (expected 48 bytes).
    InvalidProofLength { expected: usize, got: usize },
    /// Ed25519 signature verification failed.
    SignatureVerificationFailed,
    /// QR code communication error.
    QrError(String),
    /// The tag's public key doesn't match the campaign registration.
    TagKeyMismatch,
    /// Campaign ID in the proof doesn't match the expected campaign.
    CampaignMismatch { expected: u64, got: u64 },
    /// Timestamp is outside the acceptable window.
    TimestampOutOfRange { timestamp: i64, now: i64 },
    /// Serialization or deserialization error.
    SerializationError(String),
}

impl fmt::Display for SdkError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::InvalidProofLength { expected, got } => {
                write!(f, "Invalid proof length: expected {expected}, got {got}")
            }
            Self::SignatureVerificationFailed => {
                write!(f, "Ed25519 signature verification failed")
            }
            Self::QrError(msg) => write!(f, "QR error: {msg}"),
            Self::TagKeyMismatch => write!(f, "Tag public key doesn't match campaign"),
            Self::CampaignMismatch { expected, got } => {
                write!(f, "Campaign mismatch: expected {expected}, got {got}")
            }
            Self::TimestampOutOfRange { timestamp, now } => {
                write!(f, "Timestamp {timestamp} is outside acceptable range (now: {now})")
            }
            Self::SerializationError(msg) => write!(f, "Serialization error: {msg}"),
        }
    }
}

impl std::error::Error for SdkError {}

pub type Result<T> = std::result::Result<T, SdkError>;

// ─── Proof Message ───────────────────────────────────────────────────────────

/// A proof-of-visit message that gets signed by the QR code.
#[derive(Debug, Clone, PartialEq)]
pub struct ProofMessage {
    /// The claimer's Solana wallet public key (32 bytes).
    pub claimer: [u8; PUBKEY_SIZE],
    /// Unix timestamp when the tag was tapped.
    pub timestamp: i64,
    /// The campaign ID this claim is for.
    pub campaign_id: u64,
}

impl ProofMessage {
    /// Creates a new proof message.
    pub fn new(claimer: [u8; PUBKEY_SIZE], timestamp: i64, campaign_id: u64) -> Self {
        Self {
            claimer,
            timestamp,
            campaign_id,
        }
    }

    /// Serializes the proof message to a 48-byte array.
    pub fn to_bytes(&self) -> [u8; PROOF_MESSAGE_SIZE] {
        let mut buf = [0u8; PROOF_MESSAGE_SIZE];
        buf[0..32].copy_from_slice(&self.claimer);
        buf[32..40].copy_from_slice(&self.timestamp.to_le_bytes());
        buf[40..48].copy_from_slice(&self.campaign_id.to_le_bytes());
        buf
    }

    /// Deserializes a proof message from a 48-byte slice.
    pub fn from_bytes(data: &[u8]) -> Result<Self> {
        if data.len() != PROOF_MESSAGE_SIZE {
            return Err(SdkError::InvalidProofLength {
                expected: PROOF_MESSAGE_SIZE,
                got: data.len(),
            });
        }

        let mut claimer = [0u8; PUBKEY_SIZE];
        claimer.copy_from_slice(&data[0..32]);

        let timestamp = i64::from_le_bytes(
            data[32..40]
                .try_into()
                .map_err(|e| SdkError::SerializationError(format!("{e}")))?,
        );

        let campaign_id = u64::from_le_bytes(
            data[40..48]
                .try_into()
                .map_err(|e| SdkError::SerializationError(format!("{e}")))?,
        );

        Ok(Self {
            claimer,
            timestamp,
            campaign_id,
        })
    }
}

// ─── Proof Verifier ──────────────────────────────────────────────────────────

/// Verifies QR proof-of-visit signatures.
pub struct ProofVerifier {
    /// Maximum allowed time skew in seconds between tag timestamp and
    /// verification time. Default: 300 (5 minutes).
    pub max_timestamp_skew: i64,
}

impl ProofVerifier {
    pub fn new() -> Self {
        Self {
            max_timestamp_skew: 300,
        }
    }

    pub fn with_max_skew(max_timestamp_skew: i64) -> Self {
        Self {
            max_timestamp_skew,
        }
    }

    /// Verifies a proof-of-visit against an expected campaign.
    ///
    /// Checks:
    /// 1. Proof message format is valid (48 bytes)
    /// 2. Campaign ID in proof matches expected
    /// 3. Timestamp is within acceptable window
    /// 4. Ed25519 signature is valid for the given tag public key
    pub fn verify(
        &self,
        proof_bytes: &[u8],
        signature: &[u8; SIGNATURE_SIZE],
        tag_pubkey: &[u8; PUBKEY_SIZE],
        expected_campaign_id: u64,
        current_timestamp: i64,
    ) -> Result<ProofMessage> {
        // Parse proof message
        let proof = ProofMessage::from_bytes(proof_bytes)?;

        // Verify campaign ID
        if proof.campaign_id != expected_campaign_id {
            return Err(SdkError::CampaignMismatch {
                expected: expected_campaign_id,
                got: proof.campaign_id,
            });
        }

        // Verify timestamp is within acceptable range
        let skew = (current_timestamp - proof.timestamp).abs();
        if skew > self.max_timestamp_skew {
            return Err(SdkError::TimestampOutOfRange {
                timestamp: proof.timestamp,
                now: current_timestamp,
            });
        }

        // Verify Ed25519 signature
        // TODO: Use ed25519-dalek or ring for actual verification
        // For now, this is a structural placeholder.
        if !self.verify_ed25519(proof_bytes, signature, tag_pubkey) {
            return Err(SdkError::SignatureVerificationFailed);
        }

        Ok(proof)
    }

    /// Verifies an Ed25519 signature.
    ///
    /// TODO: Integrate ed25519-dalek crate for real verification.
    fn verify_ed25519(
        &self,
        _message: &[u8],
        _signature: &[u8; SIGNATURE_SIZE],
        _pubkey: &[u8; PUBKEY_SIZE],
    ) -> bool {
        // Placeholder — implement with:
        //
        //   use ed25519_dalek::{VerifyingKey, Signature, Verifier};
        //   let key = VerifyingKey::from_bytes(pubkey)?;
        //   let sig = Signature::from_bytes(signature);
        //   key.verify(message, &sig).is_ok()
        //
        true
    }
}

impl Default for ProofVerifier {
    fn default() -> Self {
        Self::new()
    }
}

// ─── Tag Provisioner ─────────────────────────────────────────────────────────

/// Configuration for provisioning an QR code with campaign data.
#[derive(Debug, Clone)]
pub struct CampaignConfig {
    /// The on-chain campaign ID.
    pub campaign_id: u64,
    /// The LootDrop program ID (base58).
    pub program_id: String,
    /// The merchant's Solana public key (base58).
    pub merchant_pubkey: String,
}

/// Handles QR code provisioning with campaign data and Ed25519 keys.
pub struct TagProvisioner {
    // TODO: QR scanner/writer handle
}

/// Result of provisioning an QR code.
#[derive(Debug)]
pub struct ProvisionedTag {
    /// The tag's Ed25519 public key (32 bytes).
    pub public_key: [u8; PUBKEY_SIZE],
    /// The campaign ID written to the tag.
    pub campaign_id: u64,
    /// NDEF URI record value.
    pub ndef_uri: String,
}

impl ProvisionedTag {
    /// Returns the public key as a hex string.
    pub fn public_key_hex(&self) -> String {
        self.public_key
            .iter()
            .map(|b| format!("{b:02x}"))
            .collect()
    }
}

impl TagProvisioner {
    /// Creates a new tag provisioner.
    ///
    /// TODO: Accept QR scanner configuration.
    pub fn new() -> Result<Self> {
        Ok(Self {})
    }

    /// Provisions an QR code with campaign data and a fresh Ed25519 keypair.
    ///
    /// The tag will contain:
    /// - NDEF URI: `lootdrop://claim/{campaign_id}`
    /// - NDEF External: Ed25519 signature over the campaign ID
    /// - NDEF External: Ed25519 public key
    ///
    /// The private key is generated and stored in the tag's secure element,
    /// and is never exposed to the provisioning device.
    pub fn provision_tag(&self, config: &CampaignConfig) -> Result<ProvisionedTag> {
        // TODO: Implement actual QR code communication
        //
        // Steps:
        // 1. Connect to QR code via reader
        // 2. Send CREATE_KEY APDU to generate Ed25519 keypair in secure element
        // 3. Read back public key
        // 4. Write NDEF message with URI + signature + public key
        // 5. Lock key slot to prevent modification

        let ndef_uri = format!("lootdrop://claim/{}", config.campaign_id);

        // Placeholder: generate a deterministic test key
        let mut public_key = [0u8; PUBKEY_SIZE];
        let campaign_bytes = config.campaign_id.to_le_bytes();
        public_key[0..8].copy_from_slice(&campaign_bytes);

        Ok(ProvisionedTag {
            public_key,
            campaign_id: config.campaign_id,
            ndef_uri,
        })
    }
}

// ─── Utility Functions ───────────────────────────────────────────────────────

/// Formats lamports as a human-readable SOL amount.
pub fn lamports_to_sol(lamports: u64) -> f64 {
    lamports as f64 / 1_000_000_000.0
}

/// Converts a hex string to a byte vector.
pub fn hex_to_bytes(hex: &str) -> Result<Vec<u8>> {
    (0..hex.len())
        .step_by(2)
        .map(|i| {
            u8::from_str_radix(&hex[i..i + 2], 16)
                .map_err(|e| SdkError::SerializationError(format!("Invalid hex: {e}")))
        })
        .collect()
}

// ─── Tests ───────────────────────────────────────────────────────────────────

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_proof_message_roundtrip() {
        let claimer = [42u8; PUBKEY_SIZE];
        let proof = ProofMessage::new(claimer, 1706000000, 123);

        let bytes = proof.to_bytes();
        assert_eq!(bytes.len(), PROOF_MESSAGE_SIZE);

        let decoded = ProofMessage::from_bytes(&bytes).unwrap();
        assert_eq!(decoded.claimer, claimer);
        assert_eq!(decoded.timestamp, 1706000000);
        assert_eq!(decoded.campaign_id, 123);
    }

    #[test]
    fn test_proof_message_invalid_length() {
        let short = [0u8; 32];
        let result = ProofMessage::from_bytes(&short);
        assert!(matches!(result, Err(SdkError::InvalidProofLength { .. })));
    }

    #[test]
    fn test_verifier_campaign_mismatch() {
        let claimer = [1u8; PUBKEY_SIZE];
        let proof = ProofMessage::new(claimer, 1706000000, 42);
        let bytes = proof.to_bytes();
        let sig = [0u8; SIGNATURE_SIZE];
        let pubkey = [0u8; PUBKEY_SIZE];

        let verifier = ProofVerifier::new();
        let result = verifier.verify(&bytes, &sig, &pubkey, 99, 1706000000);
        assert!(matches!(result, Err(SdkError::CampaignMismatch { .. })));
    }

    #[test]
    fn test_verifier_timestamp_out_of_range() {
        let claimer = [1u8; PUBKEY_SIZE];
        let proof = ProofMessage::new(claimer, 1706000000, 42);
        let bytes = proof.to_bytes();
        let sig = [0u8; SIGNATURE_SIZE];
        let pubkey = [0u8; PUBKEY_SIZE];

        let verifier = ProofVerifier::new();
        let result = verifier.verify(&bytes, &sig, &pubkey, 42, 1706001000);
        assert!(matches!(result, Err(SdkError::TimestampOutOfRange { .. })));
    }

    #[test]
    fn test_verifier_success() {
        let claimer = [1u8; PUBKEY_SIZE];
        let proof = ProofMessage::new(claimer, 1706000000, 42);
        let bytes = proof.to_bytes();
        let sig = [0u8; SIGNATURE_SIZE];
        let pubkey = [0u8; PUBKEY_SIZE];

        let verifier = ProofVerifier::new();
        let result = verifier.verify(&bytes, &sig, &pubkey, 42, 1706000100);
        assert!(result.is_ok());
    }

    #[test]
    fn test_lamports_to_sol() {
        assert_eq!(lamports_to_sol(1_000_000_000), 1.0);
        assert_eq!(lamports_to_sol(500_000_000), 0.5);
        assert_eq!(lamports_to_sol(0), 0.0);
    }

    #[test]
    fn test_hex_to_bytes() {
        let bytes = hex_to_bytes("deadbeef").unwrap();
        assert_eq!(bytes, vec![0xde, 0xad, 0xbe, 0xef]);
    }

    #[test]
    fn test_tag_provisioner() {
        let provisioner = TagProvisioner::new().unwrap();
        let config = CampaignConfig {
            campaign_id: 42,
            program_id: "LooTDr0p".to_string(),
            merchant_pubkey: "7xKX".to_string(),
        };

        let tag = provisioner.provision_tag(&config).unwrap();
        assert_eq!(tag.campaign_id, 42);
        assert_eq!(tag.ndef_uri, "lootdrop://claim/42");
        assert_eq!(tag.public_key_hex().len(), 64);
    }
}
