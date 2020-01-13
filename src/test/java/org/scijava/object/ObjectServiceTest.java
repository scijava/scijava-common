package org.scijava.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.SciJavaPlugin;

public class ObjectServiceTest {

	private Context context;
	private ObjectService objectService;

	@Before
	public void setUp() {
		context = new Context(ObjectService.class);
		objectService = context.getService(ObjectService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testAddRemoveObjects() {
		Object obj1 = new Object();
		String name1 = "Object 1";
		Object obj2 = "";
		Object obj3 = new Double(0.3);
		PluginInfo<SciJavaPlugin> obj4 = PluginInfo.create(TestPlugin.class, SciJavaPlugin.class);
		obj4.setName("TestPlugin name");

		objectService.addObject(obj1, name1);
		assertEquals("Name of object 1", name1, objectService.getName(obj1));
		objectService.addObject(obj2);
		assertEquals("Name of object 2", obj2.toString(), objectService.getName(obj2));
		objectService.addObject(obj3, null);
		assertEquals("Name of object 3", obj3.toString(), objectService.getName(obj3));
		objectService.addObject(obj4);
		assertNotNull(objectService.getName(obj4));
		assertEquals("Name of object 4", obj4.getName(), objectService.getName(obj4));

		assertTrue("Object 1 registered", objectService.getObjects(Object.class).contains(obj1));
		assertTrue("Object 2 registered", objectService.getObjects(Object.class).contains(obj2));
		assertTrue("Object 3 registered", objectService.getObjects(Object.class).contains(obj3));
		assertTrue("Object 4 registered", objectService.getObjects(Object.class).contains(obj4));

		objectService.removeObject(obj1);
		objectService.removeObject(obj2);
		objectService.removeObject(obj3);
		objectService.removeObject(obj4);

		assertFalse("Object 1 removed", objectService.getObjects(Object.class).contains(obj1));
		assertFalse("Object 2 removed", objectService.getObjects(Object.class).contains(obj2));
		assertFalse("Object 3 removed", objectService.getObjects(Object.class).contains(obj3));
		assertFalse("Object 4 removed", objectService.getObjects(Object.class).contains(obj4));
	}

	@Test
	public void testNamedObjectIndex() {
		ObjectIndex<Object> index = objectService.getIndex();
		assertTrue(index instanceof NamedObjectIndex);
	}

	private class TestPlugin implements SciJavaPlugin {

	}
}
