package suereth.helper

import scala.actors._

/**
 * An Actor Scheduler which defers all processing to the Swing Event Queue.
 */
object SwingEventQueueScheduler extends IScheduler {
  /** Submits a closure for execution.
   *
   *  @param  fun  the closure to be executed
   */
  def execute(fun: => Unit): Unit = {
    execute(new Runnable {
      def run = fun
    })
  }

  /** Submits a <code>Runnable</code> for execution.
   *
   *  @param  task  the task to be executed
   */
  def execute(task: Runnable): Unit = {
    //TODO - If we are in the event queue thread, can we be greedy and just process now?
    import javax.swing.SwingUtilities
    SwingUtilities.invokeLater(task)
  }

  /** Notifies the scheduler about activity of the
   *  executing actor.
   *
   *  @param  a  the active actor
   */
  def tick(a: Actor): Unit = {}

  /** Shuts down the scheduler.
   */
  def shutdown(): Unit = {}

  def onLockup(handler: () => Unit): Unit = {}
  def onLockup(millis: Int)(handler: () => Unit): Unit = {}
  def printActorDump: Unit = {}
}

/**
 * An actor which processes all of its messages on the swing event queue.
 */
trait SwingActor extends Actor  {
  override protected def scheduler = SwingEventQueueScheduler   
  //TODO - Do we need to override start and remove the ActorGC junk?  
   //We automatically start the actor, because we rely on normal GC, and our scheduler doesn't do anything special besides processing on the AWT-Thread.
   start
}



import scala.swing.Reactor
import scala.swing.event.Event
/** A marker class for messages passed into a component via the actors library */
case class ExternalMessage[A](msg : A) extends Event

/** This trait can be used with scala.swing Reactor compoennts to allow the processing of external messages via reactions*/
trait ActorReactorBridge extends SwingActor with Reactor {   
  def act() : Unit = react {
    //TODO - handle internal actor messages (Exit, etc) directly    
    case msg : Event =>
      reactions(msg)
      act()
    case msg =>
      reactions(ExternalMessage(msg))
      act()
  }
}

