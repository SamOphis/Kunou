package io.github.samophis.kunou.commands.list

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.user.User
import io.github.samophis.kunou.commands.Command
import io.github.samophis.kunou.startup.Kunou

class PingCommand extends Command {
  override val name: String = "ping"

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    // Specifying the type explicitly is unnecessary here, but IntelliJ doesn't like it and I don't want my IDE
    // - showing errors but the compiler being fine. I like consistency.

    val currentTimeNano = System.nanoTime
    bot.catnip.rest.user.getCurrentUser.subscribe((_: User) => {
      val nextCurrentTimeNano = System.nanoTime
      val differenceInTimeMillis = (nextCurrentTimeNano - currentTimeNano) / 1000000.toDouble
      val differenceFormatted = f"**$differenceInTimeMillis%.2fms**"

      import io.github.samophis.kunou.commands.okResponseBase
      val response = okResponseBase(message).description(s"Discord API Latency: $differenceFormatted").build()
      message.channel.sendMessage(response)
    })
  }
}