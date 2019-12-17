package org.scijava.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class NamedObjectIndexTest {

	@Test
	public void testNamedObjects() {
		NamedObjectIndex<String> index = new NamedObjectIndex<>(String.class);
		String obj1 = "obj1";
		String name1 = "name1";
		String obj2 = "obj1";
		String name2 = "name1";
		assertTrue(index.add(obj1, name1));
		assertTrue(index.add(obj2, String.class, name2, false));
		assertTrue(index.contains(obj1));
		assertTrue(index.contains(obj2));
		assertEquals(name1, index.getName(obj1));
		assertEquals(name2, index.getName(obj2));
		assertTrue(index.remove(obj1));
		assertTrue(index.remove(obj2));
		assertFalse(index.contains(obj1));
		assertFalse(index.contains(obj2));
	}

	@Test
	public void testNullNames() {
		NamedObjectIndex<String> index = new NamedObjectIndex<>(String.class);
		String obj1 = "object1";
		String name1 = null;
		String obj2 = "object2";
		String name2 = "";
		assertTrue(index.add(obj1, name1));
		assertTrue(index.add(obj2, name2));
		assertEquals(name1, index.getName(obj1));
		assertEquals(name2, index.getName(obj2));
	}
}
