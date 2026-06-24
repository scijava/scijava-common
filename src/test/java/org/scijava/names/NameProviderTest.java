package org.scijava.names;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.AbstractHandlerPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

public class NameProviderTest {

	private Context context;

	@Before
	public void setUp() throws Exception {
		context = new Context();
		context.service(PluginService.class).addPlugin(PluginInfo.create(ThirdPartyObjectNameProvider.class, NameProvider.class));
	}

	@After
	public void tearDown() throws Exception {
		context.dispose();
	}

	@Test
	public void test() {
		String expected = "foo";
		ThirdPartyObject foo = new ThirdPartyObject(expected);
		NameProvider handler = context.service(NameService.class).getHandler(foo);
		assertNotNull(handler);
		assertEquals(expected, handler.getName(foo));
	}

	public static class ThirdPartyObjectNameProvider extends AbstractHandlerPlugin<Object> implements NameProvider {

		@Override
		public String getName(Object thing) {
			return ((ThirdPartyObject) thing).someNonStandardMethod();
		}

		@Override
		public boolean supports(Object thing) {
			//System.err.println("NameProvider queried with " + thing + " of class " + thing.getClass());
			return ThirdPartyObject.class.isAssignableFrom(thing.getClass());
		}
		
	}

	public static class ThirdPartyObject {
		private String name;

		public ThirdPartyObject (String name) {
			this.name = name;
		}

		public String someNonStandardMethod() {
			return name;
		}

		@Override
		public String toString() {
			return null;
		}
	}
}
