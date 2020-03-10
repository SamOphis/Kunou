package io.github.samophis.kunou

import java.awt.Color
import java.time.OffsetDateTime

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.message.Message

import scala.jdk.CollectionConverters._

package object commands {
  val errorColor = new Color(0xFF4942)
  val warningColor = new Color(0xFFA046)
  val defaultSuccessColor = new Color(0xD38EFF)

  def successColor(message: Message): Color = if (message.channel.isDM) {
    defaultSuccessColor
  } else {
    message.guild()
      .selfMember()
      .orderedRoles()
      .asScala
      .filter(_.color() != 0)
      .lastOption
      .map(role => new Color(role.color()))
      .getOrElse(defaultSuccessColor)
  }

  def okResponseBase(message: Message): EmbedBuilder = new EmbedBuilder()
    .color(successColor(message))
    .footer(s"Requested by ${message.author().discordTag()}", message.author().effectiveAvatarUrl())
    .timestamp(OffsetDateTime.now())

  def errorResponseBase(message: Message): EmbedBuilder = new EmbedBuilder()
    .color(errorColor)
    .description("An error occurred, unfortunately. Sorry about that.")
    .footer("Note: Please report this issue in Kunou's support server.", message.catnip().selfUser().effectiveAvatarUrl())
    .timestamp(OffsetDateTime.now())

  def warningResponseBase(message: Message): EmbedBuilder = new EmbedBuilder()
    .color(warningColor)
    .footer("Note: Most warnings are user-caused.", message.catnip().selfUser().effectiveAvatarUrl())
    .timestamp(OffsetDateTime.now())
}
