package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.BottomNavigationBar
import com.example.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: JournalViewModel,
    onNavigateTab: (String) -> Unit,
    onNavigateToDay: (String) -> Unit
) {
    val entries by viewModel.allEntries.collectAsStateWithLifecycle()

    // Setup Calendar for current month
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)

    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.FRENCH)
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.FRENCH)
    
    val entriesByDate = remember(entries) {
        entries.groupBy { dateFormat.format(Date(it.createdAt)) }
    }
    
    // Calculate days for the grid
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday
    
    val paddingDays = if (startDayOfWeek == Calendar.SUNDAY) 6 else startDayOfWeek - 2
    
    val gridItems = mutableListOf<CalendarDayItem>()
    for (i in 0 until paddingDays) {
        gridItems.add(CalendarDayItem(0, ""))
    }
    
    for (day in 1..daysInMonth) {
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val dateStr = dateFormat.format(calendar.time)
        gridItems.add(CalendarDayItem(day, dateStr))
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendrier", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Light) },
            )
        },
        bottomBar = {
            BottomNavigationBar(currentRoute = "trends", onNavigate = onNavigateTab) // Still highlights trends
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = monthFormat.format(calendar.time).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Days of week header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach { dayStr ->
                    Text(text = dayStr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gridItems) { item ->
                    if (item.day == 0) {
                        Spacer(modifier = Modifier.size(40.dp))
                    } else {
                        val dayEntries = entriesByDate[item.dateStr] ?: emptyList()
                        val hasEntries = dayEntries.isNotEmpty()
                        
                        val backgroundColor = if (hasEntries) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        val contentColor = if (hasEntries) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(backgroundColor)
                                .clickable {
                                    if (hasEntries) {
                                        onNavigateToDay(item.dateStr)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = item.day.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (hasEntries) FontWeight.Bold else FontWeight.Normal,
                                    color = contentColor
                                )
                                if (hasEntries) {
                                    val intensityAvg = dayEntries.map { it.intensity }.average()
                                    val opacity = (intensityAvg / 10.0).toFloat().coerceIn(0.2f, 1f)
                                    Box(modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = opacity))
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (entriesByDate.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Aucune entrée pour ce mois.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                     shape = RoundedCornerShape(12.dp),
                     modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Aperçu du mois", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Les points sous la date indiquent l'intensité moyenne des émotions de la journée.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

data class CalendarDayItem(val day: Int, val dateStr: String)
