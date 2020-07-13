package kunou.commands.list

import ackcord.CacheSnapshot
import ackcord.data.OutgoingEmbed
import ackcord.requests.GetCurrentUser
import com.typesafe.scalalogging.Logger
import kunou.commands.Category.Category
import kunou.commands.{Category, Command, Context}
import kunou.startup.Kunou
import kunou.syntax._

import scala.collection.mutable
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

package object general {
  class PingCommand(bot: Kunou) extends Command {
    import bot.discordClient.requestsHelper.run
    import bot.clientSettings.executionContext

    private val logger = Logger[PingCommand]
    override def name: String = "ping"

    override def execute(implicit context: Context): Unit = context match {
      case Context(_, msg, _, _ ,_ ) =>

        // We don't care about the actual user from the request, so we convert back to Future[Option[User]]
        // - so that we can just run Future#onComplete.
        // Used to obtain network latency (for the REST API).

        implicit val c: CacheSnapshot = context.cacheSnapshot
        val firstTimeNanos = System.nanoTime()

        run(GetCurrentUser).value.onComplete {
          case Success(_) =>
            val secondTimeNanos = System.nanoTime()
            val apparantLatency = (secondTimeNanos - firstTimeNanos) / 1000000d
            val formattedLatencyString = f"$apparantLatency%.2fms"
            run(msg.channelId.sendEmbed(info"🏓 Discord API Latency: **$formattedLatencyString**"))

          case Failure(NonFatal(error)) =>
            logger.error(s"An error occurred while making a request to the Discord API.", error)
            run(msg.channelId.sendEmbed(error"An error occurred. Sorry about that."))
        }
    }
  }

  class CommandListCommand(bot: Kunou) extends Command {
    import bot.discordClient.requestsHelper.run
    import scala.collection.{Set => SetParent}

    // This uses a set rather than a normal buffer to account for aliases.
    private val categoryCommandSetMap: mutable.Map[Category, mutable.Set[Command]] = mutable.Map()

    override def name: String = "commandlist"

    override def aliases: Set[String] = Set("cmdlist", "list")

    override def execute(implicit context: Context): Unit = {
      implicit val c: CacheSnapshot = context.cacheSnapshot

      if (categoryCommandSetMap.isEmpty) {
        bot.commandManager.commandMap.values.foreach(command => {
          categoryCommandSetMap.get(command.category) match {
            case Some(set) => set += command
            case _ =>
              val newSet = mutable.Set[Command]()
              newSet += command
              categoryCommandSetMap.put(command.category, newSet)
          }
        })
      }

      def generateEmbed(categorySet: SetParent[Category]): OutgoingEmbed = if (categorySet.nonEmpty) {
        val stringBuilder = new mutable.StringBuilder
        categorySet.foreach(category => {
          val commandBuffer = categoryCommandSetMap(category)
          stringBuilder ++= "**"
          stringBuilder ++= category.toString
          stringBuilder ++= "**: "
          stringBuilder ++= commandBuffer.map(command => s"${context.prefix}${command.name}").mkString(", ")
          stringBuilder ++= "\n"
        })
        info"${stringBuilder.toString}"
      } else {
        val categoryList = categoryCommandSetMap.keySet.map(_.toString).mkString(", ")
        warn"No valid command categories were specified. Pick from this list: $categoryList."
      }

      def argumentToCategory(argument: String): Option[Category] =
        Category.values.find(_.toString.equalsIgnoreCase(argument))

      val createMessage = context match {
        case Context(_, msg, _, List(), _) =>
          val embed = generateEmbed(categoryCommandSetMap.keySet)
          msg.channelId.sendEmbed(embed)

        case Context(_, msg, _, args, _) =>
          val embed = generateEmbed(args.flatMap(argumentToCategory).toSet)
          msg.channelId.sendEmbed(embed)
      }

      run(createMessage)
    }
  }
}
