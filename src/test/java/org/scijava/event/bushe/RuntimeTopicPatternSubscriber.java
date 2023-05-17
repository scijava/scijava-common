package org.scijava.event.bushe;

import java.util.List;

class RuntimeTopicPatternSubscriber implements SubscriberForTesting {
	private final String topicPattern;
	private long timesCalled;

	public RuntimeTopicPatternSubscriber(String topicPattern) {
		this.topicPattern = topicPattern;

		AnnotationProcessor.process(this);
	}

	@RuntimeTopicPatternEventSubscriber
	public void handleEvent(String topic, List<String> event) {
		timesCalled++;
	}

   @RuntimeTopicPatternEventSubscriber
   public boolean shouldVeto(String topic, List<String> e) {
      return e == null;
   }

	public String getTopicPatternName() {
		return topicPattern;
	}

	public long getTimesCalled() {
		return timesCalled;
	}
}
