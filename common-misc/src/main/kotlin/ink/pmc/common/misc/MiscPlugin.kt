package ink.pmc.common.misc

import ink.pmc.common.misc.api.MiscAPI
import ink.pmc.common.misc.api.elevator.ElevatorManager
import ink.pmc.common.misc.api.sit.SitManager
import ink.pmc.common.misc.impl.MiscAPIImpl
import ink.pmc.common.misc.impl.elevator.ElevatorManagerImpl
import ink.pmc.common.misc.impl.elevator.builders.IronElevatorBuilder
import ink.pmc.common.misc.impl.sit.SitManagerImpl
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager

lateinit var commandManager: PaperCommandManager<CommandSender>
lateinit var plugin: JavaPlugin
lateinit var sitManager: SitManager
lateinit var elevatorManager: ElevatorManager
var disabled = true

@Suppress("UNUSED")
class MiscPlugin : JavaPlugin() {

    override fun onEnable() {
        plugin = this
        disabled = false

        commandManager = PaperCommandManager.createNative(
            this,
            ExecutionCoordinator.asyncCoordinator()
        )

        sitManager = SitManagerImpl()
        elevatorManager = ElevatorManagerImpl()

        MiscAPI.instance = MiscAPIImpl
        MiscAPIImpl.internalSitManager = sitManager
        MiscAPIImpl.internalElevatorManager = elevatorManager

        elevatorManager.registerBuilder(IronElevatorBuilder)

        commandManager.registerBrigadier()
        commandManager.command(suicideCommand)
        commandManager.command(sitCommand)

        runSitCheckTask()
        runActionBarOverrideTask()

        server.pluginManager.registerEvents(Listeners, this)
    }

    override fun onDisable() {
        sitManager.standAll()
        disabled = true
    }

}