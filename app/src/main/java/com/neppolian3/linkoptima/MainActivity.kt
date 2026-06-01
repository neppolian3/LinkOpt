package com.neppolian3.linkoptima

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.neppolian3.linkoptima.ui.navigation.AppNavigation
import com.neppolian3.linkoptima.ui.theme.LinkOptimaProTheme
import com.neppolian3.linkoptima.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setContent {
            LinkOptimaProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val authState = authViewModel.authState.collectAsState()
                    
                    LaunchedEffect(Unit) {
                        authViewModel.checkAuthStatus()
                    }
                    
                    AppNavigation(
                        isUserAuthenticated = authState.value.isAuthenticated,
                        isLoading = authState.value.isLoading
                    )
                }
            }
        }
    }
}