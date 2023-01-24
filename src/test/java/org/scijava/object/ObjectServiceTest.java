/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
