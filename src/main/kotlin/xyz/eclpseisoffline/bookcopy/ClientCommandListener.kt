package xyz.eclpseisoffline.bookcopy

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.CommandBuildContext
import xyz.eclpseisoffline.bookcopy.command.ExportCommand
import xyz.eclpseisoffline.bookcopy.command.ImportCommand

class ClientCommandListener : ClientCommandRegistrationCallback {

    override fun register(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        registryAccess: CommandBuildContext?
    ) {
        dispatcher.register(ImportCommand().build())
        dispatcher.register(ExportCommand().build())
    }
}
