package io.github.samophis.kunou.startup

import com.github.natanbc.weeb4j.{TokenType, Weeb4J}
import com.mewna.catnip.shard.DiscordEvent
import com.mewna.catnip.{Catnip, CatnipOptions}
import com.redis.RedisClient
import com.typesafe.scalalogging.Logger
import io.github.samophis.kunou.commands.CommandManager
import io.sentry.Sentry

class Kunou {
  private[this] val token = sys.env("KUNOU_BOT_TOKEN")
  private[this] val logger = Logger[Kunou]
  private[this] val catnipOptions = new CatnipOptions(token)
    .validateToken(true)

  val catnip: Catnip = Catnip.catnip(catnipOptions)
  val ownerId: Long = sys.env("KUNOU_OWNER_ID").toLong
  val defaultCommandPrefix: String = sys.env.get("KUNOU_DEFAULT_PREFIX") match {
    case None => "k."
    case Some(default) => default
  }
  // Database Stuff
  val redisClient = new RedisClient("localhost", 6379) // Kunou runs Redis.

  // Weeb4J - Optional
  val weeb4JOption: Option[Weeb4J] = sys.env.get("KUNOU_WEEBSH_TOKEN").map {
    new Weeb4J.Builder()
      .setToken(TokenType.WOLKE, _)
      .build()
  }
  private[this] val commandManager = new CommandManager(this)

  // There is a warning here (can convert to method value), but Catnip doesn't like it.
  // +1 for Java interoperability, Scala.
  catnip.observable(DiscordEvent.MESSAGE_CREATE).subscribe(commandManager.handleMessage(_))
  catnip.connect()

  sys.env.get("KUNOU_SENTRY_DSN") match {
    case Some(sentryDsn) =>
      Sentry.init(sentryDsn)
      logger.info("Sentry Client initialized! Errors and warnings will be printed to the console and sent to Sentry.")
    case None =>
      logger.warn("No Sentry DSN specified. Errors and warnings - like this one - will only be tracked in the console.")
  }

  logger.info("Connected to Discord!")
}

object Kunou {

  case class Guild(id: Long, prefix: String)

}