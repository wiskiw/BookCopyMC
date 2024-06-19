package xyz.eclpseisoffline.bookcopy.universalbookcontentio

import xyz.eclpseisoffline.bookcopy.model.UniversalBookContent
import java.io.IOException
import java.nio.file.Path

interface UniversalBookContentIo {

    @Throws(IOException::class)
    fun write(book: UniversalBookContent, destination: Path)


    @Throws(IOException::class)
    fun read(source: Path): UniversalBookContent
}
