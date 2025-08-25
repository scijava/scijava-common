
package org.scijava.event.bushe;

public class BadEventService extends ThreadSafeEventService {

	/**
	 * @see org.scijava.event.bushe.EventService#subscribe(String,org.scijava.event.bushe.EventTopicSubscriber)
	 */
	public boolean subscribe(String topic, EventTopicSubscriber eh) {
		throw new RuntimeException("For testing");
	}
}
