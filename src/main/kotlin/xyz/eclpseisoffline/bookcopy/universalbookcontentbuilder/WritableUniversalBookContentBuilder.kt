package xyz.eclpseisoffline.bookcopy.universalbookcontentbuilder

import net.minecraft.server.network.Filterable
import net.minecraft.world.item.component.WritableBookContent
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclpseisoffline.bookcopy.model.UniversalBookContent

class WritableUniversalBookContentBuilder(
    private val writableContent: WritableBookContent,
) : UniversalBookContentBuilder {

    override fun build(): UniversalBookContent {
        val pages = writableContent.pages()
            .mapNotNull { page -> getPageText(page) }
            .toList()

        return UniversalBookContent(
            title = null,
            author = null,
            pages = pages,
        )
    }

    private fun getPageText(page: Filterable<String>): String? {
        val notFiltered: Any = page.get(false)
        return when (notFiltered) {
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
}
