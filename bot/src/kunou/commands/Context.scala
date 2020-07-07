package kunou.commands

import ackcord.CacheSnapshot
import ackcord.data.Message
import kunou.startup.Kunou

case class Context(bot: Kunou, message: Message, cacheSnapshot: CacheSnapshot, arguments: List[String], prefix: String)
