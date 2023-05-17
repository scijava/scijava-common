package org.scijava.event.bushe.annotation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import org.scijava.event.bushe.EDTUtil;
import org.scijava.event.bushe.EventBus;
import org.scijava.event.bushe.EventService;
import org.scijava.event.bushe.EventServiceLocator;
import org.scijava.event.bushe.EventServiceLocatorTestCase;
import org.scijava.event.bushe.annotation.runtime.Factory;
import org.scijava.event.bushe.annotation.runtime.SubscriberForTesting;

public class TestSubscriberAnnotation extends TestCase {

   @Override
   public void setUp() {
      EventServiceLocatorTestCase.clearEventServiceLocator();
      EventBus.getGlobalEventService();
      EventBus.clearAllSubscribers();
      AnnotatedEventSubscriber.setTimesCalled(0);
      AnnotatedEventSubscriber.setLastCall(null);
      System.gc();
   }

   protected void tearDown() throws Exception {
      EventServiceLocatorTestCase.clearEventServiceLocator();      
   }

   public void testSimple() throws InvocationTargetException, InterruptedException {
      AnnotatedEventSubscriber.setTimesColorChanged(0);
      final AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      EventBus.publish(Color.BLUE);
      Collection subs = EventBus.getSubscribers(Color.class);
      assertEquals(0, subs.size());
      EDTUtil.waitForEDT();
      assertEquals(0, AnnotatedEventSubscriber.getTimesColorChanged());
      SwingUtilities.invokeAndWait(new Runnable() {
         public void run() {
            AnnotationProcessor.process(subscriber);
         }
      });

      subs = EventBus.getSubscribers(Color.class);
      assertEquals(1, subs.size());
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesColorChanged());
      
      //Add veto
      subs = EventBus.getVetoSubscribers(Color.class);
      assertEquals(0, subs.size());
      final AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      SwingUtilities.invokeAndWait(new Runnable() {
         public void run() {
            AnnotationProcessor.process(vetoSubscriber);
         }
      });

