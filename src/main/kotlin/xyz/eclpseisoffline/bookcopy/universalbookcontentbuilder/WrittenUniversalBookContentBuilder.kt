package xyz.eclpseisoffline.bookcopy.universalbookcontentbuilder

import net.minecraft.network.chat.Component
import net.minecraft.server.network.Filterable
import net.minecraft.world.item.component.WrittenBookContent
import xyz.eclipseisoffline.bookcopy.BookCopy
import xyz.eclpseisoffline.bookcopy.model.UniversalBookContent

class WrittenUniversalBookContentBuilder(
    private val writtenContent: WrittenBookContent,
) : UniversalBookContentBuilder {

    override fun build(): UniversalBookContent {
        val pages = writtenContent.pages()
            .mapNotNull { page -> getPageText(page) }
            .toList()

        return UniversalBookContent(
            title = writtenContent.title.get(false),
            author = writtenContent.author,
            pages = pages,
        )
    }

    private fun getPageText(page: Filterable<Component>): String? {
        val notFiltered: Any = page.get(false)
        return when (notFiltered) {
            is Component -> notFiltered.string
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
