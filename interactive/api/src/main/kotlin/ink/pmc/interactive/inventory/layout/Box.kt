package ink.pmc.interactive.inventory.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ink.pmc.interactive.inventory.components.state.IntSize
import ink.pmc.interactive.inventory.jetpack.Alignment
import ink.pmc.interactive.inventory.jetpack.LayoutDirection
import ink.pmc.interactive.inventory.modifiers.Modifier

@Composable
fun Box(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
    val measurePolicy = remember(contentAlignment) { BoxMeasurePolicy(contentAlignment) }
    Layout(
        measurePolicy,
        modifier = modifier,
        content = content
    )
}

internal data class BoxMeasurePolicy(
    private val alignment: Alignment,
) : RowColumnMeasurePolicy() {
    override fun placeChildren(placeables: List<Placeable>, width: Int, height: Int): MeasureResult {
        return MeasureResult(width, height) {
            for (child in placeables) {
                child.placeAt(alignment.align(child.size, IntSize(width, height), LayoutDirection.Ltr))
            }
        }
    }
}
