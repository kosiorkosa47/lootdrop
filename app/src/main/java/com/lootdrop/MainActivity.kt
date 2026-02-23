package com.lootdrop

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcVerifier: NfcVerifier

    private val _nfcState = mutableStateOf<NfcScanState>(NfcScanState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcVerifier = NfcVerifier(this)

        setContent {
            LootDropTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LootDropScreen(
                        nfcState = _nfcState.value,
                        nfcAvailable = nfcAdapter != null,
                        onScanRequest = { startNfcScan() }
                    )
                }
            }
        }

        // Handle NFC intent if app was launched from a tag tap
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun enableNfcForegroundDispatch() {
        val adapter = nfcAdapter ?: return

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                addDataScheme("lootdrop")
            },
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )

        adapter.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED -> {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                if (tag != null) {
                    processNfcTag(tag)
                }
            }
        }
    }

    private fun processNfcTag(tag: Tag) {
        _nfcState.value = NfcScanState.Scanning

        nfcVerifier.verifyTag(tag) { result ->
            runOnUiThread {
                _nfcState.value = when (result) {
                    is NfcVerifyResult.Success -> NfcScanState.Verified(
                        campaignId = result.campaignId,
                        rewardAmount = result.rewardAmount,
                        txSignature = result.txSignature
                    )
                    is NfcVerifyResult.InvalidTag -> NfcScanState.Error("Invalid LootDrop tag")
                    is NfcVerifyResult.AlreadyClaimed -> NfcScanState.Error("Already claimed this reward")
                    is NfcVerifyResult.CampaignExpired -> NfcScanState.Error("Campaign has expired")
                    is NfcVerifyResult.NetworkError -> NfcScanState.Error("Network error: ${result.message}")
                }
            }
        }
    }

    private fun startNfcScan() {
        _nfcState.value = NfcScanState.WaitingForTap
    }
}

// ─── State ───────────────────────────────────────────────────────────────────

sealed class NfcScanState {
    data object Idle : NfcScanState()
    data object WaitingForTap : NfcScanState()
    data object Scanning : NfcScanState()
    data class Verified(
        val campaignId: String,
        val rewardAmount: Double,
        val txSignature: String
    ) : NfcScanState()
    data class Error(val message: String) : NfcScanState()
}

sealed class NfcVerifyResult {
    data class Success(
        val campaignId: String,
        val rewardAmount: Double,
        val txSignature: String
    ) : NfcVerifyResult()
    data object InvalidTag : NfcVerifyResult()
    data object AlreadyClaimed : NfcVerifyResult()
    data object CampaignExpired : NfcVerifyResult()
    data class NetworkError(val message: String) : NfcVerifyResult()
}

// ─── Composables ─────────────────────────────────────────────────────────────

@Composable
fun LootDropScreen(
    nfcState: NfcScanState,
    nfcAvailable: Boolean,
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
            text = "Tap. Earn. Explore.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        when (nfcState) {
            is NfcScanState.Idle -> {
                if (nfcAvailable) {
                    Button(
                        onClick = onScanRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Scan NFC Tag", fontSize = 18.sp)
                    }
                } else {
                    Text(
                        text = "NFC is not available on this device",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is NfcScanState.WaitingForTap -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hold your phone near the LootDrop tag...")
            }

            is NfcScanState.Scanning -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Verifying & claiming reward...")
            }

            is NfcScanState.Verified -> {
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
                        Text("Amount: ${nfcState.rewardAmount} SOL")
                        Text("Campaign: ${nfcState.campaignId}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TX: ${nfcState.txSignature.take(16)}...",
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

            is NfcScanState.Error -> {
                Text(
                    text = nfcState.message,
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
