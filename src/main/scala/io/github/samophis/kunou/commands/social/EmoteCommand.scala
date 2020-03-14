package io.github.samophis.kunou.commands.social

import com.github.natanbc.reliqua.request.RequestException
import com.github.natanbc.weeb4j.image.Image
import com.mewna.catnip.entity.message.Message
import com.typesafe.scalalogging.Logger
import io.github.samophis.kunou.commands.CommandCategory.CommandCategory
import io.github.samophis.kunou.commands.{Command, CommandCategory, errorResponseBase, okResponseBase}
import io.github.samophis.kunou.startup.Kunou

trait EmoteCommand extends Command {
  private[this] val logger = Logger[EmoteCommand]

  override lazy val description: String = s"Sends an image/gif related to the `$name` image type."
  override val category: CommandCategory = CommandCategory.Social

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {

    bot.weeb4JOption.get.getImageProvider.getRandomImage(name).async((image: Image) => {
      val embed = okResponseBase(message).image(image.getUrl).build()
      message.channel().sendMessage(embed)
      () // scala hates java interop
    }, (error: RequestException) => {
      logger.error(s"Error when using $name command.", error)
      message.channel.sendMessage(errorResponseBase(message).build())
      ()
    })
  }
}

object EmoteCommand {
  def apply(imageType: String): EmoteCommand = new EmoteCommand {
    override val name: String = imageType
  }
}
