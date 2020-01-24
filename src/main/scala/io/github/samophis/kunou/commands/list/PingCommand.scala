package io.github.samophis.kunou.commands.list

import com.mewna.catnip.entity.message.Message
import io.github.samophis.kunou.commands.Command
import io.github.samophis.kunou.startup.Kunou

class PingCommand extends Command {
  override val name: String = "ping"

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = message.channel().sendMessage("Pong!")
}