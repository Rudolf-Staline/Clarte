package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewEntryScreen(
    viewModel: JournalViewModel,
    writingMode: String,
    initialMood: String?,
    onNavigateBack: () -> Unit,
    onNavigateToReflection: (Long) -> Unit
) {
    val scrollState = rememberScrollState()

    var textContent by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(initialMood ?: "Calme") }
    var intensityValue by remember { mutableFloatStateOf(5f) }
    var errorMessage by remember { mutableStateOf("") }

    val selectedTags = remember { mutableStateListOf<String>() }

    // Analysis type dropdown
    var showExplanationMenu by remember { mutableStateOf(false) }
    var selectedAnalysisType by remember { mutableStateOf("Comprendre ce que je ressens") }

    val analysisTypeOptions = listOf(
        "Comprendre ce que je ressens",
        "Prendre du recul",
        "Décider quoi faire",
        "Me calmer",
        "Transformer en citation",
        "Résumer ma pensée",
        "Analyse profonde",
        "Questionnement socratique"
    )

    // Mode-specific template placeholders
    val placeholderText = when (writingMode) {
        "Lettre non envoyée" -> "Écris ce que tu aimerais dire, sans chercher à l’envoyer."
        "Situation difficile" -> "Décris la situation aussi simplement que possible."
        "Décision" -> "Quelle décision essaies-tu de clarifier ?"
        "Citation" -> "Écris ta pensée brute."
        "Gratitude" -> "Note une à trois choses qui ont eu de la valeur aujourd’hui."
        "Foi / réflexion spirituelle" -> "Écris ta réflexion avec calme et sincérité."
        else -> "Écris librement. Tu n’as pas besoin d’être clair dès le début."
    }

    val moodOptions = listOf(
        "Calme" to "🧘",
        "Anxieux" to "💭",
        "Fatigué" to "🪫",
        "Triste" to "🌧️",
        "Confus" to "🌀",
        "Motivé" to "🌱",
        "Vide" to "🍃",
        "En colère" to "⚡",
        "Reconnaissant" to "✨"
    )

    val validTags = listOf(
        "Études", "Amour", "Famille", "Foi", "Solitude",
        "Ambition", "Fatigue", "Peur", "Honte", "Colère", "Espoir", "Doute"
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
                        text = "$writingMode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
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
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // AI Analysis Type selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Type de réflexion IA attendue",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { showExplanationMenu = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("analysis_type_selector")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedAnalysisType,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = "Sélectionner",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    DropdownMenu(
                        expanded = showExplanationMenu,
                        onDismissRequest = { showExplanationMenu = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        analysisTypeOptions.forEach { typeOption ->
                            DropdownMenuItem(
                                text = { Text(text = typeOption, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    selectedAnalysisType = typeOption
                                    showExplanationMenu = false
                                },
                                modifier = Modifier.testTag("analysis_option_$typeOption")
                            )
                        }
                    }
                }
            }

            // Mood Selection Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Humeur principale",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(moodOptions) { (mood, emoji) ->
                        val isSelected = selectedMood == mood
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { selectedMood = mood }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("select_mood_$mood")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(emoji, fontSize = 16.sp)
                                Text(
                                    text = mood,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            // Intensity Selector Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intensité ressentie",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "${intensityValue.toInt()}/10",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = intensityValue,
                        onValueChange = { intensityValue = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("intensity_slider")
                    )
                }
            }

            // Free Writing Text Area
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tes pensées",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedTextField(
                    value = textContent,
                    onValueChange = {
                        textContent = it
                        if (it.isNotBlank()) errorMessage = ""
                    },
                    placeholder = {
                        Text(
                            text = placeholderText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            lineHeight = 24.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .testTag("entry_text_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 24.sp
                    )
                )
            }

            // Selection Tags Chip Flow
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Thèmes reliés (Tags)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    validTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedTags.remove(tag)
                                else selectedTags.add(tag)
                            },
                            label = { Text(tag) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Sélectionné",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag("tag_chip_$tag")
                        )
                    }
                }
            }

            // Error display block
            AnimatedVisibility(
                visible = errorMessage.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                        .padding(14.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (textContent.isBlank()) {
                            errorMessage = "Écris d’abord quelques lignes avant de lancer l’analyse."
                        } else {
                            viewModel.insertEntry(
                                content = textContent.trim(),
                                mood = selectedMood,
                                intensity = intensityValue.toInt(),
                                tags = selectedTags.toList(),
                                writingMode = writingMode,
                                analysisType = selectedAnalysisType
                            ) { newId ->
                                onNavigateToReflection(newId)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_analyze"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Analyser mon entrée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (textContent.isBlank()) {
                            errorMessage = "Écris quelque chose avant de sauvegarder."
                        } else {
                            viewModel.insertEntry(
                                content = textContent.trim(),
                                mood = selectedMood,
                                intensity = intensityValue.toInt(),
                                tags = selectedTags.toList(),
                                writingMode = writingMode,
                                analysisType = null
                            ) {
                                onNavigateBack()
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_save_only"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(
                        enabled = true
                    )
                ) {
                    Text(
                        text = "Sauvegarder sans analyse",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
