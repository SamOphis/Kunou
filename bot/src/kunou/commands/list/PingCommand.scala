package kunou.commands.list

import kunou.commands.{Context, Command}
import ackcord.requests._

class PingCommand extends Command {
  override def name: String = "ping"

  override def execute(context: Context): Unit = context match {
    case Context(bot, message, cacheSnapshot, _, _) =>
      bot.discordClient.requestsHelper.run(CreateMessage.mkContent(message.channelId, "Pong!"))(cacheSnapshot)
  }
}
