package io.github.samophis.kunou.startup

import java.util

import com.mewna.catnip.cache.CacheFlag
import com.mewna.catnip.shard.DiscordEvent
import com.mewna.catnip.{Catnip, CatnipOptions}
import com.typesafe.scalalogging.Logger
import io.github.samophis.kunou.commands.CommandManager
import io.sentry.Sentry

class Kunou {
  private[this] val token = sys.env("KUNOU_BOT_TOKEN")
  private[this] val logger = Logger[Kunou]

  val ownerId: Long = sys.env("KUNOU_OWNER_ID").toLong
  val defaultCommandPrefix: String = sys.env.get("KUNOU_DEFAULT_PREFIX") match {
    case None => "k."
    case Some(default) => default
  }

  private[this] val catnipOptions = new CatnipOptions(token)
    .cacheFlags(util.Set.of(CacheFlag.DROP_GAME_STATUSES, CacheFlag.DROP_EMOJI))
    .memberChunkTimeout(5000)
    .validateToken(true)
  val catnip: Catnip = Catnip.catnip(catnipOptions).connect()
  private[this] val commandManager = new CommandManager(this)

  // There is a warning here (can convert to method value), but Catnip doesn't like it.
  // +1 for Java interoperability, Scala.
  catnip.on(DiscordEvent.MESSAGE_CREATE, commandManager.handleMessage(_))

  sys.env.get("KUNOU_SENTRY_DSN") match {
    case Some(sentryDsn) =>
      Sentry.init(sentryDsn)
      logger.info("Sentry Client initialized! Errors and warnings will be printed to the console and sent to Sentry.")
    case None =>
      logger.warn("No Sentry DSN specified. Errors and warnings - like this one - will only be tracked in the console.")
  }

  logger.info("Connected to Discord!")
}