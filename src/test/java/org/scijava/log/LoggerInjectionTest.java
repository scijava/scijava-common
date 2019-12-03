package org.scijava.log;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import java.util.Collections;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LoggerInjectionTest {

	private final Context context = new Context(LogService.class);

	@Test
	public void testInjection() {
		// setup
		LogService logService = context.service(LogService.class);
		TestLogListener listener = new TestLogListener();
		logService.addLogListener(listener);
		// process
		ObjectWithLogger object = new ObjectWithLogger();
		context.inject(object);
		object.logSomething();
		// test
		assertTrue(listener.hasLogged(m -> "Something".equals(m.text())));
	}

	@Test
	public void testDefaultLoggerName() {
		ObjectWithLogger object = new ObjectWithLogger();
		context.inject(object);
		assertEquals(ObjectWithLogger.class.getSimpleName(), object.getLogger().getName());
	}

	@Test
	public void testCustomLoggerName() {
		ObjectWithLabeledLogger object = new ObjectWithLabeledLogger();
		context.inject(object);
		assertEquals("xyz", object.getLogger().getName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingLogService() {
		Context emptyContext = new Context(Collections.emptyList());
		ObjectWithLogger object = new ObjectWithLogger();
		emptyContext.inject(object);
	}

	@Test
	public void testMissingLogServiceOptionalLogger() {
		Context emptyContext = new Context(Collections.emptyList());
		ObjectWithOptionalLogger object = new ObjectWithOptionalLogger();
		emptyContext.inject(object);
		assertNull(object.getLogger());
	}

	public static class ObjectWithLogger {

		@Parameter Logger log;

		public Logger getLogger() {
			return log;
		}

		public void logSomething() { log.warn("Something"); }
	}

	public static class ObjectWithLabeledLogger {

		@Parameter(label = "xyz") Logger log;

		public Logger getLogger() {
			return log;
		}
	}

	public static class ObjectWithOptionalLogger {

		@Parameter(required = false) Logger log;

		public Logger getLogger() {
			return log;
		}
	}
}
