package xyz.eclpseisoffline.bookcopy.universalbookcontentio

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclpseisoffline.bookcopy.model.UniversalBookContent
import java.io.IOException
import java.nio.file.Path

class UniversalBookContentNbtIo : UniversalBookContentIo {

    @Throws(IOException::class)
    override fun write(book: UniversalBookContent, destination: Path) {
        val bookNbt = createBookNbt(universalBookContent = book)
        NbtIo.write(bookNbt, destination)
    }

    private fun createBookNbt(
        universalBookContent: UniversalBookContent,
    ): CompoundTag {
        val pagesTag = ListTag()
        universalBookContent.pages.forEach { pageText ->
            pagesTag.add(StringTag.valueOf(pageText))
        }

        return CompoundTag().apply {
            put("pages", pagesTag)
        }
    }

    @Throws(IOException::class)
    override fun read(source: Path): UniversalBookContent {
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

    private fun parseBookNbt(bookNbt: CompoundTag): UniversalBookContent {
        val pages = bookNbt.getList("pages", Tag.TAG_STRING.toInt()).map { it.asString }

        return UniversalBookContent(
            title = null,
            author = null,
            pages = pages,
        )
    }
}