package kunou.commands

import java.util.concurrent.Executors

import ackcord.APIMessage
import ackcord.data.{GuildGatewayMessage, Message}
import io.github.classgraph.ClassGraph
import kunou.startup.Kunou

import scala.collection.mutable
import scala.concurrent.ExecutionContext

case class Manager(bot: Kunou) {
  // This collection is mutable so as to allow for the creation of runtime commands.
  private val commands = mutable.Map[String, Command]()

  // The execution context where commands are executed.
  private val commandExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def registerCommand(command: Command): Unit = commands.put(command.name, command)

  def registerCommandWithAliases(command: Command): Unit = {
    registerCommand(command)
    command.aliases.foreach(alias => commands.put(alias, command))
  }

  def registerCommandFiles(): Unit = {
    val scanResult = new ClassGraph()
      .acceptPackages("kunou.commands.list")
      .scan()

    try {
      scanResult.getClassesImplementing("kunou.commands.Command")
        .stream()
        .filter(!_.isAbstract)
        .map[Command](_.loadClass(false).getDeclaredConstructor().newInstance().asInstanceOf[Command])
        .forEach(registerCommand)
    } finally {
      scanResult.close()
    }
  }

  def startListening(): Unit = {
    bot.discordClient.onEventSideEffects { implicit cacheSnapshot => {
        case APIMessage.MessageCreate(message, _) =>
          // This is written without returning early, because of uncaught non-local returns exceptions.
          // TODO: Find a way to go back to the cleaner method :(

          val content = message.content
          val prefix = fetchCommandPrefix(message)

          if (content.startsWith(prefix)) {
            val arguments = content.split("\\s+")

            if (arguments.nonEmpty) {
              val firstArgument = arguments(0).replace(prefix, "")
              commands.get(firstArgument) match {
                case Some(command) =>
                  val context = Context(bot, message, cacheSnapshot, arguments.toList, prefix)
                  commandExecutionContext.execute(() => command.executeWithChecks(context))

                case _ =>
              }
            }
          }
        case _ =>
      }
    }
  }

  def fetchCommandPrefix(message: Message): String = message match {
    case guildMessage: GuildGatewayMessage => bot.redisClient
      .hget(guildMessage.guildId, "_prefix")
      .getOrElse(bot.defaultCommandPrefix)

    case _ => bot.defaultCommandPrefix
  }
}
