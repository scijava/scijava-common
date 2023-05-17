package org.scijava.event.bushe;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

/** Test class for class-based subscriptions.
 * Does not like null, empty, red or cherry */
public class AnnotatedVetoSubscriber {

   @VetoSubscriber
   public boolean vetoBlueColorChange(Color color) {
      if (color == Color.RED) {
         return true;
      } else {
         return false;
      }
   }

   @VetoSubscriber(eventClass = List.class)
   public boolean doList(Collection collection) {
      if (collection == null || collection.isEmpty()) {
         return true;
      } else {
         return false;
      }
   }

   @VetoSubscriber(eventClass = JToggleButton.class, exact = true)
   public boolean doJToggleButtonExactly(JComponent button) {
      if (button.getForeground() == Color.RED) {
         return true;
      } else {
         return false;
      }
   }

   @VetoSubscriber(eventClass = Iterator.class,
           eventServiceName = "IteratorService",
           autoCreateEventServiceClass = ThreadSafeEventService.class)
   public boolean autoCreateEventServiceClass(Iterator it) {
      if (it == null || !it.hasNext()) {
         return true;
      } else {
         return false;
      }
   }

   @VetoTopicSubscriber(topic = "File.Open")
   public boolean simpleTopicOpenFile(String topic, File file) {
      if (file == null) {
         return true;
      } else {
         return false;
      }
   }

   @VetoTopicSubscriber(topic = "Iterator",
           eventServiceName = "IteratorService",
           autoCreateEventServiceClass = ThreadSafeEventService.class)
   public boolean autoCreateEventServiceTopic(String topic, Iterator it) {
      if (it == null || !it.hasNext()) {
         return true;
      } else {
         return false;
      }
   }

   @VetoTopicPatternSubscriber(topicPattern = "IceCream.*",
           eventServiceName = "IceCreamService")
   public boolean doIceCream(String topic, String order) {
      if (topic.indexOf("Cherry") > -1) {
         return true;
      } else {
         return false;
      }
   }

}
