/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

package org.scijava;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginIndex;
import org.scijava.service.Service;
import org.scijava.service.ServiceHelper;
import org.scijava.service.ServiceIndex;
import org.scijava.util.ClassUtils;

/**
 * Top-level SciJava application context, which initializes and maintains a list
 * of services.
 * 
 * @author Curtis Rueden
 * @see Service
 */
public class Context implements Disposable {

	// -- Fields --

	/** Index of the application context's services. */
	private final ServiceIndex serviceIndex;

	/** Master index of all plugins known to the application context. */
	private final PluginIndex pluginIndex;

	/** Creates a new SciJava application context with all available services. */
	public Context() {
		this(false);
	}

	/**
	 * Creates a new SciJava application context.
	 * 
	 * @param empty If true, the context will be empty; otherwise, it will be
	 *          initialized with all available services.
	 */
	@SuppressWarnings("unchecked")
	public Context(final boolean empty) {
		this(empty ? Collections.<Class<? extends Service>> emptyList() : Arrays
			.<Class<? extends Service>> asList(Service.class));
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies).
	 * <p>
	 * <b>Developer's note:</b> This constructor's argument is raw (i.e.,
	 * {@code Class...} instead of {@code Class<? extends Service>...}) because
	 * otherwise, downstream invocations (e.g.,
	 * {@code new Context(DisplayService.class)}) yield the potentially confusing
	 * warning:
	 * </p>
	 * <blockquote>Type safety: A generic array of Class<? extends Service> is
	 * created for a varargs parameter</blockquote>
	 * <p>
	 * To avoid this, we have opted to use raw types and suppress the relevant
	 * warnings here instead.
	 * </p>
	 * 
	 * @param serviceClasses A list of types that implement the {@link Service}
	 *          interface (e.g., {@code DisplayService.class}).
	 * @throws ClassCastException If any of the given arguments do not implement
	 *           the {@link Service} interface.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Context(final Class... serviceClasses) {
		this(serviceClasses != null ? (Collection) Arrays.asList(serviceClasses)
			: Arrays.asList(Service.class));
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies).
	 * 
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code DisplayService.class}).
	 */
	public Context(final Collection<Class<? extends Service>> serviceClasses) {
		this(serviceClasses, new PluginIndex());
	}

	/**
	 * Creates a new SciJava application with the specified PluginIndex. This
	 * allows a base set of available plugins to be defined, and is useful when
	 * plugins that would not be returned by the PluginIndex's PluginFinder are
	 * desired.
	 * <p>
	 * NB: the {@link PluginIndex#discover()} method may still be called, adding
	 * additional plugins to this index. The mechanism of discovery should be
	 * configured exclusively through the attached PluginFinder.
	 * </p>
	 * 
	 * @param pluginIndex The plugin index to use when discovering and indexing
	 *          plugins. If you wish to completely control how services are
	 *          discovered (i.e., use your own
	 *          {@link org.scijava.plugin.PluginFinder} implementation), then you
	 *          can pass a custom {@link PluginIndex} here.
	 */
	@SuppressWarnings("unchecked")
	public Context(final PluginIndex pluginIndex) {
		this(Arrays.<Class<? extends Service>> asList(Service.class), pluginIndex);
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies). Service dependency candidates are
	 * selected from those discovered by the given {@link PluginIndex}'s
	 * associated {@link org.scijava.plugin.PluginFinder}.
	 * <p>
	 * NB: Context creation is an important step of a SciJava applictation's
	 * lifecycle. Particularly in environments where more than one implementation
	 * exists for various services, careful consideration should be exercised
	 * regaring what classes and plugins are provided to the Context, and what
	 * needs to occur during the initialization of these services (especially
	 * those of lower priority). See {@link ServiceHelper#loadServices()} for more
	 * information.
	 * </p>
	 * 
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code DisplayService.class}).
	 * @param pluginIndex The plugin index to use when discovering and indexing
	 *          plugins. If you wish to completely control how services are
	 *          discovered (i.e., use your own
	 *          {@link org.scijava.plugin.PluginFinder} implementation), then you
	 *          can pass a custom {@link PluginIndex} here.
	 */
	public Context(final Collection<Class<? extends Service>> serviceClasses,
		final PluginIndex pluginIndex)
	{
		serviceIndex = new ServiceIndex();

		this.pluginIndex = pluginIndex;
		pluginIndex.discover();

		final ServiceHelper serviceHelper = new ServiceHelper(this, serviceClasses);
		serviceHelper.loadServices();
	}

	// -- Context methods --

	public ServiceIndex getServiceIndex() {
		return serviceIndex;
	}

	public PluginIndex getPluginIndex() {
		return pluginIndex;
	}

	/**
	 * Gets the service of the given class, or null if there is no matching
	 * service.
	 */
	public <S extends Service> S getService(final Class<S> c) {
		return serviceIndex.getService(c);
	}

	/** Gets the service of the given class name (useful for scripts). */
	public Service getService(final String className) {
		try {
			final ClassLoader loader = Thread.currentThread().getContextClassLoader();
			final Class<?> c = loader.loadClass(className);
			if (!Service.class.isAssignableFrom(c)) {
				// not a service class
				return null;
			}
			@SuppressWarnings("unchecked")
			final Class<? extends Service> serviceClass =
				(Class<? extends Service>) c;
			return getService(serviceClass);
		}
		catch (final ClassNotFoundException exc) {
			return null;
		}
	}

	/**
	 * Injects the application context into the given object. This does three
	 * distinct things:
	 * <ul>
	 * <li>If the given object has any non-final {@link Context} fields annotated
	 * with @{@link Parameter}, sets the value of those fields to this context.</li>
	 * <li>If the given object has any non-final {@link Service} fields annotated
	 * with @{@link Parameter}, sets the value of those fields to the
	 * corresponding service available from this context.</li>
	 * <li>Calls {@link EventService#subscribe(Object)} with the object to
	 * register any @{@link EventHandler} annotated methods as event subscribers.</li>
	 * .</li>
	 * </ul>
	 * 
	 * @param o The object to which the context should be assigned.
	 * @throws IllegalStateException If the object already has a context.
	 * @throws IllegalArgumentException If the object has a required
	 *           {@link Service} parameter (see {@link Parameter#required()})
	 *           which is not available from this context.
	 */
	public void inject(final Object o) {
		// iterate over all @Parameter annotated fields
		final List<Field> fields =
			ClassUtils.getAnnotatedFields(o.getClass(), Parameter.class);
		for (final Field f : fields) {
			f.setAccessible(true); // expose private fields

			final Class<?> type = f.getType();
			if (Service.class.isAssignableFrom(type)) {
				final Service existingService = (Service) ClassUtils.getValue(f, o);
				if (existingService != null) {
					throw new IllegalStateException("Context already injected: " +
						f.getDeclaringClass().getName() + "#" + f.getName());
				}

				// populate Service parameter
				@SuppressWarnings("unchecked")
				final Class<? extends Service> serviceType =
					(Class<? extends Service>) type;
				final Service service = getService(serviceType);
				if (service == null && f.getAnnotation(Parameter.class).required()) {
					throw new IllegalArgumentException(
						createMissingServiceMessage(serviceType));
				}
				ClassUtils.setValue(f, o, service);
			}
			else if (type.isAssignableFrom(getClass())) {
				final Context existingContext = (Context) ClassUtils.getValue(f, o);
				if (existingContext != null) {
					throw new IllegalStateException("Context already injected: " +
						f.getDeclaringClass().getName() + "#" + f.getName());
				}

				// populate Context parameter
				ClassUtils.setValue(f, o, this);
			}
		}

		// NB: Subscribe to all events handled by this object.
		// This greatly simplifies event handling.
		final EventService eventService = getService(EventService.class);
		if (eventService != null) eventService.subscribe(o);
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		// NB: Dispose services in reverse order.
		// This may or may not actually be necessary, but seems safer, since
		// dependent services will be disposed *before* their dependencies.
		final List<Service> services = serviceIndex.getAll();
		for (int s = services.size() - 1; s >= 0; s--) {
			services.get(s).dispose();
		}
	}

	// -- Helper methods --

	private String createMissingServiceMessage(
		final Class<? extends Service> serviceType)
	{
		final String nl = System.getProperty("line.separator");
		final ClassLoader classLoader =
			Thread.currentThread().getContextClassLoader();
		final StringBuilder msg =
			new StringBuilder("Required service is missing: " +
				serviceType.getName() + nl);
		msg.append("Context: " + this + nl);
		msg.append("ClassLoader: " + classLoader + nl);

		// Add list of services known to context
		msg.append(nl + "-- Services known to context --" + nl);
		for (final Service knownService : serviceIndex.getAll()) {
			msg.append(knownService + nl);
		}

		// Add list of classes known to classloader
		msg.append(nl + "-- Classpath of ClassLoader --" + nl);
		if (classLoader instanceof URLClassLoader) {
			for (final URL url : ((URLClassLoader) classLoader).getURLs()) {
				msg.append(url.getPath() + nl);
			}
		}
		else {
			msg
				.append("ClassLoader was not a URLClassLoader. Could not print classpath.");
		}
		return msg.toString();
	}

}
