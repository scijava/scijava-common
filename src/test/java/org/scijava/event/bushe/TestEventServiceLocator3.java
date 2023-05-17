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

public class TestEventServiceLocator3 extends EventServiceLocatorTestCase {
   public TestEventServiceLocator3(String name) {
      super(name);
   }

   public void testSetEventBusService() {
      EventService ebs = new SwingEventService();
      try {
         EventServiceLocator.setEventService(EventServiceLocator.SERVICE_NAME_SWING_EVENT_SERVICE, ebs);
      } catch (EventServiceExistsException e) {
         fail("It doesn't exist yet");
      }
      EventService eb = EventServiceLocator.getEventService(EventServiceLocator.SERVICE_NAME_SWING_EVENT_SERVICE);
      assertTrue(eb instanceof ThreadSafeEventService);
      assertTrue(eb == ebs);
      EventService ses = EventServiceLocator.getEventService(EventServiceLocator.SERVICE_NAME_EVENT_BUS);
      assertTrue(ses == ebs);
      assertTrue(ses instanceof ThreadSafeEventService);
      try {
         EventServiceLocator.setEventService(EventServiceLocator.SERVICE_NAME_SWING_EVENT_SERVICE, ebs);
         fail("It already exist yet");
      } catch (EventServiceExistsException e) {
      }
   }
}