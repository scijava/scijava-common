package org.scijava.event.bushe.annotation;

/**
 * Intended to answer this post:
 * https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=30702&forumID=1834
 */
public abstract class AbstractSubscriber {
    private String targetType;

    abstract protected void initialize(String type);

    @EventSubscriber(eventClass=MyData.class)
    public void loadDocumentAnalysis(MyData data) {
        System.out.println(data + " received by " + getClass().getName());
        setTargetType(data.getClassification());
        //getStatusCallback().startProgress(getClass().getCanonicalName(), true, null);
        initialize(getTargetType());
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetType() {
        return targetType;
    }
}
