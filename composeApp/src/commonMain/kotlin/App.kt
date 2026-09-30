import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import di.appModule
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import ui.DynamisApp

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule())
    }) {
        DynamisApp()
    }
}
