package xyz.eclpseisoffline.bookcopy.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundEditBookPacket
import net.minecraft.world.item.Items
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclipseisoffline.bookcopy.BookSuggestionProvider
import xyz.eclipseisoffline.bookcopy.FileUtils
import java.io.IOException
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

        val bookNBT = importBookNbt(name)
        val pages = bookNBT["pages"] as ListTag?
        if (pages == null) {
            val errorMessage = Component.literal("Failed reading book file (no page content found)")
            throw SimpleCommandExceptionType(errorMessage).create()
        }

        val slot = context.source.player.inventory.selected
        val pageStrings = pages.stream()
            .map { obj: Tag -> obj.asString }
            .toList()
        context.source.player.connection.send(
            ServerboundEditBookPacket(slot, pageStrings, Optional.empty())
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

    @Throws(CommandSyntaxException::class)
    private fun importBookNbt(name: String): CompoundTag {
        try {
            val bookNBT = NbtIo.read(FileUtils.getBookSavePath().resolve(name))
            if (bookNBT == null) {
                val errorMessage = Component.literal("Failed reading book file (no NBT data found)")
                throw SimpleCommandExceptionType(errorMessage).create()
            }
            return bookNBT

        } catch (exception: IOException) {
            val errorMessage = Component.literal(
                "Failed reading book file (an error occurred while reading, please check your Minecraft logs)"
            )
            BookCopy.LOGGER.error("Failed reading book file!", exception)
            throw SimpleCommandExceptionType(errorMessage).create()
        }
    }
}
