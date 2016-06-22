/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

package org.scijava.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.Optional;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.event.ServicesLoadedEvent;
import org.scijava.util.ClassUtils;

/**
 * Helper class for discovering and instantiating available services.
 * 
 * @author Curtis Rueden
 */
public class ServiceHelper extends AbstractContextual {

	/** For logging. */
	private LogService log;

	/**
	 * Classes to scan when searching for dependencies. Data structure is a map
	 * with keys being relevant classes, and values being associated priorities.
	 */
	private final Map<Class<? extends Service>, Double> classPoolMap;

	/** Classes to scan when searching for dependencies, sorted by priority. */
	private final List<Class<? extends Service>> classPoolList;

	/** Classes to instantiate as services. */
	private final List<Class<? extends Service>> serviceClasses;

	/**
	 * Whether service loading will fail fast when there is an error instantiating
	 * a required service.
	 */
	private final boolean strict;

	/**
	 * Creates a new service helper for discovering and instantiating services.
	 * 
	 * @param context The application context for which services should be
	 *          instantiated.
	 */
	public ServiceHelper(final Context context) {
		this(context, null);
	}

	/**
	 * Creates a new service helper for discovering and instantiating services.
	 * 
	 * @param context The application context to which services should be added.
	 * @param serviceClasses The service classes to instantiate.
	 */
	public ServiceHelper(final Context context,
		final Collection<Class<? extends Service>> serviceClasses)
	{
		this(context, serviceClasses, true);
	}

	/**
	 * Creates a new service helper for discovering and instantiating services.
	 * 
	 * @param context The application context to which services should be added.
	 * @param serviceClasses The service classes to instantiate.
	 * @param strict Whether service loading will fail fast when there is an error
	 *          instantiating a required service.
	 */
	public ServiceHelper(final Context context,
		final Collection<Class<? extends Service>> serviceClasses,
		final boolean strict)
	{
		setContext(context);
		log = context.getService(LogService.class);
		if (log == null) log = new StderrLogService();
		classPoolMap = new HashMap<>();
		classPoolList = new ArrayList<>();
		findServiceClasses(classPoolMap, classPoolList);
		if (classPoolList.isEmpty()) {
			log.warn("Class pool is empty: forgot to call Thread#setClassLoader?");
		}
		this.serviceClasses = new ArrayList<>();
		if (serviceClasses == null) {
			// load all discovered services
			this.serviceClasses.addAll(classPoolList);
		}
		else {
			// load only the services that were explicitly specified
			this.serviceClasses.addAll(serviceClasses);
		}
		this.strict = strict;
	}

	// -- ServiceHelper methods --

	/**
	 * Ensures all candidate service classes are registered in the index, locating
	 * and instantiating compatible services as needed.
	 * <p>
	 * This is a NxM operation, where N is the number of service classes attached
	 * to this ServiceHelper, and M is the number of discovered plugins. Multiple
	 * implementations of a given service subclass can be loaded. For example, if
	 * FooService, a subclass of {@link Service} (whether abstract, concrete, or
	 * an interface), appears on the service list, all subclasses of FooService in
	 * the plugin class pool will be loaded.
	 * </p>
	 * <p>
	 * NB: In typical use, a {@link Context} will only return the highest priority
	 * implementation for a loaded service. To retrieve the lower priority
	 * service(s), they must be requested directly (or through a superclass not
	 * shared with the higher priority implementation). Thus, as these lower
	 * priority services will go unused in many cases, it is critical that service
	 * loading (initialization) is as lightweight as possible.
	 * </p>
	 * 
	 * @throws IllegalArgumentException if one of the requested services is
	 *           required (i.e., not marked {@link Optional}) but cannot be
	 *           filled.
	 */
	public void loadServices() {
		for (final Class<? extends Service> serviceClass : serviceClasses) {
			// Load all compatible classes
			for (Class<? extends Service> c : classPoolList) {
				if (serviceClass.isAssignableFrom(c)) loadService(c);
			}

			// Make sure loadService gets called once on the actual provided class
			loadService(serviceClass);

			if (LogService.class.isAssignableFrom(serviceClass)) {
				final LogService logService = context().getService(LogService.class);
				if (logService != null) log = logService;
			}
		}
		final EventService eventService = context().getService(EventService.class);
		if (eventService != null) {
			eventService.publishLater(new ServicesLoadedEvent());
		}
	}

	/**
	 * Obtains a service compatible with the given class, instantiating it (and
	 * registering it in the index) if necessary.
	 * 
	 * @return an existing compatible service if one is already registered; or
	 *         else a newly created instance of the service with highest priority;
	 *         or null if no suitable service can be created
	 * @throws IllegalArgumentException if no suitable service can be created and
	 *           the class is required (i.e., not marked {@link Optional})
	 */
	public <S extends Service> S loadService(final Class<S> c) {
		return loadService(c, !isOptional(c));
	}

	/**
	 * Instantiates a service of the given class, registering it in the index.
	 * 
	 * @return the newly created service, or null if the given class cannot be
	 *         instantiated
	 */
	public <S extends Service> S createExactService(final Class<S> c) {
		return createExactService(c, false);
	}

	// -- Helper methods --

