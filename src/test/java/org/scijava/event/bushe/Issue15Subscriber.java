package org.scijava.event.bushe;

import java.util.List;
import javax.swing.SwingUtilities;

/**
 *
 */
public class Issue15Subscriber {
   private long timesCalled;

   public Issue15Subscriber() {
        AnnotationProcessor.process(this);
    }
    
    @EventSubscriber(eventClass = List.class)
    public void handleClassSubscription(List c) {
       timesCalled++;
        if (c != null) {
            System.out.println("In handleClassSubscription");
            System.out.println("By class: " + c);
            System.out.println("Is on EDT: " + SwingUtilities.isEventDispatchThread());
            System.out.println();
        }
    }

   /*
    @EventTopicSubscriber(topic = "Topic1")
    public void handleTopic1Subscription(String topic, Object o) {
        if (o != null) {
            System.out.println("In handleTopic1Subscription");
            System.out.println("By topic: " + topic);
            System.out.println("    for class: " + o.getClass());
            System.out.println("Is on EDT: " + SwingUtilities.isEventDispatchThread());
            System.out.println();
        }
    }


    @EventTopicSubscriber(topic = "Topic2")
    public void handleTopic2Subscription(String topic, Object o) {
        if (o != null) {
            System.out.println("In handleTopic2Subscription");
            System.out.println("By topic: " + topic);
            System.out.println("    for class: " + o.getClass());
            System.out.println("Is on EDT: " + SwingUtilities.isEventDispatchThread());
            System.out.println();
        }
    }
    
    
    
    @EventTopicPatternSubscriber(topicPattern = ".*")
    public void handleAllTopicsSubscription(String topic, Object o) {
        if (o != null) {
            System.out.println("In handleAllTopicsSubscription");
            System.out.println("By topic: " + topic);
            System.out.println("    for class: " + o.getClass());
            System.out.println("Is on EDT: " + SwingUtilities.isEventDispatchThread());
            System.out.println();
        }
    }
    */

   public long getTimesCalled() {
      return timesCalled;
   }
}
