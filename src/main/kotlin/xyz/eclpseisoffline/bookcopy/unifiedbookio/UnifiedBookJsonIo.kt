package xyz.eclpseisoffline.bookcopy.unifiedbookio

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclpseisoffline.bookcopy.model.UnifiedBook
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class UnifiedBookJsonIo : UnifiedBookIo {

    companion object {
        private const val REGEX_SPLIT_PAGE_LINES = """^.*?(?=\n|${'$'})${'$'}|(?<=\n)${'$'}"""
    }

    @Throws(IOException::class)
    override fun write(book: UnifiedBook, destination: Path) {
        val jsonModelBook = createJsonBookModel(book)
        val bookJson = Json.encodeToString(JsonBookModel.serializer(), jsonModelBook)
        Files.write(destination, bookJson.toByteArray(StandardCharsets.UTF_8))
    }

    private fun createJsonBookModel(
        unifiedBook: UnifiedBook,
    ): JsonBookModel {
        val jsonModelPages = unifiedBook.pages
            .map { pageText ->
                val lines = REGEX_SPLIT_PAGE_LINES.toRegex(RegexOption.MULTILINE)
                    .findAll(pageText)
                    .map { matchResult -> matchResult.value }
                    .toList()

                return@map lines
            }

        return JsonBookModel(
            title = unifiedBook.title,
            author = unifiedBook.author,
            pages = jsonModelPages,
        )
    }

    @Throws(IOException::class)
    override fun read(source: Path): UnifiedBook {
        try {
            val jsonBookModelJson = Files.readString(source, StandardCharsets.UTF_8)
            if (jsonBookModelJson == null) {
                val errorMessage = Component.literal("Failed reading book file (no JSON data found)")
                throw SimpleCommandExceptionType(errorMessage).create()
            }
            val jsonBookModel = Json.decodeFromString<JsonBookModel>(jsonBookModelJson)
            return parseJsonBookModel(jsonBookModel)

        } catch (exception: IOException) {
            val errorMessage = Component.literal(
                "Failed reading book file (an error occurred while reading, please check your Minecraft logs)"
            )
            BookCopy.LOGGER.error("Failed reading book file!", exception)
            throw SimpleCommandExceptionType(errorMessage).create()
        }
    }

    private fun parseJsonBookModel(jsonBookModel: JsonBookModel): UnifiedBook {
        val joinedPages = jsonBookModel.pages
            .map { lines ->
                lines.joinToString("\n")
            }

        return UnifiedBook(
            title = jsonBookModel.title,
            author = jsonBookModel.author,
            pages = joinedPages,
        )
    }

    @Serializable
    data class JsonBookModel(
        @SerialName("title")
        val title: String? = null,

        @SerialName("author")
        val author: String? = null,

        @SerialName("pages")
        val pages: List<List<String>>
    )
}
