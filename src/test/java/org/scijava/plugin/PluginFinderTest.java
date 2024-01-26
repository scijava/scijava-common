/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava.plugin;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.scijava.Context;

/**
 * Tests {@link PluginFinder}.
 * 
 * @author Curtis Rueden
 */
public class PluginFinderTest {

	/**
	 * Tests that the {@code scijava.plugin.blocklist} system property works to
	 * exclude plugins from the index, even when they are on the classpath.
	 */
	@Test
	public void testPluginBlocklistSystemProperty() {
		// check that the plugin is there, normally
		Context context = new Context(PluginService.class);
		PluginService pluginService = context.service(PluginService.class);
		PluginInfo<SciJavaPlugin> plugin = //
			pluginService.getPlugin(BlocklistedPlugin.class);
		assertSame(BlocklistedPlugin.class.getName(), plugin.getClassName());
		context.dispose();

		// blocklist the plugin, then check that it is absent
		System.setProperty("scijava.plugin.blocklist", ".*BlocklistedPlugin");
		context = new Context(PluginService.class);
		pluginService = context.service(PluginService.class);
		plugin = pluginService.getPlugin(BlocklistedPlugin.class);
		assertNull(plugin);
		context.dispose();

		// reset the system
		System.getProperties().remove("scijava.plugin.blocklist");
	}

	@Plugin(type = SciJavaPlugin.class)
	public static class BlocklistedPlugin implements SciJavaPlugin {
		// NB: No implementation needed.
	}

}
