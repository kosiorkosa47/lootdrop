package com.lootdrop

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * NfcVerifier handles reading LootDrop NFC tags, verifying their
 * cryptographic signatures, and submitting claim transactions to the
 * Solana network via the LootDrop backend.
 *
 * LootDrop NFC tags contain an NDEF message with:
 *   Record 0: URI "lootdrop://claim/{campaign_id}"
 *   Record 1: External type "lootdrop.xyz:sig" — Ed25519 signature
 *   Record 2: External type "lootdrop.xyz:pk"  — Tag public key (32 bytes)
 */
class NfcVerifier(private val context: Context) {

    companion object {
        private const val TAG = "NfcVerifier"
        private const val LOOTDROP_SCHEME = "lootdrop"
        private const val CLAIM_PATH_PREFIX = "//claim/"
        private const val BACKEND_URL = BuildConfig.BACKEND_URL
    }

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Reads and verifies an NFC tag, then submits a claim to the backend.
     */
    fun verifyTag(tag: Tag, callback: (NfcVerifyResult) -> Unit) {
        scope.launch {
            try {
                val result = processTag(tag)
                callback(result)
            } catch (e: Exception) {
                Log.e(TAG, "Tag processing failed", e)
                callback(NfcVerifyResult.NetworkError(e.message ?: "Unknown error"))
            }
        }
    }

    private suspend fun processTag(tag: Tag): NfcVerifyResult {
        // Step 1: Read NDEF data from tag
        val tagData = readNdefTag(tag) ?: return NfcVerifyResult.InvalidTag

        Log.d(TAG, "Read tag: campaignId=${tagData.campaignId}")

        // Step 2: Verify the tag's Ed25519 signature
        if (!verifyTagSignature(tagData)) {
            Log.w(TAG, "Tag signature verification failed")
            return NfcVerifyResult.InvalidTag
        }

        // Step 3: Build proof-of-visit message
        // Format: [wallet_pubkey (32) | timestamp (8) | campaign_id (8)]
        val walletPubkey = getWalletPublicKey() ?: return NfcVerifyResult.NetworkError("Wallet not connected")
        val timestamp = System.currentTimeMillis() / 1000
        val proofMessage = buildProofMessage(walletPubkey, timestamp, tagData.campaignId)

        // Step 4: Sign the proof with the NFC tag's key (via challenge-response)
        // In production, the NFC tag would sign the proof message
        val nfcSignature = signWithTag(tag, proofMessage) ?: return NfcVerifyResult.InvalidTag

        // Step 5: Submit claim to backend → on-chain transaction
        return submitClaim(
            campaignId = tagData.campaignId,
            walletPubkey = walletPubkey,
            nfcSignature = nfcSignature,
            proofMessage = proofMessage,
            tagPubkey = tagData.publicKey
        )
    }

