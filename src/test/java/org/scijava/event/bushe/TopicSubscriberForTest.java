
package org.scijava.event.bushe;

/**
 * @author Michael Bushe
 * @since Nov 19, 2005 11:00:53 PM
 */
public class TopicSubscriberForTest implements EventTopicSubscriber {

	private boolean throwException;
	private Long waitTime;
	private EBTestCounter testDefaultEventService;

	public TopicSubscriberForTest(EBTestCounter testDefaultEventService,
		Long waitTime)
	{
		this.testDefaultEventService = testDefaultEventService;
		this.waitTime = waitTime;
	}

	public TopicSubscriberForTest(EBTestCounter testDefaultEventService,
		boolean throwException)
	{
		this.testDefaultEventService = testDefaultEventService;
		this.throwException = throwException;
	}

	public void onEvent(String topic, Object evt) {
		if (waitTime != null) {
			try {
				Thread.sleep(waitTime.longValue());
			}
			catch (InterruptedException e) {}
		}
		testDefaultEventService.eventsHandledCount++;
		if (throwException) {
			testDefaultEventService.subscribeExceptionCount++;
			throw new IllegalArgumentException();
		}
	}
}
