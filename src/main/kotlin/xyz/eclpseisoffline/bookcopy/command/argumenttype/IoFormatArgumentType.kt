package xyz.eclpseisoffline.bookcopy.command.argumenttype

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import xyz.eclpseisoffline.bookcopy.model.IoFormat
import java.util.*
import java.util.concurrent.CompletableFuture


class IoFormatArgumentType private constructor() : ArgumentType<IoFormat> {

    companion object {
        fun ioFormat() = IoFormatArgumentType()

        fun <T> getIoFormat(context: CommandContext<T>, name: String): IoFormat =
            context.getArgument(name, IoFormat::class.java)
    }

    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): IoFormat {
        val ioFormatFlag = reader.readString()

        try {
            return IoFormat.fromFlag(ioFormatFlag)
        } catch (ex: IllegalArgumentException) {
            throw SimpleCommandExceptionType(LiteralMessage(ex.message)).create()
        }
    }

    override fun <S : Any?> listSuggestions(
        commandContext: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> = SharedSuggestionProvider.suggest(IoFormat.allFlags(), builder)
}
