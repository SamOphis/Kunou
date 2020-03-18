package io.github.samophis.kunou.commands.list.fun

import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.{Base64, Collections}

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission
import com.typesafe.scalalogging.Logger
import io.github.samophis.kunou.commands.CommandCategory.CommandCategory
import io.github.samophis.kunou.commands._
import io.github.samophis.kunou.startup.Kunou

class StealEmojiCommand extends Command {
  override val name: String = "stealemoji"
  override lazy val description: String = "Steals an emoji from a different server, uploading it to another server."
  override val usage: String = "<emoji/emote>"
  override val category: CommandCategory = CommandCategory.Fun
  override val aliases: Set[String] = Set("steal", "stealemote")

  override val requiredUserPermissions: List[Permission] = List(Permission.MANAGE_EMOJI)
  override val requiredBotPermissions: List[Permission] = List(Permission.MANAGE_EMOJI)

  private[this] val emojiPattern = "<(a)?:(\\w+):(\\d+)>".r
  private[this] val firefoxUserAgent = "Mozilla/5.0 (Windows; rv:17.0) Gecko/20100101 Firefox/17.0"
  private[this] val logger = Logger[StealEmojiCommand]

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    val args = message.content.split("\\s+")
    if (args.length < 2) {
      val warning = warningResponseBase(message)
        .description("You need to specify an emoji to steal.")
        .build
      message.channel.sendMessage(warning)
      return
    }

    val (name, emojiUrl) = args(1) match {
      case emojiPattern(null, name, id) => (name, s"https://cdn.discordapp.com/emojis/$id.png")
      case emojiPattern(_, name, id) => (name, s"https://cdn.discordapp.com/emojis/a_$id.gif")
      case _ =>
        val warning = warningResponseBase(message)
          .description("You need to specify a valid emoji to steal.")
          .build
        message.channel.sendMessage(warning)
        return
    }

    val request = HttpRequest.newBuilder
      .uri(URI.create(emojiUrl))
      .timeout(Duration.ofSeconds(2))
      .header("User-Agent", firefoxUserAgent)
      .GET
      .build
    bot.catnip.options.httpClient.sendAsync(request, BodyHandlers.ofByteArray).thenAccept(response => {
      if (response.body.length > 256000) {
        val warning = warningResponseBase(message)
          .description("The emoji you specified is too big. Sorry about that.")
          .build
        message.channel.sendMessage(warning)
        return
      }

      val base64Data = Base64.getEncoder.encodeToString(response.body)
      val uri = URI.create(s"data:image/png;base64,$base64Data")
      message.guild.createEmoji(name, uri, Collections.emptyList[String]).doOnSuccess(_ => {
        val success = okResponseBase(message)
          .description(s"Successfully stole the **$name** emoji.")
          .build
        message.channel.sendMessage(success)
      }).doOnError(error => {
        logger.error(s"Error when stealing emoji -> creation, ${message.guildId}.", error)
        message.channel.sendMessage(errorResponseBase(message).build)
      }).subscribe
    }).exceptionally(exception => {
      logger.error(s"Error when stealing emoji -> download, ${message.guildId}.", exception)
      message.channel.sendMessage(errorResponseBase(message).build)
      null
    })
  }
}
