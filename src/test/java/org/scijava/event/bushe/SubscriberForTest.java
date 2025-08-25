
package org.scijava.event.bushe;

import java.util.Date;

/**
 * @author Michael Bushe
 * @since Nov 19, 2005 11:01:06 PM
 */
public class SubscriberForTest implements EventSubscriber {

	private boolean throwException;
	private Long waitTime;
	private EBTestCounter testDefaultEventService;
	Date callTime = null;

	public SubscriberForTest(EBTestCounter testDefaultEventService,
		Long waitTime)
	{
		this.testDefaultEventService = testDefaultEventService;
		this.waitTime = waitTime;
	}

	public SubscriberForTest(EBTestCounter testDefaultEventService,
		boolean throwException)
	{
		this.testDefaultEventService = testDefaultEventService;
		this.throwException = throwException;
	}

	public void onEvent(Object evt) {
		callTime = new Date();
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

	public boolean equals(Object obj) {
		return (this == obj);
	}
}
