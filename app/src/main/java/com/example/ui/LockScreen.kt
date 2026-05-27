package com.example.ui

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.JournalViewModel
import kotlinx.coroutines.delay

@Composable
fun LockScreen(viewModel: JournalViewModel) {
    val context = LocalContext.current
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var inCooldown by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (isBiometricEnabled && context is FragmentActivity) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(context, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.unlockApp()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        errorMessage = "Empreinte non reconnue."
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Déverrouiller Clarté")
                .setSubtitle("Confirme ton identité pour accéder à ton journal.")
                .setNegativeButtonText("Utiliser le code")
                .build()

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                // Ignore, fallback to PIN is visible
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Clarté",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ton journal est verrouillé.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { 
                    pin = it
                    errorMessage = null
                },
                label = { Text("Code PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !inCooldown
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (pin.isNotEmpty()) {
                        if (viewModel.verifyPin(pin)) {
                            // unlock success
                            errorMessage = null
                        } else {
                            attempts++
                            if (attempts >= 5) {
                                errorMessage = "Trop de tentatives. Réessaie dans quelques instants."
                                inCooldown = true
                            } else {
                                errorMessage = "Code incorrect."
                            }
                            pin = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !inCooldown
            ) {
                Text("Déverrouiller")
            }

            if (isBiometricEnabled && context is FragmentActivity) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        val executor = ContextCompat.getMainExecutor(context)
                        val biometricPrompt = BiometricPrompt(context, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    viewModel.unlockApp()
                                }
                            })

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Déverrouiller Clarté")
                            .setSubtitle("Confirme ton identité pour accéder à ton journal.")
                            .setNegativeButtonText("Utiliser le code")
                            .build()
                        try {
                            biometricPrompt.authenticate(promptInfo)
                        } catch (e: Exception) {}
                    }
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Utiliser la biométrie")
                }
            }
        }
    }

    if (inCooldown) {
        LaunchedEffect(Unit) {
            delay(30000) // 30s cooldown
            inCooldown = false
            attempts = 0
            errorMessage = null
        }
    }
}
