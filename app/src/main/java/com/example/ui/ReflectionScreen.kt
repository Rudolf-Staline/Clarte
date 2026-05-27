package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(
    viewModel: JournalViewModel,
    entryId: Long,
    onNavigateHome: () -> Unit
) {
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val lastGeneratedReflection by viewModel.lastGeneratedReflection.collectAsStateWithLifecycle()

    var isSaved by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Trigger AI compilation upon entry loading
    LaunchedEffect(entryId) {
        viewModel.generateReflectionForEntry(entryId) {
            // Callback: Generation finished
        }
    }

    // Elegant slow pulsing animation for loading state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Éclairage de l'IA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isGenerating) {
                // Premium Slow Pulsing Introspective Loading State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Génération",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("ai_pulse_icon")
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Clarté se lit en toi...",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nous trions tes pensées pour en extraire l'essentiel et t'apporter un éclairage calme et structuré.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            } else {
                // Result screen
                val reflectionText = lastGeneratedReflection ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (reflectionText.isBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Nous n'avons pas pu générer d'analyse. Essaye de modifier ton texte ou de rafraîchir la connexion.",
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val parsedSections = remember(reflectionText) {
                            parseReflectionText(reflectionText)
                        }

                        parsedSections.forEach { (title, body) ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reflection_card_${title.lowercase().replace(" ", "_")}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        thickness = 1.dp
                                    )
                                    Text(
                                        text = body,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                        lineHeight = 25.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Action Bar
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedContent(
                            targetState = isSaved,
                            label = "save_state_anim"
                        ) { saved ->
                            if (saved) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Done,
                                            contentDescription = "Succès",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Réflexion enregistrée.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.testTag("saved_feedback")
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (reflectionText.isNotBlank()) {
                                            viewModel.saveReflection(entryId, reflectionText)
                                            isSaved = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("btn_save_reflection"),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(
                                        text = "Enregistrer la réflexion",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateHome,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_return_home")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Home,
                                    contentDescription = "Accueil",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Retour à l'accueil",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A highly robust line-by-line parser that parses the standard structured output.
 * If standard structures aren't matched, it safely encapsulates the text into general sections.
 */
fun parseReflectionText(raw: String): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val lines = raw.lines()

    var currentHeader = ""
    val bodyBuilder = StringBuilder()

    val subHeaderPrefixes = listOf(
        "Ce que tu sembles ressentir",
        "Ce qui a pu te déclencher",
        "Les faits observables",
        "Les interprétations possibles",
        "Une question d’introspection",
        "Une petite action calme"
    )

    fun flush() {
        if (currentHeader.isNotEmpty() && bodyBuilder.toString().trim().isNotEmpty()) {
            list.add(currentHeader to bodyBuilder.toString().trim())
            bodyBuilder.clear()
        }
    }

    for (line in lines) {
        val trimmed = line.trim()
        val matchedHeader = subHeaderPrefixes.find { header ->
            trimmed.contains(header, ignoreCase = true) && (
                trimmed.startsWith("1") || trimmed.startsWith("2") || trimmed.startsWith("3") ||
                trimmed.startsWith("4") || trimmed.startsWith("5") || trimmed.startsWith("6") ||
                trimmed.startsWith("**") || trimmed.startsWith("#") || trimmed.startsWith("-") ||
                trimmed.startsWith("Ce qui") || trimmed.startsWith("Ce que") || trimmed.startsWith("Les faits")
            )
        }

        if (matchedHeader != null) {
            flush()
            // Cleanup title prefixes (like "1. ", "**", etc) for visual cleanliness
            currentHeader = matchedHeader
        } else {
            if (currentHeader.isEmpty()) {
                // If we hit text before any section header, treat it as introductory text
                if (trimmed.isNotEmpty()) {
                    currentHeader = "Observations"
                    bodyBuilder.append(line).append("\n")
                }
            } else {
                // Append text removing markdown bold asterisks for clean display
                val cleanedLine = line.replace("**", "").replace("__", "")
                bodyBuilder.append(cleanedLine).append("\n")
            }
        }
    }
    flush()

    // Fallback if formatting was non-standard
    if (list.isEmpty() && raw.isNotBlank()) {
        list.add("Réflexion Clarté" to raw.replace("**", "").trim())
    }

    return list
}
