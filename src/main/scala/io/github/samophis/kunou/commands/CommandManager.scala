package io.github.samophis.kunou.commands

import java.util.concurrent.Executors

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

  def handleMessage(message: Message): Unit = prefix(message).onComplete {
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

  // TODO: contact database!
  def prefix(message: Message): Future[String] = Future {
    bot.defaultCommandPrefix
  }
}
