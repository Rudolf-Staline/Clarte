package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JournalEntry
import com.example.ui.navigation.BottomNavigationBar
import com.example.viewmodel.JournalViewModel

import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudSync
import kotlinx.coroutines.launch
import com.example.auth.AuthState
import com.example.auth.AuthViewModel
import com.example.sync.SyncManager
import com.example.sync.SyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: JournalViewModel,
    authViewModel: AuthViewModel,
    syncManager: SyncManager,
    onNavigateTab: (String) -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToPrivacyCenter: () -> Unit
) {
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isForceMockEnabled by viewModel.isForceMockEnabled.collectAsStateWithLifecycle()
    val hidePreviewsHistory by viewModel.hidePreviewsHistory.collectAsStateWithLifecycle()
    val hideLatestPreviewHome by viewModel.hideLatestPreviewHome.collectAsStateWithLifecycle()
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val syncState by syncManager.syncState.collectAsStateWithLifecycle()
    val currentUserEmail by authViewModel.currentUserEmail.collectAsStateWithLifecycle()
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showExportWarning by remember { mutableStateOf<(() -> Unit)?>(null) }
    val hideExportWarning by viewModel.hideExportWarning.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paramètres",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "settings",
                onNavigate = onNavigateTab
            )
        }
    ) { innerPadding ->
        if (showExportWarning != null) {
            val action = showExportWarning!!
            AlertDialog(
                onDismissRequest = { showExportWarning = null },
                title = { Text("Avertissement de confidentialité") },
                text = { Text("Ce fichier contiendra des données lisibles. Garde-le dans un endroit sûr.") },
                confirmButton = {
                    Button(onClick = {
                        showExportWarning = null
                        action()
                    }) {
                        Text("Exporter")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            viewModel.setHideExportWarning(true)
                            showExportWarning = null
                            action()
                        }) {
                            Text("Ne plus afficher", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { showExportWarning = null }) {
                            Text("Annuler")
                        }
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Introduction Description Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "À propos de Clarté",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Clarté est un journal privé conçu pour t'aider à mettre de l'ordre dans tes pensées.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Avertissement légal : Cette app t'accompagne à clarifier tes pensées, mais elle ne remplace en aucun cas le suivi par un professionnel de la santé mentale.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                        lineHeight = 18.sp
                    )
                }
            }

            // Account Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Compte",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Compte",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToAccount() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (authState is AuthState.Authenticated) {
                                Text("Connecté", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(currentUserEmail ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            } else {
                                Text("Se connecter / S'inscrire", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Pour sauvegarder dans le cloud", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
                
                val projectId = com.google.firebase.FirebaseOptions.fromResource(context)?.projectId
                if (projectId == "dummy-project") {
                    Text(
                        text = "Firebase n’est pas encore configuré. La sauvegarde cloud est indisponible.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Sync Section
            if (authState is AuthState.Authenticated) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Sauvegarde",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Sauvegarde cloud",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Tes données restent stockées localement sur cet appareil. Si tu actives la sauvegarde cloud, une copie sera enregistrée dans Firebase pour permettre la restauration.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            
                            val statusText = when (syncState) {
                                is SyncState.Idle -> "Sauvegarde désactivée / Prêt"
                                is SyncState.Syncing -> "Synchronisation en cours..."
                                is SyncState.Success -> "À jour (Dernière synchro à : ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.FRENCH).format(java.util.Date((syncState as SyncState.Success).syncTime))})"
                                is SyncState.Error -> "Erreur de synchronisation : ${(syncState as SyncState.Error).message}"
                            }

                            Text("Statut: $statusText", style = MaterialTheme.typography.labelLarge)

                            Button(
                                onClick = { coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) { syncManager.syncNow() } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Synchroniser maintenant")
                            }

                            var showRestoreConfirm by remember { mutableStateOf(false) }
                            var showRestorePassphraseDialog by remember { mutableStateOf(false) }
                            var passphraseInputError by remember { mutableStateOf<String?>(null) }
                            var restorePassphrase by remember { mutableStateOf("") }
                            
                            OutlinedButton(
                                onClick = { showRestoreConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Restaurer depuis le cloud")
                            }
                            
                            if (showRestoreConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showRestoreConfirm = false },
                                    title = { Text("Restaurer les données ?") },
                                    text = { Text("Les entrées manquantes seront téléchargées. Tes données locales récentes priment en cas de conflit.") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                showRestoreConfirm = false
                                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                    try {
                                                        val success = syncManager.restoreFromCloud()
                                                        if (!success) {
                                                            // Usually means PASSPHRASE_REQUIRED
                                                            showRestorePassphraseDialog = true
                                                        }
                                                    } catch (e: Exception) {
                                                        if (e.message == "INVALID_PASSPHRASE") {
                                                            passphraseInputError = "Phrase de récupération incorrecte. Les données n’ont pas été restaurées."
                                                            showRestorePassphraseDialog = true
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Restaurer")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showRestoreConfirm = false }) {
                                            Text("Annuler")
                                        }
                                    }
                                )
                            }
                            
                            if (showRestorePassphraseDialog) {
                                AlertDialog(
                                    onDismissRequest = { showRestorePassphraseDialog = false },
                                    title = { Text("Déchiffrer la sauvegarde") },
                                    text = { 
                                        Column {
                                            Text("Ta sauvegarde est chiffrée de bout en bout. Entre ta phrase de récupération pour y accéder.", style = MaterialTheme.typography.bodyMedium)
                                            Spacer(Modifier.height(12.dp))
                                            OutlinedTextField(
                                                value = restorePassphrase,
                                                onValueChange = { restorePassphrase = it; passphraseInputError = null },
                                                label = { Text("Phrase de récupération") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            if (passphraseInputError != null) {
                                                Text(passphraseInputError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                    try {
                                                        val success = syncManager.restoreFromCloud(restorePassphrase)
                                                        if (success) {
                                                            showRestorePassphraseDialog = false
                                                            restorePassphrase = ""
                                                        } else {
                                                            passphraseInputError = "Erreur de restauration."
                                                        }
                                                    } catch (e: Exception) {
                                                        if (e.message == "INVALID_PASSPHRASE") {
                                                            passphraseInputError = "Phrase de récupération incorrecte. Les données n’ont pas été restaurées."
                                                        } else {
                                                            passphraseInputError = "Erreur lors du déchiffrement."
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Déchiffrer")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showRestorePassphraseDialog = false }) {
                                            Text("Annuler")
                                        }
                                    }
                                )
                            }

                            // Chiffrée (Encrypted Backup UI)
                            var showEncryptionSetupDialog by remember { mutableStateOf(false) }
                            var showTestPassphraseDialog by remember { mutableStateOf(false) }
                            val isEncryptedBackupEnabled by viewModel.encryptedBackupEnabled.collectAsStateWithLifecycle()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text("Sauvegarde chiffrée", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    val desc = if (isEncryptedBackupEnabled) "Sauvegarde chiffrée active. Tes écrits sont protégés de bout en bout." else "La sauvegarde chiffrée protège tes écrits avant leur envoi dans le cloud. Attention, tes mots ne seront pas chiffrés sans l'activation."
                                    Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                }
                                Switch(
                                    checked = isEncryptedBackupEnabled,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            showEncryptionSetupDialog = true
                                        } else {
                                            viewModel.disableEncryptedBackup()
                                        }
                                    }
                                )
                            }
                            
                            if (isEncryptedBackupEnabled) {
                                TextButton(
                                    onClick = { showTestPassphraseDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Tester ma phrase de récupération")
                                }
                            }
                            
                            if (showTestPassphraseDialog) {
                                var testPassphrase by remember { mutableStateOf("") }
                                var testResult by remember { mutableStateOf<String?>(null) }
                                
                                AlertDialog(
                                    onDismissRequest = { showTestPassphraseDialog = false },
                                    title = { Text("Tester ma phrase") },
                                    text = { 
                                        Column {
                                            Text("Saisis ta phrase pour t'assurer que c'est la bonne.", style = MaterialTheme.typography.bodyMedium)
                                            Spacer(Modifier.height(12.dp))
                                            OutlinedTextField(
                                                value = testPassphrase,
                                                onValueChange = { testPassphrase = it; testResult = null },
                                                label = { Text("Phrase de récupération") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            if (testResult != null) {
                                                Text(
                                                    text = testResult!!, 
                                                    color = if (testResult == "Phrase vérifiée.") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, 
                                                    style = MaterialTheme.typography.bodySmall, 
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                    try {
                                                        // Test derivation
                                                        val success = syncManager.testPassphrase(testPassphrase)
                                                        testResult = if (success) "Phrase vérifiée." else "Phrase incorrecte."
                                                    } catch (e: Exception) {
                                                        testResult = "Erreur lors de la vérification."
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Vérifier")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showTestPassphraseDialog = false }) {
                                            Text("Fermer")
                                        }
                                    }
                                )
                            }
                            
                            if (showEncryptionSetupDialog) {
                                var passphrase by remember { mutableStateOf("") }
                                var confirmPassphrase by remember { mutableStateOf("") }
                                var errorMsg by remember { mutableStateOf("") }
                                var understandRisk by remember { mutableStateOf(false) }
                                
                                AlertDialog(
                                    onDismissRequest = { showEncryptionSetupDialog = false },
                                    title = { Text("Activer la sauvegarde chiffrée") },
                                    text = { 
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Crée une phrase de récupération pour chiffrer tes données. Si tu la perds, tes sauvegardes seront impossibles à restaurer sur un nouvel appareil.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            OutlinedTextField(
                                                value = passphrase,
                                                onValueChange = { passphrase = it; errorMsg = "" },
                                                label = { Text("Phrase de récupération") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = confirmPassphrase,
                                                onValueChange = { confirmPassphrase = it; errorMsg = "" },
                                                label = { Text("Confirmer la phrase") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = understandRisk, onCheckedChange = { understandRisk = it })
                                                Text("J’ai compris que Clarté ne pourra pas récupérer ma phrase si je la perds.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                                            }
                                            if (errorMsg.isNotEmpty()) {
                                                Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                if (passphrase.isBlank() || passphrase.length < 8) {
                                                    errorMsg = "La phrase doit contenir au moins 8 caractères."
                                                } else if (passphrase != confirmPassphrase) {
                                                    errorMsg = "Les phrases ne correspondent pas."
                                                } else {
                                                    viewModel.setEncryptedBackupEnabled(true, passphrase)
                                                    showEncryptionSetupDialog = false
                                                }
                                            },
                                            enabled = understandRisk
                                        ) {
                                            Text("Activer")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showEncryptionSetupDialog = false }) {
                                            Text("Annuler")
                                        }
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            var showDeleteCloudConfirm by remember { mutableStateOf(false) }
                            
                            TextButton(
                                onClick = { showDeleteCloudConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Supprimer la sauvegarde cloud")
                            }
                            
                            if (showDeleteCloudConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteCloudConfirm = false },
                                    title = { Text("Supprimer du cloud ?") },
                                    text = { Text("Toutes tes données sauvegardées dans le cloud seront effacées. Tes données locales ne seront pas supprimées. Cette action est irréversible.") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                showDeleteCloudConfirm = false
                                                val syncRepository = com.example.sync.FirebaseSyncRepository()
                                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) { syncRepository.deleteCloudData() }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Supprimer")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteCloudConfirm = false }) {
                                            Text("Annuler")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Theme Selection Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Thème",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Thème visuel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        val themeOptions = listOf(
                            "système" to "Système (Par défaut)",
                            "sombre" to "Sombre",
                            "clair" to "Clair"
                        )

                        themeOptions.forEach { (key, label) ->
                            val isSelected = currentTheme == key
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateTheme(key) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .testTag("theme_row_$key"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateTheme(key) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // AI Service Custom options
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "IA",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Génération d'analyse",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Forcer l'IA locale (Mock)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Désactive la connexion avec le serveur Gemini et simule des analyses d'introspection instantanées.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = isForceMockEnabled,
                                onCheckedChange = { viewModel.toggleForceMock(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("switch_mock_ai")
                            )
                        }
                    }
                }
            }

        // Privacy Options Panel
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = "Confidentialité",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Confidentialité & Masquage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onNavigateToPrivacyCenter,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text("Centre de Confidentialité")
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        // Toggle 1: Hide history preview text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Masquer les aperçus",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Masque le contenu des anciennes entrées sur l’écran de l'historique.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = hidePreviewsHistory,
                                onCheckedChange = { viewModel.setHidePreviewsHistory(it) },
                                modifier = Modifier.testTag("switch_hide_history_previews")
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        // Toggle 2: Hide home screen latest entry previews
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Masquer la dernière entrée sur l’accueil",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Une mesure de sécurité contre les yeux indiscrets quand tu ouvres l'application.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = hideLatestPreviewHome,
                                onCheckedChange = { viewModel.setHideLatestPreviewHome(it) },
                                modifier = Modifier.testTag("switch_hide_home_preview")
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        // Toggle 3: Hide export warning
                        val hideExportWarning by viewModel.hideExportWarning.collectAsStateWithLifecycle()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Masquer l'avertissement d'export",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ne plus afficher l'écran d'avertissement avant d'exporter des données.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = hideExportWarning,
                                onCheckedChange = { viewModel.setHideExportWarning(it) }
                            )
                        }
                    }
                }
            }

            // Verrouillage Privé
            val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
            val lockOnBackground by viewModel.lockOnBackground.collectAsStateWithLifecycle()
            val autoLockDelayMinutes by viewModel.autoLockDelayMinutes.collectAsStateWithLifecycle()

            var showPinSetup by remember { mutableStateOf(false) }

            if (showPinSetup) {
                var newPin by remember { mutableStateOf("") }
                var confirmPin by remember { mutableStateOf("") }
                var pinError by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { showPinSetup = false },
                    title = { Text("Créer un code de verrouillage") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Ce code protège l’accès local à ton journal sur cet appareil.", style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = newPin,
                                onValueChange = { newPin = it; pinError = null },
                                label = { Text("Nouveau code PIN") },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = confirmPin,
                                onValueChange = { confirmPin = it; pinError = null },
                                label = { Text("Confirmer le code PIN") },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (pinError != null) {
                                Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newPin.isEmpty() || newPin.length < 4) {
                                pinError = "Le code doit contenir au moins 4 chiffres."
                            } else if (newPin != confirmPin) {
                                pinError = "Les codes ne correspondent pas."
                            } else {
                                viewModel.setupPin(newPin)
                                showPinSetup = false
                            }
                        }) {
                            Text("Activer")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPinSetup = false }) { Text("Annuler") }
                    }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verrouillage",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Verrouillage privé",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Activer le verrouillage",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Protège l'accès à l'application avec un code PIN.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = isAppLockEnabled,
                                onCheckedChange = { 
                                    if (it) {
                                        showPinSetup = true 
                                    } else {
                                        viewModel.disableAppLock()
                                    }
                                }
                            )
                        }

                        if (isAppLockEnabled) {
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Utiliser la biométrie si disponible",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = isBiometricEnabled,
                                    onCheckedChange = { viewModel.setBiometricEnabled(it) }
                                )
                            }
                            
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Déverrouiller quand l'app passe en premier plan",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = lockOnBackground,
                                    onCheckedChange = { viewModel.setLockOnBackground(it) }
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            
                            Text(
                                text = "Verrouillage automatique après inactivité :",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            val delays = listOf(
                                0 to "Immédiatement",
                                1 to "1 minute",
                                5 to "5 minutes",
                                15 to "15 minutes"
                            )
                            delays.forEach { (minutes, label) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { viewModel.setAutoLockDelayMinutes(minutes) }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                                    RadioButton(
                                        selected = autoLockDelayMinutes == minutes,
                                        onClick = { viewModel.setAutoLockDelayMinutes(minutes) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Export Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Exportation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Données & Exportation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sauvegarder mon journal",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Exporte l’intégralité de tes réflexions et écrits sous forme de document structuré au format JSON réutilisable.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = { 
                                val act = { exportAllEntries(context, allEntries) }
                                if (hideExportWarning) act() else showExportWarning = act
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_export_all_json"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Exporter au format JSON (Sauvegarde complète)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // EXPORT FAVORITES
            val favorites = remember(allEntries) { allEntries.filter { it.isFavorite } }
            if (favorites.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Favoris",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Exporter tes ${favorites.size} entrées favorites.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            var showFavMenu by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { showFavMenu = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Exporter les favoris")
                                }
                                DropdownMenu(
                                    expanded = showFavMenu,
                                    onDismissRequest = { showFavMenu = false }
                                ) {
                                    val runExport = { action: () -> Unit ->
                                        showFavMenu = false
                                        if (hideExportWarning) action() else showExportWarning = action
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Au format PDF") },
                                        onClick = {
                                            runExport {
                                                val text = favorites.joinToString("\n\n---\n\n") { "Date: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRENCH).format(java.util.Date(it.createdAt))}\n${it.content}" }
                                                com.example.utils.ExportHelper.exportReviewAsPdf(context, "Favoris", text, "favoris", "clarte_favoris")
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Au format JSON") },
                                        onClick = {
                                            runExport { exportAllEntries(context, favorites) }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Au format Texte") },
                                        onClick = {
                                            runExport {
                                                val text = favorites.joinToString("\n\n---\n\n") { "Date: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRENCH).format(java.util.Date(it.createdAt))}\n${it.content}" }
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(android.content.Intent.EXTRA_TEXT, text)
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Partager les favoris"))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Clear Database Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showClearConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_clear_data"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Delete",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Effacer toutes les données",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Confirmation Alert Dialog
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Danger",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Te voilà prévenu",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Es-tu sûr de vouloir effacer définitivement toutes les entrées du journal ? Cette action efface la base de données locale.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmation = false
                    },
                    modifier = Modifier.testTag("dialog_confirm_clear")
                ) {
                    Text(
                        text = "Tout effacer",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmation = false },
                    modifier = Modifier.testTag("dialog_cancel_clear")
                ) {
                    Text(
                        text = "Conserver",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

private fun exportAllEntries(context: android.content.Context, entries: List<JournalEntry>) {
    try {
        val jsonArray = org.json.JSONArray().apply {
            entries.forEach { entry ->
                put(org.json.JSONObject().apply {
                    put("id", entry.id)
                    put("content", entry.content)
                    put("mood", entry.mood)
                    put("intensity", entry.intensity)
                    put("tags", org.json.JSONArray(entry.tags))
                    put("createdAt", entry.createdAt)
                    put("aiReflection", entry.aiReflection ?: "")
                    put("hasReflection", entry.hasReflection)
                    put("writingMode", entry.writingMode)
                    put("analysisType", entry.analysisType ?: "")
                    put("isFavorite", entry.isFavorite)
                    put("isPinned", entry.isPinned)
                    put("updatedAt", entry.updatedAt ?: 0L)
                })
            }
        }
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, jsonArray.toString(4))
            type = "application/json"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "Exporter toutes les entrées")
        shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        // Safe play
    }
}
