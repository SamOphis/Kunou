package io.github.samophis.kunou.commands

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission
import io.github.samophis.kunou.commands.CommandCategory.CommandCategory
import io.github.samophis.kunou.startup.Kunou

trait Command {
  val name: String
  // This field is lazy as some commands (read: EmoteCommand as of 2020/03/12) rely on initially null values.
  lazy val description: Option[String] = None

  val usage: String = name
  // These permission lists are lists, not sets, so that we can inter-op with Catnip seamlessly.
  // There are probably hacks to convert a Scala set to Java var-args, but I am not aware of such things at the moment.
  // The aliases set is a set because there is no point where I have to use it in a Java library.
  val requiredUserPermissions: List[Permission] = List.empty
  val requiredBotPermissions: List[Permission] = List(Permission.SEND_MESSAGES)
  val category: CommandCategory = CommandCategory.General
  val aliases: Set[String] = Set.empty

  def execute(bot: Kunou, message: Message, prefix: String): Unit = ()
}

object Command {
  def apply(name: String, function: (Kunou, Message, String) => Unit): Command = new MyCommand(name, function)

  private class MyCommand(val name: String, function: (Kunou, Message, String) => Unit) extends Command {
    override def execute(bot: Kunou, message: Message, prefix: String): Unit = function(bot, message, prefix)
  }
}
