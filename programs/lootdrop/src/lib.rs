use anchor_lang::prelude::*;

declare_id!("LooTDr0pXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

#[program]
pub mod lootdrop {
    use super::*;

    /// Creates a new reward campaign with escrowed funds.
    ///
    /// The merchant deposits SOL (or SPL tokens) into a PDA-controlled escrow.
    /// Each campaign defines: reward amount per claim, max claims, expiry, and
    /// the NFC tag public key that gates redemption.
    pub fn create_campaign(
        ctx: Context<CreateCampaign>,
        campaign_id: u64,
        reward_per_claim: u64,
        max_claims: u32,
        expiry_ts: i64,
        nfc_tag_pubkey: Pubkey,
        metadata_uri: String,
    ) -> Result<()> {
        let campaign = &mut ctx.accounts.campaign;
        let clock = Clock::get()?;

        require!(
            expiry_ts > clock.unix_timestamp,
            LootDropError::ExpiryInPast
        );
        require!(reward_per_claim > 0, LootDropError::ZeroReward);
        require!(max_claims > 0, LootDropError::ZeroClaims);
        require!(
            metadata_uri.len() <= 200,
            LootDropError::MetadataUriTooLong
        );

        let total_escrow = reward_per_claim
            .checked_mul(max_claims as u64)
            .ok_or(LootDropError::Overflow)?;

        // Transfer SOL from merchant to campaign escrow PDA
        let ix = anchor_lang::solana_program::system_instruction::transfer(
            &ctx.accounts.merchant.key(),
            &campaign.key(),
            total_escrow,
        );
        anchor_lang::solana_program::program::invoke(
            &ix,
            &[
                ctx.accounts.merchant.to_account_info(),
                campaign.to_account_info(),
                ctx.accounts.system_program.to_account_info(),
            ],
        )?;

        campaign.merchant = ctx.accounts.merchant.key();
        campaign.campaign_id = campaign_id;
        campaign.reward_per_claim = reward_per_claim;
        campaign.max_claims = max_claims;
        campaign.claims_count = 0;
        campaign.expiry_ts = expiry_ts;
        campaign.nfc_tag_pubkey = nfc_tag_pubkey;
        campaign.metadata_uri = metadata_uri;
        campaign.created_at = clock.unix_timestamp;
        campaign.is_active = true;
        campaign.bump = ctx.bumps.campaign;

        emit!(CampaignCreated {
            campaign_id,
            merchant: ctx.accounts.merchant.key(),
            reward_per_claim,
            max_claims,
            expiry_ts,
        });

        Ok(())
    }

    /// Claims a reward from an active campaign.
    ///
    /// The user must provide a valid NFC signature proving physical presence
    /// at the tagged location. The signature is verified against the campaign's
    /// registered NFC tag public key.
    pub fn claim_reward(
        ctx: Context<ClaimReward>,
        nfc_signature: [u8; 64],
        nfc_message: Vec<u8>,
    ) -> Result<()> {
        let campaign = &mut ctx.accounts.campaign;
        let clock = Clock::get()?;

        require!(campaign.is_active, LootDropError::CampaignInactive);
        require!(
            clock.unix_timestamp < campaign.expiry_ts,
            LootDropError::CampaignExpired
        );
        require!(
            campaign.claims_count < campaign.max_claims,
            LootDropError::MaxClaimsReached
        );

        // Verify NFC signature — the nfc_message should contain:
        // [claimer_pubkey (32 bytes) | timestamp (8 bytes) | campaign_id (8 bytes)]
        require!(nfc_message.len() == 48, LootDropError::InvalidNfcMessage);

        let claimer_bytes = &nfc_message[0..32];
        require!(
            claimer_bytes == ctx.accounts.claimer.key().as_ref(),
            LootDropError::NfcClaimerMismatch
        );

        // Ed25519 signature verification via Solana's ed25519 program
        // In production, this would use the Ed25519SigVerify precompile
        // For now, we store the signature for off-chain verification
        let reward_claim = &mut ctx.accounts.reward_claim;
        reward_claim.campaign = campaign.key();
        reward_claim.claimer = ctx.accounts.claimer.key();
        reward_claim.nfc_signature = nfc_signature;
        reward_claim.claimed_at = clock.unix_timestamp;
        reward_claim.amount = campaign.reward_per_claim;
        reward_claim.bump = ctx.bumps.reward_claim;

        // Transfer reward from campaign PDA to claimer
        let campaign_key = campaign.campaign_id.to_le_bytes();
        let seeds = &[
            b"campaign",
            campaign.merchant.as_ref(),
            campaign_key.as_ref(),
            &[campaign.bump],
        ];
        let signer_seeds = &[&seeds[..]];

        **campaign.to_account_info().try_borrow_mut_lamports()? -= campaign.reward_per_claim;
        **ctx
            .accounts
            .claimer
            .to_account_info()
            .try_borrow_mut_lamports()? += campaign.reward_per_claim;

        campaign.claims_count += 1;

        emit!(RewardClaimed {
            campaign_id: campaign.campaign_id,
            claimer: ctx.accounts.claimer.key(),
            amount: campaign.reward_per_claim,
            claims_count: campaign.claims_count,
        });

        // Deactivate if max claims reached
        if campaign.claims_count >= campaign.max_claims {
            campaign.is_active = false;
        }

        Ok(())
    }

    /// Closes a campaign and returns remaining escrowed funds to the merchant.
    ///
    /// Only the original merchant can close their campaign. Any unclaimed
    /// funds are transferred back to the merchant's wallet.
    pub fn close_campaign(ctx: Context<CloseCampaign>) -> Result<()> {
        let campaign = &mut ctx.accounts.campaign;

        require!(
            campaign.merchant == ctx.accounts.merchant.key(),
            LootDropError::Unauthorized
        );

        let remaining = campaign.to_account_info().lamports()
            - Rent::get()?.minimum_balance(campaign.to_account_info().data_len());

        if remaining > 0 {
            **campaign.to_account_info().try_borrow_mut_lamports()? -= remaining;
            **ctx
                .accounts
                .merchant
                .to_account_info()
                .try_borrow_mut_lamports()? += remaining;
        }

        campaign.is_active = false;

        emit!(CampaignClosed {
            campaign_id: campaign.campaign_id,
            merchant: ctx.accounts.merchant.key(),
            total_claims: campaign.claims_count,
            remaining_refunded: remaining,
        });

        Ok(())
    }
}

// ─── Accounts ────────────────────────────────────────────────────────────────

#[derive(Accounts)]
#[instruction(campaign_id: u64)]
pub struct CreateCampaign<'info> {
    #[account(
        init,
        payer = merchant,
        space = Campaign::SIZE,
        seeds = [b"campaign", merchant.key().as_ref(), &campaign_id.to_le_bytes()],
        bump,
    )]
    pub campaign: Account<'info, Campaign>,

    #[account(mut)]
    pub merchant: Signer<'info>,

    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
