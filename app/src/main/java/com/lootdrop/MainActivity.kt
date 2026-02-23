package com.lootdrop

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var cameraPermissionGranted = mutableStateOf(false)
    private lateinit var qrVerifier: QrVerifier

    private val _qrState = mutableStateOf<QrScanState>(QrScanState.Idle)

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted.value = granted
        if (granted) {
            startQrScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        qrVerifier = QrVerifier(this)
        cameraPermissionGranted.value = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        setContent {
            LootDropTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LootDropScreen(
                        qrState = _qrState.value,
                        qrAvailable = cameraPermissionGranted.value,
                        onScanRequest = { requestCameraAndScan() }
                    )
                }
            }
        }
    }

    private fun requestCameraAndScan() {
        if (cameraPermissionGranted.value) {
            startQrScan()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startQrScan() {
        _qrState.value = QrScanState.WaitingForScan
        // TODO: Launch CameraX + ML Kit barcode scanner
        // The scanner will call processQrCode(payload) when a QR code is detected
    }

    private fun processQrCode(payload: String) {
        _qrState.value = QrScanState.Scanning

        qrVerifier.verifyQrCode(payload) { result ->
            runOnUiThread {
                _qrState.value = when (result) {
                    is QrVerifyResult.Success -> QrScanState.Verified(
                        campaignId = result.campaignId,
                        rewardAmount = result.rewardAmount,
                        txSignature = result.txSignature
                    )
                    is QrVerifyResult.InvalidTag -> QrScanState.Error("Invalid LootDrop QR code")
                    is QrVerifyResult.AlreadyClaimed -> QrScanState.Error("Already claimed this reward")
                    is QrVerifyResult.CampaignExpired -> QrScanState.Error("Campaign has expired")
                    is QrVerifyResult.NetworkError -> QrScanState.Error("Network error: ${result.message}")
                }
            }
        }
    }
}

// ─── State ───────────────────────────────────────────────────────────────────

sealed class QrScanState {
    data object Idle : QrScanState()
    data object WaitingForScan : QrScanState()
    data object Scanning : QrScanState()
    data class Verified(
        val campaignId: String,
        val rewardAmount: Double,
        val txSignature: String
    ) : QrScanState()
    data class Error(val message: String) : QrScanState()
}

sealed class QrVerifyResult {
    data class Success(
        val campaignId: String,
        val rewardAmount: Double,
        val txSignature: String
    ) : QrVerifyResult()
    data object InvalidTag : QrVerifyResult()
    data object AlreadyClaimed : QrVerifyResult()
    data object CampaignExpired : QrVerifyResult()
    data class NetworkError(val message: String) : QrVerifyResult()
}

// ─── Composables ─────────────────────────────────────────────────────────────

@Composable
fun LootDropScreen(
    qrState: QrScanState,
    qrAvailable: Boolean,
    onScanRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LootDrop",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Scan. Earn. Explore.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        when (qrState) {
            is QrScanState.Idle -> {
                Button(
                    onClick = onScanRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Scan QR Code", fontSize = 18.sp)
                }
            }

            is QrScanState.WaitingForScan -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Point your camera at the LootDrop QR code...")
            }

            is QrScanState.Scanning -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Verifying & claiming reward...")
            }

            is QrScanState.Verified -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Reward Claimed!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Amount: ${qrState.rewardAmount} SOL")
                        Text("Campaign: ${qrState.campaignId}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TX: ${qrState.txSignature.take(16)}...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onScanRequest) {
                    Text("Scan Another")
                }
            }

            is QrScanState.Error -> {
                Text(
                    text = qrState.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onScanRequest) {
                    Text("Try Again")
                }
            }
        }
    }
}

// ─── Theme (minimal) ────────────────────────────────────────────────────────

@Composable
fun LootDropTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}
