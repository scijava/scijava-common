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

package org.scijava.log;

import org.scijava.service.SciJavaService;

/**
 * Interface for the logging service.
 * <p>
 * The service supports five common logging levels: {@link #ERROR},
 * {@link #WARN}, {@link #INFO}, {@link #TRACE} and {@link #DEBUG}. It is
 * extensible to additional levels as needed. It provides methods for logging
 * messages, exception stack traces and combinations of the two.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Matthias Arzt
 */
public interface LogService extends SciJavaService, Logger {

	/** System property to set for overriding the default logging level. */
	String LOG_LEVEL_PROPERTY = "scijava.log.level";

	String LOG_LEVEL_BY_SOURCE_PROPERTY = "scijava.log.level.source";

	/** Changes the log level of the root logger. */
	void setLevel(int level);

	/**
	 * For messages that are logged directly to the LogService. The log level can
	 * be set depending on the class that makes the log.
	 * 
	 * @param classOrPackageName If this is the name of a class. Messages logged
	 *          directly by this class are logged, if the message's level is less
	 *          or equal to the given level. If this is a package, the same holds
	 *          for all classes in this package.
	 * @param level Given level.
	 */
	void setLevel(String classOrPackageName, int level);

	/**
	 * Setting the log level for loggers depending on their {@link LogSource}.
	 * This will only affect loggers that are created after this method has been
	 * called.
	 */
	void setLevelForLogger(String source, int level);

	// -- Deprecated --

	/** @deprecated Use {@link LogLevel#NONE}. */
	@Deprecated
	int NONE = LogLevel.NONE;
	/** @deprecated Use {@link LogLevel#ERROR}. */
	@Deprecated
	int ERROR = LogLevel.ERROR;
	/** @deprecated Use {@link LogLevel#WARN}. */
	@Deprecated
	int WARN = LogLevel.WARN;
	/** @deprecated Use {@link LogLevel#INFO}. */
	@Deprecated
	int INFO = LogLevel.INFO;
	/** @deprecated Use {@link LogLevel#DEBUG}. */
	@Deprecated
	int DEBUG = LogLevel.DEBUG;
	/** @deprecated Use {@link LogLevel#TRACE}. */
	@Deprecated
	int TRACE = LogLevel.TRACE;
}
