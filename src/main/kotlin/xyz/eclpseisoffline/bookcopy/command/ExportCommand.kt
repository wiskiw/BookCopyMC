package xyz.eclpseisoffline.bookcopy.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.BookContent
import net.minecraft.world.item.component.WritableBookContent
import net.minecraft.world.item.component.WrittenBookContent
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclipseisoffline.bookcopy.BookSuggestionProvider
import xyz.eclipseisoffline.bookcopy.FileUtils
import xyz.eclpseisoffline.bookcopy.command.argumenttype.IoFormatArgumentType
import xyz.eclpseisoffline.bookcopy.model.IoFormat
import xyz.eclpseisoffline.bookcopy.universalbookcontentbuilder.WritableUniversalBookContentBuilder
import xyz.eclpseisoffline.bookcopy.universalbookcontentbuilder.WrittenUniversalBookContentBuilder
import xyz.eclpseisoffline.bookcopy.universalbookcontentio.UniversalBookContentIo
import java.io.IOException
import java.nio.file.Path

class ExportCommand {

    private object Args {
        const val FILE_NAME = "file_name"
        const val FORMAT_FLAG = "format"
    }

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
        val destination = FileUtils.getBookSavePath().resolve(fileName)
        when {
            handItem.`is`(Items.WRITABLE_BOOK) -> {
                tryExportWritableBookContent(
                    content = handItem.get(DataComponents.WRITABLE_BOOK_CONTENT),
                    destination = destination,
                    universalBookContentIo = ioFormat.universalBookContentIo,
                )
            }

            handItem.`is`(Items.WRITTEN_BOOK) -> {
                tryExportWrittenBookContent(
                    content = handItem.get(DataComponents.WRITTEN_BOOK_CONTENT),
                    destination = destination,
                    universalBookContentIo = ioFormat.universalBookContentIo,
                )
            }

            else -> {
                val errorMessage = Component.literal("Must hold a book and quill or written book")
                throw SimpleCommandExceptionType(errorMessage).create()
            }
        }

        context.source.sendFeedback(
            Component.literal("Saved book to file")
        )

        return Command.SINGLE_SUCCESS
    }

    private fun tryExportWritableBookContent(
        content: WritableBookContent?,
        destination: Path,
        universalBookContentIo: UniversalBookContentIo,
    ) {
        if (content == null || content.pages().isEmpty()) {
            val errorMessage: Message = Component.literal("Book has no content")
            throw SimpleCommandExceptionType(errorMessage).create()
        }

        val bookContent = WritableUniversalBookContentBuilder(content).build()

        try {
            universalBookContentIo.write(bookContent, destination)
        } catch (exception: IOException) {
            val errorMessage = Component.literal(
                "Failed saving book to file (an error occurred while saving, please check your Minecraft logs)"
            )
            BookCopy.LOGGER.error("Failed saving book file!", exception)
            throw SimpleCommandExceptionType(errorMessage).create()
        }
    }

    private fun tryExportWrittenBookContent(
        content: WrittenBookContent?,
        destination: Path,
        universalBookContentIo: UniversalBookContentIo,
    ) {
        if (content == null || content.pages().isEmpty()) {
            val errorMessage: Message = Component.literal("Book has no content")
            throw SimpleCommandExceptionType(errorMessage).create()
        }

        val bookContent = WrittenUniversalBookContentBuilder(content).build()

        try {
            universalBookContentIo.write(bookContent, destination)
        } catch (exception: IOException) {
            val errorMessage = Component.literal(
                "Failed saving book to file (an error occurred while saving, please check your Minecraft logs)"
            )
            BookCopy.LOGGER.error("Failed saving book file!", exception)
            throw SimpleCommandExceptionType(errorMessage).create()
        }
    }
}
