package org.scijava.event.bushe;

import junit.framework.TestCase;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestEventServiceLocatorConfiguration3 extends EventServiceLocatorTestCase {

   public static class ES1 extends ThreadSafeEventService {

   }

   public static class ES2 extends ThreadSafeEventService {

   }

   public TestEventServiceLocatorConfiguration3(String name) {
      super(name);
   }

   public void testConfigurableEventService3() {
      System.setProperty(EventServiceLocator.EVENT_BUS_CLASS, ES2.class.getName());
      EventService es = EventServiceLocator.getEventBusService();
      assertTrue(es instanceof ES2);
      es = EventServiceLocator.getSwingEventService();
      assertTrue(es instanceof SwingEventService);
   }

}