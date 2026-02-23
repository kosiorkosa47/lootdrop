"""
LootDrop Backend — Merchant API & Claim Relay

Handles campaign management, claim verification, and analytics.
Acts as a relay between the mobile app and the Solana program.
"""

from __future__ import annotations

import os
import time
import uuid
from datetime import datetime, timezone
from enum import Enum
from typing import Optional

import httpx
from fastapi import FastAPI, HTTPException, Query, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

# ─── App Setup ────────────────────────────────────────────────────────────────

app = FastAPI(
    title="LootDrop API",
    description="Location-based crypto rewards network for Solana Seeker",
    version="0.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # TODO: restrict in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

SOLANA_RPC = os.getenv("SOLANA_RPC_URL", "https://api.devnet.solana.com")
PROGRAM_ID = os.getenv("LOOTDROP_PROGRAM_ID", "LooTDr0pXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

# ─── Models ───────────────────────────────────────────────────────────────────


class CampaignStatus(str, Enum):
    ACTIVE = "active"
    PAUSED = "paused"
    EXPIRED = "expired"
    CLOSED = "closed"


class CampaignCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    description: str = Field(default="", max_length=500)
    merchant_wallet: str = Field(..., min_length=32, max_length=44)
    reward_per_claim_lamports: int = Field(..., gt=0)
    max_claims: int = Field(..., gt=0, le=1_000_000)
    expiry_ts: int = Field(..., description="Unix timestamp for campaign expiry")
    nfc_tag_pubkey: str = Field(..., min_length=32, max_length=44)
    latitude: float = Field(..., ge=-90, le=90)
    longitude: float = Field(..., ge=-180, le=180)
    radius_meters: int = Field(default=50, ge=10, le=5000)
    metadata_uri: str = Field(default="", max_length=200)


class CampaignResponse(BaseModel):
    id: str
    name: str
    description: str
    merchant_wallet: str
    reward_per_claim_lamports: int
    max_claims: int
    claims_count: int
    expiry_ts: int
    status: CampaignStatus
    nfc_tag_pubkey: str
    latitude: float
    longitude: float
    radius_meters: int
    metadata_uri: str
    created_at: str
    on_chain_address: Optional[str] = None


class ClaimRequest(BaseModel):
    campaign_id: str
    wallet_pubkey: str = Field(..., min_length=32, max_length=44)
    nfc_signature: str = Field(..., min_length=128, max_length=128, description="Hex-encoded Ed25519 signature")
    proof_message: str = Field(..., min_length=96, max_length=96, description="Hex-encoded 48-byte proof")
    tag_pubkey: str = Field(..., min_length=32, max_length=44)


class ClaimResponse(BaseModel):
    tx_signature: str
    reward_amount: float
    campaign_name: str
    claimed_at: str


class MerchantCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    wallet_address: str = Field(..., min_length=32, max_length=44)
    business_type: str = Field(default="retail", max_length=50)
    website: str = Field(default="", max_length=200)
    contact_email: str = Field(default="", max_length=200)


class MerchantResponse(BaseModel):
    id: str
    name: str
    wallet_address: str
    business_type: str
    website: str
    contact_email: str
    total_campaigns: int
    total_rewards_distributed_lamports: int
    created_at: str


class AnalyticsResponse(BaseModel):
    total_campaigns: int
    active_campaigns: int
    total_claims: int
    total_rewards_distributed_lamports: int
    unique_claimers: int
    claims_last_24h: int
    claims_last_7d: int
    top_campaigns: list[dict]


# ─── In-memory storage (replace with DB in production) ────────────────────────

campaigns_db: dict[str, dict] = {}
merchants_db: dict[str, dict] = {}
claims_db: list[dict] = []

# ─── Health Check ─────────────────────────────────────────────────────────────


@app.get("/health")
async def health():
    return {
        "status": "ok",
        "version": "0.1.0",
        "solana_rpc": SOLANA_RPC,
        "program_id": PROGRAM_ID,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


# ─── Campaign Endpoints ──────────────────────────────────────────────────────


@app.post("/api/v1/campaigns", response_model=CampaignResponse, status_code=201)
async def create_campaign(body: CampaignCreate):
    """Create a new reward campaign. Triggers on-chain escrow creation."""

    if body.expiry_ts <= int(time.time()):
        raise HTTPException(400, "Expiry must be in the future")

    campaign_id = str(uuid.uuid4())
    now = datetime.now(timezone.utc).isoformat()

    # TODO: Submit create_campaign instruction to Solana program
    # For now, store locally and return
    on_chain_address = None  # Will be set after on-chain tx confirms

    campaign = {
        "id": campaign_id,
        "name": body.name,
        "description": body.description,
        "merchant_wallet": body.merchant_wallet,
        "reward_per_claim_lamports": body.reward_per_claim_lamports,
        "max_claims": body.max_claims,
        "claims_count": 0,
        "expiry_ts": body.expiry_ts,
        "status": CampaignStatus.ACTIVE,
        "nfc_tag_pubkey": body.nfc_tag_pubkey,
        "latitude": body.latitude,
        "longitude": body.longitude,
        "radius_meters": body.radius_meters,
        "metadata_uri": body.metadata_uri,
        "created_at": now,
        "on_chain_address": on_chain_address,
    }

    campaigns_db[campaign_id] = campaign
    return CampaignResponse(**campaign)


@app.get("/api/v1/campaigns", response_model=list[CampaignResponse])
async def list_campaigns(
    status: Optional[CampaignStatus] = Query(None),
    merchant: Optional[str] = Query(None),
    lat: Optional[float] = Query(None, ge=-90, le=90),
    lon: Optional[float] = Query(None, ge=-180, le=180),
    radius_km: Optional[float] = Query(None, gt=0, le=100),
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
):
    """List campaigns with optional filtering by status, merchant, and location."""
    results = list(campaigns_db.values())

    if status:
        results = [c for c in results if c["status"] == status]

    if merchant:
        results = [c for c in results if c["merchant_wallet"] == merchant]

    # TODO: geospatial filtering with Haversine formula
    if lat is not None and lon is not None and radius_km is not None:
        pass  # Placeholder for geo-query

    total = len(results)
    results = results[offset : offset + limit]

    return [CampaignResponse(**c) for c in results]


@app.get("/api/v1/campaigns/{campaign_id}", response_model=CampaignResponse)
async def get_campaign(campaign_id: str):
    """Get a single campaign by ID."""
    campaign = campaigns_db.get(campaign_id)
    if not campaign:
        raise HTTPException(404, "Campaign not found")
    return CampaignResponse(**campaign)


@app.delete("/api/v1/campaigns/{campaign_id}", status_code=204)
async def close_campaign(campaign_id: str, merchant_wallet: str = Query(...)):
    """Close a campaign and return remaining escrow to merchant."""
    campaign = campaigns_db.get(campaign_id)
    if not campaign:
        raise HTTPException(404, "Campaign not found")
    if campaign["merchant_wallet"] != merchant_wallet:
        raise HTTPException(403, "Not the campaign owner")

    # TODO: Submit close_campaign instruction to Solana program
    campaign["status"] = CampaignStatus.CLOSED
    return None


# ─── Claim Endpoints ─────────────────────────────────────────────────────────


@app.post("/api/v1/claims", response_model=ClaimResponse)
async def submit_claim(body: ClaimRequest):
    """Submit a reward claim with NFC proof-of-visit."""

    campaign = campaigns_db.get(body.campaign_id)
    if not campaign:
        raise HTTPException(404, "Campaign not found")

    if campaign["status"] != CampaignStatus.ACTIVE:
        raise HTTPException(410, "Campaign is no longer active")

    if campaign["claims_count"] >= campaign["max_claims"]:
        raise HTTPException(410, "All rewards have been claimed")

    if int(time.time()) >= campaign["expiry_ts"]:
        campaign["status"] = CampaignStatus.EXPIRED
        raise HTTPException(410, "Campaign has expired")

    # Check for duplicate claims
    for claim in claims_db:
        if (claim["campaign_id"] == body.campaign_id and
            claim["wallet_pubkey"] == body.wallet_pubkey):
            raise HTTPException(409, "Already claimed this reward")

    # TODO: Verify NFC signature on-chain via Ed25519 precompile
    # TODO: Submit claim_reward instruction to Solana program

    # Mock transaction signature (replace with actual Solana tx)
    tx_sig = f"sim_{uuid.uuid4().hex[:56]}"
    now = datetime.now(timezone.utc).isoformat()
    reward_sol = campaign["reward_per_claim_lamports"] / 1_000_000_000

    claim = {
        "campaign_id": body.campaign_id,
        "wallet_pubkey": body.wallet_pubkey,
        "tx_signature": tx_sig,
        "reward_lamports": campaign["reward_per_claim_lamports"],
        "claimed_at": now,
    }
    claims_db.append(claim)
    campaign["claims_count"] += 1

    # Auto-close if max claims reached
    if campaign["claims_count"] >= campaign["max_claims"]:
        campaign["status"] = CampaignStatus.CLOSED

    return ClaimResponse(
        tx_signature=tx_sig,
        reward_amount=reward_sol,
        campaign_name=campaign["name"],
        claimed_at=now,
    )


# ─── Merchant Endpoints ──────────────────────────────────────────────────────


@app.post("/api/v1/merchants", response_model=MerchantResponse, status_code=201)
async def register_merchant(body: MerchantCreate):
    """Register a new merchant."""
    merchant_id = str(uuid.uuid4())
    now = datetime.now(timezone.utc).isoformat()

    merchant = {
        "id": merchant_id,
        "name": body.name,
        "wallet_address": body.wallet_address,
        "business_type": body.business_type,
        "website": body.website,
        "contact_email": body.contact_email,
        "total_campaigns": 0,
        "total_rewards_distributed_lamports": 0,
        "created_at": now,
    }

    merchants_db[merchant_id] = merchant
    return MerchantResponse(**merchant)


@app.get("/api/v1/merchants", response_model=list[MerchantResponse])
async def list_merchants(
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
):
    """List registered merchants."""
    results = list(merchants_db.values())
    return [MerchantResponse(**m) for m in results[offset : offset + limit]]


@app.get("/api/v1/merchants/{merchant_id}", response_model=MerchantResponse)
async def get_merchant(merchant_id: str):
    """Get merchant details."""
    merchant = merchants_db.get(merchant_id)
    if not merchant:
        raise HTTPException(404, "Merchant not found")
    return MerchantResponse(**merchant)


# ─── Analytics Endpoints ──────────────────────────────────────────────────────


@app.get("/api/v1/analytics", response_model=AnalyticsResponse)
async def get_analytics(merchant_wallet: Optional[str] = Query(None)):
    """Get platform or merchant-specific analytics."""

    campaigns = list(campaigns_db.values())
    claims = claims_db

    if merchant_wallet:
        campaigns = [c for c in campaigns if c["merchant_wallet"] == merchant_wallet]
        campaign_ids = {c["id"] for c in campaigns}
        claims = [cl for cl in claims if cl["campaign_id"] in campaign_ids]

    now = int(time.time())
    claims_24h = [cl for cl in claims if now - _parse_ts(cl["claimed_at"]) < 86400]
    claims_7d = [cl for cl in claims if now - _parse_ts(cl["claimed_at"]) < 604800]

    active = [c for c in campaigns if c["status"] == CampaignStatus.ACTIVE]
    total_distributed = sum(cl.get("reward_lamports", 0) for cl in claims)
    unique_claimers = len({cl["wallet_pubkey"] for cl in claims})

    # Top campaigns by claim count
    campaign_claims: dict[str, int] = {}
    for cl in claims:
        cid = cl["campaign_id"]
        campaign_claims[cid] = campaign_claims.get(cid, 0) + 1

    top = sorted(campaign_claims.items(), key=lambda x: x[1], reverse=True)[:5]
    top_campaigns = []
    for cid, count in top:
        c = campaigns_db.get(cid, {})
        top_campaigns.append({
            "campaign_id": cid,
            "name": c.get("name", "Unknown"),
            "claims": count,
        })

    return AnalyticsResponse(
        total_campaigns=len(campaigns),
        active_campaigns=len(active),
        total_claims=len(claims),
        total_rewards_distributed_lamports=total_distributed,
        unique_claimers=unique_claimers,
        claims_last_24h=len(claims_24h),
        claims_last_7d=len(claims_7d),
        top_campaigns=top_campaigns,
    )


@app.get("/api/v1/analytics/campaign/{campaign_id}")
async def get_campaign_analytics(campaign_id: str):
    """Get detailed analytics for a specific campaign."""
    campaign = campaigns_db.get(campaign_id)
    if not campaign:
        raise HTTPException(404, "Campaign not found")

    campaign_claims = [cl for cl in claims_db if cl["campaign_id"] == campaign_id]
    unique = len({cl["wallet_pubkey"] for cl in campaign_claims})
    distributed = sum(cl.get("reward_lamports", 0) for cl in campaign_claims)

    return {
        "campaign_id": campaign_id,
        "campaign_name": campaign["name"],
        "total_claims": len(campaign_claims),
        "unique_claimers": unique,
        "rewards_distributed_lamports": distributed,
        "rewards_remaining_lamports": (
            campaign["reward_per_claim_lamports"]
            * (campaign["max_claims"] - campaign["claims_count"])
        ),
        "claim_rate": (
            campaign["claims_count"] / campaign["max_claims"]
            if campaign["max_claims"] > 0 else 0
        ),
        "status": campaign["status"],
    }


def _parse_ts(iso_str: str) -> int:
    """Parse ISO timestamp to unix timestamp."""
    try:
        dt = datetime.fromisoformat(iso_str.replace("Z", "+00:00"))
        return int(dt.timestamp())
    except Exception:
        return 0


# ─── Entry Point ──────────────────────────────────────────────────────────────

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
