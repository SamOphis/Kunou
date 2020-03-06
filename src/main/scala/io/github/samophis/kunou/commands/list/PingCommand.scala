package io.github.samophis.kunou.commands.list

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.user.User
import io.github.samophis.kunou.commands.Command
import io.github.samophis.kunou.startup.Kunou

class PingCommand extends Command {
  override val name: String = "ping"

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    val currentTimeNano = System.nanoTime
    bot.catnip.rest.user.getCurrentUser.subscribe((_: User) => {
      val nextCurrentTimeNano = System.nanoTime
      val differenceInTimeMillis = (nextCurrentTimeNano.toDouble - currentTimeNano.toDouble) / 1000000
      val differenceFormatted = String.format("**%.2fms**", differenceInTimeMillis)

      import io.github.samophis.kunou.commands.okResponseBase
      val response = okResponseBase(message).description("Discord API Latency: " + differenceFormatted).build()
      message.channel.sendMessage(response)
    })
  }
}