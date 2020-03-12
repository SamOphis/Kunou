package io.github.samophis.kunou.commands.list

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission
import io.github.samophis.kunou.commands.{Command, okResponseBase}
import io.github.samophis.kunou.startup.Kunou

class HelpCommand extends Command {
  override val name: String = "help"

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    import io.github.samophis.kunou.commands._

    val args = message.content().split("\\s+")
    if (args.length < 2) {
      sendHelpMessage(bot, message, prefix)
      return
    }

    bot.commandManager.commands.get(args(1).toLowerCase.replace(prefix, "")) match {
      case Some(command) =>
        val builder = okResponseBase(message)
          .title(s"$prefix${command.name} Command")
          .description(command.description)
          .field("Usage", s"$prefix${command.name}", true)
          .field("Category", command.category.toString, true)
          .thumbnail(bot.catnip.selfUser.effectiveAvatarUrl)

        command.aliases match {
          case aliases if aliases.isEmpty =>
          case aliases =>
            val asString = aliases.mkString("\n")
            builder.field("Aliases", asString, true)
        }

        command.requiredUserPermissions match {
          case perms if perms.isEmpty =>
          case perms =>
            val asString = perms.map(_.permName).mkString(", ")
            builder.field("Required User Permissions", asString, false)
        }

        command.requiredBotPermissions match {
          case List(Permission.SEND_MESSAGES) =>
          case perms =>
            val asString = perms.map(_.permName).mkString(", ")
            builder.field("Required Bot Permissions", asString, false)
        }

        message.channel.sendMessage(builder.build)
      case None =>
        sendHelpMessage(bot, message, prefix)
    }

  }

  private[this] def sendHelpMessage(bot: Kunou, message: Message, prefix: String): Unit = {
    val helpText =
      s"""
         |Kunou is a small bot made mostly for fun and music!
         |You can find the source code, server link and the invite link in the blue links.
         |
         |Please say "<@${bot.userId}> prefix" (without quotes) to find Kunou's prefix for the current server, which is `$prefix` as of now.
         |
         |The default prefix globally is `${bot.defaultCommandPrefix}`, but please remember that server admins
         |can change Kunou's default prefix for their specific server using the k->prefix command.
         |
         |You can find a list of commands with the k->cmdlist command. Thank you for using Kunou!
         |""".stripMargin
    val embed = okResponseBase(message).title("Introduction").description(helpText).build()
    message.channel.sendMessage(embed)
  }
}
