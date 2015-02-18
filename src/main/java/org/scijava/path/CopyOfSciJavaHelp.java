
package org.scijava.path;

import org.scijava.plugin.Plugin;

@Plugin(type = Path.class)
public class CopyOfSciJavaHelp extends AbstractTroubleshootingPath {

	public final static String names = "Image Problems>Black Image>bc";
	public final static String[] descs = { "My image looks wrong", "My image is black", "Let's try punching someone" };

	public CopyOfSciJavaHelp() {
		setDescription(names, descs);
	}

	@Override
	public String getScriptName() {
		return "test.ijm";
	}

	@Override
	public String getScript() {
		// Return Command or Module...
		return "run(\"Clown (14K)\");";
	}
}
