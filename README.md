# Kunou
Yet another rewrite of Kunou, written in [Scala](https://www.scala-lang.org/), using the [Catnip](https://github.com/mewna/catnip)
Discord API wrapper. Kunou was originally meant to be targeted towards music, but there is no specific focus in mind as of 2020/03/17.

Data is stored within [Redis](https://redis.io/) as I'm fairly comfortable using the service, and I love the speed it provides.
In the fairly unlikely chance that Kunou ever becomes a massive bot, other data solutions may be considered.

Music, when done, will be using [lavaplayer](https://github.com/sedmelluq/lavaplayer), [lavadsp](https://github.com/natanbc/lavadsp),
[Magma](https://github.com/napstr/Magma), and [jda-nas](https://github.com/sedmelluq/jda-nas).

Social emote commands use the [weeb.sh](https://weeb.sh/) service.

# Self-hosting Details
Kunou isn't particularly complicated to self-host. The project uses the [sbt](https://www.scala-sbt.org/) build tool,
and a custom `runKunou` task has been made *for local debug purposes only*, which just creates a user-friendly application packaged into a `tar.xz` archive, then unpacks it, runs the application, and any output is echoed to the console.

> **Note:** `runKunou` relies on a `run.sh` shell script in working directory. I generally advise *against* using the `runKunou` task unless you have a dev environment equivalent to my local one.

For production, you can use the `universal:packageXzTarball` task or any other similar ones depending on your system, unpack it
and use the generated executable. Note that a [Redis](https://redis.io/) server must be running locally on port 6379, or all messages
received will cause the command handler to spam connection errors. You must configure Redis to save data to disk periodically,
instead of the default in-memory cache configuration.

This is generally as simple as changing `appendonly no` to `appendonly yes`, however the Redis documentation does recommend
*also* using RDB in addition to AOF.

Kunou requires a fair amount of environment variables for configuration:
* `KUNOU_BOT_TOKEN` - Pretty simple. This one is just your bot token, available on the applications page.
* `KUNOU_SENTRY_DSN` - This one is not required, but is recommended. If specified, Kunou will send warnings and errors to [Sentry](https://sentry.io/welcome/).
* `KUNOU_OWNER_ID` - This is the User ID of the person self-hosting the bot. Used for owner-specific commands and debug purposes.
* `KUNOU_DEFAULT_PREFIX` - This one is not required and substitutes to `k.`. It's the default command prefix.
* `KUNOU_WEEBSH_TOKEN` - This is your `Wolke` [weeb.sh](https://weeb.sh/) key. Not required, but without it emote commands are disabled.
