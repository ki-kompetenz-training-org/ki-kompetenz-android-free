package ai.ki_kompetenz_training_org

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ai.ki_kompetenz_training_org.ui.navigation.KiKompetenzNavHost
import ai.ki_kompetenz_training_org.ui.theme.KiKompetenzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiKompetenzTheme {
                KiKompetenzNavHost()
            }
        }
    }
}