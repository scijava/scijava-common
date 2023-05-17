package org.scijava.event.bushe.annotation;

/**
 * Intended to answer this post:
 * https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=30702&forumID=1834
 */
public class ConcreteSubscriber extends AbstractSubscriber {
    private boolean wasInitialized = false;

    protected void initialize(String type) {
        this.wasInitialized = true;
    }

    public boolean isInitialized() {
        return wasInitialized;
    }
}