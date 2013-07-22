/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
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

	/**
	 * For logging.
	 */
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
		setContext(context);
		log = context.getService(LogService.class);
		if (log == null) log = new StderrLogService();
		classPoolMap = new HashMap<Class<? extends Service>, Double>();
		classPoolList = new ArrayList<Class<? extends Service>>();
		findServiceClasses(classPoolMap, classPoolList);
		if (classPoolList.isEmpty()) {
			log.warn("Class pool is empty: forgot to call Thread#setClassLoader?");
		}
		this.serviceClasses = new ArrayList<Class<? extends Service>>();
		if (serviceClasses == null) {
			// load all discovered services
			this.serviceClasses.addAll(classPoolList);
		}
		else {
			// load only the services that were explicitly specified
			this.serviceClasses.addAll(serviceClasses);
		}
	}

	// -- ServiceHelper methods --

	/**
	 * Ensures all candidate service classes are registered in the index, locating
	 * and instantiating compatible services as needed.
	 */
	public void loadServices() {
		for (final Class<? extends Service> serviceClass : serviceClasses) {
			loadService(serviceClass);
			if (serviceClass == LogService.class) {
				final LogService logService = getContext().getService(LogService.class);
				if (logService != null) log = logService;
			}
		}
		final EventService eventService =
			getContext().getService(EventService.class);
		if (eventService != null) eventService.publish(new ServicesLoadedEvent());
	}

	/**
	 * Obtains a service compatible with the given class, instantiating it (and
	 * registering it in the index) if necessary.
	 * 
	 * @return an existing compatible service if one is registered, or else the
	 *         newly created service, or null if none can be instantiated
	 * @throws IllegalArgumentException if no suitable service class is found
	 */
	public <S extends Service> S loadService(final Class<S> c) {
		// if a compatible service already exists, return it
		final S service = getContext().getServiceIndex().getService(c);
		if (service != null) return service;

		// scan the class pool for a suitable match
		for (final Class<? extends Service> serviceClass : classPoolList) {
			if (c.isAssignableFrom(serviceClass)) {
				// found a match; now instantiate it
				@SuppressWarnings("unchecked")
				final S result = (S) createExactService(serviceClass);
				return result;
			}
		}

		if (c.isInterface()) {
			throw new IllegalArgumentException("No class found that implements " + c);
		}

		return createExactService(c);
	}

	/**
	 * Instantiates a service of the given class, registering it in the index.
	 * 
	 * @return the newly created service, or null if the given class cannot be
	 *         instantiated
	 */
	public <S extends Service> S createExactService(final Class<S> c) {
		log.debug("Creating service: " + c.getName(), null);
		try {
			final S service = createService(c);
			getContext().getServiceIndex().add(service);
			log.info("Created service: " + c.getName());
			return service;
		}
		catch (final Throwable t) {
			if (log.isDebug()) {
				// when in debug mode, always give full stack trace of invalid services
				log.debug("Invalid service: " + c.getName(), t);
			}
			else if (!Optional.class.isAssignableFrom(c)) {
				// for required (i.e., non-optional) services, we also dump the stack
				log.warn("Invalid service: " + c.getName(), t);
			}
			else {
				// we emit only a short warning for failing optional services
				log.warn("Invalid service: " + c.getName());
			}
		}
		return null;
	}

	// -- Helper methods --

	/** Instantiates a service using the given constructor. */
	private <S extends Service> S createService(final Class<S> c)
		throws InstantiationException, IllegalAccessException
	{
		final S service = c.newInstance();
		service.setContext(getContext());

		// propagate priority if known
		final Double priority = classPoolMap.get(c);
		if (priority != null) service.setPriority(priority);

		// populate service parameters
		final List<Field> fields =
			ClassUtils.getAnnotatedFields(c, Parameter.class);
		for (final Field f : fields) {
			f.setAccessible(true); // expose private fields

			final Class<?> type = f.getType();
			if (!Service.class.isAssignableFrom(type)) {
				throw new IllegalArgumentException("Invalid parameter: " + f.getName());
			}
			@SuppressWarnings("unchecked")
			final Class<Service> serviceType = (Class<Service>) type;
			Service s = getContext().getServiceIndex().getService(serviceType);
			if (s == null) {
				// recursively obtain needed service
				s = loadService(serviceType);
			}
			ClassUtils.setValue(f, service, s);
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
			getContext().getPluginIndex().getPlugins(Service.class);

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

}
