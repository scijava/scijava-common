package org.scijava.event.bushe.annotation.runtime;

public class Factory {

	public static SubscriberForTesting newRuntimeTopicSubscriber(String topic) {
		return new RuntimeTopicSubscriber(topic);
	}

	public static SubscriberForTesting newRuntimeTopicPatternSubscriber(String topicPattern) {
		return new RuntimeTopicPatternSubscriber(topicPattern);
	}
}
