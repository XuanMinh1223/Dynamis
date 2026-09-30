import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import di.appModule
import org.koin.compose.KoinApplication
import ui.DynamisApp
import ui.theme.DynamisTheme

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule())
    }) {
        DynamisTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                DynamisApp()
            }
        }
    }
}
