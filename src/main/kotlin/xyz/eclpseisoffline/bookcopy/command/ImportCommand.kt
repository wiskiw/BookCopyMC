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
import xyz.eclpseisoffline.bookcopy.universalbookcontentio.UniversalBookContentNbtIo
import java.util.*

class ImportCommand {

    private object Args {
        const val NAME = "name"
    }

    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommandManager.literal("bookcopy")
            .then(
                ClientCommandManager.literal("import")
                    .then(
                        ClientCommandManager.argument(Args.NAME, StringArgumentType.word())
                            .suggests(BookSuggestionProvider())
                            .executes { context: CommandContext<FabricClientCommandSource> ->
                                return@executes execute(
                                    context = context,
                                    name = StringArgumentType.getString(context, Args.NAME),
                                )
                            }
                    )
            )

    private fun execute(
        context: CommandContext<FabricClientCommandSource>,
        name: String,
    ): Int {
        assertPlayerHoldWritableBook(context)

        val path = FileUtils.getBookSavePath().resolve(name)
        val bookContent = UniversalBookContentNbtIo().read(path)

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
