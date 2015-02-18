
package org.scijava.path;

import org.scijava.plugin.Plugin;

@Plugin(type = Path.class)
public class SciJavaHelp extends AbstractTroubleshootingPath {

	// Just declare parent?
	public final static String names = "Image Problems>Black Image>bc";
	public final static String[] descs = { "My image looks wrong", "My image is black", "Let's try running Brightness/Contrast" };

	public SciJavaHelp() {
		setDescription(names, descs);
	}

	@Override
	public String getScriptName() {
		return "test.ijm";
	}

	@Override
	public String getScript() {
		// Return Command or Module...
		return "run(\"Enhance Contrast...\", \"saturated=0.4\");";
	}
}
