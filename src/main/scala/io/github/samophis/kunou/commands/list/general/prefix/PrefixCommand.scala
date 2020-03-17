package io.github.samophis.kunou.commands.list.general.prefix

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission
import io.github.samophis.kunou.commands.CommandCategory.CommandCategory
import io.github.samophis.kunou.commands.{Command, CommandCategory}
import io.github.samophis.kunou.startup.Kunou

class PrefixCommand extends Command {
  override val name: String = "prefix"
  override lazy val description: String = "Modifies Kunou's local prefix for this server."
  override val requiredUserPermissions: List[Permission] = List(Permission.ADMINISTRATOR)
  override val category: CommandCategory = CommandCategory.General

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    import io.github.samophis.kunou.commands.{okResponseBase, warningResponseBase}

    val args = message.content.split("\\s+")
    if (args.length < 2) {
      val warning = warningResponseBase(message)
        .description("You need to provide a prefix.")
        .build
      message.channel.sendMessage(warning)
      return
    }

    val prefix = args(1) match {
      case arg if arg.length > 20 =>
        val warning = warningResponseBase(message)
          .description("Prefixes can't be longer than 20 characters. Sorry about that.")
          .build
        message.channel.sendMessage(warning)
        return
      case arg if arg.contains("`") =>
        val warning = warningResponseBase(message)
          .description("Prefixes can't contain the ` character. Sorry about that.")
          .build
        message.channel.sendMessage(warning)
        return
      case arg => arg
    }

    bot.redisClient.hset(message.guildId, "_prefix", prefix)
    val response = okResponseBase(message)
      .description(s"Successfully set the prefix to `$prefix`.")
      .build
    message.channel.sendMessage(response)
  }
}
