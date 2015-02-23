
package org.scijava.path;

import org.scijava.module.Module;
import org.scijava.plugin.Plugin;

@Plugin(type = Path.class, menuRoot = TroubleshootingPath.ROOT,
	menuPath = "ImageProblems>Black Image>" + TroubleshootingPath.TAIL)
public class SciJavasNoHelp implements TroubleshootingPath {

	public final static String[] descs = { "My image looks wrong",
		"My image is black", "Let's try punching someone" };

	@Override
	public Module getModule() {
//		return "run(\"Clown (14K)\");";
		return null;
	}

	@Override
	public String[] getPathLabels() {
		return descs;
	}
}
