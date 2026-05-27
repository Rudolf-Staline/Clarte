package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
fun HistoryScreen(
    viewModel: JournalViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateTab: (String) -> Unit
) {
    val entries by viewModel.allEntries.collectAsStateWithLifecycle()
    val hidePreviewsHistory by viewModel.hidePreviewsHistory.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    
    // Filters State
    var selectedMoodFilter by remember { mutableStateOf<String?>(null) }
    var selectedModeFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var showOnlyPinned by remember { mutableStateOf(false) }
    
    // Sorting (Date Desc default)
    var sortByOption by remember { mutableStateOf("Date (Récent)") } // "Date (Récent)", "Date (Ancien)", "Intensité (+)", "Intensité (-)"

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

    // Filtered entries list
    val filteredEntries = remember(entries, searchQuery, selectedMoodFilter, selectedModeFilter, showOnlyFavorites, showOnlyPinned, sortByOption) {
        var result = entries.toList()

        // Search text filter
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.lowercase()
            result = result.filter { entry ->
                entry.content.lowercase().contains(q) ||
                entry.mood.lowercase().contains(q) ||
                entry.writingMode.lowercase().contains(q) ||
                (entry.aiReflection?.lowercase()?.contains(q) ?: false) ||
                entry.tags.any { it.lowercase().contains(q) }
            }
        }

        // Mood filters
        selectedMoodFilter?.let { mood ->
            result = result.filter { it.mood == mood }
        }

        // Writing mode filters
        selectedModeFilter?.let { mode ->
            result = result.filter { it.writingMode == mode }
        }

        // Favorites filter
        if (showOnlyFavorites) {
            result = result.filter { it.isFavorite }
        }

        // Pinned filter
        if (showOnlyPinned) {
            result = result.filter { it.isPinned }
        }

        // Sorting
        result = when (sortByOption) {
            "Date (Ancien)" -> result.sortedBy { it.createdAt }
            "Intensité (+)" -> result.sortedByDescending { it.intensity }
            "Intensité (-)" -> result.sortedBy { it.intensity }
            else -> result.sortedByDescending { it.createdAt } // Date (Récent)
        }

        result
    }

    val availableMoods = remember(entries) { entries.map { it.mood }.distinct() }
    val availableModes = remember(entries) { entries.map { it.writingMode }.distinct() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historique",
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
                currentRoute = "history",
                onNavigate = onNavigateTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher dans mes écrits...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_history_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                singleLine = true
            )

            // Horizontal filters bar (Row of scrollable elements)
            val filtersScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filtersScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pin Filter Chip
                FilterChip(
                    selected = showOnlyPinned,
                    onClick = { showOnlyPinned = !showOnlyPinned },
                    label = { Text("Épinglés") },
                    leadingIcon = { Icon(imageVector = Icons.Default.PushPin, contentDescription = "Pinned", modifier = Modifier.size(16.dp)) }
                )

                // Favorite Filter Chip
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = { showOnlyFavorites = !showOnlyFavorites },
                    label = { Text("Favoris") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite", modifier = Modifier.size(16.dp)) }
                )

                // Mood Filter Chip (if selected)
                availableMoods.forEach { mood ->
                    val isSelected = selectedMoodFilter == mood
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMoodFilter = if (isSelected) null else mood },
                        label = { Text("${moodEmojis[mood] ?: ""} $mood") }
                    )
                }

                // Mode Filter Chip
                availableModes.forEach { mode ->
                    val isSelected = selectedModeFilter == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedModeFilter = if (isSelected) null else mode },
                        label = { Text(mode) }
                    )
                }
            }

            // Sorting dropdown selection
            var showShortMenu by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredEntries.size} entrées trouvées",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )

                Box {
                    TextButton(onClick = { showShortMenu = true }) {
                        Icon(imageVector = Icons.Default.Sort, contentDescription = "Tri", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sortByOption, style = MaterialTheme.typography.bodyMedium)
                    }
                    DropdownMenu(expanded = showShortMenu, onDismissRequest = { showShortMenu = false }) {
                        listOf("Date (Récent)", "Date (Ancien)", "Intensité (+)", "Intensité (-)").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    sortByOption = option
                                    showShortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Entries List
            if (filteredEntries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Vide",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (entries.isEmpty()) "Ton historique est vide. Tes premières entrées apparaîtront ici." else "Aucun écrit ne correspond à tes filtres.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        // Render pinned / favorite indicator banners
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDetail(entry.id) }
                                .testTag("history_item_${entry.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Date header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val emoji = moodEmojis[entry.mood] ?: "📝"
                                        Text(emoji, fontSize = 20.sp)
                                        Column {
                                            Text(
                                                text = entry.mood,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = entry.writingMode,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (entry.isPinned) {
                                            Icon(imageVector = Icons.Default.PushPin, contentDescription = "Epinglé", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                        if (entry.isFavorite) {
                                            Icon(imageVector = Icons.Default.Star, contentDescription = "Favori", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = formatHistoryDate(entry.createdAt),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                // Preview content (Supports privacy mask toggling)
                                Text(
                                    text = if (hidePreviewsHistory) "Contenu masqué pour ta confidentialité." else entry.content,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontStyle = if (hidePreviewsHistory) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                    ),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = if (hidePreviewsHistory) 0.45f else 0.8f
                                    ),
                                    lineHeight = 22.sp
                                )

                                // Display tags if they exist
                                if (entry.tags.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        entry.tags.take(3).forEach { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = tag,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        if (entry.tags.size > 3) {
                                            Text(
                                                text = "+${entry.tags.size - 3}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                    thickness = 1.dp
                                )

                                // Bottom Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Intensité : ${entry.intensity}/10",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        if (entry.hasReflection) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = entry.analysisType ?: "Introspection",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Ouvrir",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatHistoryDate(timeMs: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.FRENCH)
    return sdf.format(Date(timeMs))
}
