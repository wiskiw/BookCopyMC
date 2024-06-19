package xyz.eclpseisoffline.bookcopy.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundEditBookPacket
import net.minecraft.world.item.Items
import xyz.eclipseisoffline.bookcopy.BookSuggestionProvider
import xyz.eclipseisoffline.bookcopy.FileUtils
import xyz.eclpseisoffline.bookcopy.command.argumenttype.IoFormatArgumentType
import xyz.eclpseisoffline.bookcopy.model.IoFormat
import java.util.*

class ImportCommand {

    private object Args {
        const val FILE_NAME = "file_name"
        const val FORMAT_FLAG = "format"
    }

    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommandManager.literal("bookcopy")
            .then(
                ClientCommandManager
                    .literal("import")
                    .then(
                        ClientCommandManager.argument(Args.FILE_NAME, StringArgumentType.word())
                            .suggests(BookSuggestionProvider())
                            .then(
                                ClientCommandManager.argument(Args.FORMAT_FLAG, IoFormatArgumentType.ioFormat())
                                    .executes { context ->
                                        execute(
                                            context = context,
                                            name = StringArgumentType.getString(context, Args.FILE_NAME),
                                            ioFormat = IoFormatArgumentType.getIoFormat(context, Args.FORMAT_FLAG),
                                        )
                                    }
                            )
                    )
            )

    private fun execute(
        context: CommandContext<FabricClientCommandSource>,
        name: String,
        ioFormat: IoFormat,
    ): Int {
        assertPlayerHoldWritableBook(context)

        val path = FileUtils.getBookSavePath().resolve(name)
        val bookContent = ioFormat.universalBookContentIo.read(path)

        val slot = context.source.player.inventory.selected
        context.source.player.connection.send(
            ServerboundEditBookPacket(slot, bookContent.pages, Optional.empty())
        )
        context.source.sendFeedback(Component.literal("Read book from file"))

        return Command.SINGLE_SUCCESS
    }

    @Throws(CommandSyntaxException::class)
    private fun assertPlayerHoldWritableBook(context: CommandContext<FabricClientCommandSource>) {
        val book = context.source.player.mainHandItem
        if (!book.`is`(Items.WRITABLE_BOOK)) {
            val errorMessage = Component.literal("Must hold a book and quill")
            throw SimpleCommandExceptionType(errorMessage).create()
        }
    }
}
