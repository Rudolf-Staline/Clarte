package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    onNavigateBack: () -> Unit,
    onReviewOnboarding: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confidentialité") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PrivacySection(
                title = "1. Mode local",
                description = "Tes entrées sont enregistrées dans la base locale de l’application sur cet appareil."
            )
            
            PrivacySection(
                title = "2. Sauvegarde cloud",
                description = "Si tu actives la sauvegarde cloud, une copie de tes données est envoyée vers ton espace privé."
            )
            
            PrivacySection(
                title = "3. Verrouillage local",
                description = "Le verrouillage protège l’accès à l’app sur cet appareil. Il ne remplace pas le chiffrement des sauvegardes cloud."
            )

            PrivacySection(
                title = "4. Sauvegarde chiffrée",
                description = "Si la sauvegarde chiffrée est activée, le contenu de tes entrées est chiffré avant l’envoi. Le cloud ne reçoit pas le texte lisible de ton journal."
            )
            
            PrivacySection(
                title = "5. Ce qui peut rester visible",
                description = "Certaines métadonnées nécessaires à la synchronisation peuvent rester visibles : dates, statut de suppression, favoris, épingles et version de chiffrement."
            )
            
            PrivacySection(
                title = "6. Phrase de récupération",
                description = "Ta phrase de récupération n’est pas sauvegardée par Clarté. Si tu la perds, les sauvegardes chiffrées ne pourront pas être restaurées sur un nouvel appareil."
            )
            
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "7. Limite importante",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Clarté aide à clarifier tes pensées, mais ne remplace pas un professionnel de santé mentale.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Button(
                onClick = onReviewOnboarding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Revoir l’introduction")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PrivacySection(title: String, description: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
