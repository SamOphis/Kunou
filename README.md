# Kunou

Kunou is my personal [Discord](https://discord.com) Bot, written in [Scala](https://www.scala-lang.org/) using [AckCord](https://github.com/Katrix/AckCord).
For at least a short while, Kunou is not considered user-ready.

The focuses in mind at the moment are music streaming, utility and social commands.

# Self-hosting

* Install the [Mill](https://github.com/lihaoyi/mill) build tool.
* Run `mill mill.scalalib.GenIdea/idea` if you use [IntelliJ IDEA](https://www.jetbrains.com/idea/).
* Run `mill bot.assembly`.
* Set the environment variable `KUNOU_BOT_TOKEN` to your bot token.
* Run `java -jar out/bot/assembly/dest/out.jar`. Make sure you add a liberal heap size limit. Kunou currently uses a max heap size of 512MB.