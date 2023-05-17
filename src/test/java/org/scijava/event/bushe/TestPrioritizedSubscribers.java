package org.scijava.event.bushe;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.Arrays;
import java.util.regex.Pattern;

import java.awt.Color;

import junit.framework.TestCase;

import org.scijava.event.bushe.annotation.AnnotationProcessor;

/**
 * Tests the Prioritized interface va. normal FIFO order.
 */
public class TestPrioritizedSubscribers extends TestCase {
    private ThreadSafeEventService eventService = null;
    private static final String EVENT_SERVICE_TEST_PRIORITY_ANNOTATION = "testPriorityAnnotation";


    /**
     * Base class that adds itself to the list it's given when told.  Nice for annotation subscribers.
     */
    class OrderRecorder {
        private List listToRecordTo;

        public OrderRecorder(List listToRecordTo) {
            this.listToRecordTo = listToRecordTo;
        }

        public void record() {
            listToRecordTo.add(this);
        }
    }

    /**
     * A subscriber that adds itself to a supplied list so that the order of calls is recorded.
     */
    class OrderRecorderSubscriber extends OrderRecorder implements EventSubscriber {

        OrderRecorderSubscriber(List listToRecordTo) {
            super(listToRecordTo);
        }

        public void onEvent(Object event) {
            record();
        }
    }

    /**
     * Ditto, for topics
     */
    class OrderRecorderTopicSubscriber extends OrderRecorder implements EventTopicSubscriber {

        OrderRecorderTopicSubscriber(List listToRecordTo) {
            super(listToRecordTo);
        }

        public void onEvent(String topic, Object event) {
            record();
        }
    }

    class PrioritizedOrderRecorderSubscriber extends OrderRecorderSubscriber implements Prioritized {
        private int priority;

