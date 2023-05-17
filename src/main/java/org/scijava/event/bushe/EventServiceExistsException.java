package org.scijava.event.bushe;

/** Exception thrown by the EventServiceLocator when an EventService already is registered for a name. */
public class EventServiceExistsException extends Exception {
   public EventServiceExistsException(String msg) {
      super(msg);
   }
}
