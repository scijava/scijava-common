/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.scijava.event.ContextDisposingEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginIndex;
import org.scijava.service.Service;
import org.scijava.service.ServiceHelper;
import org.scijava.service.ServiceIndex;
import org.scijava.util.ClassUtils;
import org.scijava.util.Query;

/**
 * Top-level SciJava application context, which initializes and maintains a list
 * of services.
 *
 * @author Curtis Rueden
 * @see Service
 */
public class Context implements Disposable {

	// -- Constants --

	/**
	 * System property indicating whether the context should fail fast when
	 * attempting to instantiate a required service which is invalid or missing.
	 * If this property is set to "false" then the context creation will attempt
	 * to continue even when a required service cannot be instantiated. Otherwise,
	 * the constructor will throw an {@link IllegalArgumentException} in that
	 * situation.
	 */
	public static final String STRICT_PROPERTY = "scijava.context.strict";

	// -- Fields --

	/** Index of the application context's services. */
	private final ServiceIndex serviceIndex;

	/** Master index of all plugins known to the application context. */
	private final PluginIndex pluginIndex;

	/**
	 * Whether context creation and injection should behave strictly, failing fast
	 * when attempting to instantiate a required service which is invalid or
	 * missing.
	 * <ul>
	 * <li>If the flag is false, then the context creation will attempt to
	 * continue even when a required service cannot be instantiated. Otherwise,
	 * the constructor will throw an {@link IllegalArgumentException} in that
	 * situation.</li>
	 * <li>If this flag is false, then a call to {@link Context#inject(Object)}
	 * will attempt to catch any errors that occur during context injection
	 * (notably: {@link NoClassDefFoundError} when scanning for event handler
	 * methods), logging them as errors.</li>
	 * </ul>
	 */
	private boolean strict;

	/**
	 * Creates a new SciJava application context with all available services.
	 *
	 * @see #Context(Collection, PluginIndex, boolean)
	 */
	public Context() {
		this(false);
	}

	/**
	 * Creates a new SciJava application context.
	 *
	 * @param empty If true, the context will be empty of services; otherwise, it
	 *          will be initialized with all available services.
	 * @see #Context(boolean, boolean)
	 */
	public Context(final boolean empty) {
		this(empty, false);
	}

