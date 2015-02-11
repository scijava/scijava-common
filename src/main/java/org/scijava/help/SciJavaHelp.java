
package org.scijava.help;

import org.scijava.plugin.Plugin;

@Plugin(type = TroubleshootingPath.class)
public class SciJavaHelp extends AbstractTroubleshootingPath {

	public final static String names = "x>y>z>scijava";
	public final static String[] descs = { "arf", "arf2", "arf3", "arf4" };

	public SciJavaHelp() {
		setDescription(names, descs);
	}
}
