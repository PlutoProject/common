package ink.pmc.interactive

import androidx.compose.runtime.Composable
import ink.pmc.interactive.api.Gui
import ink.pmc.interactive.examples.ExampleScreen1
import ink.pmc.interactive.examples.ExampleScreen2
import ink.pmc.interactive.examples.ExampleScreen3
import ink.pmc.interactive.examples.ExampleScreen4
import ink.pmc.interactive.examples.form.ExampleFormScreen1
import ink.pmc.utils.PaperCm
import ink.pmc.utils.PaperCtx
import ink.pmc.utils.dsl.cloud.invoke
import ink.pmc.utils.dsl.cloud.sender
import org.bukkit.entity.Player

private const val PERMISSION = "interactive.example"

fun PaperCm.interactive(alias: Array<String>) {
    this("interactive", *alias) {
        permission(PERMISSION)
        "example_1" {
            permission(PERMISSION)
            handler {
                startInventory {
                    ExampleScreen1()
                }
            }
        }

        "example_2" {
            permission(PERMISSION)
            handler {
                startInventory {
                    ExampleScreen2()
                }
            }
        }

        "example_3" {
            permission(PERMISSION)
            handler {
                startInventory {
                    ExampleScreen3()
                }
            }
        }

        "example_4" {
            permission(PERMISSION)
            handler {
                startInventory {
                    ExampleScreen4()
                }
            }
        }

        "example_form_1" {
            permission(PERMISSION)
            handler {
                startForm {
                    ExampleFormScreen1(0)
                }
            }
        }
    }
}

private fun PaperCtx.startInventory(content: @Composable  () -> Unit) {
    val sender = sender.sender as? Player?: return
    Gui.startInventory(sender) {
        content()
    }
}

private fun PaperCtx.startForm(content: @Composable  () -> Unit) {
    val sender = sender.sender as? Player?: return
    Gui.startForm(sender) {
        content()
    }
}