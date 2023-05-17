package org.scijava.event.bushe;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests the implementation of publication states
 */
public class TestPublicationStates extends TestCase {
   List stuffHappens = new ArrayList();
   ObjectEvent event = new ObjectEvent(null, null) {
      @Override
      public void setPublicationStatus(PublicationStatus status) {
         super.setPublicationStatus(status);
         stuffHappens.add(status);
      }
   };
   IEventSubscriber subscriber = new IEventSubscriber() {
      public void onEvent(Object event) {
         stuffHappens.add(this);
      }
   };

   EventService es = new ThreadSafeEventService() {
      @Override
      protected void setStatus(PublicationStatus status, Object event, String topic, Object eventObj) {
         super.setStatus(status, event, topic, eventObj);
      }
   };

   public void testStates() {
      stuffHappens.clear();
      es.subscribe(ObjectEvent.class, subscriber);
      es.publish(event);
      List expected = new ArrayList();
      expected.add(PublicationStatus.Initiated);
      expected.add(PublicationStatus.Queued);
      expected.add(PublicationStatus.Publishing);
      expected.add(subscriber);
      expected.add(PublicationStatus.Completed);
      Assert.assertEquals(expected.size(), stuffHappens.size());
      for (int i = 0; i < expected.size(); i++) {
         Assert.assertEquals(expected.get(i), stuffHappens.get(i));
      }
   }

   public void testVetoStates() {
      stuffHappens.clear();
      es.subscribe(ObjectEvent.class, subscriber);
      es.subscribeVetoListener(ObjectEvent.class, new VetoEventListener() {
         public boolean shouldVeto(Object event) {
            return true;
         }
      });
      es.publish(event);
      List expected = new ArrayList();
      expected.add(PublicationStatus.Initiated);
      expected.add(PublicationStatus.Vetoed);
      Assert.assertEquals(expected.size(), stuffHappens.size());
      for (int i = 0; i < expected.size(); i++) {
         Assert.assertEquals(expected.get(i), stuffHappens.get(i));
      }
   }
}
