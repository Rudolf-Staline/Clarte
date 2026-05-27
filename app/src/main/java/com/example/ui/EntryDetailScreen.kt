package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    viewModel: JournalViewModel,
    entryId: Long,
    onNavigateBack: () -> Unit
) {
    val entryState by viewModel.getEntryFlow(entryId).collectAsStateWithLifecycle(initialValue = null)
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRegenerateMenu by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showExportWarning by remember { mutableStateOf<(() -> Unit)?>(null) }
    val hideExportWarning by viewModel.hideExportWarning.collectAsStateWithLifecycle()

    val moodEmojis = mapOf(
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            val entry = entryState
            TopAppBar(
                title = {
                    Text(
                        text = "Détails",
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
                actions = {
                    if (entry != null) {
                        // PIN Toggle BUTTON
                        IconButton(
                            onClick = { viewModel.togglePinned(entry.id) },
                            modifier = Modifier.testTag("btn_pin_entry")
                        ) {
                            Icon(
                                imageVector = if (entry.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Epingler",
                                tint = if (entry.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // FAVORITE Toggle BUTTON
                        IconButton(
                            onClick = { viewModel.toggleFavorite(entry.id) },
                            modifier = Modifier.testTag("btn_favorite_entry")
                        ) {
                            Icon(
                                imageVector = if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Favori",
                                tint = if (entry.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // SHARE / EXPORT BUTTON
                        Box {
                            IconButton(
                                onClick = { showExportMenu = true },
                                modifier = Modifier.testTag("btn_export_entry")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Exporter",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            DropdownMenu(
                                expanded = showExportMenu,
                                onDismissRequest = { showExportMenu = false }
                            ) {
                                val runExport = { action: () -> Unit ->
                                    showExportMenu = false
                                    if (hideExportWarning) {
                                        action()
                                    } else {
                                        showExportWarning = action
                                    }
                                }
                                DropdownMenuItem(
                                    text = { Text("Exporter en PDF") },
                                    onClick = { runExport { com.example.utils.ExportHelper.exportEntryAsPdf(context, entry) } }
                                )
                                DropdownMenuItem(
                                    text = { Text("Exporter en image") },
                                    onClick = { runExport { com.example.utils.ExportHelper.exportQuoteImage(context, entry.content) } }
                                )
                                DropdownMenuItem(
                                    text = { Text("Exporter en texte") },
                                    onClick = { runExport { exportEntryAsText(context, entry) } }
                                )
                            }
                        }

                        // DELETE BUTTON
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("btn_delete_entry")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        val entry = entryState
        if (entry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Date and metadata card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formatFullDate(entry.createdAt),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Mode : ${entry.writingMode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Intensité : ${entry.intensity}/10",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Mood indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(moodEmojis[entry.mood] ?: "📝", fontSize = 24.sp)
                            Text(
                                text = "Humeur ressentie : ${entry.mood}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }

                        // Tags displaying list
                        if (entry.tags.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                entry.tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Main journal entry content card
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
                        Text(
                            text = "Ton récit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                            thickness = 1.dp
                        )
                        Text(
                            text = entry.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 26.sp
                        )
                    }
                }

                // REGENERATION LOADING / STATUS BLOCK
                if (isGenerating) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Text(
                                text = "Clarté reformule un éclairage structuré...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // AI Reflection display block if exists, or regeneration helper
                if (entry.hasReflection && !entry.aiReflection.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "L'éclairage de Clarté (${entry.analysisType ?: "Introspection"})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        // Option to choose new reflection type and refresh
                        IconButton(
                            onClick = { showRegenerateMenu = true },
                            modifier = Modifier.testTag("btn_trigger_regenerate")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regénérer l'éclairage", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    val sections = remember(entry.aiReflection) {
                        parseReflectionText(entry.aiReflection!!)
                    }

                    sections.forEach { (title, body) ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_reflection_${title.lowercase().replace(" ","_")}")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                    thickness = 1.dp
                                )
                                Text(
                                    text = body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val daysOld = (System.currentTimeMillis() - entry.createdAt) / (24 * 60 * 60 * 1000L)
                    if (daysOld >= 0) {
                        Button(
                            onClick = { viewModel.generateHindsightReflection(entry) {} },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Relire avec recul")
                        }
                    }
                } else {
                    // Entry has no reflection (saved without analysis) -> offer generating one in-place
                    Button(
                        onClick = { showRegenerateMenu = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_generate_analysis_missing"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Générer l'analyse Clarté")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Regina selection Dropdown/Dialog Menu
    if (showRegenerateMenu) {
        val entry = entryState
        if (entry != null) {
            AlertDialog(
                onDismissRequest = { showRegenerateMenu = false },
                title = {
                    Text(
                        text = "Choisir le filtre d'analyse",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "L'IA adaptera sa réflexion à l'angle choisi :",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        analysisTypeOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        showRegenerateMenu = false
                                        viewModel.regenerateReflectionForEntry(entry.id, option) {}
                                    }
                                    .padding(vertical = 12.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRegenerateMenu = false }) {
                        Text("Fermer", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

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

    // Material 3 Confirmation French dialog
    if (showDeleteConfirm) {
        val entry = entryState
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Attention",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Suppression",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Supprimer définitivement cette entrée ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (entry != null) {
                            viewModel.removeEntry(entry)
                        }
                        showDeleteConfirm = false
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("dialog_confirm_btn")
                ) {
                    Text(
                        text = "Supprimer",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.testTag("dialog_cancel_btn")
                ) {
                    Text(
                        text = "Annuler",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

private fun formatFullDate(timeMs: Long): String {
    val sdf = SimpleDateFormat("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH)
    return sdf.format(Date(timeMs)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
}

private fun exportEntryAsText(context: android.content.Context, entry: JournalEntry) {
    try {
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRENCH).format(java.util.Date(entry.createdAt))
        val text = """
            CLARTÉ - REFLEXION INTROSPECTIVE
            Date : $dateStr
            Mode d’écriture : ${entry.writingMode}
            Humeur : ${entry.mood} (Intensité : ${entry.intensity}/10)
            Thématiques : ${entry.tags.joinToString(", ")}
            
            --- TEXTE DE L'ENTRÉE ---
            ${entry.content}
            
            --- RÉFLEXION CLARTÉ ---
            ${entry.aiReflection ?: "Aucune réflexion générée."}
        """.trimIndent()

        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "Exporter mon entrée")
        shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        // Safe play
    }
}
