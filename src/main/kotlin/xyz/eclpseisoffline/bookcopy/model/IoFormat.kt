package xyz.eclpseisoffline.bookcopy.model

import xyz.eclpseisoffline.bookcopy.universalbookcontentio.UniversalBookContentIo
import xyz.eclpseisoffline.bookcopy.universalbookcontentio.UniversalBookContentJsonIo
import xyz.eclpseisoffline.bookcopy.universalbookcontentio.UniversalBookContentNbtIo

enum class IoFormat(
    val flag: String,
    val universalBookContentIo: UniversalBookContentIo,
) {
    NBT(
        flag = "-nbt",
        universalBookContentIo = UniversalBookContentNbtIo(),
    ),
    JSON(
        flag = "-json",
        universalBookContentIo = UniversalBookContentJsonIo(),
    )
    ;

    companion object {
        @Throws(IllegalArgumentException::class)
        fun fromFlag(flag: String): IoFormat = IoFormat.entries
            .firstOrNull {
                it.flag == flag
            }
            ?: run {
                throw IllegalArgumentException(
                    "Unknown IO format flag '${flag}'." +
                            " Please use one of the following: ${IoFormat.allFlags()}"
                )
            }

        fun allFlags(): List<String> = IoFormat.entries.map { it.flag }
    }
}
