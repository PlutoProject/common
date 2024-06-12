package ink.pmc.visual.renderers

import ink.pmc.utils.chat.nms
import ink.pmc.utils.item.bukkit
import ink.pmc.utils.player.sendPacket
import ink.pmc.utils.structure.emptyOptional
import ink.pmc.utils.structure.optional
import ink.pmc.visual.api.toast.Toast
import ink.pmc.visual.api.toast.ToastRenderer
import ink.pmc.visual.api.toast.ToastType
import net.kyori.adventure.text.Component
import net.minecraft.advancements.*
import net.minecraft.advancements.critereon.ImpossibleTrigger
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket
import net.minecraft.resources.ResourceLocation
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("UNUSED")
object PaperToastRenderer : ToastRenderer<Player>() {

    private val location = optional(ResourceLocation("visual", "paper_toast_renderer"))
    private val criteria = mapOf("for_free" to Criterion(ImpossibleTrigger(), ImpossibleTrigger.TriggerInstance()))
    private val fixedRequirements = listOf("for_free")
    private val requirements = AdvancementRequirements(listOf(fixedRequirements))
    private val adventureTexture =
        optional(ResourceLocation("minecraft:textures/gui/advancements/backgrounds/adventure.png"))
    private val progress = mapOf(location.get() to AdvancementProgress().apply {
        update(requirements)
        getCriterion("for_free")?.grant()
    })

    private val ToastType.nms: AdvancementType
        get() {
            return when (this) {
                ToastType.TASK -> AdvancementType.TASK
                ToastType.CHALLENGE -> AdvancementType.CHALLENGE
                ToastType.GOAL -> AdvancementType.GOAL
            }
        }

    override fun render(player: Player, obj: Toast) {
        val material = obj.icon.bukkit
        val item = ItemStack(material, 1)
        val display = optional(
            DisplayInfo(
                CraftItemStack.asNMSCopy(item),
                obj.message.nms,
                Component.text("PlutoProject Visual - Paper Toast Renderer").nms,
                optional(ResourceLocation(obj.frame.texture)),
                obj.type.nms,
                true,
                false,
                true,
            )
        )

        val advancement = Advancement(emptyOptional(), display, AdvancementRewards.EMPTY, criteria, requirements, false)
        val holder = AdvancementHolder(location.get(), advancement)
        val displayPacket = ClientboundUpdateAdvancementsPacket(false, listOf(holder), setOf(), progress)
        player.sendPacket(displayPacket)

        val removed = setOf(location.get())
        val removePacket = ClientboundUpdateAdvancementsPacket(
            false,
            listOf(),
            removed,
            mapOf<ResourceLocation, AdvancementProgress>()
        )
        player.sendPacket(removePacket)
    }

}