package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.util.DataFormatUtils
import kotlin.math.abs

@Composable
fun CarrierBalanceDialog(
    deviceRemainingBytes: Long,
    onSaveReconciliation: (carrierBytes: Long, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var carrierBalanceInput by remember { mutableStateOf("") }
    var isGb by remember { mutableStateOf(true) }

    val carrierBytes = remember(carrierBalanceInput, isGb) {
        val num = carrierBalanceInput.toDoubleOrNull() ?: 0.0
        val multiplier = if (isGb) 1024L * 1024 * 1024 else 1024L * 1024
        (num * multiplier).toLong()
    }

    val differenceBytes = if (carrierBalanceInput.isNotBlank()) {
        carrierBytes - deviceRemainingBytes
    } else null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("carrier_balance_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Carrier Balance Verification",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Enter the balance shown on your carrier's balance inquiry (*310# or SMS) to compare with your phone's tracker.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = carrierBalanceInput,
                        onValueChange = { carrierBalanceInput = it },
                        label = { Text("Carrier Balance") },
                        placeholder = { Text(if (isGb) "e.g. 14.5" else "e.g. 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("carrier_balance_input"),
                        singleLine = true
                    )

                    OutlinedButton(
                        onClick = { isGb = !isGb },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(if (isGb) "GB" else "MB")
                    }
                }

                if (differenceBytes != null && carrierBytes > 0) {
                    val diffFormatted = DataFormatUtils.formatBytes(abs(differenceBytes))
                    val isCarrierHigher = differenceBytes > 0

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Comparison Result",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Phone measured remaining: ${DataFormatUtils.formatBytes(deviceRemainingBytes)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Carrier reported balance: ${DataFormatUtils.formatBytes(carrierBytes)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = if (isCarrierHigher) "Carrier reports $diffFormatted MORE than your phone."
                                else "Carrier reports $diffFormatted LESS than your phone.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Honest explanation
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Why do counters differ?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• Billing increments: Carriers round data sessions into billing blocks.\n• Zero-rating: Free social bundles or promo apps aren't billed by carriers.\n• Latency: Carrier balance updates may lag active usage by 15-30 minutes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            if (carrierBytes > 0) {
                                onSaveReconciliation(carrierBytes, "Manual reconciliation")
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_reconciliation_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = carrierBytes > 0
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
