package org.scijava.log;

import org.junit.Test;

import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Tests {@link LogService}.
 *
 * @author Matthias Arzt
 */
public class LogServiceTest {

	@Test
	public void testGetPrefix() {
		TestableLogService logService = new TestableLogService();
		assertEquals("[ERROR]", logService.getPrefix(LogLevel.ERROR));
		assertEquals("[TRACE]", logService.getPrefix(LogLevel.TRACE));
	}

	@Test
	public void testCompleteLogMethod() {
		testCompleteLogMethod("ERROR", (logService, msg, t) -> logService.error(msg, t));
		testCompleteLogMethod("WARN", (logService, msg, t) -> logService.warn(msg, t));
		testCompleteLogMethod("INFO", (logService, msg, t) -> logService.info(msg, t));
		testCompleteLogMethod("DEBUG", (logService, msg, t) -> logService.debug(msg, t));
		testCompleteLogMethod("TRACE", (logService, msg, t) -> logService.trace(msg, t));
	}

	@Test
	public void testMessageLogMethod() {
		testMessageLogMethod("ERROR", (logService, msg) -> logService.error(msg));
		testMessageLogMethod("WARN", (logService, msg) -> logService.warn(msg));
		testMessageLogMethod("INFO", (logService, msg) -> logService.info(msg));
		testMessageLogMethod("DEBUG", (logService, msg) -> logService.debug(msg));
		testMessageLogMethod("TRACE", (logService, msg) -> logService.trace(msg));
	}

	@Test
	public void testExceptionLogMethod() {
		testExceptionLogMethod("ERROR", (logService, t) -> logService.error(t));
		testExceptionLogMethod("WARN", (logService, t) -> logService.warn(t));
		testExceptionLogMethod("INFO", (logService, t) -> logService.info(t));
		testExceptionLogMethod("DEBUG", (logService, t) -> logService.debug(t));
		testExceptionLogMethod("TRACE", (logService, t) -> logService.trace(t));
	}

	private void testCompleteLogMethod(String prefix, LogMethodCall logMethodCall) {
		testLogMethod(prefix, logMethodCall, true, true);
	}

	private void testMessageLogMethod(String prefix, BiConsumer<LogService, Object> call) {
		testLogMethod(prefix, (log, text, exception) -> call.accept(log, text), true, false);
	}

	private void testExceptionLogMethod(String prefix, BiConsumer<LogService, Throwable> call) {
		testLogMethod(prefix, (log, text, exception) -> call.accept(log, exception), false, true);

	}

	private void testLogMethod(String prefix, LogMethodCall logMethodCall, boolean testMessage, boolean testException) {
		// setup
		TestableLogService logService = new TestableLogService();
		logService.setLevel(LogLevel.TRACE);
		String text = "Message";
		NullPointerException exception = new NullPointerException();
		// process
		logMethodCall.run(logService, text, exception);
		// test
		if(testMessage) {
			assertTrue(logService.message().contains(prefix));
			assertTrue(logService.message().contains(text));
		}
		if(testException)
			assertEquals(exception, logService.exception());
	}

	@Test
	public void testSetLevel() {
		TestableLogService logService = new TestableLogService();
		logService.setLevel(LogLevel.TRACE);
		assertEquals(LogLevel.TRACE, logService.getLevel());
		logService.setLevel(LogLevel.ERROR);
		assertEquals(LogLevel.ERROR, logService.getLevel());
	}

	@Test
	public void testSetClassSpecificLevel() {
		TestableLogService logService = new TestableLogService();
		MyTestClass testClass = new MyTestClass(logService);
		logService.setLevel(LogLevel.ERROR);
		logService.setLevel(MyTestClass.class.getName(), LogLevel.TRACE);
		assertEquals(LogLevel.ERROR, logService.getLevel());
		assertEquals(LogLevel.TRACE, testClass.getLevel());
	}

	@Test
	public void testIsWarn() {
		testIsLevel(LogLevel.ERROR, LogService::isError);
		testIsLevel(LogLevel.WARN, LogService::isWarn);
		testIsLevel(LogLevel.INFO, LogService::isInfo);
		testIsLevel(LogLevel.DEBUG, LogService::isDebug);
		testIsLevel(LogLevel.TRACE, LogService::isTrace);
	}

	private void testIsLevel(int level, Function<LogService, Boolean> isLevel) {
		TestableLogService logService = new TestableLogService();
		logService.setLevel(LogLevel.NONE);
		assertFalse(isLevel.apply(logService));
		logService.setLevel(level);
		assertTrue(isLevel.apply(logService));
		logService.setLevel(LogLevel.TRACE);
		assertTrue(isLevel.apply(logService));
	}

	@Test
	public void testDefaultLevel() {
		assertEquals(LogLevel.INFO, new TestableLogService().getLevel());
	}

	@Test
	public void testMainSystemProperty() {
		Properties properties = new Properties();
		properties.setProperty(LogService.LOG_LEVEL_PROPERTY, "error");
		int level = new TestableLogService(properties).getLevel();
		assertEquals(LogLevel.ERROR, level);
	}

	static class Dummy {
		public static int getLevel(LogService log) {
			return log.getLevel();
		}
	}

	@Test
	public void testClassLogLevel() {
		final TestableLogService log = new TestableLogService();
		log.setLevel(LogLevel.DEBUG);
		log.setLevel(Dummy.class.getName(), LogLevel.ERROR);
		int level = Dummy.getLevel(log);
		assertEquals(LogLevel.ERROR, level);
	}

	@Test
	public void testClassLogLevelViaProperties() {
		Properties properties = new Properties();
		properties.setProperty(LogService.LOG_LEVEL_PROPERTY + ":" + Dummy.class.getName(), LogLevel.prefix(LogLevel.ERROR));
		properties.setProperty(LogService.LOG_LEVEL_PROPERTY + ":" + this.getClass().getName(), LogLevel.prefix(LogLevel.TRACE));
		final LogService log = new TestableLogService(properties);
		log.setLevel(LogLevel.DEBUG);
		int level = Dummy.getLevel(log);
		assertEquals(LogLevel.ERROR, level);
	}

	@Test
	public void testPackageLogLevel() {
		final LogService log = new TestableLogService();
		log.setLevel("org.scijava.log", LogLevel.TRACE);
		log.setLevel("xyz.foo.bar", LogLevel.ERROR);
		int level = log.getLevel();
		assertEquals(LogLevel.TRACE, level);
	}

	// -- Helper classes --

	private static class MyTestClass {

		private final LogService log;

		MyTestClass(LogService log) {
			this.log = log;
		}

		int getLevel() {
			return log.getLevel();
		}
	}

	private interface LogMethodCall {
		void run(LogService logService, Object text, Throwable exception);
	}

	private static class TestableLogService extends AbstractLogService {

		String message = null;
		Throwable exception = null;

		public TestableLogService() {
			this(new Properties());
		}

		public TestableLogService(Properties properties) {
			super(properties);
		}

		public String message() {
			return message;
		}

		public Throwable exception() {
			return exception;
		}

		@Override
		public void alwaysLog(int level, Object msg, Throwable t) {
			this.message = LogLevel.prefix(level) + msg;
			this.exception = t;
		}
	}
}
