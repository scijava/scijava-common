package org.scijava.io.event;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataEventTest {

	@Test
	public void testDeprecatedMethods() {
		String localPath = "/local/absolute/path.txt";
		Object obj = null;
		DataOpenedEvent openedEvent = new DataOpenedEvent(localPath, obj);
		DataSavedEvent savedEvent = new DataSavedEvent(localPath, obj);
		assertEquals(localPath, openedEvent.getSource());
		assertEquals(localPath, savedEvent.getDestination());

//		String remotepath = "https://remote.org/path.txt";
//		openedEvent = new DataOpenedEvent(remotepath, obj);
//		savedEvent = new DataSavedEvent(remotepath, obj);
//		assertEquals(remotepath, openedEvent.getSource());
//		assertEquals(remotepath, savedEvent.getDestination());
	}

}
