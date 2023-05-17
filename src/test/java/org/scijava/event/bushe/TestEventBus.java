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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.awt.EventQueue;

import javax.swing.JComponent;

import junit.framework.TestCase;

public class TestEventBus extends TestCase {

   private EventSubscriber eventSubscriber = null;
   private EventTopicSubscriber eventTopicSubscriber;
   private EBTestCounter testCounter = new EBTestCounter();

   public TestEventBus(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
      EventBus.getGlobalEventService().clearAllSubscribers();
   }

   protected void tearDown() throws Exception {
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
      SubscriberForTest test = new SubscriberForTest(testCounter, throwException);
      return test;
   }

   private EventTopicSubscriber createEventTopicSubscriber(boolean throwException) {
      return new TopicSubscriberForTest(testCounter, throwException);
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

   public void testSubscribe() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = EventBus.subscribe(getEventClass(), subscriber);
      assertTrue("testSubscribe(new subscriber)", actualReturn);

      actualReturn = EventBus.subscribe(getEventClass(), subscriber);
      assertFalse("testSubscribe(duplicate subscriber)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      try {
         actualReturn = EventBus.subscribe((Class) null, getEventSubscriber());
         fail("subscribeStrongly(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = EventBus.subscribe(getEventClass(), null);
         fail("subscribeStrongly(x, null) should have thrown exception");
      } catch (Exception e) {
      }

   }
   
   public static class SwingThreadTestEventSubscriber implements EventSubscriber {
      public boolean wasOnSwingThread;

      public void onEvent(Object event) {
         wasOnSwingThread = EventQueue.isDispatchThread();
      }
   }

   public void testSwingThreading() {   
      SwingThreadTestEventSubscriber sub = new SwingThreadTestEventSubscriber();
      EventBus.subscribe(Number.class, sub);
      EventBus.publish(1);
      EDTUtil.waitForEDT();
      assertTrue("Expected the EventBus to dispatch on the EDT", sub.wasOnSwingThread);
   }
   
   public void testSubscribeWeakly() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = EventBus.subscribe(getEventClass(), subscriber);
      assertTrue("testSubscribeWeakly(new subscriber)", actualReturn);

      actualReturn = EventBus.subscribe(getEventClass(), subscriber);
      assertFalse("testSubscribe(duplicate subscriber)", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
      subscriber = null;
      System.gc();
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      try {
         actualReturn = EventBus.subscribeStrongly((Class) null, getEventSubscriber());
         fail("subscribeStrongly(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = EventBus.subscribeStrongly(getEventClass(), null);
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

      actualReturn = EventBus.subscribe(getEventClass(), subscriber);

      VetoEventListener vetoListener = new VetoEventListenerForTest();
      actualReturn = EventBus.subscribeVetoListenerStrongly(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      EventBus.unsubscribeVetoListener(getEventClass(), vetoListener);
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);

   }

   public void testVetoException() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = EventBus.subscribe(getEventClass(), subscriber);
      assertTrue(actualReturn);
      VetoEventListener vetoListener = new VetoEventListenerForTest(true);
      actualReturn = EventBus.subscribeVetoListenerStrongly(getEventClass(), vetoListener);
      assertTrue(actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      EventBus.unsubscribeVetoListener(getEventClass(), vetoListener);
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 2, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      System.out.println("Prevent garbage collection of subscriber by sys'outing it at the end:"+subscriber);
   }

   public void testVetoTopic() {
      boolean actualReturn;
      EventTopicSubscriber subscriber = createEventTopicSubscriber(false);

      actualReturn = EventBus.subscribeStrongly("FooTopic", subscriber);

      VetoTopicEventListener vetoListener = new VetoTopicEventListener() {
         public boolean shouldVeto(String topic, Object data) {
            return true;
         }
      };
      actualReturn = EventBus.subscribeVetoListenerStrongly("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish("FooTopic", "Bar");
      EDTUtil.waitForEDT();

      //The test passes if 0 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      EventBus.unsubscribeVetoListener("FooTopic", vetoListener);
      EventBus.publish("FooTopic", "Bar");
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }


   public void testVetoWeak() {
      boolean actualReturn;
      EventSubscriber subscriber = createEventSubscriber(false);

      actualReturn = EventBus.subscribe(getEventClass(), subscriber);

      VetoEventListener vetoListener = new VetoEventListener() {
         public boolean shouldVeto(Object evt) {
            return true;
         }
      };
      actualReturn = EventBus.subscribeVetoListener(getEventClass(), vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      vetoListener = null;
      System.gc();
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   public void testVetoTopicWeak() {
      boolean actualReturn;
      EventTopicSubscriber subscriber = createEventTopicSubscriber(false);

      actualReturn = EventBus.subscribeStrongly("FooTopic", subscriber);

      VetoTopicEventListener vetoListener = new VetoTopicEventListener() {
         public boolean shouldVeto(String topic, Object data) {
            return true;
         }
      };
      actualReturn = EventBus.subscribeVetoListener("FooTopic", vetoListener);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish("FooTopic", "Bar");
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
      vetoListener = null;
      System.gc();
      EventBus.publish("FooTopic", "Bar");
      EDTUtil.waitForEDT();
      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testVeto(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testVeto(exceptions)", 0, testCounter.subscribeExceptionCount);
   }


   public void testUnsubscribe() {
      EventBus.subscribe(getEventClass(), getEventSubscriber(false));

      boolean actualReturn;

      try {
         actualReturn = EventBus.unsubscribe((Class) null, getEventSubscriber());
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = EventBus.unsubscribe(getEventClass(), null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      actualReturn = EventBus.unsubscribe(getEventClass(), getEventSubscriber());
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   public void testUnsubscribeTopic() {
      EventTopicSubscriber eventTopicSubscriber = createEventTopicSubscriber(false);
      EventBus.subscribeStrongly("FooTopic", eventTopicSubscriber);

      boolean actualReturn;

      try {
         actualReturn = EventBus.unsubscribe((String) null, eventTopicSubscriber);
         fail("unsubscribe(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         actualReturn = EventBus.unsubscribe("FooTopic", null);
         fail("unsubscribe(x, null) should have thrown exception");
      } catch (Exception e) {
      }

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish("FooTopic", "Foo");
      EDTUtil.waitForEDT();

      //The test passes if 1 subscribers completed and 0 subscribers threw exception.
      assertEquals("testPublish(total)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      actualReturn = EventBus.unsubscribe("FooTopic", eventTopicSubscriber);
      assertTrue("return value", actualReturn);

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish("FooTopic", "Foo");
      EDTUtil.waitForEDT();

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
         EventBus.publish(null);
         EDTUtil.waitForEDT();
         fail("publish(null) should have thrown exception");
      } catch (Exception e) {
      }

      try {
         EventBus.publish((String) null, createEvent());
         EDTUtil.waitForEDT();
         fail("publish(null, x) should have thrown exception");
      } catch (Exception e) {
      }

      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      EventBus.publish("Foo", "Bar");
      EDTUtil.waitForEDT();
      assertEquals("testPublish(completed)", 0, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);

      EventBus.subscribe(getEventClass(), createEventSubscriber(true));
      EventBus.subscribe(getEventClass(), createEventSubscriber(false));
      EventBus.subscribe(getEventClass(), createEventSubscriber(true));
      EventBus.subscribe(getEventClass(), createEventSubscriber(false));

      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      EventBus.publish(createEvent());
      EDTUtil.waitForEDT();

      //The test passes if 2 subscribers completed and 2 subscribers threw exception.
      assertEquals("testPublish(completed)", 4, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 2, testCounter.subscribeExceptionCount);

      EventSubscriber eventSubscriber = createEventSubscriber(false);
      EventBus.subscribe(ObjectEvent.class, eventSubscriber);
      testCounter.eventsHandledCount = 0;
      testCounter.subscribeExceptionCount = 0;
      ObjectEvent evt = new ObjectEvent("Foo", "Bar");
      assertEquals(evt.getEventObject(), "Bar");
      EventBus.publish(evt);
      EDTUtil.waitForEDT();
      assertEquals("testPublish(completed)", 1, testCounter.eventsHandledCount);
      assertEquals("testPublish(exceptions)", 0, testCounter.subscribeExceptionCount);
   }

   /**
    * This tests whether the EventBus has a static method for each EventService method
    */
   public void testNumOfMethods() {
      Method[] esMethods = EventService.class.getMethods();
      Method[] ebMethods = EventBus.class.getMethods();
      //Are all the es methods in the eb?
      for (int i = 0; i < esMethods.length; i++) {
         Method esMethod = esMethods[i];
         boolean foundMatch = false;
         nextMethod:
         for (int j = 0; j < ebMethods.length; j++) {
            Method ebMethod = ebMethods[j];
            if (esMethod.getName().equals(ebMethod.getName())) {
               TypeVariable<Method>[] esTypes = esMethod.getTypeParameters();
               TypeVariable<Method>[] ebTypes = ebMethod.getTypeParameters();
               if (esTypes.length != ebTypes.length) {
                  break;
               }
               for (int typeCount = 0; typeCount < ebTypes.length; typeCount++) {
                  TypeVariable<Method> esType = esTypes[typeCount];
                  TypeVariable<Method> ebType = ebTypes[typeCount];
                  if (!(ebType+"").equals((""+esType))) {
                     continue nextMethod;
                  }
               }
               Class[] esParams = esMethod.getParameterTypes();
               Class[] ebParams = ebMethod.getParameterTypes();
               if (esParams.length != ebParams.length) {
                  continue nextMethod;
               }
               for (int typeCount = 0; typeCount < ebParams.length; typeCount++) {
                  Class esType = esParams[typeCount];
                  Class ebType = ebParams[typeCount];
                  if (!ebType.equals(esType)) {
                     continue nextMethod;
                  }
               }
               foundMatch = true;
            }
         }
         if (!foundMatch) {
            System.out.println("No match for es method:" + esMethod.getName() + ", " + esMethod +", i="+i);
         }
         assertTrue(foundMatch);
      }

      //Are all the eb methods static?
      ebMethods = EventBus.class.getDeclaredMethods();
      for (int i = 0; i < ebMethods.length; i++) {
         Method ebMethod = ebMethods[i];
         int modifiers = ebMethod.getModifiers();
         boolean isStatic = Modifier.isStatic(modifiers);
         if (!isStatic) {
            System.out.println("EventBus has a non-static method:" + ebMethod);
         }
         assertTrue(isStatic);
      }
   }

   //Really a compilation test
   public void testGeneric() {
      EventBus.subscribe(String.class, new EventSubscriber<JComponent>() {
         public void onEvent(JComponent event) {
         }
      });
      EventBus.subscribe("foo", new EventTopicSubscriber<JComponent>() {
         public void onEvent(String topic, JComponent data) {
         }
      });
   }
}
