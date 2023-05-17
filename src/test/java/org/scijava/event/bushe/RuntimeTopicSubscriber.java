package org.scijava.event.bushe;

import java.util.List;

class RuntimeTopicSubscriber implements SubscriberForTesting {
	private long timesCalled;
	private final String topic;

	public RuntimeTopicSubscriber(String topic) {
		this.topic = topic;
		AnnotationProcessor.process(this);
	}
	
	@RuntimeTopicEventSubscriber
	public void handleEvent(String topic, List<String> e) {
		timesCalled++;
	}

   @RuntimeTopicEventSubscriber
   public boolean shouldVeto(String topic, List<String> e) {
      return e == null;
   }

	public String getTopicName() {
		return topic;
	}

	public long getTimesCalled() {
		return timesCalled;
	}
}
