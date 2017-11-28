package werlang

import werlang.scaffolder._
import play.api.libs.json._
import java.io._



object Message {

    val line = (0 to 50).map(x => "*").mkString
    val errorLine = (0 to 50).map(x => "-").mkString

    def handleParseErrors(e:JsError) {
        this.error("Failed to parse JSON :(")
        println(" ")
        e.errors.foreach {
            case (path, validationErrors) => {
                println(scala.Console.RED + errorLine + scala.Console.RESET)
                this.parseError("Path: " + path)
                validationErrors.foreach(error => {
                    this.parseError("Error: " + error.message)
                })
                println(scala.Console.RED + errorLine + scala.Console.RESET)

                println(" ")
            }
        }
    }

    def parseError(error:String) {
        val closing = (0 to (49 - error.length - 2)).map(x => " ").mkString
        println(scala.Console.RED + "| " + scala.Console.RESET + error + closing + scala.Console.RED + "|" + scala.Console.RESET)
        
    }

    def error(msg:String) {
        val closing = (0 to (49 - msg.length - 9)).map(x => " ").mkString
        println(scala.Console.RED + line)
        println("* Error: " + scala.Console.RESET+ msg + scala.Console.RED + closing + "*")
        println(scala.Console.RED + line + scala.Console.RESET)
    }

    def info(msg:String) {
        val closing = (0 to (49 - msg.length - 2)).map(x => " ").mkString
        println(scala.Console.CYAN + line)
        println("* "+scala.Console.RESET+ msg +scala.Console.CYAN + closing + "*")
        println(line + scala.Console.RESET)
    }

    def green(msg:String) {
        val closing = (0 to (49 - msg.length - 2)).map(x => " ").mkString
        println(scala.Console.GREEN + line)
        println("* "+scala.Console.RESET+ msg +scala.Console.GREEN + closing + "*")
        println(line + scala.Console.RESET)
    }

    def ok(msg:String) {
        println("[" + scala.Console.GREEN + "OK" + scala.Console.RESET + "] " + msg)
    }

    def fail(msg:String) {
        println("[" + scala.Console.RED + "Fail" + scala.Console.RESET + "] " + msg)
    }

}