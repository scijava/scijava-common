package org.scijava.event.bushe.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

import junit.framework.TestCase;

import org.scijava.event.bushe.CleanupEvent;
import org.scijava.event.bushe.EDTUtil;
import org.scijava.event.bushe.EventBus;
import org.scijava.event.bushe.EventService;
import org.scijava.event.bushe.EventSubscriber;
import org.scijava.event.bushe.ThreadSafeEventService;

public class TestSubscriberAnnotationMemoryLeaks extends TestCase {
   Random rand = new Random();
   
   public void setUp() {
      EventBus.getGlobalEventService();
      EventBus.clearAllSubscribers();
      System.gc();
   }

   public void testStrongClassAnnotatedEventSubscriber() {
      StrongClassAnnotatedEventSubscriber subscriber = new StrongClassAnnotatedEventSubscriber();
      AnnotationProcessor.process(subscriber);
      StrongClassAnnotatedEventSubscriber.setTimesCalled(0);
      assertEquals(0, StrongClassAnnotatedEventSubscriber.getTimesCalled());
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, StrongClassAnnotatedEventSubscriber.getTimesCalled());
      subscriber = null;
      System.gc();
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(2, StrongClassAnnotatedEventSubscriber.getTimesCalled());
      List subscribers = EventBus.getSubscribers(List.class);
      assertEquals(1, subscribers.size());
      //I can unsubscribe without ever explicitly subscribing
      EventBus.unsubscribe(List.class, (org.scijava.event.bushe.EventSubscriber) subscribers.get(0));
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(2, StrongClassAnnotatedEventSubscriber.getTimesCalled());
      subscribers = EventBus.getSubscribers(List.class);
      assertEquals(0, subscribers.size());
   }

   public void testWeakClassAnnotatedEventSubscriber() {
      WeakClassAnnotatedEventSubscriber subscriber = new WeakClassAnnotatedEventSubscriber();
      AnnotationProcessor.process(subscriber);
      WeakClassAnnotatedEventSubscriber.setTimesCalled(0);
      assertEquals(0, WeakClassAnnotatedEventSubscriber.getTimesCalled());
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, WeakClassAnnotatedEventSubscriber.getTimesCalled());

      subscriber = null;
      System.gc();
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, WeakClassAnnotatedEventSubscriber.getTimesCalled());
      List subscribers = EventBus.getSubscribers(List.class);
      assertEquals(0, subscribers.size());
   }

   public void testWeakClassAnnotatedEventSubscriberUnsubscription() {
      WeakClassAnnotatedEventSubscriber subscriber = new WeakClassAnnotatedEventSubscriber();
      AnnotationProcessor.process(subscriber);
      WeakClassAnnotatedEventSubscriber.setTimesCalled(0);
      assertEquals(0, WeakClassAnnotatedEventSubscriber.getTimesCalled());
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, WeakClassAnnotatedEventSubscriber.getTimesCalled());

      EventBus.unsubscribe(List.class, subscriber);

      subscriber = null;
      System.gc();
      EventBus.publish(new ArrayList());
      EDTUtil.waitForEDT();
      assertEquals(1, WeakClassAnnotatedEventSubscriber.getTimesCalled());
      List subscribers = EventBus.getSubscribers(List.class);
      assertEquals(0, subscribers.size());
   }
   
   public void testCleanup() {
      final int cleanStartThreshold = 100;
      final long period = 5000L;
      final int stopThreshold = 10;
      EventService es = new ThreadSafeEventService(cleanStartThreshold, stopThreshold,period);
      CleanupEventSubscriber cleanupEventSubscriber = new CleanupEventSubscriber();
      es.subscribe(CleanupEvent.class, cleanupEventSubscriber);
      //Go right up to the edge, but don't cross it.
      Object[] subscribers = new Object[cleanStartThreshold];
      for (int i = 0; i < cleanStartThreshold -2; i++) {
         subscribers[i] = createSubscriber(es);
         //these should have no effect 
         es.subscribeStrongly(List.class, new DummySubscriber());
      }
      es.publish(new ArrayList());
      try {
         Thread.sleep(100);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(0, cleanupEventSubscriber.events.size());
      
      //Go over the edge, cleanup should start
      subscribers[cleanStartThreshold-1] = createSubscriber(es);
      try {
         Thread.sleep(1000);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      //There are no stale refs yet, should have tried cleaning, but not done it.
      assertEquals(3, cleanupEventSubscriber.events.size()); 
      assertEquals(CleanupEvent.Status.STARTING, cleanupEventSubscriber.events.get(0).getStatus());
      assertEquals(CleanupEvent.Status.OVER_STOP_THRESHOLD_CLEANING_BEGUN, cleanupEventSubscriber.events.get(1).getStatus());
      assertEquals(CleanupEvent.Status.FINISHED_CLEANING, cleanupEventSubscriber.events.get(2).getStatus());
      assertTrue(cleanStartThreshold == cleanupEventSubscriber.events.get(0).getTotalWeakRefsAndProxies());
      assertTrue(cleanStartThreshold == cleanupEventSubscriber.events.get(1).getTotalWeakRefsAndProxies());
      assertTrue(cleanStartThreshold == cleanupEventSubscriber.events.get(2).getTotalWeakRefsAndProxies());
      assertEquals(new Integer(0), cleanupEventSubscriber.events.get(2).getNumStaleSubscribersCleaned());

      //Does it run again after the interval (exactly once more)?
      try {
         Thread.sleep(250+period);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(6, cleanupEventSubscriber.events.size()); 
      assertEquals(CleanupEvent.Status.STARTING, cleanupEventSubscriber.events.get(3).getStatus());
      assertEquals(CleanupEvent.Status.OVER_STOP_THRESHOLD_CLEANING_BEGUN, cleanupEventSubscriber.events.get(4).getStatus());
      assertEquals(CleanupEvent.Status.FINISHED_CLEANING, cleanupEventSubscriber.events.get(5).getStatus());
      assertTrue(cleanStartThreshold == cleanupEventSubscriber.events.get(3).getTotalWeakRefsAndProxies());
      assertTrue(cleanStartThreshold == cleanupEventSubscriber.events.get(4).getTotalWeakRefsAndProxies());
      assertTrue(cleanStartThreshold == cleanupEventSubscriber.events.get(5).getTotalWeakRefsAndProxies());
      assertEquals(new Integer(0), cleanupEventSubscriber.events.get(5).getNumStaleSubscribersCleaned());

      //Now make some stale
      int numberToMakeStale = 10;
      for (int i = 0; i < numberToMakeStale; i++) {
         subscribers[i] = null;
      }
      System.gc();
      try {
         Thread.sleep(100);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      //Period has not yet pass, should not expect a cleaning yet.
      assertEquals(6, cleanupEventSubscriber.events.size()); 

      //After period, stale refs should be cleaned
      try {
         Thread.sleep(period);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(9, cleanupEventSubscriber.events.size()); 
      assertEquals(CleanupEvent.Status.STARTING, cleanupEventSubscriber.events.get(6).getStatus());
      assertEquals(CleanupEvent.Status.OVER_STOP_THRESHOLD_CLEANING_BEGUN, cleanupEventSubscriber.events.get(7).getStatus());
      assertEquals(CleanupEvent.Status.FINISHED_CLEANING, cleanupEventSubscriber.events.get(8).getStatus());
      assertEquals((int)cleanStartThreshold, (int)cleanupEventSubscriber.events.get(6).getTotalWeakRefsAndProxies());
      assertEquals((int)cleanStartThreshold, (int)cleanupEventSubscriber.events.get(7).getTotalWeakRefsAndProxies());
      assertEquals((int)(cleanStartThreshold - numberToMakeStale), (int)cleanupEventSubscriber.events.get(8).getTotalWeakRefsAndProxies());
      assertEquals((int)numberToMakeStale, (int)cleanupEventSubscriber.events.get(8).getNumStaleSubscribersCleaned());      

      //Now make so many stale that it gets below the stop threshold
      int numberAlreadyStale = numberToMakeStale;
      numberToMakeStale = cleanStartThreshold - stopThreshold;
      for (int i = numberAlreadyStale; i < numberToMakeStale; i++) {
         subscribers[i] = null;
      }
      System.gc();
      //After period, stale refs should be cleaned
      try {
         Thread.sleep(period);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(12, cleanupEventSubscriber.events.size()); 
      assertEquals(CleanupEvent.Status.STARTING, cleanupEventSubscriber.events.get(9).getStatus());
      assertEquals(CleanupEvent.Status.OVER_STOP_THRESHOLD_CLEANING_BEGUN, cleanupEventSubscriber.events.get(10).getStatus());
      assertEquals(CleanupEvent.Status.FINISHED_CLEANING, cleanupEventSubscriber.events.get(11).getStatus());
      assertEquals((int)cleanStartThreshold-numberAlreadyStale, (int)cleanupEventSubscriber.events.get(9).getTotalWeakRefsAndProxies());
      assertEquals((int)cleanStartThreshold-numberAlreadyStale, (int)cleanupEventSubscriber.events.get(10).getTotalWeakRefsAndProxies());
      assertEquals((int)(cleanStartThreshold - numberToMakeStale), (int)cleanupEventSubscriber.events.get(11).getTotalWeakRefsAndProxies());
      assertEquals((int)numberToMakeStale, (int)cleanupEventSubscriber.events.get(11).getNumStaleSubscribersCleaned()+numberAlreadyStale);      

      //After period, next cleaning run should tell us that cleaning is stopped
      try {
         Thread.sleep(period);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(14, cleanupEventSubscriber.events.size()); 
      assertEquals(CleanupEvent.Status.STARTING, cleanupEventSubscriber.events.get(12).getStatus());
      assertEquals(CleanupEvent.Status.UNDER_STOP_THRESHOLD_CLEANING_CANCELLED, cleanupEventSubscriber.events.get(13).getStatus());
      assertEquals((int)cleanStartThreshold-numberToMakeStale, (int)cleanupEventSubscriber.events.get(12).getTotalWeakRefsAndProxies());
      assertEquals((int)cleanStartThreshold-numberToMakeStale, (int)cleanupEventSubscriber.events.get(13).getTotalWeakRefsAndProxies());
      assertEquals(null, cleanupEventSubscriber.events.get(13).getNumStaleSubscribersCleaned());      

      //After period, no more cleaning should be done
      try {
         Thread.sleep(period);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(14, cleanupEventSubscriber.events.size()); 

      es.clearAllSubscribers();
      es.subscribe(CleanupEvent.class, cleanupEventSubscriber);

      //Go back over the limit and the cleaning should restart
      for (int i = 0; i < cleanStartThreshold-1; i++) {
         subscribers[i] = createSubscriber(es);
      }
      subscribers[99] = createSubscriber(es);
      System.out.println("Cleanup should be starting");
      try {
         Thread.sleep(250);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(17, cleanupEventSubscriber.events.size()); 
      assertEquals(CleanupEvent.Status.STARTING, cleanupEventSubscriber.events.get(14).getStatus());
      assertEquals(CleanupEvent.Status.OVER_STOP_THRESHOLD_CLEANING_BEGUN, cleanupEventSubscriber.events.get(15).getStatus());
      assertEquals(CleanupEvent.Status.FINISHED_CLEANING, cleanupEventSubscriber.events.get(16).getStatus());
      assertEquals(0, (int)cleanupEventSubscriber.events.get(16).getNumStaleSubscribersCleaned());      

      es.clearAllSubscribers();
      es.subscribe(CleanupEvent.class, cleanupEventSubscriber);
      try {
         Thread.sleep(period);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(19, cleanupEventSubscriber.events.size()); 
      assertEquals(CleanupEvent.Status.STARTING, cleanupEventSubscriber.events.get(17).getStatus());
      assertEquals(CleanupEvent.Status.UNDER_STOP_THRESHOLD_CLEANING_CANCELLED, cleanupEventSubscriber.events.get(18).getStatus());
      assertEquals(null, cleanupEventSubscriber.events.get(18).getNumStaleSubscribersCleaned());      

      //After period, no more cleaning should be done
      try {
         Thread.sleep(period);
      } catch (InterruptedException ex) {
         Logger.getLogger(TestSubscriberAnnotationMemoryLeaks.class.getName()).log(Level.SEVERE, null, ex);
      }
      assertEquals(19, cleanupEventSubscriber.events.size());       
   }
   
   private Object createSubscriber(EventService es) {
      Object result = null;
      int randNum = rand.nextInt(2);
      if (randNum == 0) {
         //class
         randNum = rand.nextInt(2);
         if (true || randNum == 0) {
            //normal
            result = new DummySubscriber();
            Class subscriptionClass = null;
            randNum = rand.nextInt(3);
            switch (randNum) {
            case 0: subscriptionClass = List.class; break;
            case 1: subscriptionClass = String.class;break;
            case 2: subscriptionClass = JComponent.class;break;
            }
            es.subscribe(subscriptionClass, (EventSubscriber)result);
         } else {
            //annotated
            result = new AnnotatedDummySubscriber();            
            AnnotationProcessor.process(result);
         }
      } else {
         //topic
         randNum = rand.nextInt(2);
         if (true || randNum == 0) {
            //normal
            result = new DummyTopicSubscriber();
            String topic = null;
            randNum = rand.nextInt(3);
            switch (randNum) {
            case 0: topic = "Lis"; break;
            case 1: topic = "Strin";break;
            case 2: topic = "JCompon";break;
            }
            es.subscribe(topic, (org.scijava.event.bushe.EventTopicSubscriber)result);
         } else {
            //annotated
            result = new AnnotatedTopicDummySubscriber();            
            AnnotationProcessor.process(result);
         }
      }
      return result;
   }

   private class CleanupEventSubscriber implements EventSubscriber<CleanupEvent> {

      public List<CleanupEvent> events = new ArrayList<CleanupEvent>();

      public CleanupEventSubscriber() {
      }

      public void onEvent(CleanupEvent event) {
         this.events.add(event);
      }
   }

   private static class DummySubscriber implements EventSubscriber<List> {
      public void onEvent(List list) {
      }      
   }
   
   private static class DummyTopicSubscriber implements org.scijava.event.bushe.EventTopicSubscriber {
      public void onEvent(String topic, Object data) {
      }
   }
   
   private static class AnnotatedDummySubscriber  {
      @org.scijava.event.bushe.annotation.EventSubscriber(eventClass=List.class)
      public void foo(Object event) {
      }      
   }   

   private static class AnnotatedTopicDummySubscriber  {
      @EventTopicSubscriber(topic="bar")
      public void foo(Object event) {
      }      
   }   
}
