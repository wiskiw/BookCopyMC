package xyz.eclpseisoffline.bookcopy.unifiedbookio

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclpseisoffline.bookcopy.model.UnifiedBook
import java.io.IOException
import java.nio.file.Path

@Suppress("RedundantCompanionReference")
class UnifiedBookNbtIo : UnifiedBookIo {

    private companion object NbtField {
        private const val TITLE = "title"
        private const val AUTHOR = "author"
        private const val PAGES = "pages"
    }

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
            unifiedBook.title?.let { put(NbtField.TITLE, StringTag.valueOf(it)) }
            unifiedBook.author?.let { put(NbtField.AUTHOR, StringTag.valueOf(it)) }
            put(NbtField.PAGES, pagesTag)
        }
    }

    @Throws(IOException::class)
    override fun read(source: Path): UnifiedBook {
        try {
            val bookNbt = NbtIo.read(source)
            if (bookNbt == null) {
                val errorMessage = Component.literal("Failed reading book in '${source.fileName}' (no NBT data found)")
                throw SimpleCommandExceptionType(errorMessage).create()
            }
            return parseBookNbt(bookNbt)

        } catch (exception: IOException) {
            val errorMessage = Component.literal(
                "Failed reading '${source.fileName}'"
            )
            BookCopy.LOGGER.error("Failed reading book file!", exception)
            throw SimpleCommandExceptionType(errorMessage).create()
        }
    }

    private fun parseBookNbt(bookNbt: CompoundTag): UnifiedBook {
        val title = bookNbt.getString(NbtField.TITLE).replaceEmptyWithNull()
        val author = bookNbt.getString(NbtField.AUTHOR).replaceEmptyWithNull()
        val pages = bookNbt.getList(NbtField.PAGES, Tag.TAG_STRING.toInt()).map { it.asString }

        return UnifiedBook(
            title = title,
            author = author,
            pages = pages,
        )
    }

    private fun String.replaceEmptyWithNull(): String? = this.ifEmpty { null }
}