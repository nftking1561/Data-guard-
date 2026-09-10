package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BudgetMode
import com.example.data.model.DataPlan

@Composable
fun EditPlanDialog(
    currentPlan: DataPlan?,
    onSave: (DataPlan) -> Unit,
    onDismiss: () -> Unit
) {
    var carrier by remember { mutableStateOf(currentPlan?.networkCarrier ?: "MTN") }
    var planName by remember { mutableStateOf(currentPlan?.planName ?: "Monthly 20GB") }
    var sizeGb by remember {
        mutableStateOf(
            if (currentPlan != null) (currentPlan.planSizeBytes / (1024.0 * 1024.0 * 1024.0)).toString()
            else "20"
        )
    }
    var costNgn by remember { mutableStateOf(currentPlan?.priceNgn?.toInt()?.toString() ?: "5000") }
    var durationDays by remember { mutableStateOf("30") }
    var budgetMode by remember { mutableStateOf(currentPlan?.budgetMode ?: BudgetMode.SMART) }

    val carriers = listOf("MTN", "Airtel", "Glo", "9mobile", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_plan_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (currentPlan == null) "Set Up Data Plan" else "Edit Data Plan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Carrier Selection
                Text(
                    text = "Carrier:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    carriers.forEach { c ->
                        val isSelected = carrier == c
                        OutlinedButton(
                            onClick = { carrier = c },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            colors = if (isSelected) androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                text = c,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Plan Name
                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Plan Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_name_input")
                )

                // Size in GB
                OutlinedTextField(
                    value = sizeGb,
                    onValueChange = { sizeGb = it },
                    label = { Text("Plan Size (GB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_size_gb_input")
                )

                // Price in NGN
                OutlinedTextField(
                    value = costNgn,
                    onValueChange = { costNgn = it },
                    label = { Text("Price (₦)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_cost_ngn_input")
                )

                // Duration
                OutlinedTextField(
                    value = durationDays,
                    onValueChange = { durationDays = it },
                    label = { Text("Duration (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Budget Mode
                Text(
                    text = "Budget Mode:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = budgetMode == BudgetMode.SMART,
                        onClick = { budgetMode = BudgetMode.SMART }
                    )
                    Text(
                        text = "Smart (Adaptive daily targets)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable { budgetMode = BudgetMode.SMART }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = budgetMode == BudgetMode.FIXED,
                        onClick = { budgetMode = BudgetMode.FIXED }
                    )
                    Text(
                        text = "Fixed Daily (Equal daily allocation)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable { budgetMode = BudgetMode.FIXED }
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val sizeDouble = sizeGb.toDoubleOrNull() ?: 20.0
                            val costDouble = costNgn.toDoubleOrNull() ?: 5000.0
                            val daysInt = durationDays.toIntOrNull() ?: 30
                            val now = System.currentTimeMillis()

                            val updated = DataPlan(
                                id = currentPlan?.id ?: 0,
                                networkCarrier = carrier,
                                planName = planName.ifBlank { "$carrier ${sizeDouble.toInt()}GB" },
                                planSizeBytes = (sizeDouble * 1024 * 1024 * 1024).toLong(),
                                priceNgn = costDouble,
                                startDateEpochMs = currentPlan?.startDateEpochMs ?: now,
                                expiryDateEpochMs = (currentPlan?.startDateEpochMs ?: now) + (daysInt.toLong() * 24 * 60 * 60 * 1000),
                                budgetMode = budgetMode
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_plan_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Plan")
                    }
                }
            }
        }
    }
}
