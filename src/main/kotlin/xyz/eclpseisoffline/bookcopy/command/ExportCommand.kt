package xyz.eclpseisoffline.bookcopy.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.WritableBookContent
import net.minecraft.world.item.component.WrittenBookContent
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclipseisoffline.bookcopy.BookSuggestionProvider
import xyz.eclipseisoffline.bookcopy.FileUtils
import xyz.eclpseisoffline.bookcopy.UnifiedBookFactory
import xyz.eclpseisoffline.bookcopy.command.argumenttype.IoFormatArgumentType
import xyz.eclpseisoffline.bookcopy.model.IoFormat
import xyz.eclpseisoffline.bookcopy.model.UnifiedBook
import xyz.eclpseisoffline.bookcopy.unifiedbookio.UnifiedBookIo
import java.io.IOException
import java.nio.file.Path

class ExportCommand {

    private object Args {
        const val FILE_NAME = "file_name"
        const val FORMAT_FLAG = "format"
    }

    private val unifiedBookFactory by lazy { UnifiedBookFactory() }

    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommandManager.literal("bookcopy")
            .then(
                ClientCommandManager.literal("export")
                    .then(
                        ClientCommandManager.argument(Args.FILE_NAME, StringArgumentType.word())
                            .suggests(BookSuggestionProvider())
                            .then(
                                ClientCommandManager.argument(Args.FORMAT_FLAG, IoFormatArgumentType.ioFormat())
                                    .executes { context ->
                                        execute(
                                            context = context,
                                            fileName = StringArgumentType.getString(context, Args.FILE_NAME),
                                            ioFormat = IoFormatArgumentType.getIoFormat(context, Args.FORMAT_FLAG),
                                        )
                                    }
                            )
                    )
            )

    private fun execute(
        context: CommandContext<FabricClientCommandSource>,
        fileName: String,
        ioFormat: IoFormat,
    ): Int {
        val handItem = context.source.player.mainHandItem
        val unifiedBook = when {
            handItem.`is`(Items.WRITABLE_BOOK) ->
                handItem.get(DataComponents.WRITABLE_BOOK_CONTENT).safelyToUnifiedBook()

            handItem.`is`(Items.WRITTEN_BOOK) ->
                handItem.get(DataComponents.WRITTEN_BOOK_CONTENT).safelyToUnifiedBook()

            else -> {
                val errorMessage = Component.literal("Must hold a book and quill or written book")
                throw SimpleCommandExceptionType(errorMessage).create()
            }
        }

        export(
            context = context,
            book = unifiedBook,
            destination = FileUtils.getBookSavePath().resolve(fileName),
            unifiedBookIo = ioFormat.unifiedBookIo,
        )

        return Command.SINGLE_SUCCESS
    }

    @Throws(CommandSyntaxException::class)
    private fun WritableBookContent?.safelyToUnifiedBook(): UnifiedBook {
        if (this == null || this.pages().isEmpty()) {
            val errorMessage: Message = Component.literal("Book is empty")
            throw SimpleCommandExceptionType(errorMessage).create()
        }
        return unifiedBookFactory.create(this)
    }

    @Throws(CommandSyntaxException::class)
    private fun WrittenBookContent?.safelyToUnifiedBook(): UnifiedBook {
        if (this == null || this.pages().isEmpty()) {
            val errorMessage: Message = Component.literal("Book is empty")
            throw SimpleCommandExceptionType(errorMessage).create()
        }
        return unifiedBookFactory.create(this)
    }

    @Throws(CommandSyntaxException::class)
    private fun export(
        context: CommandContext<FabricClientCommandSource>,
        unifiedBookIo: UnifiedBookIo,
        destination: Path,
        book: UnifiedBook,
    ) {
        try {
            unifiedBookIo.write(book, destination)
            context.source.sendFeedback(Component.literal("Book saved to ${destination.fileName}"))
        } catch (exception: IOException) {
            val errorMessage = Component.literal(
                "Failed saving book to file (an error occurred while saving, please check your Minecraft logs)"
            )
            BookCopy.LOGGER.error("Failed saving book file!", exception)
            throw SimpleCommandExceptionType(errorMessage).create()
        }
    }
}