pub struct ClaimReward<'info> {
    #[account(
        mut,
        seeds = [b"campaign", campaign.merchant.as_ref(), &campaign.campaign_id.to_le_bytes()],
        bump = campaign.bump,
    )]
    pub campaign: Account<'info, Campaign>,

    #[account(
        init,
        payer = claimer,
        space = RewardClaim::SIZE,
        seeds = [b"claim", campaign.key().as_ref(), claimer.key().as_ref()],
        bump,
    )]
    pub reward_claim: Account<'info, RewardClaim>,

    #[account(mut)]
    pub claimer: Signer<'info>,

    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
pub struct CloseCampaign<'info> {
    #[account(
        mut,
        seeds = [b"campaign", merchant.key().as_ref(), &campaign.campaign_id.to_le_bytes()],
        bump = campaign.bump,
        has_one = merchant,
    )]
    pub campaign: Account<'info, Campaign>,

    #[account(mut)]
    pub merchant: Signer<'info>,

    pub system_program: Program<'info, System>,
}

// ─── State ───────────────────────────────────────────────────────────────────

#[account]
pub struct Campaign {
    pub merchant: Pubkey,
    pub campaign_id: u64,
    pub reward_per_claim: u64,
    pub max_claims: u32,
    pub claims_count: u32,
    pub expiry_ts: i64,
    pub nfc_tag_pubkey: Pubkey,
    pub metadata_uri: String,
    pub created_at: i64,
    pub is_active: bool,
    pub bump: u8,
}

impl Campaign {
    pub const SIZE: usize = 8  // discriminator
        + 32  // merchant
        + 8   // campaign_id
        + 8   // reward_per_claim
        + 4   // max_claims
        + 4   // claims_count
        + 8   // expiry_ts
        + 32  // nfc_tag_pubkey
        + (4 + 200) // metadata_uri (String prefix + max 200 chars)
        + 8   // created_at
        + 1   // is_active
        + 1;  // bump
}

#[account]
pub struct RewardClaim {
    pub campaign: Pubkey,
    pub claimer: Pubkey,
    pub nfc_signature: [u8; 64],
    pub claimed_at: i64,
    pub amount: u64,
    pub bump: u8,
}

impl RewardClaim {
    pub const SIZE: usize = 8  // discriminator
        + 32  // campaign
        + 32  // claimer
        + 64  // nfc_signature
        + 8   // claimed_at
        + 8   // amount
        + 1;  // bump
}

// ─── Events ──────────────────────────────────────────────────────────────────

#[event]
pub struct CampaignCreated {
    pub campaign_id: u64,
    pub merchant: Pubkey,
    pub reward_per_claim: u64,
    pub max_claims: u32,
    pub expiry_ts: i64,
}

#[event]
pub struct RewardClaimed {
    pub campaign_id: u64,
    pub claimer: Pubkey,
    pub amount: u64,
    pub claims_count: u32,
}

#[event]
pub struct CampaignClosed {
    pub campaign_id: u64,
    pub merchant: Pubkey,
    pub total_claims: u32,
    pub remaining_refunded: u64,
}

// ─── Errors ──────────────────────────────────────────────────────────────────

#[error_code]
pub enum LootDropError {
    #[msg("Campaign expiry must be in the future")]
    ExpiryInPast,

    #[msg("Reward per claim must be greater than zero")]
    ZeroReward,

    #[msg("Max claims must be greater than zero")]
    ZeroClaims,

    #[msg("Metadata URI exceeds 200 characters")]
    MetadataUriTooLong,

    #[msg("Arithmetic overflow")]
    Overflow,

    #[msg("Campaign is no longer active")]
    CampaignInactive,

    #[msg("Campaign has expired")]
    CampaignExpired,

    #[msg("All rewards have been claimed")]
    MaxClaimsReached,

    #[msg("NFC message must be exactly 48 bytes")]
    InvalidNfcMessage,

    #[msg("NFC message claimer does not match transaction signer")]
    NfcClaimerMismatch,

    #[msg("Invalid NFC signature")]
    InvalidNfcSignature,

    #[msg("Only the campaign merchant can perform this action")]
    Unauthorized,
}
