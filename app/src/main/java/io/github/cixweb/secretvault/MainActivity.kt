package io.github.cixweb.secretvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.cixweb.secretvault.crypto.KeystoreAeadProvider
import io.github.cixweb.secretvault.data.SecretVault
import io.github.cixweb.secretvault.ui.MainScreen
import io.github.cixweb.secretvault.ui.SecretViewModel
import io.github.cixweb.secretvault.ui.theme.AppTheme

/**
 * The only activity. It builds the [SecretViewModel], which opens the vault in the app's
 * private files directory with a Keystore-backed key and keeps it across configuration
 * changes.
 */
class MainActivity : ComponentActivity() {
    private val viewModel: SecretViewModel by viewModels {
        viewModelFactory {
            initializer {
                SecretViewModel { scope ->
                    SecretVault.open(
                        dir = applicationContext.filesDir,
                        aeadProvider = KeystoreAeadProvider(applicationContext),
                        scope = scope,
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainScreen(viewModel)
            }
        }
    }
}
