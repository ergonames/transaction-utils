object Main {

  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println("No arguments provided.")
      return
    }
    val boxId = args(0)
    val liveMode = args(1)
    
    val submittedTxId = "dfdkfjdkfjdkfjdkfjdkfjkdjdkfjdkfdkf"
    println("Submitted Transaction ID: " + submittedTxId)
  }
}