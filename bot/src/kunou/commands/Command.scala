package kunou.commands

import ackcord.data.{GuildGatewayMessage, Permission, SparseMessage}
import kunou.commands.Category.Category

trait Command {
  def name: String

  def description: String = "No available description."

  def usage: String = name

  def category: Category = Category.General

  def requiredUserPermissions: Set[Permission] = Set.empty

  def requiredBotPermissions: Set[Permission] = Set(Permission.SendMessages)

  def requiredPreconditions: Set[Context => Boolean] = Set.empty

  def aliases: Set[String] = Set.empty

  def execute(implicit context: Context): Unit

  final def executeWithChecks(implicit context: Context): Unit = {
    for (precondition <- requiredPreconditions) {
      if (!precondition(context)) {
        return
      }
    }

    execute(context)
  }
}

abstract case class GuildCommand() extends Command {
  override def requiredPreconditions: Set[Context => Boolean] = Set(_.message.isInstanceOf[GuildGatewayMessage])
}

abstract case class DMCommand() extends Command {
  override def requiredPreconditions: Set[Context => Boolean] = Set(_.message.isInstanceOf[SparseMessage])
}

object Command {
  // Short-hand syntax for creating runtime commands.
  // The default commands are registered via reflection upon startup.
  def apply(commandName: String, function: Context => Unit): Command = new Command {
    override def name: String = commandName

    override def execute(implicit context: Context): Unit = function(context)
  }
}
