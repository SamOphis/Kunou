package kunou.util.logging.filters

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

// This could probably be implemented in the logback.xml configuration file.
// I don't have a good enough understanding of it though, so this code implementation will do.

class OutFilter extends Filter[LoggingEvent] {
  override def decide(event: LoggingEvent): FilterReply = event.getLevel match {
    case _ if !isStarted => FilterReply.NEUTRAL
    case Level.ERROR | Level.WARN => FilterReply.DENY
    case _ => FilterReply.NEUTRAL
  }
}
