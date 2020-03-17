package io.github.samophis.kunou.commands.list.general.prefix

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission
import io.github.samophis.kunou.commands.{Command, okResponseBase, warningResponseBase}
import io.github.samophis.kunou.startup.Kunou

class ResetPrefixCommand extends Command {
  override val name: String = "resetprefix"
  override lazy val description: String = "Resets the prefix of this server to the global default."
  override val aliases: Set[String] = Set("deleteprefix", "delprefix")
  override val requiredUserPermissions: List[Permission] = List(Permission.ADMINISTRATOR)

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    bot.redisClient.hget(message.guildId, "_prefix") match {
      case Some(_) =>
        bot.redisClient.hdel(message.guildId, "_prefix")
        val response = okResponseBase(message)
          .description(s"Successfully removed this server's custom prefix. It is now `${bot.defaultCommandPrefix}`.")
          .build
        message.channel.sendMessage(response)
      case None =>
        val response = warningResponseBase(message)
          .description("This server already has no custom prefix. Nothing has been changed.")
          .build
        message.channel.sendMessage(response)
    }
  }
}
