package kunou

import java.time.OffsetDateTime

import ackcord.CacheSnapshot
import ackcord.data.{OutgoingEmbed, OutgoingEmbedFooter, TextChannel, User}
import ackcord.requests.{CreateMessage, CreateMessageData}
import kunou.commands.Context

package object syntax {
  object ContextSyntax {
    private val DefaultInfoEmbedColor = 9607928
  }

  implicit class ContextSyntax(private val context: Context) extends AnyVal {
    private implicit def cacheSnapshot: CacheSnapshot = context.cacheSnapshot

    def channel: Option[TextChannel] = context.message.channelId.resolve
    def effectiveEmbedColor: Int = context.message.guild match {
      case Some(guild) =>
        val selfMember = guild.members(cacheSnapshot.botUser.id)
        val highestRole = selfMember.roleIds
          .map(guild.roles)
          .filter(_.color != 0)
          .sortBy(- _.position)
          .headOption
        highestRole match {
          case Some(value) => value.color
          case None => ContextSyntax.DefaultInfoEmbedColor
        }
      case None => ContextSyntax.DefaultInfoEmbedColor
    }
  }

  object UserSyntax {
    val DefaultAvatarUrl: String = "https://cdn.discordapp.com/embed/avatars/%s.png"
    val AvatarUrl: String = "https://cdn.discordapp.com/avatars/%s/%s.%s"
  }

  implicit class UserSyntax(private val user: User) extends AnyVal {
    def asTag: String = s"${user.username}#${user.discriminator}"

    def effectiveAvatarUrl: String = user.avatar match {
      case Some(avatarHash) if avatarHash.startsWith("a_") => UserSyntax.AvatarUrl.format(user.id, avatarHash, "gif")
      case Some(avatarHash) => UserSyntax.AvatarUrl.format(user.id, avatarHash, "png")
      case None => UserSyntax.DefaultAvatarUrl.format(user.discriminator.toInt % 5)
    }
  }

  implicit class TextChannelSyntax(private val textChannel: TextChannel) extends AnyVal {
    def sendEmbed(embed: OutgoingEmbed): CreateMessage =
      CreateMessage(textChannel.id, CreateMessageData(embed = Some(embed)))
  }

  object EmbedHelperSyntax {
    private val WarnEmbedColor = 16417616
    private val ErrorEmbedColor = 16406607

    private val InfoFooterText = "Requested by %s"
    private val WarnFooterText = "Note: Most warnings are caused by incorrect command usage."
    private val ErrorFooterText = "Note: Reporting this to the developers would be greatly appreciated!"
  }

  implicit class EmbedHelperSyntax(private val stringContext: StringContext) extends AnyVal {
    def warn(args: Any*)(implicit cacheSnapshot: CacheSnapshot): OutgoingEmbed = createEmbed(makeString(args),
      EmbedHelperSyntax.WarnFooterText,
      cacheSnapshot.botUser.effectiveAvatarUrl, EmbedHelperSyntax.WarnEmbedColor)

    def error(args: Any*)(implicit cacheSnapshot: CacheSnapshot): OutgoingEmbed = createEmbed(makeString(args),
      EmbedHelperSyntax.ErrorFooterText, cacheSnapshot.botUser.effectiveAvatarUrl, EmbedHelperSyntax.ErrorEmbedColor)

    def info(args: Any*)(implicit context: Context): OutgoingEmbed = {
      implicit val cacheSnapshot: CacheSnapshot = context.cacheSnapshot

      context.message.authorUser match {
        case Some(user) => createEmbed(makeString(args), EmbedHelperSyntax.InfoFooterText.format(user.asTag),
          user.effectiveAvatarUrl, context.effectiveEmbedColor)
        case None => createEmbed(makeString(args), context.effectiveEmbedColor)
      }
    }

    private def makeString(args: Any*): String = {
      val argumentIterator = args(0).asInstanceOf[Seq[String]].iterator
      val partIterator = stringContext.parts.iterator
      val stringBuilder = new StringBuilder

      while (partIterator.hasNext) {
        stringBuilder.append(partIterator.next)
        // This check is because of the argument sequence having one more element than the part sequence.
        if (argumentIterator.hasNext)
          stringBuilder.append(argumentIterator.next)
      }

      stringBuilder.toString
    }

    private def createEmbed(content: String, color: Int): OutgoingEmbed =
      OutgoingEmbed(description = Some(content), color = Some(color))

    private def createEmbed(content: String, footerText: String, footerIconUrl: String, color: Int): OutgoingEmbed = {
      val embedFooter = OutgoingEmbedFooter(footerText, Some(footerIconUrl))
      OutgoingEmbed(description = Some(content), color = Some(color), footer = Some(embedFooter),
        timestamp = Some(OffsetDateTime.now))
    }
  }
}
