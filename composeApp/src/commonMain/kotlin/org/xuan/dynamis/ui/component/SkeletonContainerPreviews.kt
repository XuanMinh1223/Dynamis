package org.xuan.dynamis.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
private fun PreviewCard(isLoading: Boolean, index: Int? = null) {
    SkeletonContainer(
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        placeholderShape = RoundedCornerShape(12.dp),
        index = index,
    ) {
        Box(Modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.Center) {
            Text("Loaded content ${(index ?: 0) + 1}")
        }
    }
}

@Preview(name = "Loading", widthDp = 360, heightDp = 140, showBackground = true)
@Composable
private fun SkeletonLoadingPreview() {
    Box(Modifier.padding(24.dp)) { PreviewCard(isLoading = true) }
}

@Preview(name = "Loaded", widthDp = 360, heightDp = 140, showBackground = true)
@Composable
private fun SkeletonLoadedPreview() {
    Box(Modifier.padding(24.dp)) { PreviewCard(isLoading = false) }
}

/** Run with Interactive Mode: the pulse travels down the rows; the button reveals them. */
@Preview(name = "Lazy list (interactive)", widthDp = 360, heightDp = 520, showBackground = true)
@Composable
private fun SkeletonLazyListPreview() {
    var loading by remember { mutableStateOf(true) }
    Column(Modifier.padding(16.dp)) {
        Button(onClick = { loading = !loading }) { Text(if (loading) "Finish loading" else "Reload") }
        SkeletonClockProvider {
            LazyColumn(
                Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(List(6) { it }) { index, _ -> PreviewCard(loading, index) }
            }
        }
    }
}

/** Standalone blocks: each pulses with its own period and start offset. */
@Preview(name = "Standalone variety", widthDp = 360, heightDp = 360, showBackground = true)
@Composable
private fun SkeletonStandalonePreview() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(4) { PreviewCard(isLoading = true) }
    }
}
