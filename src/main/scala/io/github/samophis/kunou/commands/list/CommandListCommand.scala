package io.github.samophis.kunou.commands.list

import com.mewna.catnip.entity.message.Message
import io.github.samophis.kunou.commands.Command
import io.github.samophis.kunou.commands.CommandCategory.CommandCategory
import io.github.samophis.kunou.startup.Kunou

import scala.collection.mutable

class CommandListCommand extends Command {
  override val name: String = "commandlist"
  override lazy val description: String = "Generates a list of all Kunou's commands, sorted by category."
  override val aliases: Set[String] = Set("cmdlist")

  private[this] var commandList: mutable.Set[(CommandCategory, String)] = _

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    import io.github.samophis.kunou.commands.okResponseBase

    if (commandList == null) {
      initializeCommandList(bot, prefix)
    }

    val builder = okResponseBase(message)
      .author("Kunou Command List", null, bot.catnip.selfUser.effectiveAvatarUrl)
      .description(s"A list of all of Kunou's commands, sorted by category. Use `${prefix}help <command>` to find more " +
        "information about a command.")

    commandList.foreach {
      case (category, commands) => builder.field(category.toString, commands, false)
    }

    message.channel.sendMessage(builder.build)
  }

  private[this] def initializeCommandList(bot: Kunou, prefix: String): Unit = {
    commandList = new mutable.HashSet
    bot.commandManager.commands.values.map(_.category).foreach(category => {
      val commandsSorted = bot.commandManager.commands
        .values
        .toSet // this is to remove multiple occurrences of the same command (aliases)
        .filter(_.category == category)
        .map(command => s"`$prefix${command.name}`")
        .mkString(", ")
      commandList += ((category, commandsSorted))
    })
  }
}
