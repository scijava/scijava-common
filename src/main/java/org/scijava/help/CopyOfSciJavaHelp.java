
package org.scijava.help;

import org.scijava.plugin.Plugin;

@Plugin(type = TroubleshootingPath.class)
public class CopyOfSciJavaHelp extends AbstractTroubleshootingPath {

	public final static String names = "x>y>b>scijava";
	public final static String[] descs = { "wug", "wug2", "wug3", "wug4" };

	public CopyOfSciJavaHelp() {
		setDescription(names, descs);
	}
}
