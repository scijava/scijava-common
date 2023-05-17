/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scijava.event.bushe;

import junit.framework.TestCase;

public class TestEventServiceLocator extends EventServiceLocatorTestCase {
   
   public TestEventServiceLocator(String name) {
      super(name);
   }

   public void testDefaultEventBusService() {
      EventService ebs = EventServiceLocator.getEventBusService();
      assertTrue(ebs instanceof SwingEventService);
      EventService ses = EventServiceLocator.getSwingEventService();
      assertTrue(ses == ebs);
   }
   public void testDefaultEventBusService2() {
      EventService ses = EventServiceLocator.getSwingEventService();
      assertTrue(ses instanceof SwingEventService);
      EventService ebs = EventServiceLocator.getEventBusService();
      assertTrue(ses == ebs);
   }
   public void testNamedEventBusService1() {
      EventService ses = EventServiceLocator.getEventService(EventServiceLocator.SERVICE_NAME_SWING_EVENT_SERVICE);
      assertTrue(ses instanceof SwingEventService);
      EventService ebs = EventServiceLocator.getEventService(EventServiceLocator.SERVICE_NAME_EVENT_BUS);
      assertTrue(ses == ebs);
   }
   public void testNamedEventBusService2() {
      EventService ebs = EventServiceLocator.getEventService(EventServiceLocator.SERVICE_NAME_EVENT_BUS);
      assertTrue(ebs instanceof SwingEventService);
      EventService ses = EventServiceLocator.getEventService(EventServiceLocator.SERVICE_NAME_SWING_EVENT_SERVICE);
      assertTrue(ses == ebs);
   }
}
