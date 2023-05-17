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

import java.io.Serializable;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import java.awt.Container;
import java.awt.Component;
import javax.swing.JComponent;

import junit.framework.TestCase;

import org.scijava.event.bushe.generics.DataRequestEvent;
import org.scijava.event.bushe.generics.TypeReference;

/** The DefaultEventService is NOT Swing-safe!  But it's easier to test... */
public class TestDefaultEventService extends TestCase {

   private ThreadSafeEventService eventService = null;
   private EventSubscriber eventSubscriber = null;
   private EventTopicSubscriber eventTopicSubscriber;
   private SubscriberTimingEvent timing;
   private EBTestCounter testCounter = new EBTestCounter();

   public TestDefaultEventService(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
      eventService = new ThreadSafeEventService(null, false);
      EventServiceLocatorTestCase.clearEventServiceLocator();
   }

   protected void tearDown() throws Exception {
      eventService = null;
      EventServiceLocatorTestCase.clearEventServiceLocator();
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

   public void testTyping() {
      EventSubscriber subscriber = createEventSubscriber(false);

      Double doub = 3.14;
      Number numb = doub;
      eventService.subscribe(Number.class, subscriber);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(doub);
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      eventService.publish(numb);
      assertEquals("testPublish(total)", 2, testCounter.eventsHandledCount);
      eventService.unsubscribe(Number.class, subscriber);
      eventService.subscribe(Double.class, subscriber);
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(doub);
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      eventService.publish(numb);
      assertEquals("testPublish(total)", 2, testCounter.eventsHandledCount);
   }

   public void testSubscribe() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);
      assertTrue("testSubscribe(new subscriber)", actualReturn);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);
      assertFalse("testSubscribe(duplicate subscriber)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      try {
         actualReturn = eventService.subscribe((Class) null, getEventSubscriber());
         fail("subscribeStrongly(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.subscribe(getEventClass(), null);
         fail("subscribeStrongly(x, null) should have thrown exception");
      } catch (Exception e) {
      }

   }

   public void testSubscribeOrder() {
      boolean actualReturn;
      SubscriberForTest subscriber1 = (SubscriberForTest) createEventSubscriber(new Long(100));
      SubscriberForTest subscriber2 = (SubscriberForTest) createEventSubscriber(new Long(100));
      SubscriberForTest subscriber3 = (SubscriberForTest) createEventSubscriber(new Long(100));

      actualReturn = eventService.subscribe(getEventClass(), subscriber1);
      actualReturn = eventService.subscribe(getEventClass(), subscriber2);
      actualReturn = eventService.subscribe(getEventClass(), subscriber3);

      eventService.publish(createEvent());

      assertTrue(subscriber1.callTime.before(subscriber2.callTime));
      assertTrue(subscriber2.callTime.before(subscriber3.callTime));

      actualReturn = eventService.subscribe(getEventClass(), subscriber1);
      eventService.publish(createEvent());

      assertTrue(subscriber2.callTime.before(subscriber3.callTime));
      assertTrue(subscriber3.callTime.before(subscriber1.callTime));

      List subscribers = eventService.getSubscribers(getEventClass());
      assertEquals(3, subscribers.size());
      for (int i = 0; i < subscribers.size(); i++) {
         EventSubscriber subscriber = (EventSubscriber) subscribers.get(i);
         eventService.unsubscribe(getEventClass(), subscriber);
      }
      eventService.subscribe(getEventClass(), (EventSubscriber) subscribers.get(1));
      eventService.subscribe(getEventClass(), (EventSubscriber) subscribers.get(0));
      eventService.subscribe(getEventClass(), (EventSubscriber) subscribers.get(2));
      eventService.publish(createEvent());
      assertTrue(subscriber3.callTime.before(subscriber2.callTime));
      assertTrue(subscriber2.callTime.before(subscriber1.callTime));
   }

   public void testSubscribeWeakly() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);
      assertTrue("testSubscribeWeakly(new subscriber)", actualReturn);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);
      assertFalse("testSubscribe(duplicate subscriber)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
      subscriber = null;
      System.gc();
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      try {
         actualReturn = eventService.subscribeStrongly((Class) null, getEventSubscriber());
         fail("subscribeStrongly(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.subscribeStrongly(getEventClass(), null);
         fail("subscribeStrongly(x, null) should have thrown exception");
      } catch (Exception e) {
      }
   }

   public void testSubscribeStrongly() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribeStrongly(getEventClass(), subscriber);
      assertTrue("testSubscribeWeakly(new subscriber)", actualReturn);

      actualReturn = eventService.subscribeStrongly(getEventClass(), subscriber);
      assertFalse("testSubscribe(duplicate subscriber)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
      subscriber = null;
      System.gc();
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      try {
         actualReturn = eventService.subscribeStrongly((Class) null, getEventSubscriber());
         fail("subscribeStrongly(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.subscribeStrongly(getEventClass(), null);
         fail("subscribeStrongly(x, null) should have thrown exception");
      } catch (Exception e) {
      }
   }


   public void testIllegalArgs() {
      try {
         EventBus.subscribeVetoListenerStrongly((Class) null, new VetoEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListenerStrongly((String) null, new VetoTopicEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListenerStrongly("foo", null);
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.subscribeVetoListenerStrongly(getEventClass(), null);
         fail();
      } catch (Throwable t) {
      }


      try {
         EventBus.unsubscribeVetoListener((Class) null, new VetoEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.unsubscribeVetoListener((String) null, new VetoTopicEventListenerForTest());
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.unsubscribeVetoListener("foo", null);
         fail();
      } catch (Throwable t) {
      }
      try {
         EventBus.unsubscribeVetoListener(getEventClass(), null);
         fail();
      } catch (Throwable t) {
      }

   }

   public void testVeto() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);

      VetoEventListener vetoListener = new VetoEventListenerForTest();
      actualReturn = eventService.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      eventService.unsubscribeVetoListener(getEventClass(), vetoListener);
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);

   }

   public void testVetoException() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);

      VetoEventListener vetoListener = new VetoEventListenerForTest(true);
      actualReturn = eventService.subscribeVetoListenerStrongly(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      eventService.unsubscribeVetoListener(getEventClass(), vetoListener);
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 2, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);

   }

   public void testVetoTopic() {
      boolean actualReturn;
      EventTopicSubscriber subscriber = createEventTopicSubscriber(false);

      actualReturn = eventService.subscribe("FooTopic", subscriber);

      VetoTopicEventListener vetoListener = new VetoTopicEventListener() {
         public boolean shouldVeto(String topic, Object data) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListenerStrongly("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish("FooTopic", "Bar");

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      eventService.unsubscribeVetoListener("FooTopic", vetoListener);
      eventService.publish("FooTopic", "Bar");

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }


   public void testVetoWeak() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = eventService.subscribe(getEventClass(), subscriber);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(Object evt) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      vetoListener = null;
      System.gc();
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   public void testVetoTopicWeak() {
      boolean actualReturn;
      EventTopicSubscriber subscriber = createEventTopicSubscriber(false);

      actualReturn = eventService.subscribe("FooTopic", subscriber);

      VetoTopicEventListener vetoListener = new VetoTopicEventListener() {
         public boolean shouldVeto(String topic, Object data) {
            return true;
         }
      };
      actualReturn = eventService.subscribeVetoListener("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      vetoListener = null;
      System.gc();
      eventService.publish("FooTopic", createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }


   public void testUnsubscribe() {
      eventService.subscribe(getEventClass(), getEventSubscriber(false));

      boolean actualReturn;

      try {
         actualReturn = eventService.unsubscribe((Class) null, getEventSubscriber());
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.unsubscribe(getEventClass(), null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      actualReturn = eventService.unsubscribe(getEventClass(), getEventSubscriber());
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   public void testUnsubscribeTopic() {
      EventTopicSubscriber eventTopicSubscriber = createEventTopicSubscriber(false);
      eventService.subscribe("FooTopic", eventTopicSubscriber);

      boolean actualReturn;

      try {
         actualReturn = eventService.unsubscribe((String) null, eventTopicSubscriber);
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = eventService.unsubscribe("FooTopic", null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish("FooTopic", "Foo");

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      actualReturn = eventService.unsubscribe("FooTopic", eventTopicSubscriber);
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish("FooTopic", "Foo");

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   /**
    * Test that the publish method works and that exceptions thrown in event subscribers don't halt publishing. In the
    * test 2 subscribers are good and 2 subscribers throw exceptions.
    */
   public void testPublish() {
      try {
         eventService.publish(null);
         fail("publish(null) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         eventService.publish((String) null, createEvent());
         fail("publish(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      eventService.publish(createEvent());
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      eventService.publish("Foo", "Bar");
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      eventService.subscribe(getEventClass(), createEventSubscriber(true));
      eventService.subscribe(getEventClass(), createEventSubscriber(false));
      eventService.subscribe(getEventClass(), createEventSubscriber(true));
      eventService.subscribe(getEventClass(), createEventSubscriber(false));

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      eventService.publish(createEvent());

      //The test passes if 2 subscribers completed and 2 subscribers threw exception.
      assertEquals("testPublish(completed)", 4, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 2, testCounter.subscribeExceptionCount);

      EventBus.subscribe(ObjectEvent.class, createEventSubscriber(false));
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      ObjectEvent evt = new ObjectEvent("Foo", "Bar");
      assertEquals(evt.getEventObject(), "Bar");
      EventBus.publish(evt);
      //Since we are using hte event bus from a non-awt thread, stay alive for a sec
      //to give time for the EDT to start and post the message
      try {
         Thread.sleep(500);
      } catch (InterruptedException e) {
      }
      assertEquals("testPublish(completed)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   public void testTimeHandling() {
      eventService.subscribe(getEventClass(), createEventSubscriber(new Long(200L)));
      final Boolean[] wasCalled = new Boolean[1];
      eventService.subscribe(SubscriberTimingEvent.class, new EventSubscriber() {
         public void onEvent(Object evt) {
            wasCalled[0] = Boolean.TRUE;
         }
      });
      eventService.publish(createEvent());
      assertTrue(wasCalled[0] == null);
      eventService = new ThreadSafeEventService(new Long(100), true);
      eventService.subscribe(getEventClass(), createEventSubscriber(new Long(200L)));
      final Boolean[] wasCalled2 = new Boolean[1];
      eventService.subscribe(SubscriberTimingEvent.class, new EventSubscriber() {
         public void onEvent(Object evt) {
            wasCalled2[0] = Boolean.TRUE;
            timing = (SubscriberTimingEvent) evt;
         }
      });
      eventService.publish(createEvent());
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

   public void testEventLocator() {
      EventServiceLocatorTestCase.clearEventServiceLocator();
      EventService es = EventServiceLocator.getSwingEventService();
      assertTrue(es instanceof SwingEventService);
      es = new ThreadSafeEventService(null, false);
      try {
         EventServiceLocator.setEventService("foo", es);
      } catch (EventServiceExistsException e) {
         fail("First set should succeed.");
      }
      EventService es2 = EventServiceLocator.getEventService("foo");
      assertTrue(es2 == es);
      try {
         es = new ThreadSafeEventService(null, false);
         EventServiceLocator.setEventService("foo", es);
         fail("Second set should fail.");
      } catch (EventServiceExistsException e) {
      }
      es2 = EventServiceLocator.getEventService("foo");
      assertFalse(es2 == es);
      try {
         EventServiceLocator.setEventService("foo", null);
      } catch (EventServiceExistsException e) {
         fail("Null should succeed.");
      }
      es2 = EventServiceLocator.getEventService("foo");
      assertNull(es2);
      assertEquals(EventServiceLocator.getSwingEventService(), EventBus.getGlobalEventService());
   }

   /**
    * Test for ISSUE #1: If a class implements both subscriber interfaces I've seen a topic 'event' be published from a
    * publish method with the correct (topic) signature, yet be subscribed at the wrong subscriber method (the one with
    * the signature for real event classes, not topics
    */
   public void testSimultaneousTopicAndClass() {
      DoubleSubscriber doubleSubscriber = new DoubleSubscriber();
      eventService.subscribe(org.scijava.event.bushe.ObjectEvent.class, doubleSubscriber);
      eventService.subscribe("org.scijava.event.bushe.ObjectEvent.class", doubleSubscriber);
      ObjectEvent evt = new ObjectEvent("Foo", "Bar");
      assertEquals(evt.getEventObject(), "Bar");
      eventService.publish(evt);
      assertEquals(1, doubleSubscriber.timesEventCalled);
      assertEquals(0, doubleSubscriber.timesTopicCalled);
      assertEquals(evt, doubleSubscriber.lastEvent);
      assertEquals(null, doubleSubscriber.lastEventString);
      eventService.publish("org.scijava.event.bushe.ObjectEvent.class", "Bar");
      assertEquals(1, doubleSubscriber.timesEventCalled);
      assertEquals(1, doubleSubscriber.timesTopicCalled);
      assertEquals(evt, doubleSubscriber.lastEvent);
      assertEquals("org.scijava.event.bushe.ObjectEvent.class", doubleSubscriber.lastEventString);
   }

   public void testRegex() {
      DoubleSubscriber doubleSubscriber = new DoubleSubscriber();
      Pattern pat = Pattern.compile("Foo[1-5]");
      eventService.subscribe(pat, doubleSubscriber);
      List subscribers = eventService.getSubscribersToPattern(pat);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribersByPattern("Foo1");
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribers("Foo1");
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());

      eventService.publish("Foo1", "Bar");
      assertEquals(0, doubleSubscriber.timesEventCalled);
      assertEquals(1, doubleSubscriber.timesTopicCalled);
      assertEquals(null, doubleSubscriber.lastEvent);
      assertEquals("Foo1", doubleSubscriber.lastEventString);
      eventService.publish("Foo2", "Bar");
      assertEquals(0, doubleSubscriber.timesEventCalled);
      assertEquals(2, doubleSubscriber.timesTopicCalled);
      assertEquals(null, doubleSubscriber.lastEvent);
      assertEquals("Foo2", doubleSubscriber.lastEventString);
   }

   public void testTypeSubscription() {
      DoubleSubscriber subscriber = new DoubleSubscriber();

      eventService.subscribe(TopLevelEvent.class, subscriber);
      List subscribers = eventService.getSubscribersToClass(TopLevelEvent.class);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribersToClass(DerivedEvent.class);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribers(DerivedEvent.class);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());
      subscribers = eventService.getSubscribers(TopLevelEvent.class);
      assertNotNull(subscribers);
      assertEquals(1, subscribers.size());

      DerivedEvent derivedEvent = new DerivedEvent(this);
      eventService.publish(derivedEvent);
      assertEquals(1, subscriber.timesEventCalled);
      assertEquals(0, subscriber.timesTopicCalled);
      assertEquals(derivedEvent, subscriber.lastEvent);
      assertEquals(null, subscriber.lastEventString);
      TopLevelEvent topLevelEvent = new TopLevelEvent(this);
      eventService.publish(topLevelEvent);
      assertEquals(2, subscriber.timesEventCalled);
      assertEquals(0, subscriber.timesTopicCalled);
      assertEquals(topLevelEvent, subscriber.lastEvent);
      assertEquals(null, subscriber.lastEventString);
   }

   //Parameterized Type
   public void testParameterizedEvent() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
      final int[] timesCalled = new int[1];
      ParameterizedEvent<String> stringRequestEvent = new ParameterizedEvent<String>();
      ParameterizedEvent<Integer> integerRequestEvent = new ParameterizedEvent<Integer>();

      TypeReference<ParameterizedEvent<String>> stringTypeReference = new TypeReference<ParameterizedEvent<String>>(){};
      TypeReference<ParameterizedEvent<Integer>> integerTypeReference = new TypeReference<ParameterizedEvent<Integer>>(){};
//      ParameterizedEvent<String> dre = stringTypeReference.newInstance();
//      System.out.println("dre.getClass()"+dre.getClass());
//      System.out.println("stringTypeReference"+ stringTypeReference);
//      System.out.println("stringTypeReference.getType()"+ stringTypeReference.getType());

//      You can't simply do this, the TypeReference's generic type is important here
//      Type superclass = integerRequestEvent.getClass().getGenericSuperclass();
//      Type type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
//      System.out.println("superclass="+superclass);
//      System.out.println("type="+type);

      eventService.subscribe(stringTypeReference.getType(), new EventSubscriber() {
         public void onEvent(Object event) {
            timesCalled[0]++;
         }
      });
      eventService.publish(stringTypeReference.getType(), stringRequestEvent);
      eventService.publish(integerTypeReference.getType(), integerRequestEvent);
      assertEquals(1, timesCalled[0]);
   }

   public void testParameterizedEventMultiParams() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
      final int[] timesCalled = new int[1];
      DoublyParameterizedEvent<String, String> stringRequestEvent = new DoublyParameterizedEvent<String, String>();
      DoublyParameterizedEvent<Integer, String> integerRequestEvent = new DoublyParameterizedEvent<Integer, String>();
      DoublyParameterizedEvent<String, Integer> switchRequestEvent = new DoublyParameterizedEvent<String, Integer>();

      TypeReference<DoublyParameterizedEvent<String, String>> stringTypeReference = new TypeReference<DoublyParameterizedEvent<String, String>>(){};
      TypeReference<DoublyParameterizedEvent<Integer, String>> integerTypeReference = new TypeReference<DoublyParameterizedEvent<Integer, String>>(){};
      TypeReference<DoublyParameterizedEvent<String, Integer>> switchTypeReference = new TypeReference<DoublyParameterizedEvent<String, Integer>>(){};

      eventService.subscribe(stringTypeReference.getType(), new EventSubscriber() {
         public void onEvent(Object event) {
            timesCalled[0]++;
         }
      });
      eventService.subscribe(integerTypeReference.getType(), new EventSubscriber() {
         public void onEvent(Object event) {
            timesCalled[0]++;
         }
      });
      eventService.publish(stringTypeReference.getType(), stringRequestEvent);
      assertEquals(1, timesCalled[0]);
      eventService.publish(integerTypeReference.getType(), integerRequestEvent);
      assertEquals(2, timesCalled[0]);
      eventService.publish(switchTypeReference.getType(), switchRequestEvent);
      assertEquals(2, timesCalled[0]);
   }

   public void testWildcardSubscription() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
      final int[] timesCalled = new int[1];
      ParameterizedEvent<JComponent> jComponentRequestEvent = new ParameterizedEvent<JComponent>();

      TypeReference<ParameterizedEvent<? extends Container>> containerWildcardTypeRef = new TypeReference<ParameterizedEvent<? extends Container>>(){};

      eventService.subscribe(containerWildcardTypeRef.getType(), new EventSubscriber() {
         public void onEvent(Object event) {
            timesCalled[0]++;
         }
      });
      TypeReference<ParameterizedEvent<JComponent>> jComponentTypeRef = new TypeReference<ParameterizedEvent<JComponent>>(){};

      eventService.publish(jComponentTypeRef.getType(), jComponentRequestEvent);
      assertEquals(1, timesCalled[0]);

      //publishing a Component should not hit the wildcard since it doesn't extend Component
      TypeReference<ParameterizedEvent<Component>> componentTypeRef = new TypeReference<ParameterizedEvent<Component>>(){};
      ParameterizedEvent<Component> componentRequestEvent = new ParameterizedEvent<Component>();
      eventService.publish(componentTypeRef.getType(), componentRequestEvent);
      assertEquals(1, timesCalled[0]);

      //publish wildcards is a not yet supported
      try {
         eventService.publish(containerWildcardTypeRef.getType(), jComponentRequestEvent);
         fail();
      } catch (IllegalArgumentException ex) {
      }
      assertEquals(1, timesCalled[0]);

      //Test super wildcard, should be opposite of above
      eventService.clearAllSubscribers();
      TypeReference<ParameterizedEvent<? super Container>> containerSuperWildcardTypeRef = new TypeReference<ParameterizedEvent<? super Container>>(){};
      eventService.subscribe(containerSuperWildcardTypeRef.getType(), new EventSubscriber() {
         public void onEvent(Object event) {
            timesCalled[0]++;
         }
      });
      eventService.publish(jComponentTypeRef.getType(), jComponentRequestEvent);
      assertEquals(1, timesCalled[0]);
      eventService.publish(componentTypeRef.getType(), componentRequestEvent);
      assertEquals(2, timesCalled[0]);

      //Test exact matches
   }

   public class ParameterizedEvent<E> {
      private Collection<E> data;
      public Collection<E> getData() {
         return data;
      }
   }

   public class DoublyParameterizedEvent<E,F> {
      private Map<E,F> data;
      public Map<E,F> getData() {
         return data;
      }
   }

   class DoubleSubscriber implements EventTopicSubscriber, EventSubscriber {
      public int timesTopicCalled = 0;
      public int timesEventCalled = 0;
      public String lastEventString;
      public Object lastEvent;

      public void onEvent(String topic, Object data) {
         timesTopicCalled++;
         lastEventString = topic;
      }

      public void onEvent(Object evt) {
         timesEventCalled++;
         lastEvent = evt;
      }
   }

   class TopLevelEvent extends AbstractEventServiceEvent {
      public TopLevelEvent(Object source) {
         super(source);
      }
   }

   class DerivedEvent extends TopLevelEvent {
      public DerivedEvent(Object source) {
         super(source);
      }
   }


   /**
    * Test match for generic type's of generic types
    */
   public void testGenericGeneric() {
      final int[] timesCalled = new int[1];
      DataRequestEvent<List<Integer>> request = new DataRequestEvent<List<Integer>>();
      Type type = new TypeReference<DataRequestEvent<List<Integer>>>() {}.getType();
      eventService.subscribe(type, new EventSubscriber() {
         public void onEvent(Object event) {
            timesCalled[0]++;
         }
      });
      eventService.publish(type, request);
      assertEquals(1, timesCalled[0]);
      DataRequestEvent<List<String>> stringRequest = new DataRequestEvent<List<String>>();
      Type stringType = new TypeReference<DataRequestEvent<List<String>>>() {}.getType();
      eventService.publish(stringType , stringRequest);
      assertEquals(1, timesCalled[0]);
   }

   public void testTopicsCache() {
      Object a1 = new Object();
      //All of the above should be checked with topics.
      //Topics should test that exact matches are preferred over pattern matches
      //Test that a default setting does not cache
      EventService es = new ThreadSafeEventService(null);
      es.publish("IceCream.Vanilla", a1);
      List events = es.getCachedTopicData("IceCream.Vanilla");
      assertNull(events);
      Object lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertNull(lastEventObj);
      assertEquals(0, es.getCacheSizeForTopic("IceCream.Vanilla"));
      assertEquals(0, es.getDefaultCacheSizePerClassOrTopic());
      //Test that changing the default to 1 caches 1
      es.setDefaultCacheSizePerClassOrTopic(1);
      assertEquals(1, es.getDefaultCacheSizePerClassOrTopic());
      Object publishedEventObj = new Object();
      es.publish("IceCream.Vanilla", publishedEventObj);
      events = es.getCachedTopicData("IceCream.Vanilla");
      assertNotNull(events);
      assertEquals(1, events.size());
      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertTrue(lastEventObj == publishedEventObj);
      assertEquals(1, es.getCacheSizeForTopic("IceCream.Vanilla"));
      //subscribe and see if it still works and that the new event is cached
      EventTopicSubscriber sub = new EventTopicSubscriber() {
         public void onEvent(String topic, Object data) {
            System.out.println("Barrrr");
         }
      };
      es.subscribe("IceCream.Vanilla", sub);
      publishedEventObj = new Object();
      es.publish("IceCream.Vanilla", publishedEventObj);
      events = es.getCachedTopicData("IceCream.Vanilla");
      assertNotNull(events);
      assertEquals(1, events.size());
      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertTrue(lastEventObj == publishedEventObj);
      assertEquals(1, es.getCacheSizeForTopic("IceCream.Vanilla"));

      //Test that changing the default to 5 caches 5
      es.setDefaultCacheSizePerClassOrTopic(5);
      assertEquals(5, es.getDefaultCacheSizePerClassOrTopic());
      Object publishedEventObj2 = new Object();
      Object publishedEventObj3 = new Object();
      Object publishedEventObj4 = new Object();
      Object publishedEventObj5 = new Object();
      es.publish("IceCream.Vanilla", publishedEventObj2);
      es.publish("IceCream.Vanilla", publishedEventObj3);
      es.publish("IceCream.Vanilla", publishedEventObj4);
      es.publish("IceCream.Vanilla", publishedEventObj5);
      events = es.getCachedTopicData("IceCream.Vanilla");
      assertNotNull(events);
      assertEquals(5, events.size());
      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertTrue(lastEventObj == publishedEventObj5);
      assertEquals(5, es.getCacheSizeForTopic("IceCream.Vanilla"));
      Object publishedEventObj6 = new Object();
      es.publish("IceCream.Vanilla", publishedEventObj6);
      assertEquals(5, events.size());
      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertTrue(lastEventObj == publishedEventObj6);
      assertEquals(5, es.getCacheSizeForTopic("IceCream.Vanilla"));

      //Test that setting a topic cache with 10 caches 10 for that topic, but the default for the others
      es.setCacheSizeForTopic("IceCream.Vanilla", 10);
      Object publishedEventObjB1 = new Object();
      Object publishedEventObjB2 = new Object();
      Object publishedEventObjB3 = new Object();
      Object publishedEventObjB4 = new Object();
      Object publishedEventObjB5 = new Object();
      Object publishedEventObjB6 = new Object();
      es.publish("IceCream.Vanilla", publishedEventObj6);
      es.publish("IceCream.Vanilla", publishedEventObj6);//see if reuse is OK
      es.publish("IceCream.Blueberry", publishedEventObjB1);
      es.publish("IceCream.Blueberry", publishedEventObjB2);
      es.publish("IceCream.Blueberry", publishedEventObjB3);
      es.publish("IceCream.Blueberry", publishedEventObjB4);
      es.publish("IceCream.Blueberry", publishedEventObjB5);
      es.publish("IceCream.Blueberry", publishedEventObjB6);
      es.publish("IceCream.Vanilla", publishedEventObj6);
      es.publish("IceCream.Vanilla", publishedEventObj6);
      Object publishedEvent10 = new Object();
      es.publish("IceCream.Vanilla", publishedEvent10);
      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertTrue(lastEventObj == publishedEvent10);
      events = es.getCachedTopicData("IceCream.Vanilla");
      assertNotNull(events);
      assertEquals(10, events.size());
      assertEquals(10, es.getCacheSizeForTopic("IceCream.Vanilla"));
      assertTrue(publishedEvent10 == events.get(0));
      assertTrue(publishedEventObj6 == events.get(1));
      assertTrue(publishedEventObj6 == events.get(2));
      assertTrue(publishedEventObj6 == events.get(3));
      assertTrue(publishedEventObj6 == events.get(4));
      assertTrue(publishedEventObj6 == events.get(5));
      assertTrue(publishedEventObj5 == events.get(6));
      assertTrue(publishedEventObj4 == events.get(7));
      assertTrue(publishedEventObj3 == events.get(8));
      assertTrue(publishedEventObj2 == events.get(9));
      lastEventObj = es.getLastTopicData("IceCream.Blueberry");
      assertTrue(lastEventObj == publishedEventObjB6);
      events = es.getCachedTopicData("IceCream.Blueberry");
      assertNotNull(events);
      assertEquals(5, events.size());
      assertEquals(5, es.getCacheSizeForTopic("IceCream.Blueberry"));
      assertTrue(publishedEventObjB6 == events.get(0));
      assertTrue(publishedEventObjB5 == events.get(1));
      assertTrue(publishedEventObjB4 == events.get(2));
      assertTrue(publishedEventObjB3 == events.get(3));
      assertTrue(publishedEventObjB2 == events.get(4));
      //this makes the cache resize to a smaller amount
      es.setCacheSizeForTopic("IceCream.Vanilla", 1);
      es.publish("IceCream.Vanilla", publishedEventObj4);
      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertTrue(lastEventObj == publishedEventObj4);
      events = es.getCachedTopicData("IceCream.Vanilla");
      assertNotNull(events);
      assertEquals(1, events.size());
      assertEquals(1, es.getCacheSizeForTopic("IceCream.Vanilla"));
      es.publish("IceCream.Blueberry", publishedEventObjB4);
      lastEventObj = es.getLastTopicData("IceCream.Blueberry");
      assertTrue(lastEventObj == publishedEventObjB4);
      events = es.getCachedTopicData("IceCream.Blueberry");
      assertNotNull(events);
      assertEquals(5, events.size());
      assertEquals(5, es.getCacheSizeForTopic("IceCream.Blueberry"));
      assertTrue(publishedEventObjB4 == events.get(0));
      assertTrue(publishedEventObjB6 == events.get(1));
      assertTrue(publishedEventObjB5 == events.get(2));
      assertTrue(publishedEventObjB4 == events.get(3));
      assertTrue(publishedEventObjB3 == events.get(4));

      //Test pattern cache size works, but does not override a specific topic setting
      Pattern pattern = Pattern.compile("IceCream.*");
      es.setDefaultCacheSizePerClassOrTopic(5);
      es.setCacheSizeForTopic(pattern, 2);
      es.setCacheSizeForTopic("IceCream.Vanilla", 3);
      Object publishedEventObjX1 = new Object();
      Object publishedEventObjX2 = new Object();
      Object publishedEventObjX3 = new Object();
      Object publishedEventObjX4 = new Object();
      Object publishedEventObjX5 = new Object();
      Object publishedEventObjX6 = new Object();
      Object publishedEventObjC1 = new Object();
      Object publishedEventObjC2 = new Object();
      Object publishedEventObjC3 = new Object();
      Object publishedEventObjC4 = new Object();
      es.publish("X", publishedEventObjX1);
      es.publish("IceCream.Vanilla", publishedEventObj6);
      es.publish("IceCream.Chocolate", publishedEventObjC1);
      es.publish("X", publishedEventObjX2);//see if reuse is OK
      es.publish("X", publishedEventObjX3);
      es.publish("IceCream.Chocolate", publishedEventObjC2);
      es.publish("X", publishedEventObjX4);
      es.publish("IceCream.Vanilla", publishedEventObj4);
      es.publish("IceCream.Vanilla", publishedEventObj5);
      es.publish("IceCream.Vanilla", publishedEventObj6);
      es.publish("X", publishedEventObjX5);
      es.publish("IceCream.Chocolate", publishedEventObjC3);
      es.publish("X", publishedEventObjX6);
      es.publish("IceCream.Chocolate", publishedEventObjC4);

      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertTrue(lastEventObj == publishedEventObj6);
      events = es.getCachedTopicData("IceCream.Vanilla");
      assertNotNull(events);
      assertEquals(3, events.size());
      assertEquals(3, es.getCacheSizeForTopic("IceCream.Vanilla"));
      lastEventObj = es.getLastTopicData("X");
      assertTrue(lastEventObj == publishedEventObjX6);
      events = es.getCachedTopicData("X");
      assertNotNull(events);
      assertEquals(5, events.size());
      assertEquals(5, es.getCacheSizeForEventClass(EventX.class));
      assertTrue(publishedEventObjX6 == events.get(0));
      assertTrue(publishedEventObjX5 == events.get(1));
      assertTrue(publishedEventObjX4 == events.get(2));
      assertTrue(publishedEventObjX3 == events.get(3));
      assertTrue(publishedEventObjX2 == events.get(4));
      events = es.getCachedTopicData("IceCream.Chocolate");
      assertNotNull(events);
      assertEquals(2, events.size());
      assertEquals(2, es.getCacheSizeForTopic("IceCream.Chocolate"));
      assertTrue(publishedEventObjC4 == events.get(0));
      assertTrue(publishedEventObjC3 == events.get(1));

      es.clearCache("IceCream.Blueberry");
      events = es.getCachedTopicData("IceCream.Blueberry");
      assertNull(events);
      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertNotNull(lastEventObj);
      events = es.getCachedTopicData("IceCream.Vanilla");
      assertNotNull(events);
      assertEquals(3, events.size());
      lastEventObj = es.getLastTopicData("IceCream.Chocolate");
      assertNotNull(lastEventObj);
      events = es.getCachedTopicData("IceCream.Chocolate");
      assertNotNull(events);
      assertEquals(2, events.size());
      lastEventObj = es.getLastTopicData("X");
      assertNotNull(lastEventObj);
      events = es.getCachedTopicData("X");
      assertNotNull(events);
      assertEquals(5, events.size());
      es.clearCache(pattern);
      events = es.getCachedTopicData("IceCream.Vanilla");
      assertNull(events);
      lastEventObj = es.getLastTopicData("IceCream.Chocolate");
      assertNull(lastEventObj);
      lastEventObj = es.getLastTopicData("X");
      assertNotNull(lastEventObj);
      events = es.getCachedTopicData("X");
      assertNotNull(events);
      assertEquals(5, events.size());

      es.publish("X", publishedEventObjX6);
      es.publish("IceCream.Blueberry", publishedEventObjB4);
      es.publish("IceCream.Vanilla", publishedEvent10);
      es.clearCache();
      lastEventObj = es.getLastTopicData("IceCream.Vanilla");
      assertNull(lastEventObj);
      lastEventObj = es.getLastTopicData("IceCream.Blueberry");
      assertNull(lastEventObj);
      lastEventObj = es.getLastTopicData("IceCream.Chocolate");
      assertNull(lastEventObj);
      lastEventObj = es.getLastTopicData("X");
      assertNull(lastEventObj);
   }


   public void testEventsCache() {
      //Test that a default setting does not cache
      EventService es = new ThreadSafeEventService(null);
      es.publish(new EventA());
      List<EventA> aEvents = es.getCachedEvents(EventA.class);
      assertNull(aEvents);
      EventA lastAEvent = es.getLastEvent(EventA.class);
      assertNull(lastAEvent);
      assertEquals(0, es.getCacheSizeForEventClass(EventA.class));
      assertEquals(0, es.getDefaultCacheSizePerClassOrTopic());
      //Test that changing the default to 1 caches 1
      es.setDefaultCacheSizePerClassOrTopic(1);
      assertEquals(1, es.getDefaultCacheSizePerClassOrTopic());
      EventA publishedEvent = new EventA();
      es.publish(publishedEvent);
      aEvents = es.getCachedEvents(EventA.class);
      assertNotNull(aEvents);
      assertEquals(1, aEvents.size());
      lastAEvent = es.getLastEvent(EventA.class);
      assertTrue(lastAEvent == publishedEvent);
      assertEquals(1, es.getCacheSizeForEventClass(EventA.class));
      //subscribe and see if it still works and that the new event is cached
      EventSubscriber sub = new EventSubscriber() {
         public void onEvent(Object evt) {
            System.out.println("Fooo");
         }
      };
      es.subscribe(EventA.class, sub);
      publishedEvent = new EventA();
      es.publish(publishedEvent);
      aEvents = es.getCachedEvents(EventA.class);
      assertNotNull(aEvents);
      assertEquals(1, aEvents.size());
      lastAEvent = es.getLastEvent(EventA.class);
      assertTrue(lastAEvent == publishedEvent);
      assertEquals(1, es.getCacheSizeForEventClass(EventA.class));

      //Test that changing the default to 5 caches 5
      es.setDefaultCacheSizePerClassOrTopic(5);
      assertEquals(5, es.getDefaultCacheSizePerClassOrTopic());
      EventA publishedEvent2 = new EventA();
      EventA publishedEvent3 = new EventA();
      EventA publishedEvent4 = new EventA();
      EventA publishedEvent5 = new EventA();
      es.publish(publishedEvent2);
      es.publish(publishedEvent3);
      es.publish(publishedEvent4);
      es.publish(publishedEvent5);
      aEvents = es.getCachedEvents(EventA.class);
      assertNotNull(aEvents);
      assertEquals(5, aEvents.size());
      lastAEvent = es.getLastEvent(EventA.class);
      assertTrue(lastAEvent == publishedEvent5);
      assertEquals(5, es.getCacheSizeForEventClass(EventA.class));
      EventA publishedEvent6 = new EventA();
      es.publish(publishedEvent6);
      assertEquals(5, aEvents.size());
      lastAEvent = es.getLastEvent(EventA.class);
      assertTrue(lastAEvent == publishedEvent6);
      assertEquals(5, es.getCacheSizeForEventClass(EventA.class));

      //Test that overriding a single event class with 10 caches 10 for that event, but the default for the others
      es.setCacheSizeForEventClass(EventA.class, 10);
      EventB publishedEventB1 = new EventB();
      EventB publishedEventB2 = new EventB();
      EventB publishedEventB3 = new EventB();
      EventB publishedEventB4 = new EventB();
      EventB publishedEventB5 = new EventB();
      EventB publishedEventB6 = new EventB();
      es.publish(publishedEvent6);
      es.publish(publishedEvent6);//see if reuse is OK
      es.publish(publishedEventB1);
      es.publish(publishedEventB2);
      es.publish(publishedEventB3);
      es.publish(publishedEventB4);
      es.publish(publishedEventB5);
      es.publish(publishedEventB6);
      es.publish(publishedEvent6);
      es.publish(publishedEvent6);
      EventA publishedEvent10 = new EventA();
      es.publish(publishedEvent10);
      lastAEvent = es.getLastEvent(EventA.class);
      assertTrue(lastAEvent == publishedEvent10);
      aEvents = es.getCachedEvents(EventA.class);
      assertNotNull(aEvents);
      assertEquals(10, aEvents.size());
      assertEquals(10, es.getCacheSizeForEventClass(EventA.class));
      assertTrue(publishedEvent10 == aEvents.get(0));
      assertTrue(publishedEvent6 == aEvents.get(1));
      assertTrue(publishedEvent6 == aEvents.get(2));
      assertTrue(publishedEvent6 == aEvents.get(3));
      assertTrue(publishedEvent6 == aEvents.get(4));
      assertTrue(publishedEvent6 == aEvents.get(5));
      assertTrue(publishedEvent5 == aEvents.get(6));
      assertTrue(publishedEvent4 == aEvents.get(7));
      assertTrue(publishedEvent3 == aEvents.get(8));
      assertTrue(publishedEvent2 == aEvents.get(9));
      EventB lastBEvent = es.getLastEvent(EventB.class);
      assertTrue(lastBEvent == publishedEventB6);
      List<EventB> bEvents = es.getCachedEvents(EventB.class);
      assertNotNull(bEvents);
      assertEquals(5, bEvents.size());
      assertEquals(5, es.getCacheSizeForEventClass(EventB.class));
      assertTrue(publishedEventB6 == bEvents.get(0));
      assertTrue(publishedEventB5 == bEvents.get(1));
      assertTrue(publishedEventB4 == bEvents.get(2));
      assertTrue(publishedEventB3 == bEvents.get(3));
      assertTrue(publishedEventB2 == bEvents.get(4));
      //this makes the cache resize smaller
      es.setCacheSizeForEventClass(EventA.class, 1);
      es.publish(publishedEvent4);
      lastAEvent = es.getLastEvent(EventA.class);
      assertTrue(lastAEvent == publishedEvent4);
      aEvents = es.getCachedEvents(EventA.class);
      assertNotNull(aEvents);
      assertEquals(1, aEvents.size());
      assertEquals(1, es.getCacheSizeForEventClass(EventA.class));
      es.publish(publishedEventB4);
      lastBEvent = es.getLastEvent(EventB.class);
      assertTrue(lastBEvent == publishedEventB4);
      bEvents = es.getCachedEvents(EventB.class);
      assertNotNull(bEvents);
      assertEquals(5, bEvents.size());
      assertEquals(5, es.getCacheSizeForEventClass(EventB.class));
      assertTrue(publishedEventB4 == bEvents.get(0));
      assertTrue(publishedEventB6 == bEvents.get(1));
      assertTrue(publishedEventB5 == bEvents.get(2));
      assertTrue(publishedEventB4 == bEvents.get(3));
      assertTrue(publishedEventB3 == bEvents.get(4));

      //Test that overriding a subclass event class with 2 changes and a derived class with 5 ...
      //caches 5 for the derived class
      //caches 2 for the subclass
      //caches 2 for another derived class
      // and that interfaces only take effect if the cache size of a class or it's superclasses has been set.
      es.setCacheSizeForEventClass(EventA.class, 2);
      es.setCacheSizeForEventClass(EventX.class, 5);
      es.setCacheSizeForEventClass(Serializable.class, 3);
      EventX publishedEventX1 = new EventX();
      EventX publishedEventX2 = new EventX();
      EventX publishedEventX3 = new EventX();
      EventX publishedEventX4 = new EventX();
      EventX publishedEventX5 = new EventX();
      EventX publishedEventX6 = new EventX();
      EventC publishedEventC1 = new EventC();
      EventC publishedEventC2 = new EventC();
      EventC publishedEventC3 = new EventC();
      EventC publishedEventC4 = new EventC();
      es.publish(publishedEventX1);
      es.publish(publishedEvent6);
      es.publish(publishedEventC1);
      es.publish(publishedEventX2);//see if reuse is OK
      es.publish(publishedEventX3);
      es.publish(publishedEventC2);
      es.publish(publishedEventX4);
      es.publish(publishedEvent4);
      es.publish(publishedEventX5);
      es.publish(publishedEventC3);
      es.publish(publishedEventX6);
      es.publish(publishedEventC4);

      lastAEvent = es.getLastEvent(EventA.class);
      assertTrue(lastAEvent == publishedEvent4);
      aEvents = es.getCachedEvents(EventA.class);
      assertNotNull(aEvents);
      assertEquals(2, aEvents.size());
      assertEquals(2, es.getCacheSizeForEventClass(EventA.class));
      lastAEvent = es.getLastEvent(EventX.class);
      assertTrue(lastAEvent == publishedEventX6);
      List<EventX> xEvents = es.getCachedEvents(EventX.class);
      assertNotNull(xEvents);
      assertEquals(5, xEvents.size());
      assertEquals(5, es.getCacheSizeForEventClass(EventX.class));
      assertTrue(publishedEventX6 == xEvents.get(0));
      assertTrue(publishedEventX5 == xEvents.get(1));
      assertTrue(publishedEventX4 == xEvents.get(2));
      assertTrue(publishedEventX3 == xEvents.get(3));
      assertTrue(publishedEventX2 == xEvents.get(4));
      try {
         Serializable serializableEvent = es.getLastEvent(Serializable.class);
         fail("Shouldn't be able to pass an interface.");
      } catch (IllegalArgumentException ex) {
      }
      EventC lastCEvent = es.getLastEvent(EventC.class);
      assertTrue(lastCEvent == publishedEventC4);
      try {
         List<Serializable> serializableEvents = es.getCachedEvents(Serializable.class);
         fail("Shouldn't be able to pass an interface.");
      } catch (IllegalArgumentException ex) {
      }
      List<EventC> cEvents = es.getCachedEvents(EventC.class);
      assertNotNull(cEvents);
      assertEquals(3, cEvents.size());
      assertEquals(3, es.getCacheSizeForEventClass(EventC.class));

      es.clearCache(EventB.class);
      bEvents = es.getCachedEvents(EventB.class);
      assertNull(bEvents);
      lastAEvent = es.getLastEvent(EventA.class);
      assertNotNull(lastAEvent);
      aEvents = es.getCachedEvents(EventA.class);
      assertNotNull(aEvents);
      assertEquals(2, aEvents.size());
      EventX lastXEvent = es.getLastEvent(EventX.class);
      assertNotNull(lastXEvent);
      xEvents = es.getCachedEvents(EventX.class);
      assertNotNull(xEvents);
      assertEquals(5, xEvents.size());
      es.clearCache(EventA.class);
      aEvents = es.getCachedEvents(EventA.class);
      assertNull(aEvents);
      lastXEvent = es.getLastEvent(EventX.class);
      xEvents = es.getCachedEvents(EventX.class);
      assertNull(lastXEvent);
      assertNull(xEvents);

      es.publish(publishedEventX6);
      es.publish(publishedEventB4);
      es.publish(publishedEvent10);
      es.clearCache();
      lastAEvent = es.getLastEvent(EventA.class);
      assertNull(lastAEvent);
      lastBEvent = es.getLastEvent(EventB.class);
      assertNull(lastBEvent);
      lastAEvent = es.getLastEvent(EventX.class);
      assertNull(lastAEvent);
   }


   //Base
   public static class EventA implements EventServiceEvent {
      /** @return The issuer of the event. */
      public Object getSource() {
         return null;
      }
   }

   //No relation
   public static class EventB implements EventServiceEvent, Serializable {
      /** @return The issuer of the event. */
      public Object getSource() {
         return null;
      }
   }

   //No relation
   public static class EventC implements EventServiceEvent, Serializable {
      /** @return The issuer of the event. */
      public Object getSource() {
         return null;
      }
   }

   //Derived 1
   public static class EventX extends EventA {
      /** @return The issuer of the event. */
      public Object getSource() {
         return null;
      }
   }

   //Derived 2
   public static class EventY extends EventA {
      /** @return The issuer of the event. */
      public Object getSource() {
         return null;
      }
   }

}
