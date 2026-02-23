package com.lootdrop

import android.content.Context
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
 * QrVerifier handles reading LootDrop QR codes, verifying their
 * cryptographic signatures, and submitting claim transactions to the
 * Solana network via the LootDrop backend.
 *
 * LootDrop QR codes encode a JSON payload containing:
 *   - campaign_id: The campaign identifier
 *   - signature: Hex-encoded Ed25519 signature
 *   - public_key: Hex-encoded tag public key (32 bytes)
 *   - timestamp: Unix timestamp of QR code generation
 */
class QrVerifier(private val context: Context) {

    companion object {
        private const val TAG = "QrVerifier"
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
     * Reads and verifies a QR code payload, then submits a claim to the backend.
     */
    fun verifyQrCode(payload: String, callback: (QrVerifyResult) -> Unit) {
        scope.launch {
            try {
                val result = processQrPayload(payload)
                callback(result)
            } catch (e: Exception) {
                Log.e(TAG, "QR code processing failed", e)
                callback(QrVerifyResult.NetworkError(e.message ?: "Unknown error"))
            }
        }
    }

    private suspend fun processQrPayload(payload: String): QrVerifyResult {
        // Step 1: Parse the QR code payload
        val qrData = parseQrPayload(payload) ?: return QrVerifyResult.InvalidTag

        Log.d(TAG, "Parsed QR code: campaignId=${qrData.campaignId}")

        // Step 2: Verify the QR code's Ed25519 signature
        if (!verifyQrSignature(qrData)) {
            Log.w(TAG, "QR code signature verification failed")
            return QrVerifyResult.InvalidTag
        }

        // Step 3: Build proof-of-visit message
        // Format: [wallet_pubkey (32) | timestamp (8) | campaign_id (8)]
        val walletPubkey = getWalletPublicKey() ?: return QrVerifyResult.NetworkError("Wallet not connected")
        val timestamp = System.currentTimeMillis() / 1000
        val proofMessage = buildProofMessage(walletPubkey, timestamp, qrData.campaignId)

        // Step 4: Sign the proof with the device key
        val qrSignature = signProof(proofMessage) ?: return QrVerifyResult.InvalidTag

        // Step 5: Submit claim to backend -> on-chain transaction
        return submitClaim(
            campaignId = qrData.campaignId,
            walletPubkey = walletPubkey,
            qrSignature = qrSignature,
            proofMessage = proofMessage,
            qrPubkey = qrData.publicKey
        )
    }

    /**
     * Parses the QR code payload string into structured data.
     * Expected format: lootdrop://claim/{campaign_id}?sig={hex}&pk={hex}
     */
    private fun parseQrPayload(payload: String): LootDropQrData? {
        return try {
            val uri = android.net.Uri.parse(payload)

            if (uri.scheme != LOOTDROP_SCHEME) return null

            val pathSegments = uri.pathSegments
            if (pathSegments.size < 2 || pathSegments[0] != "claim") return null

            val campaignId = pathSegments[1]
            val sigHex = uri.getQueryParameter("sig") ?: return null
            val pkHex = uri.getQueryParameter("pk") ?: return null

            val signature = hexToBytes(sigHex)
            val publicKey = hexToBytes(pkHex)

            if (signature.size == 64 && publicKey.size == 32) {
                LootDropQrData(campaignId, signature, publicKey)
            } else {
                Log.w(TAG, "Invalid signature or public key length in QR code")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse QR payload", e)
            null
        }
    }

    /**
     * Verifies the QR code's Ed25519 signature over the campaign ID.
     */
    private fun verifyQrSignature(qrData: LootDropQrData): Boolean {
        return try {
            val pubKeyParams = Ed25519PublicKeyParameters(qrData.publicKey, 0)
            val verifier = Ed25519Signer()
            verifier.init(false, pubKeyParams)
            verifier.update(qrData.campaignId.toByteArray(), 0, qrData.campaignId.length)
            verifier.verifySignature(qrData.signature)
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
     * Signs a proof message using the device's key.
     * In production, this uses Solana Mobile Wallet Adapter for signing.
     */
    private fun signProof(message: ByteArray): ByteArray? {
        // TODO: Implement signing via Solana Mobile Wallet Adapter
        //
        // The real flow:
        //   1. Connect to wallet via MWA
        //   2. Request signature over the proof message
        //   3. Return 64-byte Ed25519 signature
        Log.d(TAG, "QR proof signing (stub)")
        return ByteArray(64) // Placeholder -- wallet signing TODO
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
        return null // Placeholder -- wallet integration TODO
    }

    /**
     * Submits the claim to the LootDrop backend for on-chain execution.
     */
    private suspend fun submitClaim(
        campaignId: String,
        walletPubkey: ByteArray,
        qrSignature: ByteArray,
        proofMessage: ByteArray,
        qrPubkey: ByteArray
    ): QrVerifyResult {
        return try {
            val response = httpClient.post("$BACKEND_URL/api/v1/claims") {
                contentType(ContentType.Application.Json)
                setBody(ClaimRequest(
                    campaign_id = campaignId,
                    wallet_pubkey = walletPubkey.toHexString(),
                    qr_signature = qrSignature.toHexString(),
                    proof_message = proofMessage.toHexString(),
                    qr_pubkey = qrPubkey.toHexString()
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val claimResponse = response.body<ClaimResponse>()
                    QrVerifyResult.Success(
                        campaignId = campaignId,
                        rewardAmount = claimResponse.reward_amount,
                        txSignature = claimResponse.tx_signature
                    )
                }
                HttpStatusCode.Conflict -> QrVerifyResult.AlreadyClaimed
                HttpStatusCode.Gone -> QrVerifyResult.CampaignExpired
                else -> QrVerifyResult.NetworkError("HTTP ${response.status.value}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Claim submission failed", e)
            QrVerifyResult.NetworkError(e.message ?: "Network error")
        }
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}

// ─── Data Classes ────────────────────────────────────────────────────────────

data class LootDropQrData(
    val campaignId: String,
    val signature: ByteArray,
    val publicKey: ByteArray
)

@Serializable
data class ClaimRequest(
    val campaign_id: String,
    val wallet_pubkey: String,
    val qr_signature: String,
    val proof_message: String,
    val qr_pubkey: String
)

@Serializable
data class ClaimResponse(
    val tx_signature: String,
    val reward_amount: Double,
    val campaign_name: String
)
