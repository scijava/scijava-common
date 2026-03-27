package org.scijava.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileFilter;
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

public class FileListPreprocessorTest {

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
		// assert that single File[] parameters get filled by ui.chooseFiles()
		CommandInfo info = new CommandInfo(SingleParameterFileListCommand.class);
		Module mod = moduleService.run(info, true).get();
		assertNotNull(mod);
		assertEquals(DUMMY_FILE_NAME, ((File[])mod.getInput("inputFiles"))[0].getName());

		// assert that the presence of any other unresolved parameters prevents
		// calls to ui.chooseFiles()
		CommandInfo info2 = new CommandInfo(ParameterFileListAndOthersCommand.class);
		Module mod2 = moduleService.run(info2, true).get();
		assertNotNull(mod2);
		assertNull(mod2.getInput("inputFiles"));

		// assert that 'autoFill = false' prevents calls to ui.chooseFiles()
		CommandInfo info3 = new CommandInfo(NoAutofillParameterFileListCommand.class);
		Module mod3 = moduleService.run(info3, true).get();
		assertNotNull(mod3);
		assertNull(mod3.getInput("inputFiles"));
	}

	private class CustomUI extends HeadlessUI {

		@Override
		public File[] chooseFiles(File parent, File[] files, FileFilter filter, String style) {
			return new File[] { new File(DUMMY_FILE_NAME) };
		}
	}

	public static class SingleParameterFileListCommand implements Command {

		@Parameter(persist = false)
		private File[] inputFiles;

		@Override
		public void run() {
			// do nothing
		}

	}

	public static class ParameterFileListAndOthersCommand implements Command {

		@Parameter(persist = false)
		private File[] inputFiles;

		@Parameter(required = false)
		private String dummyString;

		@Override
		public void run() {
			// do nothing
		}

	}

	public static class NoAutofillParameterFileListCommand implements Command {

		@Parameter(autoFill = false, persist = false)
		private File[] inputFiles;

		@Override
		public void run() {
			// do nothing
		}

	}
}
