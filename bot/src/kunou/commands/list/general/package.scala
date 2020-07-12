package kunou.commands.list

import ackcord.CacheSnapshot
import ackcord.requests.GetCurrentUser
import com.typesafe.scalalogging.Logger
import kunou.commands.{Command, Context}
import kunou.syntax._

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

package object general {
  class PingCommand extends Command {
    private val logger = Logger[PingCommand]
    override def name: String = "ping"

    override def execute(implicit context: Context): Unit = context match {
      case Context(bot, msg, _, _ ,_ ) =>
        import bot.discordClient.requestsHelper.run
        import bot.clientSettings.executionContext

        implicit val c: CacheSnapshot = context.cacheSnapshot
        val channel = msg.channelId.resolve.get

        // We don't care about the actual user from the request, so we convert back to Future[Option[User]]
        // - so that we can just run Future#onComplete.
        // Used to obtain network latency (for the REST API).

        val firstTimeNanos = System.nanoTime()
        run(GetCurrentUser).value.onComplete {
          case Success(_) =>
            val secondTimeNanos = System.nanoTime()
            val apparantLatency = (secondTimeNanos - firstTimeNanos) / 1000000d
            val formattedLatencyString = f"$apparantLatency%.2fms"
            run(channel.sendEmbed(info"🏓 Discord API Latency: **$formattedLatencyString**"))

          case Failure(NonFatal(error)) =>
            logger.error(s"An error occurred while making a request to the Discord API.", error)
            run(channel.sendEmbed(error"An error occurred. Sorry about that."))
        }
    }
  }
}
