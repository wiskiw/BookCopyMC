package xyz.eclpseisoffline.bookcopy

import net.minecraft.network.chat.Component
import net.minecraft.server.network.Filterable
import net.minecraft.world.item.component.WritableBookContent
import net.minecraft.world.item.component.WrittenBookContent
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclpseisoffline.bookcopy.model.UnifiedBook

class UnifiedBookFactory {

    fun create(writableContent: WritableBookContent): UnifiedBook {
        val pages = writableContent.pages()
            .mapNotNull { page -> getWritableBookPageText(page) }
            .toList()

        return UnifiedBook(
            title = null,
            author = null,
            pages = pages,
        )
    }

    private fun getWritableBookPageText(page: Filterable<String>): String? {
        val notFiltered: Any = page.get(false)
        return when (notFiltered) {
            is String -> notFiltered
            else -> {
                logUnexpectedPagesType(notFiltered)
                null
            }
        }
    }

    fun create(writtenContent: WrittenBookContent): UnifiedBook {
        val pages = writtenContent.pages()
            .mapNotNull { page -> getWrittenBookPageText(page) }
            .toList()

        return UnifiedBook(
            title = writtenContent.title.get(false),
            author = writtenContent.author,
            pages = pages,
        )
    }

    private fun getWrittenBookPageText(page: Filterable<Component>): String? {
        val notFiltered: Any = page.get(false)
        return when (notFiltered) {
            is Component -> notFiltered.string
            else -> {
                logUnexpectedPagesType(notFiltered)
                null
            }
        }
    }

    private fun logUnexpectedPagesType(notFiltered: Any) {
        BookCopy.LOGGER.warn(
            "Found unexpected page type '${notFiltered.javaClass}'!" +
                    " If you are not a developer, report this issue on the issue tracker at Github",
        )
    }
}
