package ink.pmc.interactive.inventory.modifiers.click

import ink.pmc.interactive.inventory.modifiers.Modifier

open class ClickModifier(
    val merged: Boolean = false,
    val cancelClickEvent: Boolean,
    val onClick: (ClickScope.() -> Unit),
) : Modifier.Element<ClickModifier> {
    override fun mergeWith(other: ClickModifier) = ClickModifier(
        merged = true,
        cancelClickEvent = cancelClickEvent || other.cancelClickEvent,
        onClick = {
            if (!other.merged)
                onClick()
            other.onClick(this)
        },
    )
}

fun Modifier.clickable(
    cancelClickEvent: Boolean = true,
    onClick: ClickScope.() -> Unit
) =
    then(
        ClickModifier(
            cancelClickEvent = cancelClickEvent,
            onClick = onClick,
        )
    )
