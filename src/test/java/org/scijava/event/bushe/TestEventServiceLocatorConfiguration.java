package org.scijava.event.bushe;

import junit.framework.TestCase;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestEventServiceLocatorConfiguration extends EventServiceLocatorTestCase {

   public static class ES1 extends ThreadSafeEventService {

   }

   public static class ES2 extends ThreadSafeEventService {

   }

   public TestEventServiceLocatorConfiguration(String name) {
      super(name);
   }

   public void testConfigurableEventService() {
      System.setProperty(EventServiceLocator.SWING_EVENT_SERVICE_CLASS, ES1.class.getName());
      System.setProperty(EventServiceLocator.EVENT_BUS_CLASS, ES2.class.getName());
      EventService es = EventServiceLocator.getSwingEventService();
      assertTrue(es instanceof ES1);
      es = EventServiceLocator.getEventBusService();
      assertTrue(es instanceof ES2);
   }
}