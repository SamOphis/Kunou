package io.github.samophis.kunou.commands.list

import com.mewna.catnip.entity.message.Message
import io.github.samophis.kunou.commands.Command
import io.github.samophis.kunou.startup.Kunou

class TestCommand extends Command {
  override val name: String = "test"

  override def execute(bot: Kunou, message: Message, prefix: String): Unit = {
    import io.github.samophis.kunou.commands._

    val ok = okResponseBase(message).description("Success Message.").build()
    val warn = warningResponseBase(message).description("Warning Message.").build()
    val error = errorResponseBase(message).build()
    val error2 = errorResponseBase(message).description("Custom Error Message.").build()

    message.channel().sendMessage(ok)
    message.channel().sendMessage(warn)
    message.channel().sendMessage(error)
    message.channel().sendMessage(error2)
  }
}