	/**
	 * Obtains a service compatible with the given class, instantiating it (and
	 * registering it in the index) if necessary.
	 * 
	 * @return an existing compatible service if one is already registered; or
	 *         else a newly created instance of the service with highest priority;
	 *         or null if no suitable service can be created
	 * @throws IllegalArgumentException if no suitable service can be created and
	 *           the {@code required} flag is {@code true}
	 */
	private <S extends Service> S loadService(final Class<S> c,
		final boolean required)
	{
		// if a compatible service already exists, return it
		final S service = context().getService(c);
		if (service != null) return service;

		// scan the class pool for a suitable match
		for (final Class<? extends Service> serviceClass : classPoolList) {
			if (c.isAssignableFrom(serviceClass)) {
				// found a match; now instantiate it
				@SuppressWarnings("unchecked")
				final S result = (S) createExactService(serviceClass, required);
				if (required && result == null) {
					final String error = "No match: " + serviceClass.getName();
					if (strict) throw new IllegalArgumentException(error);
					log.error(error);
				}
				return result;
			}
		}

		if (required && c.isInterface()) {
			final String error = "No compatible service: " + c.getName();
			if (strict) throw new IllegalArgumentException(error);
			log.error(error);
			return null;
		}

		return createExactService(c, required);
	}

	/**
	 * Instantiates a service of the given class, registering it in the index.
	 * 
	 * @return the newly created service, or null if the given class cannot be
	 *         instantiated
	 * 
	 * @throws IllegalArgumentException if there is an error creating the service
	 *           and the {@code required} flag is {@code true}
	 */
	private <S extends Service> S createExactService(final Class<S> c,
		final boolean required)
	{
		final String name = c.getName();
		log.debug("Creating service: " + name, null);
		try {
			long start = 0, end = 0;
			boolean debug = log.isDebug();
			if (debug) start = System.currentTimeMillis();
			final S service = createServiceRecursively(c);
			context().getServiceIndex().add(service);
			if (debug) {
				end = System.currentTimeMillis();
				log.debug("Created service '" + name + "' in " + (end - start) + " ms");
			}
			return service;
		}
		catch (final Throwable t) {
			if (required) {
				final String error = "Invalid service: " + name;
				if (strict) throw new IllegalArgumentException(error, t);
				log.error(error, t);
			}
			else if (log.isDebug()) {
				// when in debug mode, give full stack trace of invalid services
				log.debug("Invalid service: " + name, t);
			}
			else {
				// we emit only a short warning for failing optional services
				log.warn("Invalid service: " + name);
			}
		}
		return null;
	}

	/**
	 * Instantiates a service of the given class, recursively populating its
	 * service parameters.
	 */
	private <S extends Service> S createServiceRecursively(final Class<S> c)
		throws InstantiationException, IllegalAccessException
	{
		final S service = c.newInstance();
		service.setContext(getContext());

		// propagate priority if known
		final Double priority = classPoolMap.get(c);
		if (priority != null) service.setPriority(priority);

		// NB: If there are any @EventHandler annotated methods, we treat the
		// EventService as a required dependency, _unless_ there is also an
		// EventService field annotated with @Parameter(required = false).
		boolean eventServiceRequired = true;

		// populate service parameters
		final List<Field> fields =
			ClassUtils.getAnnotatedFields(c, Parameter.class);
		for (final Field f : fields) {
			f.setAccessible(true); // expose private fields

			final Class<?> type = f.getType();
			if (type.isAssignableFrom(context().getClass())) {
				// populate annotated Context field
				ClassUtils.setValue(f, service, getContext());
				continue;
			}
			if (!Service.class.isAssignableFrom(type)) {
				final String error = "Invalid parameter: " +
					f.getDeclaringClass().getName() + "#" + f.getName();
				if (strict) throw new IllegalArgumentException(error);
				log.error(error);
				continue;
			}
			@SuppressWarnings("unchecked")
			final Class<? extends Service> serviceType =
				(Class<? extends Service>) type;
			Service s = context().getService(serviceType);
			if (s == null) {
				// recursively obtain needed service
				final boolean required = f.getAnnotation(Parameter.class).required();
				s = loadService(serviceType, required);
				// NB: Remember when there is an optional EventService parameter.
				if (s instanceof EventService) eventServiceRequired = required;
			}
			ClassUtils.setValue(f, service, s);
		}

		// check for event handlers
		if (!ClassUtils.getAnnotatedMethods(c, EventHandler.class).isEmpty()) {
			// NB: There are @EventHandler methods; we need an EventService.
			loadService(EventService.class, eventServiceRequired);
		}

		service.initialize();
		service.registerEventHandlers();
		return service;
	}

	/** Asks the plugin index for all available service implementations. */
	private void findServiceClasses(
		final Map<Class<? extends Service>, Double> serviceMap,
		final List<Class<? extends Service>> serviceList)
	{
		// ask the plugin index for the (sorted) list of available services
		final List<PluginInfo<Service>> services =
			context().getPluginIndex().getPlugins(Service.class);

		for (final PluginInfo<Service> info : services) {
			try {
				final Class<? extends Service> c = info.loadClass();
				final double priority = info.getPriority();
				serviceMap.put(c, priority);
				serviceList.add(c);
			}
			catch (final Throwable e) {
				log.error("Invalid service: " + info, e);
			}
		}
	}

	/** Returns true iff the given class is {@link Optional}. */
	private boolean isOptional(final Class<?> c) {
		return Optional.class.isAssignableFrom(c);
	}

}