	/**
	 * Creates a new SciJava application context.
	 *
	 * @param noServices If true, the context will contain no services; otherwise,
	 *          it will be initialized with all available services.
	 * @param noPlugins If true, the context will contain no plugins; otherwise,
	 *          it will be initialized with all available plugins.
	 * @see #Context(Collection, PluginIndex, boolean)
	 */
	public Context(final boolean noServices, final boolean noPlugins) {
		this(services(noServices), plugins(noPlugins));
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
	 * <blockquote>Type safety: A generic array of
	 * {@code Class<? extends Service>} is created for a varargs
	 * parameter</blockquote>
	 * <p>
	 * To avoid this, we have opted to use raw types and suppress the relevant
	 * warnings here instead.
	 * </p>
	 *
	 * @param serviceClasses A list of types that implement the {@link Service}
	 *          interface (e.g., {@code DisplayService.class}). Compatible
	 *          services will be loaded in the order given, <em>regardless of
	 *          their relative priorities</em>.
	 * @see #Context(Collection, PluginIndex, boolean)
	 * @throws ClassCastException If any of the given arguments do not implement
	 *           the {@link Service} interface.
	 */
	public Context(@SuppressWarnings("rawtypes") final Class... serviceClasses) {
		this(serviceClassList(serviceClasses));
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies).
	 *
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code DisplayService.class}).
	 *          Compatible services will be loaded according to the order of the
	 *          collection, <em>regardless of their relative priorities</em>.
	 * @see #Context(Collection, PluginIndex, boolean)
	 */
	public Context(final Collection<Class<? extends Service>> serviceClasses) {
		this(serviceClasses, null);
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies).
	 *
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code DisplayService.class}).
	 *          Compatible services will be loaded according to the order of the
	 *          collection, <em>regardless of their relative priorities</em>.
	 * @param strict Whether context creation will fail fast when there is an
	 *          error instantiating a required service.
	 * @see #Context(Collection, PluginIndex, boolean)
	 */
	public Context(final Collection<Class<? extends Service>> serviceClasses,
		final boolean strict)
	{
		this(serviceClasses, null, strict);
	}

	/**
	 * Creates a new SciJava application context with all available services from
	 * the specified PluginIndex. This allows a base set of available plugins to
	 * be defined, and is useful when plugins that would not be returned by the
	 * {@link PluginIndex}'s {@link org.scijava.plugin.PluginFinder} are desired.
	 *
	 * @param pluginIndex The plugin index to use when discovering and indexing
	 *          plugins. If you wish to completely control how services are
	 *          discovered (i.e., use your own
	 *          {@link org.scijava.plugin.PluginFinder} implementation), then you
	 *          can pass a custom {@link PluginIndex} here. Passing null will
	 *          result in a default plugin index being constructed and used.
	 * @see #Context(Collection, PluginIndex, boolean)
	 */
	public Context(final PluginIndex pluginIndex) {
		this(services(false), pluginIndex);
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies). Service dependency candidates are
	 * selected from those discovered by the given {@link PluginIndex}'s
	 * associated {@link org.scijava.plugin.PluginFinder}.
	 *
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code DisplayService.class}).
	 *          Compatible services will be loaded according to the order of the
	 *          collection, <em>regardless of their relative priorities</em>.
	 * @param pluginIndex The plugin index to use when discovering and indexing
	 *          plugins. If you wish to completely control how services are
	 *          discovered (i.e., use your own
	 *          {@link org.scijava.plugin.PluginFinder} implementation), then you
	 *          can pass a custom {@link PluginIndex} here. Passing null will
	 *          result in a default plugin index being constructed and used.
	 * @see #Context(Collection, PluginIndex, boolean)
	 */
	public Context(final Collection<Class<? extends Service>> serviceClasses,
		final PluginIndex pluginIndex)
	{
		this(serviceClasses, pluginIndex, strict());
	}

	/**
	 * Creates a new SciJava application context with the specified services (and
	 * any required service dependencies). Service dependency candidates are
	 * selected from those discovered by the given {@link PluginIndex}'s
	 * associated {@link org.scijava.plugin.PluginFinder}.
	 * <p>
	 * NB: Context creation is an important step of a SciJava application's
	 * lifecycle. Particularly in environments where more than one implementation
	 * exists for various services, careful consideration should be exercised
	 * regarding what classes and plugins are provided to the Context, and what
	 * needs to occur during the initialization of these services (especially
	 * those of lower priority). See {@link ServiceHelper#loadServices()} for more
	 * information.
	 * </p>
	 *
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code DisplayService.class}).
	 *          Compatible services will be loaded according to the order of the
	 *          collection, <em>regardless of their relative priorities</em>.
	 * @param pluginIndex The plugin index to use when discovering and indexing
	 *          plugins. If you wish to completely control how services are
	 *          discovered (i.e., use your own
	 *          {@link org.scijava.plugin.PluginFinder} implementation), then you
	 *          can pass a custom {@link PluginIndex} here. Passing null will
	 *          result in a default plugin index being constructed and used.
	 * @param strict Whether context creation will fail fast when there is an
	 *          error instantiating a required service.
	 */
	public Context(final Collection<Class<? extends Service>> serviceClasses,
		final PluginIndex pluginIndex, final boolean strict)
	{
		serviceIndex = new ServiceIndex();

		this.pluginIndex = pluginIndex == null ? new PluginIndex() : pluginIndex;
		this.pluginIndex.discover();

		setStrict(strict);

		if (!serviceClasses.isEmpty()) {
			final ServiceHelper serviceHelper = //
				new ServiceHelper(this, serviceClasses, strict);
			serviceHelper.loadServices();
		}
	}

	// -- Context methods --

	public ServiceIndex getServiceIndex() {
		return serviceIndex;
	}

	public PluginIndex getPluginIndex() {
		return pluginIndex;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(final boolean strict) {
		this.strict = strict;
	}

	/**
	 * Gets the service of the given class.
	 *
	 * @throws NoSuchServiceException if the context does not have the requested
	 *           service.
	 */
	public <S extends Service> S service(final Class<S> c) {
		final S service = getService(c);
		if (service == null) {
			throw new NoSuchServiceException("Service " + c.getName() +
				" not found.");
		}
		return service;
	}

	/**
	 * Gets the service of the given class name (useful for scripts).
	 *
	 * @throws IllegalArgumentException if the class does not exist, or is not a
	 *           service class.
	 * @throws NoSuchServiceException if the context does not have the requested
	 *           service.
	 */
	public Service service(final String className) {
		final Class<?> c = ClassUtils.loadClass(className, false);
		if (!Service.class.isAssignableFrom(c)) {
			throw new IllegalArgumentException("Not a service class: " + c.getName());
		}
		@SuppressWarnings("unchecked")
		final Class<? extends Service> serviceClass = (Class<? extends Service>) c;
		return service(serviceClass);
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
		final Class<?> c = ClassUtils.loadClass(className);
		if (c == null) return null;
		if (!Service.class.isAssignableFrom(c)) return null; // not a service class
		@SuppressWarnings("unchecked")
		final Class<? extends Service> serviceClass = (Class<? extends Service>) c;
		return getService(serviceClass);
	}

	/**
	 * Injects the application context into the given object. This does three
	 * distinct things:
	 * <ol>
	 * <li>If the given object has any non-final {@link Context} fields annotated
	 * with @{@link Parameter}, sets the value of those fields to this context.
	 * </li>
	 * <li>If the given object has any non-final {@link Service} fields annotated
	 * with @{@link Parameter}, sets the value of those fields to the
	 * corresponding service available from this context.</li>
	 * <li>Calls {@link EventService#subscribe(Object)} with the object to
	 * register any @{@link EventHandler} annotated methods as event subscribers.
	 * </li>
	 * </ol>
	 *
	 * @param o The object to which the context should be assigned.
	 * @throws IllegalStateException If the object already has a context.
	 * @throws IllegalArgumentException If the object has a required
	 *           {@link Service} parameter (see {@link Parameter#required()})
	 *           which is not available from this context.
	 */
	public void inject(final Object o) {
		// Ensure parameter fields and event handler methods are cached for this
		// object.
		final Query query = new Query();
		query.put(Parameter.class, Field.class);
		query.put(EventHandler.class, Method.class);
		ClassUtils.cacheAnnotatedObjects(o.getClass(), query);

		// iterate over all @Parameter annotated fields
		final List<Field> fields = getParameterFields(o);
		for (final Field f : fields) {
			inject(f, o);
		}

		// NB: Subscribe to all events handled by this object.
		// This greatly simplifies event handling.
		subscribeToEvents(o);
	}

	/**
	 * Reports whether a parameter of the given type would be assigned a value as
	 * a consequence of calling {@link #inject(Object)}.
	 * <p>
	 * This method is notably useful for downstream code to discern between
	 * {@link Parameter} fields whose values would be injected, versus those whose
	 * values would not, without needing to hardcode type comparison checks
	 * against the {@link Service} and {@link Context} types.
	 * </p>
	 * 
	 * @param type The type of the @{@link Parameter}-annotated field.
	 * @return True iff a member field of the given type would have its value
	 *         assigned.
	 */
	public boolean isInjectable(final Class<?> type) {
		if (Service.class.isAssignableFrom(type)) return true;
		return Context.class.isAssignableFrom(type) && type.isInstance(this);
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		final EventService eventService = getService(EventService.class);
		if (eventService != null) eventService.publish(new ContextDisposingEvent());

		// NB: Dispose services in reverse order.
		// This may or may not actually be necessary, but seems safer, since
		// dependent services will be disposed *before* their dependencies.
		final List<Service> services = serviceIndex.getAll();
		for (int s = services.size() - 1; s >= 0; s--) {
			services.get(s).dispose();
		}
	}

	// -- Utility methods --

	/**
	 * Utility method for converting a varargs list of service classes to a
	 * {@link List} of those classes.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Class<? extends Service>> serviceClassList(
		final Class... serviceClasses)
	{
		return serviceClasses != null ? //
			Arrays.asList(serviceClasses) : Arrays.asList(Service.class);
	}

	// -- Helper methods --

	private List<Field> getParameterFields(final Object o) {
		try {
			return ClassUtils.getAnnotatedFields(o.getClass(), Parameter.class);
		}
		catch (final Throwable t) {
			handleSafely(t);
		}
		return Collections.emptyList();
	}

	private void inject(final Field f, final Object o) {
		try {
			f.setAccessible(true); // expose private fields

			final Class<?> type = f.getType();
			if (Service.class.isAssignableFrom(type)) {
				final Service existingService = (Service) ClassUtils.getValue(f, o);
				if (strict && existingService != null) {
					throw new IllegalStateException("Context already injected: " + //
						f.getDeclaringClass().getName() + "#" + f.getName());
				}

				// populate Service parameter
				@SuppressWarnings("unchecked")
				final Class<? extends Service> serviceType =
					(Class<? extends Service>) type;
				final Service service = getService(serviceType);
				if (service == null && f.getAnnotation(Parameter.class).required()) {
					throw new IllegalArgumentException(//
						createMissingServiceMessage(serviceType));
				}
				if (existingService != null && existingService != service) {
					// NB: Can only happen in non-strict mode.
					throw new IllegalStateException("Mismatched context: " + //
						f.getDeclaringClass().getName() + "#" + f.getName());
				}
				ClassUtils.setValue(f, o, service);
			}
			else if (Context.class.isAssignableFrom(type) && type.isInstance(this)) {
				final Context existingContext = (Context) ClassUtils.getValue(f, o);
				if (strict && existingContext != null) {
					throw new IllegalStateException("Context already injected: " + //
						f.getDeclaringClass().getName() + "#" + f.getName());
				}
				if (existingContext != null && existingContext != this) {
					// NB: Can only happen in non-strict mode.
					throw new IllegalStateException("Mismatched context: " + //
						f.getDeclaringClass().getName() + "#" + f.getName());
				}

				// populate Context parameter
				ClassUtils.setValue(f, o, this);
			}
			else if (!type.isPrimitive()) {
				// the parameter is some other object; if it is non-null, we recurse
				final Object value = ClassUtils.getValue(f, o);
				if (value != null) inject(value);
			}
		}
		catch (final Throwable t) {
			handleSafely(t);
		}
	}

	private void subscribeToEvents(final Object o) {
		try {
			final EventService eventService = getService(EventService.class);
			if (eventService != null) eventService.subscribe(o);
		}
		catch (final Throwable t) {
			handleSafely(t);
		}
	}

	private void handleSafely(final Throwable t) {
		if (isStrict()) {
			// NB: Only rethrow unchecked exceptions.
			if (t instanceof RuntimeException) throw (RuntimeException) t;
			if (t instanceof Error) throw (Error) t;
		}
		final LogService log = getService(LogService.class);
		if (log != null) log.error(t);
	}

	private String createMissingServiceMessage(
		final Class<? extends Service> serviceType)
	{
		final String nl = System.getProperty("line.separator");
		final ClassLoader classLoader = //
			Thread.currentThread().getContextClassLoader();
		final StringBuilder msg = new StringBuilder(
			"Required service is missing: " + serviceType.getName() + nl);
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
			msg.append(
				"ClassLoader was not a URLClassLoader. Could not print classpath.");
		}
		return msg.toString();
	}

	private static PluginIndex plugins(final boolean empty) {
		return empty ? new PluginIndex(null) : null;
	}

	private static List<Class<? extends Service>> services(final boolean empty) {
		if (empty) return Collections.<Class<? extends Service>>emptyList();
		return Arrays.<Class<? extends Service>>asList(Service.class);
	}

	private static boolean strict() {
		return !"false".equals(System.getProperty(STRICT_PROPERTY));
	}

}
