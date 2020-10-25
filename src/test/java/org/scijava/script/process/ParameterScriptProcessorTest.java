package org.scijava.script.process;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.script.ScriptInfo;

public class ParameterScriptProcessorTest {

	private Context context;

	@Before
	public void setUp() {
		context = new Context();
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testScriptParameterParsing() {
		String script = "" + //
				"% @String legacyStyleParameter\n" +
				"% #@ String commentedHeaderParameter\n" +
				"% ############## Some Comment ###########\n" +
				"#@ String implicitInputParameter\n" +
				"#@input String explicitInputParameter\n" +
				"\n" +
				"% @String legacyStyleBodyParameter\n" +
				"% #@ String commentedBodyParameter\n" +
				"\n" +
				"#@output implicitlyTypedOutputParameter\n" +
				"#@output String explicitlyTypedOutputParameter\n";
		final ScriptInfo info = new ScriptInfo(context, ".bsizes", new StringReader(script));
		assertEquals("legacyStyleParameter", info.getInput("legacyStyleParameter").getName());
		assertEquals("implicitInputParameter", info.getInput("implicitInputParameter").getName());
		assertEquals("explicitInputParameter", info.getInput("explicitInputParameter").getName());

		assertEquals("implicitlyTypedOutputParameter", info.getOutput("implicitlyTypedOutputParameter").getName());
		assertEquals("explicitlyTypedOutputParameter", info.getOutput("explicitlyTypedOutputParameter").getName());

		assertNull(info.getInput("commentedHeaderParameter"));
		assertNull(info.getInput("legacyStyleBodyParameter"));
		assertNull(info.getInput("commentedBodyParameter"));
	}

}
