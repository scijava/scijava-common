package org.scijava.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.headless.HeadlessUI;

public class FilePreprocessorTest {

	public static final String DUMMY_FILE_NAME = "dummy_file";
	private Context context;
	private ModuleService moduleService;

	@Before
	public void setUp() {
		context = new Context();
		context.service(UIService.class).setDefaultUI(new CustomUI());
		context.service(UIService.class).setHeadless(false);
		moduleService = context.service(ModuleService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void test() throws InterruptedException, ExecutionException {
		// assert that single File parameters get filled by ui.chooseFile()
		CommandInfo info = new CommandInfo(SingleParameterFileCommand.class);
		Module mod = moduleService.run(info, true).get();
		assertNotNull(mod);
		assertEquals(DUMMY_FILE_NAME, ((File)mod.getInput("inputFile")).getName());

		// assert that the presence of any other unresolved parameters prevents
		// calls to ui.chooseFile()
		CommandInfo info2 = new CommandInfo(ParameterFileAndOthersCommand.class);
		Module mod2 = moduleService.run(info2, true).get();
		assertNotNull(mod2);
		assertNull(mod2.getInput("inputFile"));

		// assert that 'autoFill = false' prevents calls to ui.chooseFile()
		CommandInfo info3 = new CommandInfo(NoAutofillParameterFileCommand.class);
		Module mod3 = moduleService.run(info3, true).get();
		assertNotNull(mod3);
		assertNull(mod3.getInput("inputFile"));
	}

	private class CustomUI extends HeadlessUI {

		@Override
		public File chooseFile(String title, File file, String style) {
			return new File(DUMMY_FILE_NAME);
		}
	}

	public static class SingleParameterFileCommand implements Command {

		@Parameter(persist = false)
		private File inputFile;

		@Override
		public void run() {
			// do nothing
		}

	}

	public static class ParameterFileAndOthersCommand implements Command {

		@Parameter(persist = false)
		private File inputFile;

		@Parameter(required = false)
		private String dummyString;

		@Override
		public void run() {
			// do nothing
		}

	}

	public static class NoAutofillParameterFileCommand implements Command {

		@Parameter(autoFill = false, persist = false)
		private File inputFile;

		@Override
		public void run() {
			// do nothing
		}

	}
}
