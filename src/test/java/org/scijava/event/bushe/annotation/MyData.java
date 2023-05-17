package org.scijava.event.bushe.annotation;

/**
 * Intended to answer this post:
 * https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=30702&forumID=1834
 */
public class MyData {
    private String classification = "foo";

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }
}
