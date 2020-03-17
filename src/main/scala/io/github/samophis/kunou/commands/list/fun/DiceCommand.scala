package io.github.samophis.kunou.commands.list.fun

import com.mewna.catnip.entity.message.Message
import io.github.samophis.kunou.commands.CommandCategory.CommandCategory
import io.github.samophis.kunou.commands.{Command, CommandCategory, okResponseBase}
import io.github.samophis.kunou.startup.Kunou

class DiceCommand extends Command {
  override val name: String = "dice"
  override val aliases: Set[String] = Set("roll")
  override val category: CommandCategory = CommandCategory.Fun

  private[this] val random = scala.util.Random

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    val args = message.content.split("\\s+")
    val sides = if (args.length >= 2) {
      args(1).toIntOption match {
        case Some(sides) if sides > 100 => 6
        case Some(sides) => sides
        case None => 6
      }
    } else {
      6
    }

    val rnd = random.nextInt(sides)
    val embed = okResponseBase(message).description(s"I rolled a **$rnd**. Aren't you lucky?").build()
    message.channel.sendMessage(embed)
  }
}