        public PrioritizedOrderRecorderSubscriber(int priority, List listToRecordTo) {
            super(listToRecordTo);
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    class PrioritizedOrderRecorderTopicSubscriber extends OrderRecorderTopicSubscriber implements Prioritized {
        private int priority;

        public PrioritizedOrderRecorderTopicSubscriber(int priority, List listToRecordTo) {
            super(listToRecordTo);
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    public TestPrioritizedSubscribers(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        eventService = new ThreadSafeEventService(null, false);
        EventServiceLocator.setEventService(EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, null);
    }

    protected void tearDown() throws Exception {
        eventService = null;
    }

    public void testNormalFIFO() {
        List<EventSubscriber> calledOrder = new ArrayList<EventSubscriber>();
        List<EventSubscriber> originalOrder = new ArrayList<EventSubscriber>();
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        //mixing an inner class into the test
        EventSubscriber inner = new EventSubscriber() {
            public void onEvent(Object event) {
            }
        };
        //originalOrder.add(inner);
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        //add them all
        for (EventSubscriber eventSubscriber : originalOrder) {
            eventService.subscribe(Color.class, eventSubscriber);
        }
        eventService.publish(Color.BLUE);
        assertEquals(originalOrder, calledOrder);
        System.out.println("inner sout to avoid garbage collection" + inner);
    }

    public void testNoPrioritizedWithZeroPrioritized() {
        List<EventSubscriber> calledOrder = new ArrayList<EventSubscriber>();
        List<EventSubscriber> originalOrder = new ArrayList<EventSubscriber>();
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        originalOrder.add(new PrioritizedOrderRecorderSubscriber(0, calledOrder));
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        originalOrder.add(new PrioritizedOrderRecorderSubscriber(0, calledOrder));
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        //mixing an inner class into the test
        PrioritizedEventSubscriber inner = new PrioritizedEventSubscriber() {
            public void onEvent(Object event) {
            }

            public int getPriority() {
                return 0;
            }
        };
        //originalOrder.add(inner);
        originalOrder.add(new PrioritizedOrderRecorderSubscriber(0, calledOrder));
        originalOrder.add(new OrderRecorderSubscriber(calledOrder));
        //add them all
        for (EventSubscriber eventSubscriber : originalOrder) {
            eventService.subscribe(Color.class, eventSubscriber);
        }
        eventService.publish(Color.BLUE);
        assertEquals(originalOrder, calledOrder);
        System.out.println("inner sout to avoid garbage collection" + inner);
    }

    public void testOnlyPrioritized() {
        List<EventSubscriber> calledOrder = new ArrayList<EventSubscriber>();
        List<EventSubscriber> originalOrder = new ArrayList<EventSubscriber>();
        for (int i = 0; i < 100; i++) {
            Random random = new Random();
            originalOrder.add(new PrioritizedOrderRecorderSubscriber(random.nextInt(10000) - 5000, calledOrder));
        }
        for (EventSubscriber eventSubscriber : originalOrder) {
            eventService.subscribe(Color.class, eventSubscriber);
        }
        eventService.publish(Color.BLUE);
        int lastPriority = -5001;
        for (EventSubscriber eventSubscriber : calledOrder) {
            int priority = ((PrioritizedOrderRecorderSubscriber) eventSubscriber).getPriority();
            assertTrue(priority >= lastPriority);
            lastPriority = priority;
        }
    }

    public void testMixedOfPrioritizedNonPrioritizedAndPrioritized0() {
        Random rand = new Random();
        List<EventSubscriber> calledOrder = new ArrayList<EventSubscriber>();
        List<EventSubscriber> prioritized = new ArrayList<EventSubscriber>();
        //100 negative
        for (int i = 0; i < 100; i++) {
            Random random = new Random();
            prioritized.add(new PrioritizedOrderRecorderSubscriber(random.nextInt(10000) * -1, calledOrder));
        }
        //100 positive
        for (int i = 0; i < 100; i++) {
            Random random = new Random();
            prioritized.add(new PrioritizedOrderRecorderSubscriber(random.nextInt(10000), calledOrder));
        }
        Collections.shuffle(prioritized);
        //100 fifo
        List<EventSubscriber> fifo = new ArrayList<EventSubscriber>();
        for (int i = 0; i < 100; i++) {
            if (rand.nextBoolean()) {
                fifo.add(new OrderRecorderSubscriber(calledOrder));
            } else {
                fifo.add(new PrioritizedOrderRecorderSubscriber(0, calledOrder));
            }
        }
        List<EventSubscriber> prioritizedCopy = new ArrayList(prioritized);
        List<EventSubscriber> fifoCopy = new ArrayList(fifo);
        //Subscribe all, randomizing a fifo or prioritized
        EventSubscriber eventSubscriber;
        int subscribeCount = 0;
        int prioritizedSubscribeCount = 0;
        int nonPrioritizedSubscribeCount = 0;
        for (int i = 0; i < 300; i++) {
            if (prioritizedCopy.isEmpty()) {
                eventSubscriber = fifoCopy.remove(0);
                nonPrioritizedSubscribeCount++;
            } else if (fifoCopy.isEmpty()) {
                eventSubscriber = prioritizedCopy.remove(0);
                prioritizedSubscribeCount++;
            } else {
                if (rand.nextBoolean()) {
                    eventSubscriber = fifoCopy.remove(0);
                    nonPrioritizedSubscribeCount++;
                } else {
                    eventSubscriber = prioritizedCopy.remove(0);
                    prioritizedSubscribeCount++;
                }
            }
            subscribeCount++;
            boolean success = eventService.subscribe(Color.class, eventSubscriber);
            assertFalse(!success);
        }

        List subscribersToColor = eventService.getSubscribers(Color.class);
        assertEquals(300, subscribersToColor.size());
        assertEquals(100, nonPrioritizedSubscribeCount);
        assertEquals(200, prioritizedSubscribeCount);
        assertEquals(300, subscribeCount);
        eventService.publish(Color.BLUE);
        assertEquals(300, calledOrder.size());
        int lastPriority = -10001;
        for (int i = 0; i < 99; i++) {
            EventSubscriber subscriber = calledOrder.get(i);
            assertTrue(subscriber instanceof PrioritizedOrderRecorderSubscriber);
            PrioritizedOrderRecorderSubscriber prioritizedOrderRecorderSubscriber = (PrioritizedOrderRecorderSubscriber) subscriber;
            int priority = prioritizedOrderRecorderSubscriber.getPriority();
            assertTrue(priority < 0);
            assertTrue(priority >= lastPriority);
            lastPriority = priority;
        }
        for (int i = 100; i < 199; i++) {
            EventSubscriber subscriber = calledOrder.get(i);
            assertTrue(subscriber instanceof OrderRecorderSubscriber);
            if (subscriber instanceof PrioritizedOrderRecorderSubscriber) {
                PrioritizedOrderRecorderSubscriber prioritizedOrderRecorderSubscriber = (PrioritizedOrderRecorderSubscriber) subscriber;
                int priority = prioritizedOrderRecorderSubscriber.getPriority();
                assertTrue(priority == 0);
            }
            assertEquals(subscriber, fifo.get(i - 100));
        }
        lastPriority = 0;
        for (int i = 200; i < 299; i++) {
            EventSubscriber subscriber = calledOrder.get(i);
            assertTrue(subscriber instanceof PrioritizedOrderRecorderSubscriber);
            PrioritizedOrderRecorderSubscriber prioritizedOrderRecorderSubscriber = (PrioritizedOrderRecorderSubscriber) subscriber;
            int priority = prioritizedOrderRecorderSubscriber.getPriority();
            assertTrue(priority > 0);
            assertTrue(priority >= lastPriority);
            lastPriority = priority;
        }
        System.out.println(prioritized.size());
        System.out.println(fifo.size());
    }

    public void testPriorityAnnotation() throws EventServiceExistsException {
        EventServiceLocator.setEventService(EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, eventService);
        List<OrderRecorder> calledOrder = new ArrayList<OrderRecorder>();
        OrderRecorder sn100 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -100)
            public void annotateMe(Object foo) {
                record();
            }
        };
        OrderRecorder sn50 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -100)
            public void annotateMe(Object foo) {
                record();
            }
        };
        PrioritizedOrderRecorderSubscriber spn30 = new PrioritizedOrderRecorderSubscriber(-30, calledOrder);
        OrderRecorderSubscriber so_1 = new OrderRecorderSubscriber(calledOrder);
        OrderRecorder s100 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 100)
            public void annotateMe(Object foo) {
                record();
            }
        };
        OrderRecorder sn10 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -10)
            public void annotateMe(Object foo) {
                record();
            }
        };
        OrderRecorder s0_2 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 0)
            public void annotateMe(Object foo) {
                record();
            }
        };
        OrderRecorder s0_3 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(Object foo) {
                record();
            }
        };
        OrderRecorder s50 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 50)
            public void annotateMe(Object foo) {
                record();
            }
        };
        OrderRecorder s0_4 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(Object foo) {
                record();
            }
        };
        OrderRecorder s10 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 10)
            public void annotateMe(Object foo) {
                record();
            }
        };
        OrderRecorder s0_5 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventSubscriber(eventClass = Color.class, eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(Object foo) {
                record();
            }
        };
        Object[] toAdd = {sn100, s100, so_1, spn30, s0_2, s50, s0_3, sn10, sn50, s0_4, s10, s0_5};
        List expectedResult = Arrays.asList(sn100, sn50, spn30, sn10, so_1, s0_2, s0_3, s0_4, s0_5, s10, s50, s100);
        for (Object o : toAdd) {
            if (o instanceof EventSubscriber) {
                eventService.subscribe(Color.class, (EventSubscriber) o);
            } else {
                AnnotationProcessor.process(o);
            }
        }
        eventService.publish(Color.BLUE);
        assertEquals(expectedResult, calledOrder);
    }

    /**
     * With more than one subscriber to the EventBus by class, if any of the
     * subscribers are Prioritized with a negative priority, then no  FIFO subscribers
     * are notified.
     * <p/>
     * This holds for non-Prioritized FIFO subscribers, and Prioritized subscribers
     * with Priority of 0.
     */
    public void testIssue26OneNegOthersNormal() {
        final List<Integer> calledOrder = new ArrayList<Integer>();
        //non-Prioritized FIFO subscribers
        EventSubscriber sub1 = new EventSubscriber() {
            public void onEvent(Object event) {
                calledOrder.add(1);
            }
        };
        EventSubscriber sub0 = new PrioritizedEventSubscriber() {
            public void onEvent(Object event) {
                calledOrder.add(-1);
            }
            public int getPriority() {
                return -1;
            }
        };
        EventSubscriber sub2 = new EventSubscriber() {
            public void onEvent(Object event) {
                calledOrder.add(2);
            }
        };

        eventService.subscribe(Color.class, sub1);
        eventService.subscribe(Color.class, sub0);
        eventService.subscribe(Color.class, sub2);
        eventService.publish(Color.BLUE);
        assertEquals(calledOrder.get(0).intValue(), -1);
        assertEquals(calledOrder.get(1).intValue(), 1);
        assertEquals(calledOrder.get(2).intValue(), 2);
        System.out.println("to avoid garbage collection:"+sub1+sub2+sub0);
    }

    /**
     * With more than one subscriber to the EventBus by class, if any of the
     * subscribers are Prioritized with a negative priority, then no  FIFO subscribers
     * are notified.
     * <p/>
     * This holds for non-Prioritized FIFO subscribers, and Prioritized subscribers
     * with Priority of 0.
     */
    public void testOnePosOthersNormal() {
        final List<Integer> calledOrder = new ArrayList<Integer>();
        //non-Prioritized FIFO subscribers
        EventSubscriber sub1 = new EventSubscriber() {
            public void onEvent(Object event) {
                calledOrder.add(1);
            }
        };
        EventSubscriber sub0 = new PrioritizedEventSubscriber() {
            public void onEvent(Object event) {
                calledOrder.add(11);
            }
            public int getPriority() {
                return 11;
            }
        };
        EventSubscriber sub2 = new EventSubscriber() {
            public void onEvent(Object event) {
                calledOrder.add(2);
            }
        };

        eventService.subscribe(Color.class, sub1);
        eventService.subscribe(Color.class, sub0);
        eventService.subscribe(Color.class, sub2);
        eventService.publish(Color.BLUE);
        assertEquals(1, calledOrder.get(0).intValue());
        assertEquals(2, calledOrder.get(1).intValue());
        assertEquals(11, calledOrder.get(2).intValue());
        System.out.println("to avoid garbage collection:"+sub1+sub2+sub0);
    }

    /**
     * shameless copy and paste test, only the subscriber type was changed
     */
    public void testPriorityTopicAnnotation() throws EventServiceExistsException {
        EventServiceLocator.setEventService(EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, eventService);
        List<OrderRecorder> calledOrder = new ArrayList<OrderRecorder>();
        OrderRecorder sn100 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -100)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder sn50 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -100)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        PrioritizedOrderRecorderTopicSubscriber spn30 = new PrioritizedOrderRecorderTopicSubscriber(-30, calledOrder);
        OrderRecorderTopicSubscriber so_1 = new OrderRecorderTopicSubscriber(calledOrder);
        OrderRecorder s100 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 100)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder sn10 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -10)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s0_2 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 0)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s0_3 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s50 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 50)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s0_4 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s10 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 10)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s0_5 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicSubscriber(topic = "Color", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        Object[] toAdd = {sn100, s100, so_1, spn30, s0_2, s50, s0_3, sn10, sn50, s0_4, s10, s0_5};
        List expectedResult = Arrays.asList(sn100, sn50, spn30, sn10, so_1, s0_2, s0_3, s0_4, s0_5, s10, s50, s100);
        for (Object o : toAdd) {
            if (o instanceof EventTopicSubscriber) {
                eventService.subscribe("Color", (EventTopicSubscriber) o);
            } else {
                AnnotationProcessor.process(o);
            }
        }
        eventService.publish("Color", Color.BLUE);
        assertEquals(expectedResult, calledOrder);
    }


    /**
     * Another shameless copy and paste test, only the subscriber type was changed
     */
    public void testPriorityTopicPatternAnnotation() throws EventServiceExistsException {
        EventServiceLocator.setEventService(EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, eventService);
        List<OrderRecorder> calledOrder = new ArrayList<OrderRecorder>();
        OrderRecorder sn100 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -100)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder sn50 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -50)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        PrioritizedOrderRecorderTopicSubscriber spn30 = new PrioritizedOrderRecorderTopicSubscriber(-30, calledOrder);
        OrderRecorderTopicSubscriber so_1 = new OrderRecorderTopicSubscriber(calledOrder);
        OrderRecorder s100 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 100)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder sn10 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = -10)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s0_2 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 0)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s0_3 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s50 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 50)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s0_4 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s10 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION, priority = 10)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        OrderRecorder s0_5 = new OrderRecorder(calledOrder) {
            @org.scijava.event.bushe.annotation.EventTopicPatternSubscriber(topicPattern = "Col[a-z]+", eventServiceName = EVENT_SERVICE_TEST_PRIORITY_ANNOTATION)
            public void annotateMe(String topic, Object foo) {
                record();
            }
        };
        Object[] toAdd = {sn100, s100, so_1, spn30, s0_2, s50, s0_3, sn10, sn50, s0_4, s10, s0_5};
        List expectedResult = Arrays.asList(sn100, sn50, spn30, sn10, so_1, s0_2, s0_3, s0_4, s0_5, s10, s50, s100);
        for (Object o : toAdd) {
            if (o instanceof OrderRecorderTopicSubscriber) {
                Pattern pattern = Pattern.compile("Col[a-z]+");
                eventService.subscribe(pattern, (EventTopicSubscriber) o);
            } else {
                AnnotationProcessor.process(o);
            }
        }
        eventService.publish("Color", Color.BLUE);
        assertEquals(expectedResult, calledOrder);
   }
}
