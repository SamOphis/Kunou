package kunou.commands

object Category extends Enumeration {
  type Category = Value

  val General: Value   = Value("General")
  val Fun:     Value   = Value("Fun")
  val Music:   Value   = Value("Music")
  val Debug:   Value   = Value("Debug")
}