package kunou.startup

import ackcord.{ClientSettings, DiscordClient}
import com.redis.RedisClient
import kunou.commands.Manager

import scala.language.postfixOps

case class Kunou(clientSettings: ClientSettings, discordClient: DiscordClient, redisClient: RedisClient) {
  val defaultCommandPrefix: String = sys.env.getOrElse("KUNOU_DEFAULT_COMMAND_PREFIX", "k->")
  val commandManager: Manager = Manager(this)

  def startup(): Unit = {
    commandManager.registerCommandFiles()
    commandManager.startListening()
    discordClient.login()
  }
}
