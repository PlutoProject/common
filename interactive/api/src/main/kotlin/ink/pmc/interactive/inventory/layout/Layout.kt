package ink.pmc.interactive.inventory.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import ink.pmc.interactive.inventory.canvas.LocalCanvas
import ink.pmc.interactive.inventory.modifiers.Modifier
import ink.pmc.interactive.inventory.nodes.InvNode
import ink.pmc.interactive.inventory.nodes.InvNodeApplier

/**
 * The main component for layout, it measures and positions zero or more children.
 */
@Composable
inline fun Layout(
    measurePolicy: MeasurePolicy,
    renderer: Renderer = EmptyRenderer,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val canvas = LocalCanvas.current
    ComposeNode<InvNode, InvNodeApplier>(
        factory = InvNode.Constructor,
        update = {
            set(measurePolicy) { this.measurePolicy = it }
            set(renderer) { this.renderer = it }
            //TODO dunno if this works
            set(canvas) { this.canvas = it}
            set(modifier) { this.modifier = it }
        },
        content = content,
    )
}
