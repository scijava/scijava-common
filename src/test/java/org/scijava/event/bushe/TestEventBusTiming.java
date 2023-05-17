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

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestEventBusTiming extends EventServiceLocatorTestCase {

   private EventSubscriber eventSubscriber = null;
   private EventTopicSubscriber eventTopicSubscriber;
   private SubscriberTimingEvent timing;
   private EBTestCounter testCounter = new EBTestCounter();

   public TestEventBusTiming(String name) {
      super(name);
   }

   private EventServiceEvent createEvent() {
      return new EventServiceEvent() {
         public Object getSource() {
            return "";
         }
      };
   }

   private Class getEventClass() {
      return createEvent().getClass();
   }

   private EventSubscriber createEventSubscriber(boolean throwException) {
      return new SubscriberForTest(testCounter, throwException);
   }

   private EventTopicSubscriber createEventTopicSubscriber(boolean throwException) {
      return new TopicSubscriberForTest(testCounter, throwException);
   }

   private EventSubscriber createEventSubscriber(Long waitTime) {
      return new SubscriberForTest(testCounter, waitTime);
   }

   private EventSubscriber getEventSubscriber() {
      return getEventSubscriber(true);
   }

   private EventSubscriber getEventSubscriber(boolean throwException) {
      if (eventSubscriber == null) {
         eventSubscriber = createEventSubscriber(throwException);
      }
      return eventSubscriber;
   }

   private EventTopicSubscriber getEventTopicSubscriber() {
      if (eventTopicSubscriber == null) {
         eventTopicSubscriber = createEventTopicSubscriber(false);
      }
      return eventTopicSubscriber;
   }

   public void thisOnlyWorksSometimesNow_testTimeHandling() {
      EventBus.subscribe(getEventClass(), createEventSubscriber(new Long(200L)));
      final Boolean[] wasCalled = new Boolean[1];
      EventBus.subscribe(SubscriberTimingEvent.class, new EventSubscriber() {
         public void onEvent(Object evt) {
            wasCalled[0] = Boolean.TRUE;
         }
      });
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();
      assertTrue(wasCalled[0] == null);
      EventBus.subscribe(getEventClass(), createEventSubscriber(new Long(200L)));
      final Boolean[] wasCalled2 = new Boolean[1];
      EventBus.subscribe(SubscriberTimingEvent.class, new EventSubscriber() {
         public void onEvent(Object evt) {
            wasCalled2[0] = Boolean.TRUE;
            timing = (SubscriberTimingEvent) evt;
         }
      });
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();
      assertTrue(wasCalled2[0] == Boolean.TRUE);
      assertNotNull(timing.getSource());
      assertNotNull(timing.getEnd());
      assertNotNull(timing.getEvent());
      assertNotNull(timing.getSubscriber());
      assertNotNull(timing.getStart());
      assertNotNull(timing.getTimeLimitMilliseconds());
      assertFalse(timing.isEventHandlingExceeded());
      assertFalse(timing.isVetoExceeded());
      assertNull(timing.getVetoEventListener());
   }

}
