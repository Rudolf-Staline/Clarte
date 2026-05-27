package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JournalEntry
import com.example.ui.navigation.BottomNavigationBar
import com.example.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    viewModel: JournalViewModel,
    onNavigateTab: (String) -> Unit,
    onNavigateToCalendar: () -> Unit = {}
) {
    val entries by viewModel.allEntries.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var weeklyReviewResult by remember { mutableStateOf<String?>(null) }
    var isReviewing by remember { mutableStateOf(false) }

    var showExportWarning by remember { mutableStateOf<(() -> Unit)?>(null) }
    val hideExportWarning by viewModel.hideExportWarning.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Computations
    val totalEntries = entries.size
    val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
    val thirtyDaysAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L

    val weekEntries = entries.filter { it.createdAt >= sevenDaysAgo }
    val monthEntries = entries.filter { it.createdAt >= thirtyDaysAgo }

    val dominantMoodWeek = if (weekEntries.isEmpty()) "Aucune" else {
        weekEntries.groupBy { it.mood }.maxByOrNull { it.value.size }?.key ?: "Aucune"
    }

    val dominantMoodMonth = if (monthEntries.isEmpty()) "Aucune" else {
        monthEntries.groupBy { it.mood }.maxByOrNull { it.value.size }?.key ?: "Aucune"
    }

    val avgIntensityWeek = if (weekEntries.isEmpty()) 0.0 else {
        weekEntries.map { it.intensity }.average()
    }

    val topTags = entries.flatMap { it.tags }
        .groupBy { it }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    val topModes = entries.map { it.writingMode }
        .groupBy { it }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    // STREAK
    val currentStreak = remember(entries) {
        if (entries.isEmpty()) 0 else {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.FRENCH)
            val dayStrings = entries.map { sdf.format(Date(it.createdAt)) }.distinct()
            val todayStr = sdf.format(Date())
            val yesterdayStr = sdf.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L))

            if (!dayStrings.contains(todayStr) && !dayStrings.contains(yesterdayStr)) {
                0
            } else {
                var streak = 0
                val calendar = Calendar.getInstance()
                if (!dayStrings.contains(todayStr) && dayStrings.contains(yesterdayStr)) {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                }
                while (true) {
                    val checkStr = sdf.format(calendar.time)
                    if (dayStrings.contains(checkStr)) {
                        streak++
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
                streak
            }
        }
    }

    // Patterns
    val patternsList = remember(entries) {
        val patterns = mutableListOf<String>()
        if (entries.size >= 3) {
            val recentThree = entries.take(3)
            val allHighIntensity = recentThree.all { it.intensity >= 7 }
            if (allHighIntensity) {
                patterns.add("Intensité élevée sur les dernières entrées. Surveille tes sas de décompression.")
            }
            val commonMood = recentThree.groupBy { it.mood }.maxByOrNull { it.value.size }
            if (commonMood != null && commonMood.value.size >= 2) {
                patterns.add("Humeur récurrente récente : ${commonMood.key}. Cet état guide tes réactions.")
            }
            val mostlyNight = recentThree.map { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.createdAt }
                cal.get(Calendar.HOUR_OF_DAY)
            }.count { it >= 20 || it <= 4 }
            if (mostlyNight >= 2) {
                patterns.add("Introspection nocturne fréquente. Veille à ton sommeil.")
            }
        }
        if (patterns.isEmpty()) {
            patterns.add("Besoin de plus d'entrées pour repérer tes schémas émotionnels.")
        }
        patterns
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tendances",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Light
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "trends",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Streak card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Cycle d'écriture",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$currentStreak ${if (currentStreak > 1) "jours consécutifs" else "jour"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text("🔥", fontSize = 36.sp)
                }
            }

            // Stat grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Entrées totales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        Text(text = "$totalEntries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).clickable { onNavigateToCalendar() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Outlined.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(text = "Voir le calendrier", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Mood details
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
                        text = "Aperçu de mes humeurs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dominante cette semaine :", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        Text(dominantMoodWeek, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dominante ce mois :", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        Text(dominantMoodMonth, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Intensité moyenne (semaine) :", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        Text("${String.format("%.1f", avgIntensityWeek)}/10", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Top tags and Modes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Sujets récurrents", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        if (topTags.isEmpty()) {
                            Text("Aucun tag", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        } else {
                            topTags.forEach { (tag, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("#$tag", maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                                    Text("$count", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Modes d'écriture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        if (topModes.isEmpty()) {
                            Text("Aucun mode", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        } else {
                            topModes.forEach { (mode, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mode, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                                    Text("$count", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // Patterns Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Schémas récurrents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    patternsList.forEach { pattern ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = pattern,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // MONTHLY REVIEW BUTTON & PANEL
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        isReviewing = true
                        viewModel.generateMonthlyReview { result ->
                            weeklyReviewResult = result
                            isReviewing = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_monthly_review"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isReviewing
                ) {
                    if (isReviewing) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Revue du mois", style = MaterialTheme.typography.titleMedium)
                    }
                }

                weeklyReviewResult?.let { review ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("monthly_review_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Bilan Mensuel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            Text(
                                text = review,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 22.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val act = {
                                        com.example.utils.ExportHelper.exportReviewAsPdf(
                                            context = context,
                                            title = "Revue du mois",
                                            content = review,
                                            dateSlug = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.FRENCH).format(java.util.Date()),
                                            filenamePrefix = "clarte_revue_mois"
                                        )
                                    }
                                    if (hideExportWarning) act() else showExportWarning = act
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Exporter la revue en PDF")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
