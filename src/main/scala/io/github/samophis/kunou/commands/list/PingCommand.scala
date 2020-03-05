package io.github.samophis.kunou.commands.list

import com.mewna.catnip.entity.message.Message
import io.github.samophis.kunou.commands.Command
import io.github.samophis.kunou.startup.Kunou

class PingCommand extends Command {
  override val name: String = "ping"

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    val currentTime = System.nanoTime()
    message.channel.sendMessage("Pong!").thenAccept(message => {
      val difference = (System.nanoTime() - currentTime) / 1000000d
      val formatted = String.format("%.02fms", difference)
      message.edit("Pong! " + formatted)
    })
  }
}