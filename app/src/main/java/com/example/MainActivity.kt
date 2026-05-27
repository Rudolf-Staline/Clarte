package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JournalViewModel
import com.example.viewmodel.JournalViewModelFactory

import com.example.auth.AuthViewModel
import com.example.sync.FirebaseSyncRepository
import com.example.sync.SyncManager

class MainActivity : FragmentActivity() {
    
    // Instantiate our unified JournalViewModel via Custom simple Factory
    private val viewModel: JournalViewModel by viewModels {
        JournalViewModelFactory(application)
    }

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Form un-screenshot-able if secure flag needed? WindowManager.LayoutParams.FLAG_SECURE
        // Actually, we can add tracking for lock/unlock
        lifecycle.addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> viewModel.checkAndLockIfNeeded(isBackground = true)
                androidx.lifecycle.Lifecycle.Event.ON_START -> viewModel.checkAndLockIfNeeded(isBackground = false)
                else -> {}
            }
        })
        
        // Setup modern edge-to-edge immersive viewports
        enableEdgeToEdge()
        
        val syncRepository = FirebaseSyncRepository()
        // Here we just grab the repository that JournalViewModel instantiated (normally via DI, simplified here)
        // Since we are not full DI, we recreate repository or grab from DB directly.
        val database = com.example.data.AppDatabase.getDatabase(applicationContext)
        val journalRepository = com.example.data.JournalRepository(database.journalDao())
        val settingsStore = com.example.data.SettingsStore(applicationContext)
        val syncManager = SyncManager(syncRepository, journalRepository, settingsStore)

        setContent {
            val selectedTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
            val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
            val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()

            // Pass the user preferred theme (Sombre, Clair, Système) to the theme wrapper
            MyApplicationTheme(selectedTheme = selectedTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isAppLockEnabled && isLocked) {
                        com.example.ui.LockScreen(viewModel)
                    } else {
                        AppNavigation(
                            viewModel = viewModel,
                            authViewModel = authViewModel,
                            syncManager = syncManager
                        )
                    }
                }
            }
        }
    }
}
