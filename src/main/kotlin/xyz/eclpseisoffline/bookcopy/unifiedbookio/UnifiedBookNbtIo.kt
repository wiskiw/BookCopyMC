package xyz.eclpseisoffline.bookcopy.unifiedbookio

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclpseisoffline.bookcopy.model.UnifiedBook
import java.io.IOException
import java.nio.file.Path

class UnifiedBookNbtIo : UnifiedBookIo {

    @Throws(IOException::class)
    override fun write(book: UnifiedBook, destination: Path) {
        val bookNbt = createBookNbt(unifiedBook = book)
        NbtIo.write(bookNbt, destination)
    }

    private fun createBookNbt(
        unifiedBook: UnifiedBook,
    ): CompoundTag {
        val pagesTag = ListTag()
        unifiedBook.pages.forEach { pageText ->
            pagesTag.add(StringTag.valueOf(pageText))
        }

        return CompoundTag().apply {
            put("pages", pagesTag)
        }
    }

    @Throws(IOException::class)
    override fun read(source: Path): UnifiedBook {
        try {
            val bookNbt = NbtIo.read(source)
            if (bookNbt == null) {
                val errorMessage = Component.literal("Failed reading book file (no NBT data found)")
                throw SimpleCommandExceptionType(errorMessage).create()
            }
            return parseBookNbt(bookNbt)

        } catch (exception: IOException) {
            val errorMessage = Component.literal(
                "Failed reading book file (an error occurred while reading, please check your Minecraft logs)"
            )
            BookCopy.LOGGER.error("Failed reading book file!", exception)
            throw SimpleCommandExceptionType(errorMessage).create()
        }
    }

    private fun parseBookNbt(bookNbt: CompoundTag): UnifiedBook {
        val pages = bookNbt.getList("pages", Tag.TAG_STRING.toInt()).map { it.asString }

        return UnifiedBook(
            title = null,
            author = null,
            pages = pages,
        )
    }
}