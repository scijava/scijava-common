package org.scijava.event.bushe.annotation;

import junit.framework.TestCase;
import junit.framework.Assert;
import org.scijava.event.bushe.EventBus;
import org.scijava.event.bushe.EDTUtil;

/**
 * Testing: 
 * https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=30702&forumID=1834
 */
public class TestAnnotationInAbstractClass extends TestCase {
    public void testAbstract() {
        ConcreteSubscriber concrete = new ConcreteSubscriber();
        AnnotationProcessor.process(concrete);
        EventBus.publish(new MyData());
        EDTUtil.waitForEDT();
        Assert.assertTrue(concrete.isInitialized());
    }
}
