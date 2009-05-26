package suereth.helper


import scala.swing._
import event._
import scala.actors.Actor._
import scala.actors.Actor

object TestActors {
  
  case class zOMG(msg : String) extends Event
  
   def main(args : Array[String]) {
     
     val text = new TextArea with ActorReactorBridge {
       text = "Initial Text" 
       size = (300,300)
       
       var globalCount = 1
       
       reactions += {
         case zOMG(msg) =>
           text = (text + "\n" + "Global Msg #" + globalCount + ": " +  msg)
           globalCount += 1
         case ExternalMessage(msg) =>
           text = (text + "\n" + "Global Msg #" + globalCount + ": " +  msg)
           globalCount += 1  
       }
     }
       
     
     val frame = new MainFrame { 
       frame =>
          size = (400, 400)
          title = "Test Actors"
          val panel = new Panel { panel =>
             _contents += text
             size = (400,400)
             
          }
          contents = panel
     }     
     frame.visible = true
     
     def actorGenerator(id : Int) : Actor = actor {
       loop {
         react {
           case zOMG(msg) =>
             text ! ("External Message from actor# " + id)
             Thread.sleep( (Math.random * 10).toLong)
             text ! zOMG("Actor#" + id + ": " + msg)
         }
       }     
     }
     val actors = (1 to 5).map(actorGenerator).toList
     for(i <- 1 to 5; a <- actors) {
       a ! zOMG("Msg#" + i)
     }
     ()
     //TODO - Wait for scala actors to wind down and check output
     
   }   
}
