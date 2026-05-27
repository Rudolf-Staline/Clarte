package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (pagerState.currentPage < 4) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(if (pagerState.currentPage == 4) "Terminer" else "Continuer")
                }
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Passer", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val title = when (page) {
                    0 -> "Bienvenue dans Clarté"
                    1 -> "Écrire pour comprendre"
                    2 -> "Tes données restent d’abord sur ton appareil"
                    3 -> "Sauvegarde optionnelle"
                    4 -> "Sauvegarde chiffrée"
                    else -> ""
                }
                
                val text = when (page) {
                    0 -> "Clarté est un journal privé conçu pour t’aider à mettre de l’ordre dans tes pensées."
                    1 -> "Tu peux écrire librement, analyser une situation, clarifier une décision, rédiger une lettre non envoyée ou transformer une pensée en citation."
                    2 -> "Clarté fonctionne en local. Tu peux utiliser l’app sans compte, sans internet et sans sauvegarde cloud."
                    3 -> "Tu peux créer un compte pour sauvegarder tes données dans le cloud et les restaurer sur un autre appareil. Cette option reste désactivée tant que tu ne l’actives pas."
                    4 -> "Pour plus de confidentialité, Clarté peut chiffrer tes écrits avant leur envoi dans le cloud. Ta phrase de récupération sera nécessaire pour restaurer tes données sur un nouvel appareil."
                    else -> ""
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                )

                if (page == 4) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Si tu perds cette phrase, les sauvegardes chiffrées ne pourront pas être restaurées.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
