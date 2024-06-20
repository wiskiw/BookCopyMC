package xyz.eclpseisoffline.bookcopy.unifiedbookio

import xyz.eclpseisoffline.bookcopy.model.UnifiedBook
import java.io.IOException
import java.nio.file.Path

interface UnifiedBookIo {

    @Throws(IOException::class)
    fun write(book: UnifiedBook, destination: Path)


    @Throws(IOException::class)
    fun read(source: Path): UnifiedBook
}
