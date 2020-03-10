package io.github.samophis.kunou.commands

import java.util.concurrent.Executors

import com.github.natanbc.reliqua.request.RequestException
import com.github.natanbc.weeb4j.image.Image
import com.mewna.catnip.entity.message.Message
import com.typesafe.scalalogging.Logger
import io.github.classgraph.ClassGraph
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
      logger.info("Registered {}", classInfo.getSimpleName)
    })
  scanResult.close()

  if (bot.weeb4JOption.isDefined) {
    // usually with person: cuddle, dance, hug, kiss, lick, pat, slap, stare, tickle, bite, punch, poke, blush
    // usually without person: awoo, neko, nom, owo, pout, smile, smug, triggered, nani, thinking, greet, shrug
    // ----- sleepy, poi, baka

    // We can merge these categories for now but later iterations of Kunou may separate them like in
    // --- previous versions.
    val emoteCommands = Set("cuddle", "dance", "hug", "kiss", "lick", "pat", "slap", "stare", "tickle", "bite",
      "punch", "poke", "blush", "awoo", "neko", "nom", "owo", "pout", "smile", "smug", "triggered", "nani",
      "thinking", "greet", "shrug", "sleepy", "poi", "baka")

    emoteCommands.foreach(imageType => {
      val command = Command(imageType, (_, message, _) => {
        bot.weeb4JOption.get.getImageProvider.getRandomImage(imageType).async((image: Image) => {
          val embed = okResponseBase(message).image(image.getUrl).build()
          message.channel().sendMessage(embed)
          () // scala hates java interop
        }, (error: RequestException) => {
          logger.error(s"Error when using $imageType command.", error)
          message.channel.sendMessage(errorResponseBase(message).build())
          ()
        })
      })

      commands.put(imageType, command)
    })
  }

  def handleMessage(message: Message): Unit = if (message.channel().isText) {
    prefix(message).onComplete {
      case Success(prefix) if !message.content.startsWith(prefix) =>
      case Success(prefix) =>
        val args = message.content.split("\\s+")
        val commandName = args(0).toLowerCase.substring(prefix.length)
        commands.get(commandName) match {

          case Some(command) if !message.member().hasPermissions(command.requiredUserPermissions: _*) =>
            message.channel().sendMessage("You don't have permission to use this command. \uD83D\uDE2D")
          case Some(command) if !message.guild().selfMember().hasPermissions(command.requiredBotPermissions: _*) =>
            message.channel().sendMessage("I don't have permission to use this command. \uD83D\uDE2D")
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
    val guildId = message.guildIdAsLong()
    bot.redisClient.hget(guildId, "_prefix") match {
      case Some(prefix) => prefix
      case None => bot.defaultCommandPrefix
    }
  }
}
