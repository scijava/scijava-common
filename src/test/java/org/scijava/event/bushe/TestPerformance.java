package org.scijava.event.bushe;

import junit.framework.TestCase;
import junit.framework.Assert;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.util.*;

/**
 * For proving performance.
 */
public class TestPerformance extends TestCase {
    private EventSubscriber doNothingSubscriber = new EventSubscriber() {
        public void onEvent(Object event) {
        }
    };

    private EventTopicSubscriber doNothingTopicSubscriber = new EventTopicSubscriber() {
        public void onEvent(String topic, Object payload) {
        }
    };

    public void testClassPerformance() {
        ThreadSafeEventService eventService = new ThreadSafeEventService();
        Class[] classes = {Color.class, String.class, JTextField.class, List.class, JButton.class,
            Boolean.class, Integer.class, Boolean.class, Set.class, Date.class};
        Object[] payloads = {Color.BLUE, "foo", new JTextField(), new ArrayList(), new JButton(),
            Boolean.TRUE, 35, 36L, new HashSet(), new Date()};
        for (Class aClass : classes) {
            eventService.subscribe(aClass, doNothingSubscriber);
        }

        long start = System.currentTimeMillis();
        int count = 100000;
        for (int i=0; i < count; i++) {
            for (Object payload : payloads) {
                eventService.publish(payload);
            }
        }
        long end = System.currentTimeMillis();
        long duration = (end - start)/1000;
        int numPubs = count * payloads.length;
        System.out.println("Time for "+ numPubs +" publications with subscribers to "+classes.length
            +" different classes subscribed to was "+ duration +" s. Average:"+((double)duration/(double)numPubs));
        Assert.assertTrue("Things are slowing down, "+numPubs+" class publications used to take 3.3 seconds, it now takes " +duration, duration < 7);
    }

    public void testStringPerformance() {
        ThreadSafeEventService eventService = new ThreadSafeEventService();
        String[] strings = {"Color", "String", "JTextField", "List", "JButton",
            "Boolean", "Integer", "Boolean", "Set", "Date"};
        Object[] payloads = {Color.BLUE, "foo", new JTextField(), new ArrayList(), new JButton(),
            Boolean.TRUE, 35, 36L, new HashSet(), new Date()};
        for (String aString : strings) {
            eventService.subscribe(aString, doNothingTopicSubscriber);
        }

        long start = System.currentTimeMillis();
        int count = 100000;
        for (int i=0; i < count; i++) {
            for (int j=0; j < strings.length; j++) {
                eventService.publish(strings[j], payloads[j]);
            }
        }
        long end = System.currentTimeMillis();
        long duration = (end - start)/1000;
        int numPubs = count * payloads.length;
        System.out.println("Time for "+ numPubs +" topic publications with topic subscribers to "+ strings.length
            +" different strings subscribed to was "+ duration +" s. Average:"+((double)duration/(double)numPubs));
        Assert.assertTrue("Things are slowing down, "+numPubs+" string publications used to take 1.3 seconds, it now takes "+duration, duration < 4);
    }

}
