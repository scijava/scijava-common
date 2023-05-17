package org.scijava.event.bushe;

import junit.framework.TestCase;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestEventServiceLocatorConfiguration2 extends EventServiceLocatorTestCase {

   public static class ES1 extends ThreadSafeEventService {

   }

   public static class ES2 extends ThreadSafeEventService {

   }

   public TestEventServiceLocatorConfiguration2(String name) {
      super(name);
   }

   public void testConfigurableEventService1() {
      System.setProperty(EventServiceLocator.SWING_EVENT_SERVICE_CLASS, ES1.class.getName());
      EventService es = EventServiceLocator.getEventBusService();
      assertTrue(es instanceof ThreadSafeEventService);
      es = EventServiceLocator.getSwingEventService();
      assertTrue(es instanceof ES1);
   }

}