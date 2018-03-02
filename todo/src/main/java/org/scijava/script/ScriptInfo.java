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

package org.scijava.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.NullContextException;
import org.scijava.log.LogService;
import org.scijava.module.AbstractModuleInfo;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.script.process.ParameterScriptProcessor;
import org.scijava.script.process.ScriptCallback;
import org.scijava.script.process.ScriptProcessorService;
import org.scijava.util.DigestUtils;
import org.scijava.util.FileUtils;

/**
 * Metadata about a script.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class ScriptInfo extends AbstractModuleInfo implements Contextual {

	private static final int PARAM_CHAR_MAX = 640 * 1024; // should be enough ;-)

	private final URL url;
	private final String path;
	private final String script;

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private ScriptProcessorService scriptProcessorService;

	/** Final version of the script, after script processing. */
	private String processedScript;

	/** True iff the return value should be appended as an output. */
	private boolean appendReturnValue;

	/** Script language in which the script should be executed. */
	private ScriptLanguage scriptLanguage;

	/** Routines to be invoked prior to script execution. */
	private ArrayList<ScriptCallback> callbacks;

	/**
	 * Creates a script metadata object which describes the given script file.
	 * 
	 * @param context The SciJava application context to use when populating
	 *          service inputs.
	 * @param file The script file.
	 */
	public ScriptInfo(final Context context, final File file) {
		this(context, null, file.getPath(), null);
	}

	/**
	 * Creates a script metadata object which describes the given script file.
	 * 
	 * @param context The SciJava application context to use when populating
	 *          service inputs.
	 * @param path Path to the script file.
	 */
	public ScriptInfo(final Context context, final String path) {
		this(context, null, path, null);
	}

	/**
	 * Creates a script metadata object which describes a script at the given URL.
	 * 
	 * @param context The SciJava application context to use when populating
	 *          service inputs.
	 * @param url URL which references the script.
	 * @param path Pseudo-path to the script file. This file does not actually
	 *          need to exist, but rather provides a name for the script with file
	 *          extension.
	 */
	public ScriptInfo(final Context context, final URL url, final String path)
		throws IOException
	{
		this(context, url, path, new InputStreamReader(url.openStream()));
	}

	/**
	 * Creates a script metadata object which describes a script provided by the
	 * given {@link Reader}.
	 * 
	 * @param context The SciJava application context to use when populating
	 *          service inputs.
	 * @param path Pseudo-path to the script file. This file does not actually
	 *          need to exist, but rather provides a name for the script with file
	 *          extension.
	 * @param reader Reader which provides the script itself (i.e., its contents).
	 */
	public ScriptInfo(final Context context, final String path,
		final Reader reader)
	{
		this(context, null, path, reader);
	}

	private ScriptInfo(final Context context, final URL url, final String path,
		final Reader reader)
	{
		setContext(context);
		this.url = url(url, path);
		this.path = path(url, path);

		String contents = null;
		if (reader != null) {
			try {
				contents = getReaderContentsAsString(reader);
			}
			catch (final IOException exc) {
				log.error("Error reading script: " + path, exc);
			}
		}
		script = contents;
	}

	// -- ScriptInfo methods --

	/**
	 * Gets the URL of the script.
	 * <p>
	 * If the actual source of the script is a URL (provided via
	 * {@link #ScriptInfo(Context, URL, String)}), then this will return it.
	 * </p>
	 * <p>
	 * Alternately, if the path (from {@link #getPath()}) is a real file on disk
	 * (provided via {@link #ScriptInfo(Context, File)} or
	 * {@link #ScriptInfo(Context, String)}), then the URL returned here will be a
	 * {@code file://} one reference to it.
	 * </p>
	 * <p>
	 * Otherwise, this method will return null.
	 * </p>
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Gets the path to the script on disk.
	 * <p>
	 * If the path doesn't actually exist on disk, then this is a pseudo-path
	 * merely for the purpose of naming the script with a file extension, and the
	 * actual script content is delivered by the {@link BufferedReader} given by
	 * {@link #getReader()}.
	 * </p>
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gets a reader which delivers the script's content.
	 * <p>
	 * This might be null, in which case the content is stored in a file on disk
	 * given by {@link #getPath()}.
	 * </p>
	 */
	public BufferedReader getReader() {
		if (script == null) {
			return null;
		}
		return new BufferedReader(new StringReader(script), PARAM_CHAR_MAX);
	}

	/**
	 * Gets the script contents <em>after</em> script processing.
	 * 
	 * @return The processed script.
	 * @see ScriptProcessorService#process
	 */
	public String getProcessedScript() {
		return processedScript;
	}

	/** Gets the scripting language of the script. */
	public ScriptLanguage getLanguage() {
		if (scriptLanguage == null) {
			// infer the language from the script path's extension
			final String scriptPath = getPath();
			if (scriptPath != null) {
				// use language associated with the script path extension
				final String extension = FileUtils.getExtension(scriptPath);
				scriptLanguage = scriptService.getLanguageByExtension(extension);
			}
			else {
				// use the highest priority language
				final List<ScriptLanguage> langs = scriptService.getLanguages();
				if (langs != null && !langs.isEmpty()) scriptLanguage = langs.get(0);
			}
		}
		return scriptLanguage;
	}

	/** Overrides the script language to use when executing the script. */
	public void setLanguage(final ScriptLanguage scriptLanguage) {
		this.scriptLanguage = scriptLanguage;
	}

	/** Gets whether the return value is appended as an additional output. */
	public boolean isReturnValueAppended() {
		return appendReturnValue;
	}

	/** Gets whether the return value is appended as an additional output. */
	public void setReturnValueAppended(final boolean appendReturnValue) {
		this.appendReturnValue = appendReturnValue;
	}

	/**
	 * Gets the list of routines which should be invoked each time the script is
	 * about to execute.
	 * 
	 * @return Reference to the mutable list of {@link Runnable} objects which the
	 *         {@link ScriptModule} will run prior to executing the script itself.
	 */
	public List<ScriptCallback> callbacks() {
		if (callbacks == null) callbacks = new ArrayList<>();
		return callbacks;
	}

	// -- AbstractModuleInfo methods --

	/**
	 * Performs script processing. In particular, parses the script parameters.
	 * 
	 * @see ParameterScriptProcessor
	 * @see ScriptProcessorService#process
	 */
	// NB: Widened visibility from AbstractModuleInfo.
	@Override
	public void parseParameters() {
		clearParameters();
		try {
			processedScript = scriptProcessorService.process(this);
		}
		catch (final IOException exc) {
			// TODO: Consider a better error handling approach.
			throw new RuntimeException(exc);
		}
	}

	// NB: Widened visibility from AbstractModuleInfo.
	@Override
	public void clearParameters() {
		super.clearParameters();
	}

	// NB: Widened visibility from AbstractModuleInfo.
	@Override
	public void registerInput(final ModuleItem<?> input) {
		super.registerInput(input);
	}

	// NB: Widened visibility from AbstractModuleInfo.
	@Override
	public void registerOutput(final ModuleItem<?> output) {
		super.registerOutput(output);
	}

	// -- ModuleInfo methods --

	@Override
	public String getDelegateClassName() {
		return ScriptModule.class.getName();
	}

	@Override
	public Class<?> loadDelegateClass() {
		return ScriptModule.class;
	}

	@Override
	public ScriptModule createModule() throws ModuleException {
		return new ScriptModule(this);
	}

	@Override
	public boolean canRunHeadless() {
		return is("headless");
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

	// -- Identifiable methods --

	@Override
	public String getIdentifier() {
		final String name = getName();
		final String prefix = "script:";
		if (name != null) return prefix + name;
		if (path != null) return prefix + path;
		if (script != null) return prefix + "<" + DigestUtils.bestHex(script) + ">";
		return prefix + "<unknown>";
	}

	// -- Locatable methods --

	@Override
	public String getLocation() {
		return new File(path).toURI().normalize().toString();
	}

	// -- Versioned methods --

	@Override
	public String getVersion() {
		final File file = new File(path);
		if (!file.exists()) return null; // no version for non-existent script
		try {
			return DigestUtils.bestHex(FileUtils.readFile(file));
		}
		catch (final IOException exc) {
			log.error(exc);
		}
		final Date lastModified = FileUtils.getModifiedTime(file);
		final String datestamp =
			new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(lastModified);
		return datestamp;
	}

	// -- Helper methods --

	private URL url(final URL u, final String p) {
		if (u != null) return u;
		if (p == null) return null;
		try {
			return new File(p).toURI().toURL();
		}
		catch (final MalformedURLException exc) {
			log.debug("Cannot glean URL from path: " + p, exc);
			return null;
		}
	}

	private String path(final URL u, final String p) {
		if (p != null) return p;
		return u == null ? null : u.getPath();
	}

	/**
	 * Read entire contents of a Reader and return as String.
	 *
	 * @param reader {@link Reader} whose contents should be returned as String.
	 *          Expected to never be <code>null</code>.
	 * @return contents of reader as String.
	 * @throws IOException If an I/O error occurs
	 * @throws NullPointerException If reader is <code>null</code>
	 */
	private static String getReaderContentsAsString(final Reader reader)
		throws IOException, NullPointerException
	{
		final char[] buffer = new char[8192];
		final StringBuilder builder = new StringBuilder();

		int read;
		while ((read = reader.read(buffer)) != -1) {
			builder.append(buffer, 0, read);
		}

		return builder.toString();
	}

	// -- Deprecated methods --

	/** @deprecated Use {@link #isReturnValueAppended()} instead. */
	@Deprecated
	public boolean isReturnValueDeclared() {
		return !isReturnValueAppended();
	}

}
