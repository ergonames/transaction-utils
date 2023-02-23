object Main {

  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println("Please provide a box ID")
      return
    }
    val boxId = args(0)
    println("Transaction Builder")
    println("Box ID: " + boxId)
  }
}