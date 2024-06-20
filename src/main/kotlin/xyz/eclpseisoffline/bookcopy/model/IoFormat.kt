package xyz.eclpseisoffline.bookcopy.model

import xyz.eclpseisoffline.bookcopy.unifiedbookio.UnifiedBookIo
import xyz.eclpseisoffline.bookcopy.unifiedbookio.UnifiedBookJsonIo
import xyz.eclpseisoffline.bookcopy.unifiedbookio.UnifiedBookNbtIo

enum class IoFormat(
    val flag: String,
    val unifiedBookIo: UnifiedBookIo,
) {
    NBT(
        flag = "-nbt",
        unifiedBookIo = UnifiedBookNbtIo(),
    ),
    JSON(
        flag = "-json",
        unifiedBookIo = UnifiedBookJsonIo(),
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
