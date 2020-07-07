package kunou.startup

import ackcord.ClientSettings
import com.redis.RedisClient
import com.typesafe.scalalogging.Logger


object Main extends App {
  Logger("ErrorTest").error("An error occurred while starting up!", new RuntimeException)
  val discordClientSettings = ClientSettings(sys.env("KUNOU_BOT_TOKEN"))
  val redisClient = new RedisClient("localhost", 6379)

  import discordClientSettings.executionContext
  discordClientSettings.createClient().foreach(discordClient => {
    val bot = Kunou(discordClientSettings, discordClient, redisClient)
    bot.startup()
  })
}