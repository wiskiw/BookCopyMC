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
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.server.network.Filterable
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.BookContent
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclipseisoffline.bookcopy.BookSuggestionProvider
import xyz.eclipseisoffline.bookcopy.FileUtils
import java.io.IOException

class ExportCommand {

    private object Args {
        const val NAME = "name"
    }

    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommandManager.literal("bookcopy")
            .then(
                ClientCommandManager.literal("export")
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
        val book = getPlayerReadableBook(context)
        val bookContent: BookContent<*, *> = getBookContent(book)
        val bookNbt = createBookNbt(bookContent)

        try {
            NbtIo.write(bookNbt, FileUtils.getBookSavePath().resolve(name))
        } catch (exception: IOException) {
            val errorMessage = Component.literal(
                "Failed saving book to file (an error occurred while saving, please check your Minecraft logs)"
            )
            BookCopy.LOGGER.error("Failed saving book file!", exception)
            throw SimpleCommandExceptionType(errorMessage).create()
        }

        context.source.sendFeedback(
            Component.literal("Saved book to file")
        )

        return Command.SINGLE_SUCCESS
    }

    @Throws(CommandSyntaxException::class)
    private fun getPlayerReadableBook(context: CommandContext<FabricClientCommandSource>): ItemStack {
        val book = context.source.player.mainHandItem
        if (!book.`is`(Items.WRITABLE_BOOK) && !book.`is`(Items.WRITTEN_BOOK)) {
            val errorMessage = Component.literal("Must hold a book and quill or written book")
            throw SimpleCommandExceptionType(errorMessage).create()
        }
        return book
    }

    @Throws(CommandSyntaxException::class)
    private fun getBookContent(book: ItemStack): BookContent<*, *> {
        val bookContent: BookContent<*, *>? = if (book.`is`(Items.WRITABLE_BOOK)) {
            book.get(DataComponents.WRITABLE_BOOK_CONTENT)
        } else {
            book.get(DataComponents.WRITTEN_BOOK_CONTENT)
        }

        if (bookContent == null || bookContent.pages().isEmpty()) {
            val errorMessage: Message = Component.literal("Book has no content")
            throw SimpleCommandExceptionType(errorMessage).create()
        }

        return bookContent
    }

    private fun createBookNbt(
        bookContent: BookContent<*, *>,
    ): CompoundTag {
        val pages: List<*> = bookContent.pages()
        val pagesTag = ListTag()
        for (page in pages) {
            val pageString = getPageText(page) ?: continue
            pagesTag.add(StringTag.valueOf(pageString))
        }

        return CompoundTag().apply {
            put("pages", pagesTag)
        }
    }

    private fun getPageText(page: Any?): String? {
        return when (page) {
            is Filterable<*> -> {
                val notFiltered: Any = page.get(false)

                when (notFiltered) {
                    is Component -> notFiltered.string
                    is String -> notFiltered
                    else -> {
                        BookCopy.LOGGER.warn(
                            "Found unexpected filtered page type {}! If you are not a developer, report this issue on the issue tracker at Github",
                            notFiltered.javaClass
                        )
                        null
                    }
                }
            }

            is Component -> page.string
            is String -> page
            else -> {
                BookCopy.LOGGER.warn(
                    "Found unexpected page type {}! If you are not a developer, report this issue on the issue tracker at Github",
                    page?.javaClass
                )
                null
            }
        }
    }
}
