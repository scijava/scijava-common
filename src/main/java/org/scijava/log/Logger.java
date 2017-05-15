/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.log;

import static org.scijava.log.LogLevel.DEBUG;
import static org.scijava.log.LogLevel.ERROR;
import static org.scijava.log.LogLevel.INFO;
import static org.scijava.log.LogLevel.TRACE;
import static org.scijava.log.LogLevel.WARN;

/**
 * Interface for objects which can produce log messages.
 * <p>
 * It provides methods for logging messages, exception stack traces and
 * combinations of the two.
 * </p>
 * 
 * @author Curtis Rueden
 * @see LogLevel
 * @see LogService
 */
@IgnoreAsCallingClass
public interface Logger {

	default void debug(final Object msg) {
		log(DEBUG, msg);
	}

	default void debug(final Throwable t) {
		log(DEBUG, t);
	}

	default void debug(final Object msg, final Throwable t) {
		log(DEBUG, msg, t);
	}

	default void error(final Object msg) {
		log(ERROR, msg);
	}

	default void error(final Throwable t) {
		log(ERROR, t);
	}

	default void error(final Object msg, final Throwable t) {
		log(ERROR, msg, t);
	}

	default void info(final Object msg) {
		log(INFO, msg);
	}

	default void info(final Throwable t) {
		log(INFO, t);
	}

	default void info(final Object msg, final Throwable t) {
		log(INFO, msg, t);
	}

	default void trace(final Object msg) {
		log(TRACE, msg);
	}

	default void trace(final Throwable t) {
		log(TRACE, t);
	}

	default void trace(final Object msg, final Throwable t) {
		log(TRACE, msg, t);
	}

	default void warn(final Object msg) {
		log(WARN, msg);
	}

	default void warn(final Throwable t) {
		log(WARN, t);
	}

	default void warn(final Object msg, final Throwable t) {
		log(WARN, msg, t);
	}

	default boolean isDebug() {
		return isLevel(DEBUG);
	}

	default boolean isError() {
		return isLevel(ERROR);
	}

	default boolean isInfo() {
		return isLevel(INFO);
	}

	default boolean isTrace() {
		return isLevel(TRACE);
	}

	default boolean isWarn() {
		return isLevel(WARN);
	}

	default boolean isLevel(final int level) {
		return getLevel() >= level;
	}

	/**
	 * Logs a message.
	 * 
	 * @param level The level at which the message will be logged. If the current
	 *          level (given by {@link #getLevel()} is below this one, no logging
	 *          is performed.
	 * @param msg The message to log.
	 */
	default void log(final int level, final Object msg) {
		log(level, msg, null);
	}

	/**
	 * Logs an exception.
	 * 
	 * @param level The level at which the exception will be logged. If the
	 *          current level (given by {@link #getLevel()} is below this one, no
	 *          logging is performed.
	 * @param t The exception to log.
	 */
	default void log(final int level, final Throwable t) {
		log(level, null, t);
	}

	/**
	 * Logs a message with an exception.
	 * 
	 * @param level The level at which the information will be logged. If the
	 *          current level (given by {@link #getLevel()} is below this one, no
	 *          logging is performed.
	 * @param msg The message to log.
	 * @param t The exception to log.
	 */
	default void log(final int level, final Object msg, final Throwable t) {
		if (isLevel(level)) alwaysLog(level, msg, t);
	}

	/**
	 * Logs a message with an exception. This message will always be logged even
	 * if its level is above the current level (given by {@link #getLevel()}).
	 *
	 * @param level The level at which the information will be logged.
	 * @param msg The message to log.
	 * @param t The exception to log.
	 */
	void alwaysLog(int level, Object msg, Throwable t);

	/** Returns the log level of this logger. see {@link LogLevel} */
	int getLevel();

	/**
	 * {@link LogListener}s added with this method are notified of every message,
	 * NB: Messages are only logged, if their level is lower than the logger's
	 * level.
	 *
	 * @param listener
	 */
	void addListener(LogListener listener);

	void removeListener(LogListener listener);
}
