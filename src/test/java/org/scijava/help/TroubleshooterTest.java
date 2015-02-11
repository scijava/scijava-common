
package org.scijava.help;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.path.BranchingPathNode;

public class TroubleshooterTest {

	@Test
	public void testSciJavaHelp() {
		final Context c = new Context();
		TroubleshootingService ts = c.getService(TroubleshootingService.class);
	
		BranchingPathNode root = ts.getRoot();
	
		printPath(root, 0);
	}

	private void printPath(BranchingPathNode node, int depth) {
		String s = "";
		for (int i=0; i<depth; i++) s += "\t";
		s += node.getDescription();
	
		System.out.println(s);
		
		for (String path : node.keySet()) {
			printPath(node.get(path), depth + 1);
		}
	}
}
