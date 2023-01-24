/*
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

package org.scijava.script;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.NullContextException;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.module.AbstractModule;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.script.process.ScriptCallback;

/**
 * A {@link Module} which executes a script.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class ScriptModule extends AbstractModule implements Contextual {

	public static final String RETURN_VALUE = "result";

	private final ScriptInfo info;

	@Parameter
	private Context context;

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private ConvertService conversionService;

	@Parameter
	private LogService log;

	/** Script engine with which the script should be executed. */
	private ScriptEngine scriptEngine;

	/** Destination for standard output during script execution. */
	private Writer output;

	/** Destination for standard error during script execution. */
	private Writer error;

	private Object returnValue;

	public ScriptModule(final ScriptInfo info) {
		this.info = info;
	}

	// -- ScriptModule methods --

	/** Sets the writer used to record the standard output stream. */
	public void setOutputWriter(final Writer output) {
		this.output = output;
	}

	/** Sets the writer used to record the standard error stream. */
	public void setErrorWriter(final Writer error) {
		this.error = error;
	}

	/** Gets the script engine used to execute the script. */
	public ScriptEngine getEngine() {
		if (scriptEngine == null) {
			scriptEngine = getInfo().getLanguage().getScriptEngine();
		}
		return scriptEngine;
	}

	/** Gets the return value of the script. */
	public Object getReturnValue() {
		return returnValue;
	}

	// -- Module methods --

	@Override
	public ScriptInfo getInfo() {
		return info;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		// HACK: Work around code (Groovy!) assuming
		// context class loader can't be null.
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			Thread.currentThread().setContextClassLoader(Context.getClassLoader());
		}

		final ScriptEngine engine = getEngine();
		final String path = getInfo().getPath();

		// initialize the script engine
		engine.put(ScriptEngine.FILENAME, path);
		engine.put(ScriptModule.class.getName(), this);
		final ScriptContext scriptContext = engine.getContext();
		if (output != null) scriptContext.setWriter(output);
		final PrintWriter errorPrinter;
		if (error != null) {
			scriptContext.setErrorWriter(error);
			errorPrinter = new PrintWriter(error);
		}
		else {
			errorPrinter = null;
		}

		// populate bindings with the input values
		for (final ModuleItem<?> item : getInfo().inputs()) {
			final String name = item.getName();
			engine.put(name, getInput(name));
		}

		returnValue = null;
		try {
			// invoke the callbacks
			for (final ScriptCallback c : getInfo().callbacks()) {
				c.invoke(this);
			}

			// execute script!
			returnValue = engine.eval(getInfo().getProcessedScript());
		}
		catch (Throwable e) {
			while (e instanceof ScriptException && e.getCause() != null) {
				e = e.getCause();
			}
			if (error == null) log.error(e);
			else e.printStackTrace(errorPrinter);
		}

		// populate output values
		final ScriptLanguage language = getInfo().getLanguage();
		for (final ModuleItem<?> item : getInfo().outputs()) {
			final String name = item.getName();
			final Object value;
			if (RETURN_VALUE.equals(name) && getInfo().isReturnValueAppended()) {
				// NB: This is the special implicit return value output!
				value = returnValue;
			}
			else value = engine.get(name);
			final Object decoded = language.decode(value);
			final Object typed = conversionService.convert(decoded, item.getType());
			setOutput(name, typed);
		}

		// flush output and error streams
		if (output != null) {
			try {
				output.flush();
			}
			catch (final IOException e) {
				if (error == null) log.error(e);
				else e.printStackTrace(errorPrinter);
			}
		}
		if (errorPrinter != null) errorPrinter.flush();
	}

	// -- Contextual methods --

	@Override
	public Context context() {
		if (context == null) throw new NullContextException();
		return context;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(final Context context) {
		context.inject(this);
	}

	// -- Deprecated methods --

	/** @deprecated Use {@link ScriptInfo#getLanguage()} instead. */
	@Deprecated
	public ScriptLanguage getLanguage() {
		return getInfo().getLanguage();
	}

	/** @deprecated Use {@link ScriptInfo#setLanguage(ScriptLanguage)} instead. */
	@Deprecated
	public void setLanguage(final ScriptLanguage scriptLanguage) {
		getInfo().setLanguage(scriptLanguage);
	}
}