      subs = EventBus.getSubscribers(Color.class);
      assertEquals(1, subs.size());
      subs = EventBus.getVetoSubscribers(Color.class);
      assertEquals(1, subs.size());
      EventBus.publish(Color.RED);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesColorChanged());

      SwingUtilities.invokeAndWait(new Runnable() {
         public void run() {
            AnnotationProcessor.unprocess(vetoSubscriber);
         }
      });
      EventBus.publish(Color.RED);
      EDTUtil.waitForEDT();
      assertEquals(2, AnnotatedEventSubscriber.getTimesColorChanged());
      SwingUtilities.invokeAndWait(new Runnable() {
         public void run() {
            AnnotationProcessor.unprocess(subscriber);
         }
      });
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(2, AnnotatedEventSubscriber.getTimesColorChanged());
      System.out.println("avoid garbage collection:"+subscriber + vetoSubscriber);
   }

   public void testWeakReference() {
      AnnotatedEventSubscriber.setTimesColorChanged(0);
      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(0, AnnotatedEventSubscriber.getTimesColorChanged());
      AnnotationProcessor.process(subscriber);
      AnnotationProcessor.process(vetoSubscriber);
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesColorChanged());
      EventBus.publish(Color.RED);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesColorChanged());

      System.out.println("avoid garbage collection:"+subscriber+vetoSubscriber);
      subscriber = null;
      System.gc();
      EventBus.publish(Color.BLUE);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesColorChanged());
      System.gc();
   }

   public void testEventClass() {
      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      AnnotationProcessor.process(subscriber);
      AnnotationProcessor.process(vetoSubscriber);

      //Veto subscriber stops the empty list
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(null, AnnotatedEventSubscriber.getLastCall());

      EventBus.publish(Arrays.asList("foo"));
      EDTUtil.waitForEDT();
      assertEquals("doList", AnnotatedEventSubscriber.getLastCall());

      AnnotatedEventSubscriber.setLastCall(null);
      EventBus.publish(Arrays.asList());
      EDTUtil.waitForEDT();
      assertEquals(null, AnnotatedEventSubscriber.getLastCall());
      AnnotationProcessor.unprocess(vetoSubscriber);
      EventBus.publish(Arrays.asList());
      EDTUtil.waitForEDT();
      assertEquals("doList", AnnotatedEventSubscriber.getLastCall());

      System.out.println("avoid garbage collection:"+subscriber);
      AnnotatedEventSubscriber.setLastCall(null);
      //it was subscribed to a list, though the method param is Collection, it shouldn't get called
      EventBus.publish(new HashSet());
      EDTUtil.waitForEDT();
   }

   public void testExactly() {
      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      AnnotationProcessor.process(subscriber);
      AnnotationProcessor.process(vetoSubscriber);

      JToggleButton jToggleButton = new JToggleButton();
      EventBus.publish(jToggleButton);
      EDTUtil.waitForEDT();
      System.out.println("avoid garbage collection:"+subscriber);
      assertEquals("doJToggleButtonExactly", AnnotatedEventSubscriber.getLastCall());
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());

      EventBus.publish(new JButton());
      EDTUtil.waitForEDT();
      assertEquals("doJToggleButtonExactly", AnnotatedEventSubscriber.getLastCall());
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());

      jToggleButton.setForeground(Color.RED);
      EventBus.publish(jToggleButton);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());

      AnnotationProcessor.unprocess(vetoSubscriber);
      EventBus.publish(jToggleButton);
      EDTUtil.waitForEDT();
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());

      AnnotationProcessor.unprocess(subscriber);
      EventBus.publish(jToggleButton);
      EDTUtil.waitForEDT();
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());
   }

   public void testAutoCreateEventServiceClass() {
      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      AnnotationProcessor.process(vetoSubscriber);
      AnnotationProcessor.process(subscriber);
      EventService es = EventServiceLocator.getEventService("IteratorService");
      es.publish(Arrays.asList("foo").iterator());
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());
      assertEquals("autoCreateEventServiceClass", AnnotatedEventSubscriber.getLastCall());
      es.publish(Arrays.asList().iterator());
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());
      AnnotationProcessor.unprocess(vetoSubscriber);
      es.publish(Arrays.asList().iterator());
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());
      AnnotationProcessor.unprocess(subscriber);
      es.publish(Arrays.asList().iterator());
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());
   }

   public void testStrongRef() {
      StrongAnnotatedEventSubscriber subscriber = new StrongAnnotatedEventSubscriber();
      AnnotationProcessor.process(subscriber);
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      AnnotationProcessor.process(vetoSubscriber);
      EventBus.publish(new File("foo"));
      EDTUtil.waitForEDT();
      assertEquals("doStrong", StrongAnnotatedEventSubscriber.getLastCall());
      assertEquals(1, StrongAnnotatedEventSubscriber.getTimesCalled());
      System.gc();
      EventBus.publish(new File("foo"));
      EDTUtil.waitForEDT();
      assertEquals("doStrong", StrongAnnotatedEventSubscriber.getLastCall());
      assertEquals(2, StrongAnnotatedEventSubscriber.getTimesCalled());
   }

   public void testTopic() {
      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      AnnotationProcessor.process(subscriber);
      AnnotationProcessor.process(vetoSubscriber);
      EventBus.publish("File.Open", new File("foo"));
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());
      EventBus.publish("File.Fooooooo", new File("foo"));
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());
      EventBus.publish("File.Open", null);
      EDTUtil.waitForEDT();
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());
      AnnotationProcessor.unprocess(vetoSubscriber);
      EventBus.publish("File.Open", null);
      EDTUtil.waitForEDT();
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());
      AnnotationProcessor.unprocess(subscriber);
      EventBus.publish("File.Open", null);
      EDTUtil.waitForEDT();
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());
   }

   public void testAutoCreateEventServiceTopic() {
      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      AnnotationProcessor.process(subscriber);
      AnnotationProcessor.process(vetoSubscriber);
      EventService es = EventServiceLocator.getEventService("IteratorService");
      es.publish("Iterator", new ArrayList().iterator());
      assertEquals(0, AnnotatedEventSubscriber.getTimesCalled());
      es.publish("Iterator", Arrays.asList("foo").iterator());
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());
      assertEquals("autoCreateEventServiceClass", AnnotatedEventSubscriber.getLastCall());
      AnnotationProcessor.unprocess(vetoSubscriber);
      es.publish("Iterator", new ArrayList().iterator());
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());
      AnnotationProcessor.unprocess(subscriber);
      es.publish("Iterator", Arrays.asList("foo").iterator());
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());
   }

   public void testTopicPattern() {
      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      AnnotationProcessor.process(vetoSubscriber);
      AnnotationProcessor.process(subscriber);
      EventService es = EventServiceLocator.getEventService("IceCreamService");
      es.publish("IceCream.Chocolate", "DoubleDip");
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());
      assertEquals("doIceCream", AnnotatedEventSubscriber.getLastCall());
      es.publish("IceCream.Cherry", "DoubleDip");
      assertEquals(1, AnnotatedEventSubscriber.getTimesCalled());
      AnnotationProcessor.unprocess(vetoSubscriber);
      es.publish("IceCream.Chocolate", "DoubleDip");
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());
      AnnotationProcessor.unprocess(subscriber);
      es.publish("IceCream.Chocolate", "DoubleDip");
      assertEquals(2, AnnotatedEventSubscriber.getTimesCalled());      
      System.out.println(subscriber);
   }

   public void testIssue15MultipleAnnotatedSubscribers() {
      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
      AnnotatedVetoSubscriber vetoSubscriber = new AnnotatedVetoSubscriber();
      AnnotationProcessor.process(vetoSubscriber);
      AnnotationProcessor.process(subscriber);
      AnotherAnnotatedEventSubscriber anotherSubscriber = new AnotherAnnotatedEventSubscriber();
      AnnotationProcessor.process(anotherSubscriber);
      EventBus.publish(Arrays.asList("foo"));
      EDTUtil.waitForEDT();
      assertEquals(1, AnotherAnnotatedEventSubscriber.getTimesCalled());
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, AnotherAnnotatedEventSubscriber.getTimesCalled());
      EDTUtil.waitForEDT();
      System.out.println(subscriber);
      System.out.println(anotherSubscriber);
   }

   public void testAnotherIssue15MultipleAnnotatedSubscribers() {
      EventBus.clearAllSubscribers();
      System.gc();
      Issue15Subscriber i15s1 = new Issue15Subscriber();
      Issue15Subscriber2 i15s2 = new Issue15Subscriber2();
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, i15s2.getTimesCalled());
      assertEquals(1, i15s1.getTimesCalled());
      //Ensure the garbage collector can't clean up the refs
      System.out.println(i15s1);
      System.out.println(i15s2);
   }

   //This one works with the DoubleAnnotatedEventSubscriber and AnotherDoubleAnnotatedEventSubscriber (and Single),
   //but fails with AnnotatedEventSubscriber and AnotherAnnotatedEventSubscriber
   public void testYetAnotherIssue15MultipleAnnotatedSubscribers() {
      EventBus.clearAllSubscribers();
      System.gc();
      DoubleAnnotatedEventSubscriber subscriber = new DoubleAnnotatedEventSubscriber();
      AnnotationProcessor.process(subscriber);
      DoubleAnnotatedEventSubscriber secondSubscriber = new DoubleAnnotatedEventSubscriber();
      AnnotationProcessor.process(secondSubscriber);
      AnotherDoubleAnnotatedEventSubscriber anotherSubscriber = new AnotherDoubleAnnotatedEventSubscriber();
      AnnotationProcessor.process(anotherSubscriber);
      AnotherDoubleAnnotatedEventSubscriber secondAnotherSubscriber = new AnotherDoubleAnnotatedEventSubscriber();
      AnnotationProcessor.process(secondAnotherSubscriber);
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(2, AnotherDoubleAnnotatedEventSubscriber.getTimesCalled());
      assertEquals(2, DoubleAnnotatedEventSubscriber.getTimesCalled());
      //Ensure the garbage collector can't clean up the refs
      System.out.println("finished with:"+subscriber);
      System.out.println("finished with:"+secondSubscriber);
      System.out.println("finished with:"+anotherSubscriber);
   }

//Would like to test this, but an exception isn't thrown, since you want all the subscribers to be called
//even if calling any one throws an exception
//   public void testTopicWrongType() {
//      AnnotatedEventSubscriber subscriber = new AnnotatedEventSubscriber();
//      AnnotationProcessor.process(subscriber);
//      EventService es = EventServiceLocator.getEventService("IteratorService");
//      try {
//         es.publish("Iterator", "foo");
//         fail("Should get an IllegalArgumentException");
//      } catch (Exception ex) {
//      }
//   }      

   public void testRuntimeTopicSubscriber() {
	   SubscriberForTesting runtimeTopicSubscriber = Factory.newRuntimeTopicSubscriber("foo");
	   EventBus.publish("foo", new ArrayList<String>());
	   EDTUtil.waitForEDT();
	   assertEquals(1, runtimeTopicSubscriber.getTimesCalled());
   }

   public void testRuntimeTopicPatternSubscriber() {
	   SubscriberForTesting runtimeTopicSubscriber = Factory.newRuntimeTopicPatternSubscriber("hope.*");
	   EventBus.publish("hope_and_change", new ArrayList<String>());
	   EDTUtil.waitForEDT();
	   assertEquals(1, runtimeTopicSubscriber.getTimesCalled());
   }
}
