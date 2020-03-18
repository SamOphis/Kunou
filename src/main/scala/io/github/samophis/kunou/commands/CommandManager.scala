package io.github.samophis.kunou.commands

import java.util.concurrent.Executors

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.message.Message
import com.typesafe.scalalogging.Logger
import io.github.classgraph.ClassGraph
import io.github.samophis.kunou.commands.social.EmoteCommand
import io.github.samophis.kunou.startup.Kunou

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class CommandManager(private[this] val bot: Kunou) {
  private[this] implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  private[this] val logger = Logger[CommandManager]
  val commands: mutable.Map[String, Command] = mutable.HashMap()

  private[this] val scanResult = new ClassGraph()
    .enableAllInfo()
    .whitelistPackages("io.github.samophis.kunou.commands.list")
    .scan()
  scanResult.getAllStandardClasses
    .stream
    .filter(_.implementsInterface("io.github.samophis.kunou.commands.Command"))
    .filter(!_.isAbstract)
    .forEach(classInfo => {
      logger.debug("Found {}, loading...", classInfo.getName)

      val command = classInfo.loadClass(false)
        .getDeclaredConstructor()
        .newInstance()
        .asInstanceOf[Command]

      commands.put(command.name, command)
      command.aliases.foreach(commands.put(_, command))

      logger.info("Registered {}", classInfo.getSimpleName)
    })
  scanResult.close()

  if (bot.weeb4JOption.isDefined) {
    val emoteCommands = Set("awoo", "blush", "cry", "cuddle", "dance", "hug", "kiss", "lick", "neko",
      "nom", "owo", "pat", "pout", "slap", "smile", "smug", "stare", "triggered", "tickle", "bite", "greet",
      "punch", "poke", "shrug", "sleepy", "poi", "baka")

    emoteCommands.map(EmoteCommand(_)).foreach(command => commands.put(command.name, command))
  }

  def handleMessage(message: Message): Unit = if (message.channel().isText) {
    prefix(message).onComplete {
      case Success(prefix) if message.content.matches(s"<@!?${bot.userId}>\\s?prefix") =>
        val content = if (prefix == bot.defaultCommandPrefix) {
          s"Kunou's prefix in this server is the default global one: `$prefix`"
        } else {
          s"Kunou's prefix in this server is: `$prefix`"
        }
        val embed = new EmbedBuilder().description(content).build()
        message.channel.sendMessage(embed)
      case Success(prefix) if !message.content.startsWith(prefix) =>
      case Success(prefix) =>
        val args = message.content.split("\\s+")
        val commandName = args(0).toLowerCase.substring(prefix.length)
        commands.get(commandName) match {
          case Some(command) if !message.member().hasPermissions(command.requiredUserPermissions: _*) =>
            val warning = warningResponseBase(message)
              .description("You don't have permission to use this command.")
              .build
            message.channel.sendMessage(warning)
          case Some(command) if !message.guild().selfMember().hasPermissions(command.requiredBotPermissions: _*) =>
            val warning = warningResponseBase(message)
              .description("I don't have permission to use this command.")
              .build
            message.channel.sendMessage(warning)
          case Some(command) =>
            executionContext.execute(() => command.execute(bot, message, prefix))
          case None =>
        }
      case Failure(NonFatal(exception)) =>
        logger.error(s"Non-fatal error when fetching prefix for Message ID: ${message.id}", exception)
      case Failure(fatal) =>
        logger.error(s"FATAL error when fetching prefix for Message ID: ${message.id}", fatal)
    }
  }

  def prefix(message: Message): Future[String] = Future {
    val guildId = message.guildIdAsLong
    bot.redisClient.hget(guildId, "_prefix") match {
      case Some(prefix) => prefix
      case None => bot.defaultCommandPrefix
    }
  }
}
