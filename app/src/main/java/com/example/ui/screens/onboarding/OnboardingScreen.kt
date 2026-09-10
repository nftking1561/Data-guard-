package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetMode
import com.example.data.model.DataPlan

@Composable
fun OnboardingScreen(
    onComplete: (carrier: String, plan: DataPlan?) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedCarrier by remember { mutableStateOf("MTN") }

    // Plan input state
    var planSizeGb by remember { mutableStateOf("20") }
    var planCostNgn by remember { mutableStateOf("5000") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                (1..6).forEach { i ->
                    val isActive = i <= step
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (i == step) 28.dp else 12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Content
            AnimatedContent(targetState = step, label = "OnboardingStep") { currentStep ->
                when (currentStep) {
                    1 -> OnboardingSlide(
                        icon = Icons.Default.Security,
                        title = "Meet DATA GUARD",
                        description = "Your phone uses mobile data all day.\n\nDATA GUARD helps you understand where it goes, detect unusual usage, and make your data last longer.",
                        buttonText = "Get Started",
                        onNext = { step = 2 }
                    )
                    2 -> OnboardingSlide(
                        icon = Icons.Default.Sensors,
                        title = "See where your data goes",
                        description = "Understand the exact data consumption of your apps.\n\nSee how much each app uses, distinguish Wi-Fi from mobile data, and check background activity.",
                        buttonText = "Continue",
                        onNext = { step = 3 }
                    )
                    3 -> OnboardingSlide(
                        icon = Icons.Default.NotificationsActive,
                        title = "Protect your data",
                        description = "Never get caught off guard by rapid data drain.\n\nWe alert you when usage suddenly becomes unusually high or when your data runway is in danger.",
                        buttonText = "Continue",
                        onNext = { step = 4 }
                    )
                    4 -> OnboardingSlide(
                        icon = Icons.Default.Lock,
                        title = "Your privacy matters",
                        description = "DATA GUARD is built local-first.\n\nYour usage stats stay on your phone. No tracking servers, no account required, and no selling your personal data.",
                        buttonText = "Continue",
                        onNext = { step = 5 }
                    )
                    5 -> NetworkSelectionSlide(
                        selectedCarrier = selectedCarrier,
                        onCarrierSelected = { selectedCarrier = it },
                        onNext = { step = 6 },
                        onSkip = {
                            selectedCarrier = "MTN"
                            step = 6
                        }
                    )
                    6 -> PlanSetupSlide(
                        carrier = selectedCarrier,
                        planSizeGb = planSizeGb,
                        onPlanSizeChange = { planSizeGb = it },
                        costNgn = planCostNgn,
                        onCostChange = { planCostNgn = it },
                        onFinishWithPlan = {
                            val sizeGb = planSizeGb.toDoubleOrNull() ?: 20.0
                            val cost = planCostNgn.toDoubleOrNull() ?: 5000.0
                            val now = System.currentTimeMillis()
                            val plan = DataPlan(
                                networkCarrier = selectedCarrier,
                                planName = "$selectedCarrier ${sizeGb.toInt()} GB",
                                planSizeBytes = (sizeGb * 1024 * 1024 * 1024).toLong(),
                                priceNgn = cost,
                                startDateEpochMs = now - (2L * 24 * 60 * 60 * 1000),
                                expiryDateEpochMs = now + (28L * 24 * 60 * 60 * 1000),
                                budgetMode = BudgetMode.SMART
                            )
                            onComplete(selectedCarrier, plan)
                        },
                        onFinishWithoutPlan = {
                            onComplete(selectedCarrier, null)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingSlide(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_next_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NetworkSelectionSlide(
    selectedCarrier: String,
    onCarrierSelected: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val carriers = listOf("MTN", "Airtel", "Glo", "9mobile", "Other")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What network do you use?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select your primary mobile data provider.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        carriers.forEach { carrier ->
            val isSelected = selectedCarrier == carrier
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onCarrierSelected(carrier) }
                    .testTag("carrier_option_$carrier"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = carrier,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("carrier_continue_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.testTag("carrier_skip_button")
        ) {
            Text(
                text = "Skip for now",
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun PlanSetupSlide(
    carrier: String,
    planSizeGb: String,
    onPlanSizeChange: (String) -> Unit,
    costNgn: String,
    onCostChange: (String) -> Unit,
    onFinishWithPlan: () -> Unit,
    onFinishWithoutPlan: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What's your data plan?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tell us about your $carrier package so we can compute your Data Runway and daily budgets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = planSizeGb,
            onValueChange = onPlanSizeChange,
            label = { Text("Plan Size (GB)") },
            placeholder = { Text("e.g. 20") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("plan_size_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = costNgn,
            onValueChange = onCostChange,
            label = { Text("Plan Cost (₦)") },
            placeholder = { Text("e.g. 5000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("plan_cost_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinishWithPlan,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("finish_onboarding_plan_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Save Plan & Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onFinishWithoutPlan,
            modifier = Modifier.testTag("finish_onboarding_no_plan_button")
        ) {
            Text(
                text = "I don't know / I'll add it later",
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
