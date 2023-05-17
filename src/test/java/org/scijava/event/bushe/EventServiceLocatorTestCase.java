package org.scijava.event.bushe;

import junit.framework.TestCase;

/**
 * Cleans out the event service locators before each test
 */
public class EventServiceLocatorTestCase extends TestCase {
   public EventServiceLocatorTestCase(String name) {
      super(name);
   }

   public void testEmptyTestCaseToAvoidWarning() {

   }

   @Override
   public void setUp() throws Exception {
      clearEventServiceLocator();
   }

   public static void clearEventServiceLocator() {
      System.clearProperty(EventServiceLocator.SWING_EVENT_SERVICE_CLASS);
      System.clearProperty(EventServiceLocator.EVENT_BUS_CLASS);
      EventServiceLocator.clearAll();
   }

   @Override
   protected void tearDown() throws Exception {
      clearEventServiceLocator();
   }
}