    /**
     * Reads NDEF records from the NFC tag.
     */
    private fun readNdefTag(tag: Tag): LootDropTagData? {
        val ndef = Ndef.get(tag) ?: return null

        return try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage ?: return null

            var campaignId: String? = null
            var signature: ByteArray? = null
            var publicKey: ByteArray? = null

            for (record in ndefMessage.records) {
                when {
                    // URI record: lootdrop://claim/{campaign_id}
                    record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    record.type.contentEquals(NdefRecord.RTD_URI) -> {
                        val uri = record.toUri()?.toString() ?: continue
                        if (uri.startsWith("$LOOTDROP_SCHEME:$CLAIM_PATH_PREFIX")) {
                            campaignId = uri.substringAfter(CLAIM_PATH_PREFIX)
                        }
                    }

                    // External type: signature
                    record.tnf == NdefRecord.TNF_EXTERNAL_TYPE &&
                    String(record.type) == "lootdrop.xyz:sig" -> {
                        signature = record.payload
                    }

                    // External type: public key
                    record.tnf == NdefRecord.TNF_EXTERNAL_TYPE &&
                    String(record.type) == "lootdrop.xyz:pk" -> {
                        publicKey = record.payload
                    }
                }
            }

            if (campaignId != null && signature != null && publicKey != null) {
                LootDropTagData(campaignId, signature, publicKey)
            } else {
                Log.w(TAG, "Incomplete LootDrop NDEF data")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read NDEF", e)
            null
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    /**
     * Verifies the tag's Ed25519 signature over the campaign ID.
     */
    private fun verifyTagSignature(tagData: LootDropTagData): Boolean {
        return try {
            val pubKeyParams = Ed25519PublicKeyParameters(tagData.publicKey, 0)
            val verifier = Ed25519Signer()
            verifier.init(false, pubKeyParams)
            verifier.update(tagData.campaignId.toByteArray(), 0, tagData.campaignId.length)
            verifier.verifySignature(tagData.signature)
        } catch (e: Exception) {
            Log.e(TAG, "Signature verification error", e)
            false
        }
    }

    /**
     * Builds a 48-byte proof-of-visit message.
     */
    private fun buildProofMessage(
        walletPubkey: ByteArray,
        timestamp: Long,
        campaignId: String
    ): ByteArray {
        val buffer = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(walletPubkey) // 32 bytes
        buffer.putLong(timestamp) // 8 bytes
        buffer.putLong(campaignId.toLongOrNull() ?: 0L) // 8 bytes
        return buffer.array()
    }

    /**
     * Signs a message using the NFC tag's secure element.
     * In production, this uses ISO 7816 APDU commands for challenge-response.
     */
    private fun signWithTag(tag: Tag, message: ByteArray): ByteArray? {
        // TODO: Implement ISO 7816 challenge-response with the NFC tag's
        // secure element. For now, return a placeholder for testing.
        //
        // The real flow:
        //   1. SELECT LootDrop applet (AID: F0 4C 4F 4F 54 44 52 4F 50)
        //   2. INTERNAL AUTHENTICATE with message hash
        //   3. Read 64-byte Ed25519 signature from response
        Log.d(TAG, "NFC challenge-response signing (stub)")
        return ByteArray(64) // Placeholder — secure element signing TODO
    }

    /**
     * Gets the connected Solana wallet's public key via Seed Vault / MWA.
     */
    private suspend fun getWalletPublicKey(): ByteArray? {
        // TODO: Integrate with Solana Mobile Wallet Adapter or Seed Vault
        // to get the authorized wallet public key.
        //
        // val result = walletAdapter.authorize(...)
        // return result.publicKey
        Log.d(TAG, "Wallet public key retrieval (stub)")
        return null // Placeholder — wallet integration TODO
    }

    /**
     * Submits the claim to the LootDrop backend for on-chain execution.
     */
    private suspend fun submitClaim(
        campaignId: String,
        walletPubkey: ByteArray,
        nfcSignature: ByteArray,
        proofMessage: ByteArray,
        tagPubkey: ByteArray
    ): NfcVerifyResult {
        return try {
            val response = httpClient.post("$BACKEND_URL/api/v1/claims") {
                contentType(ContentType.Application.Json)
                setBody(ClaimRequest(
                    campaign_id = campaignId,
                    wallet_pubkey = walletPubkey.toHexString(),
                    nfc_signature = nfcSignature.toHexString(),
                    proof_message = proofMessage.toHexString(),
                    tag_pubkey = tagPubkey.toHexString()
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val claimResponse = response.body<ClaimResponse>()
                    NfcVerifyResult.Success(
                        campaignId = campaignId,
                        rewardAmount = claimResponse.reward_amount,
                        txSignature = claimResponse.tx_signature
                    )
                }
                HttpStatusCode.Conflict -> NfcVerifyResult.AlreadyClaimed
                HttpStatusCode.Gone -> NfcVerifyResult.CampaignExpired
                else -> NfcVerifyResult.NetworkError("HTTP ${response.status.value}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Claim submission failed", e)
            NfcVerifyResult.NetworkError(e.message ?: "Network error")
        }
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }
}

// ─── Data Classes ────────────────────────────────────────────────────────────

data class LootDropTagData(
    val campaignId: String,
    val signature: ByteArray,
    val publicKey: ByteArray
)

@Serializable
data class ClaimRequest(
    val campaign_id: String,
    val wallet_pubkey: String,
    val nfc_signature: String,
    val proof_message: String,
    val tag_pubkey: String
)

@Serializable
data class ClaimResponse(
    val tx_signature: String,
    val reward_amount: Double,
    val campaign_name: String
)
