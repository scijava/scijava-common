
package org.scijava.help;

import org.scijava.plugin.Plugin;

@Plugin(type = TroubleshootingPath.class)
public class SciJavaHelp extends AbstractTroubleshootingPath {

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
		return "run(\"Enhance Contrast...\", \"saturated=0.4\");";
	}
}
