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

// File path shortening code adapted from:
// from: http://www.rgagnon.com/javadetails/java-0661.html

package org.scijava.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Useful methods for working with file paths.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @author Grant Harris
 */
public final class FileUtils {

	public static final int DEFAULT_SHORTENER_THRESHOLD = 4;
	public static final String SHORTENER_BACKSLASH_REGEX = "\\\\";
	public static final String SHORTENER_SLASH_REGEX = "/";
	public static final String SHORTENER_BACKSLASH = "\\";
	public static final String SHORTENER_SLASH = "/";
	public static final String SHORTENER_ELLIPSE = "...";

	/** A regular expression to match filenames containing version information. */
	private static final Pattern VERSION_PATTERN = buildVersionPattern();

	private FileUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the absolute path to the given file, with the directory separator
	 * standardized to forward slash, like most platforms use.
	 * 
	 * @param file The file whose path will be obtained and standardized.
	 * @return The file's standardized absolute path.
	 */
	public static String getPath(final File file) {
		final String path = file.getAbsolutePath();
		final String slash = System.getProperty("file.separator");
		return getPath(path, slash);
	}

	/**
	 * Gets a standardized path based on the given one, with the directory
	 * separator standardized from the specific separator to forward slash, like
	 * most platforms use.
	 * 
	 * @param path The path to standardize.
	 * @param separator The directory separator to be standardized.
	 * @return The standardized path.
	 */
	public static String getPath(final String path, final String separator) {
		// NB: Standardize directory separator (i.e., avoid Windows nonsense!).
		return path.replaceAll(Pattern.quote(separator), "/");
	}

	/**
	 * Extracts the file extension from a file.
	 * 
	 * @param file the file object
	 * @return the file extension (excluding the dot), or the empty string when
	 *         the file name does not contain dots
	 */
	public static String getExtension(final File file) {
		final String name = file.getName();
		final int dot = name.lastIndexOf('.');
		if (dot < 0) return "";
		return name.substring(dot + 1);
	}

	/**
	 * Extracts the file extension from a file path.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @return the file extension (excluding the dot), or the empty string when
	 *         the file name does not contain dots
	 */
	public static String getExtension(final String path) {
		return getExtension(new File(path));
	}

	/** Gets the {@link Date} of the file's last modification. */
	public static Date getModifiedTime(final File file) {
		final long modifiedTime = file.lastModified();
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(modifiedTime);
		return c.getTime();
	}

	/**
	 * Reads the contents of the given file into a new byte array.
	 * 
	 * @see DigestUtils#string(byte[]) To convert a byte array to a string.
	 * @throws IOException If the file cannot be read.
	 */
	public static byte[] readFile(final File file) throws IOException {
		final long length = file.length();
		if (length > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("File too large");
		}
		final byte[] bytes = new byte[(int) length];
		try (final DataInputStream dis = new DataInputStream(new FileInputStream(
			file)))
		{
			dis.readFully(bytes);
		}
		return bytes;
	}

	/**
	 * Writes the given byte array to the specified file.
	 * 
	 * @see DigestUtils#bytes(String) To convert a string to a byte array.
	 * @throws IOException If the file cannot be written.
	 */
	public static void writeFile(final File file, final byte[] bytes)
		throws IOException
	{
		try (final FileOutputStream out = new FileOutputStream(file)) {
			out.write(bytes);
		}
	}

	public static String stripFilenameVersion(final String filename) {
		final Matcher matcher = VERSION_PATTERN.matcher(filename);
		if (!matcher.matches()) return filename;
		return matcher.group(1) + matcher.group(5);
	}

	/**
	 * Lists all versions of a given (possibly versioned) file name.
	 * 
	 * @param directory the directory to scan
	 * @param filename the file name to use
	 * @return the list of matches
	 */
	public static File[] getAllVersions(final File directory,
		final String filename)
	{
		final Matcher matcher = VERSION_PATTERN.matcher(filename);
		if (!matcher.matches()) {
			final File file = new File(directory, filename);
			return file.exists() ? new File[] { file } : null;
		}
		final String baseName = matcher.group(1);
		final String classifier = matcher.group(6);
		return directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(final File dir, final String name) {
				if (!name.startsWith(baseName)) return false;
				final Matcher matcher2 = VERSION_PATTERN.matcher(name);
				return matcher2.matches() && baseName.equals(matcher2.group(1)) &&
						equals(classifier, matcher2.group(6));
			}

			private boolean equals(final String a, final String b) {
				if (a == null) {
					return b == null;
				}
				return a.equals(b);
			}
		});
	}

	/**
	 * Converts the given {@link URL} to its corresponding {@link File}.
	 * <p>
	 * This method is similar to calling {@code new File(url.toURI())} except that
	 * it also handles "jar:file:" URLs, returning the path to the JAR file.
	 * </p>
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	public static File urlToFile(final URL url) {
		return url == null ? null : urlToFile(url.toString());
	}

	/**
	 * Converts the given URL string to its corresponding {@link File}.
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	public static File urlToFile(final String url) {
		String path = url;
		if (path.startsWith("jar:")) {
			// remove "jar:" prefix and "!/" suffix
			final int index = path.indexOf("!/");
			path = path.substring(4, index);
		}
		try {
			if (PlatformUtils.isWindows() && path.matches("file:[A-Za-z]:.*")) {
				path = "file:/" + path.substring(5);
			}
			return new File(new URL(path).toURI());
		}
		catch (final MalformedURLException e) {
			// NB: URL is not completely well-formed.
		}
		catch (final URISyntaxException e) {
			// NB: URL is not completely well-formed.
		}
		if (path.startsWith("file:")) {
			// pass through the URL as-is, minus "file:" prefix
			path = path.substring(5);
			return new File(path);
		}
		throw new IllegalArgumentException("Invalid URL: " + url);
	}

	/**
	 * Shortens the path to a maximum of 4 path elements.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @return shortened path
	 */
	public static String shortenPath(final String path) {
		return shortenPath(path, DEFAULT_SHORTENER_THRESHOLD);
	}

	/**
	 * Shortens the path based on the given maximum number of path elements. E.g.,
	 * "C:/1/2/test.txt" returns "C:/1/.../test.txt" if threshold is 1.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @param threshold the number of directories to keep unshortened
	 * @return shortened path
	 */
	public static String shortenPath(final String path, final int threshold) {
		String regex = SHORTENER_BACKSLASH_REGEX;
		String sep = SHORTENER_BACKSLASH;

		if (path.indexOf("/") > 0) {
			regex = SHORTENER_SLASH_REGEX;
			sep = SHORTENER_SLASH;
		}

		String pathtemp[] = path.split(regex);
		// remove empty elements
		int elem = 0;
		{
			final String newtemp[] = new String[pathtemp.length];
			int j = 0;
			for (int i = 0; i < pathtemp.length; i++) {
				if (!pathtemp[i].equals("")) {
					newtemp[j++] = pathtemp[i];
					elem++;
				}
			}
			pathtemp = newtemp;
		}

		if (elem > threshold) {
			final StringBuilder sb = new StringBuilder();
			int index = 0;

			// drive or protocol
			final int pos2dots = path.indexOf(":");
			if (pos2dots > 0) {
				// case c:\ c:/ etc.
				sb.append(path.substring(0, pos2dots + 2));
				index++;
				// case http:// ftp:// etc.
				if (path.indexOf(":/") > 0 && pathtemp[0].length() > 2) {
					sb.append(SHORTENER_SLASH);
				}
			}
			else {
				final boolean isUNC =
					path.substring(0, 2).equals(SHORTENER_BACKSLASH_REGEX);
				if (isUNC) {
					sb.append(SHORTENER_BACKSLASH).append(SHORTENER_BACKSLASH);
				}
			}

			for (; index <= threshold; index++) {
				sb.append(pathtemp[index]).append(sep);
			}

			if (index == (elem - 1)) {
				sb.append(pathtemp[elem - 1]);
			}
			else {
				sb.append(SHORTENER_ELLIPSE).append(sep).append(pathtemp[elem - 1]);
			}
			return sb.toString();
		}
		return path;
	}

	/**
	 * Compacts a path into a given number of characters. The result is similar to
	 * the Win32 API PathCompactPathExA.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @param limit the number of characters to which the path should be limited
	 * @return shortened path
	 */
	public static String limitPath(final String path, final int limit) {
		if (path.length() <= limit) return path;

		final char shortPathArray[] = new char[limit];
		final char pathArray[] = path.toCharArray();
		final char ellipseArray[] = SHORTENER_ELLIPSE.toCharArray();

		final int pathindex = pathArray.length - 1;
		final int shortpathindex = limit - 1;

		// fill the array from the end
		int i = 0;
		for (; i < limit; i++) {
			if (pathArray[pathindex - i] != '/' && pathArray[pathindex - i] != '\\') {
				shortPathArray[shortpathindex - i] = pathArray[pathindex - i];
			}
			else {
				break;
			}
		}
		// check how much space is left
		final int free = limit - i;

		if (free < SHORTENER_ELLIPSE.length()) {
			// fill the beginning with ellipse
			for (int j = 0; j < ellipseArray.length; j++) {
				shortPathArray[j] = ellipseArray[j];
			}
		}
		else {
			// fill the beginning with path and leave room for the ellipse
			int j = 0;
			for (; j + ellipseArray.length < free; j++) {
				shortPathArray[j] = pathArray[j];
			}
			// ... add the ellipse
			for (int k = 0; j + k < free; k++) {
				shortPathArray[j + k] = ellipseArray[k];
			}
		}
		return new String(shortPathArray);
	}

	/**
	 * Creates a temporary directory.
	 * <p>
	 * Since there is no atomic operation to do that, we create a temporary file,
	 * delete it and create a directory in its place. To avoid race conditions, we
	 * use the optimistic approach: if the directory cannot be created, we try to
	 * obtain a new temporary file rather than erroring out.
	 * </p>
	 * <p>
	 * It is the caller's responsibility to make sure that the directory is
	 * deleted; see {@link #deleteRecursively(File)}.
	 * </p>
	 * 
	 * @param prefix The prefix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @return An abstract pathname denoting a newly-created empty directory
	 * @throws IOException
	 */
	public static File createTemporaryDirectory(final String prefix)
		throws IOException
	{
		return createTemporaryDirectory(prefix, null, null);
	}

	/**
	 * Creates a temporary directory.
	 * <p>
	 * Since there is no atomic operation to do that, we create a temporary file,
	 * delete it and create a directory in its place. To avoid race conditions, we
	 * use the optimistic approach: if the directory cannot be created, we try to
	 * obtain a new temporary file rather than erroring out.
	 * </p>
	 * <p>
	 * It is the caller's responsibility to make sure that the directory is
	 * deleted; see {@link #deleteRecursively(File)}.
	 * </p>
	 * 
	 * @param prefix The prefix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @param suffix The suffix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @return An abstract pathname denoting a newly-created empty directory
	 * @throws IOException
	 */
	public static File createTemporaryDirectory(final String prefix,
		final String suffix) throws IOException
	{
		return createTemporaryDirectory(prefix, suffix, null);
	}

	/**
	 * Creates a temporary directory.
	 * <p>
	 * Since there is no atomic operation to do that, we create a temporary file,
	 * delete it and create a directory in its place. To avoid race conditions, we
	 * use the optimistic approach: if the directory cannot be created, we try to
	 * obtain a new temporary file rather than erroring out.
	 * </p>
	 * <p>
	 * It is the caller's responsibility to make sure that the directory is
	 * deleted; see {@link #deleteRecursively(File)}.
	 * </p>
	 * 
	 * @param prefix The prefix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @param suffix The suffix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @param directory The directory in which the file is to be created, or null
	 *          if the default temporary-file directory is to be used
	 * @return: An abstract pathname denoting a newly-created empty directory
	 * @throws IOException
	 */
	public static File createTemporaryDirectory(final String prefix,
		final String suffix, final File directory) throws IOException
	{
		for (int counter = 0; counter < 10; counter++) {
			final File file = File.createTempFile(prefix, suffix, directory);

			if (!file.delete()) {
				throw new IOException("Could not delete file " + file);
			}

			// in case of a race condition, just try again
			if (file.mkdir()) return file;
		}
		throw new IOException(
			"Could not create temporary directory (too many race conditions?)");
	}

	/**
	 * Deletes a directory recursively.
	 * 
	 * @param directory The directory to delete.
	 * @return whether it succeeded (see also {@link File#delete()})
	 */
	public static boolean deleteRecursively(final File directory) {
		if (directory == null) return true;
		final File[] list = directory.listFiles();
		if (list == null) return true;
		for (final File file : list) {
			if (file.isFile()) {
				if (!file.delete()) return false;
			}
			else if (file.isDirectory()) {
				if (!deleteRecursively(file)) return false;
			}
		}
		return directory.delete();
	}

	/**
	 * Recursively lists the contents of the referenced directory. Directories are
	 * excluded from the result. Supported protocols include {@code file} and
	 * {@code jar}.
	 * 
	 * @param directory The directory whose contents should be listed.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 * @see #listContents(URL, boolean, boolean)
	 */
	public static Collection<URL> listContents(final URL directory) {
		return listContents(directory, true, true);
	}

	/**
	 * Lists all contents of the referenced directory. Supported protocols include
	 * {@code file} and {@code jar}.
	 * 
	 * @param directory The directory whose contents should be listed.
	 * @param recurse Whether to list contents recursively, as opposed to only the
	 *          directory's direct contents.
	 * @param filesOnly Whether to exclude directories in the resulting collection
	 *          of contents.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 */
	public static Collection<URL> listContents(final URL directory,
		final boolean recurse, final boolean filesOnly)
	{
		return appendContents(new ArrayList<URL>(), directory, recurse, filesOnly);
	}

	/**
	 * Recursively adds contents from the referenced directory to an existing
	 * collection. Directories are excluded from the result. Supported protocols
	 * include {@code file} and {@code jar}.
	 * 
	 * @param result The collection to which contents should be added.
	 * @param directory The directory whose contents should be listed.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 * @see #appendContents(Collection, URL, boolean, boolean)
	 */
	public static Collection<URL> appendContents(final Collection<URL> result,
		final URL directory)
	{
		return appendContents(result, directory, true, true);
	}

	/**
	 * Add contents from the referenced directory to an existing collection.
	 * Supported protocols include {@code file} and {@code jar}.
	 * 
	 * @param result The collection to which contents should be added.
	 * @param directory The directory whose contents should be listed.
	 * @param recurse Whether to append contents recursively, as opposed to only
	 *          the directory's direct contents.
	 * @param filesOnly Whether to exclude directories in the resulting collection
	 *          of contents.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 */
	public static Collection<URL> appendContents(final Collection<URL> result,
		final URL directory, final boolean recurse, final boolean filesOnly)
	{
		if (directory == null) return result; // nothing to append
		final String protocol = directory.getProtocol();
		if (protocol.equals("file")) {
			final File dir = urlToFile(directory);
			final File[] list = dir.listFiles();
			if (list != null) {
				for (final File file : list) {
					try {
						if (!filesOnly || file.isFile()) {
							result.add(file.toURI().toURL());
						}
						if (recurse && file.isDirectory()) {
							appendContents(result, file.toURI().toURL(), recurse, filesOnly);
						}
					}
					catch (final MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else if (protocol.equals("jar")) {
			try {
				final String url = directory.toString();
				final int bang = url.indexOf("!/");
				if (bang < 0) return result;
				final String prefix = url.substring(bang + 2);
				final String baseURL = url.substring(0, bang + 2);

				final JarURLConnection connection =
					(JarURLConnection) new URL(baseURL).openConnection();
				try (final JarFile jar = connection.getJarFile()) {
					for (final JarEntry entry : new IteratorPlus<>(jar.entries())) {
						final String urlEncoded =
							new URI(null, null, entry.getName(), null).toString();
						if (urlEncoded.length() > prefix.length() && // omit directory itself
							urlEncoded.startsWith(prefix))
						{
							if (filesOnly && urlEncoded.endsWith("/")) {
								// URL is directory; exclude it
								continue;
							}
							if (!recurse) {
								// check whether this URL is a *direct* child of the directory
								final int slash = urlEncoded.indexOf("/", prefix.length());
								if (slash >= 0 && slash != urlEncoded.length() - 1) {
									// not a direct child
									continue;
								}
							}
							result.add(new URL(baseURL + urlEncoded));
						}
					}
				}
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
			catch (final URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return result;
	}

	/**
	 * Finds {@link URL}s of available resources. Both JAR files and files on disk
	 * are searched, according to the following mechanism:
	 * <ol>
	 * <li>Resources at the given {@code pathPrefix} are discovered using
	 * {@link ClassLoader#getResources(String)} with the current thread's context
	 * class loader. In particular, this invocation discovers resources in JAR
	 * files beneath the given {@code pathPrefix}.</li>
	 * <li>The directory named {@code pathPrefix} beneath the given
	 * {@code baseDirectory} is scanned last, so that users can more easily
	 * override resources provided inside JAR files by placing a resource of the
	 * same name within that directory.</li>
	 * </ol>
	 * <p>
	 * In both cases, resources are then recursively scanned using
	 * {@link #listContents(URL)}, and anything matching the given {@code regex}
	 * pattern is added to the output map.
	 * </p>
	 *
	 * @param regex The regex to use when matching resources, or null to match
	 *          everything.
	 * @param pathPrefix The path to search for resources.
	 * @param baseDirectory The {@code baseDirectory/pathPrefix} directory to scan
	 *          <em>after</em> the URL resources.
	 * @return A map of URLs referencing the matched resources.
	 * @see AppUtils#getBaseDirectory
	 */
	public static Map<String, URL> findResources(final String regex,
		final String pathPrefix, final File baseDirectory)
	{
		// scan URL resource paths first
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		final ArrayList<URL> urls = new ArrayList<>();
		try {
			urls.addAll(Collections.list(loader.getResources(pathPrefix + "/")));
		}
		catch (final IOException exc) {
			// error loading resources; proceed with an empty list
		}

		// scan directory second; user can thus override resources from JARs
		if (baseDirectory != null) {
			try {
				urls.add(new File(baseDirectory, pathPrefix).toURI().toURL());
			}
			catch (final MalformedURLException exc) {
				// error adding directory; proceed without it
			}
		}

		return findResources(regex, urls);
	}

	/**
	 * Finds {@link URL}s of resources known to the system.
	 * <p>
	 * Each of the given {@link URL}s is recursively scanned using
	 * {@link #listContents(URL)}, and anything matching the given {@code regex}
	 * pattern is added to the output map.
	 * </p>
	 *
	 * @param regex The regex to use when matching resources, or null to match
	 *          everything.
	 * @param urls Paths to search for resources.
	 * @return A map of URLs referencing the matched resources.
	 */
	public static Map<String, URL> findResources(final String regex,
		final Iterable<URL> urls)
	{
		final HashMap<String, URL> result = new HashMap<>();
		final Pattern pattern = regex == null ? null : Pattern.compile(regex);
		for (final URL url : urls) {
			getResources(pattern, result, url);
		}
		return result;
	}

	// -- Helper methods --

	/** Builds the {@link #VERSION_PATTERN} constant. */
	private static Pattern buildVersionPattern() {
		final String version =
			"\\d+(\\.\\d+|\\d{7})+[a-z]?\\d?(-[A-Za-z0-9.]+?|\\.GA)*?";
		final String suffix = "\\.jar(-[a-z]*)?";
		return Pattern.compile("(.+?)(-" + version + ")?((-(" + classifiers() +
			"))?(" + suffix + "))");
	}

	/** Helper method of {@link #buildVersionPattern()}. */
	private static String classifiers() {
		final String[] classifiers = {
			"swing",
			"swt",
			"shaded",
			"sources",
			"javadoc",
			"native",
			"(natives-)?(android|linux|macosx|solaris|windows)-" +
				"(aarch64|amd64|arm|armv6|armv6hf|i586|universal|x86|x86_64)",
		};
		final StringBuilder sb = new StringBuilder("(");
		for (final String classifier : classifiers) {
			if (sb.length() > 1) sb.append("|");
			sb.append(classifier);
		}
		sb.append(")");
		return sb.toString();
	}

	/** Helper method of {@link #findResources(String, Iterable)}. */
	private static void getResources(final Pattern pattern,
		final Map<String, URL> result, final URL base)
	{
		final String prefix = urlPath(base);
		if (prefix == null) return; // unsupported base URL

		for (final URL url : FileUtils.listContents(base)) {
			final String s = urlPath(url);
			if (s == null || !s.startsWith(prefix)) continue;

			if (pattern == null || pattern.matcher(s).matches()) {
				// this resource matches the pattern
				final String key = urlPath(s.substring(prefix.length()));
				if (key != null) result.put(key, url);
			}
		}
	}

	/** Helper method of {@link #getResources(Pattern, Map, URL)}. */
	private static String urlPath(final URL url) {
		try {
			return url.toURI().toString();
		}
		catch (final URISyntaxException exc) {
			return null;
		}
	}

	/** Helper method of {@link #getResources(Pattern, Map, URL)}. */
	private static String urlPath(final String path) {
		try {
			return new URI(path).getPath();
		}
		catch (final URISyntaxException exc) {
			return null;
		}
	}

	// -- Deprecated methods --

	/**
	 * Returns the {@link Matcher} object dissecting a versioned file name.
	 * 
	 * @param filename the file name
	 * @return the {@link Matcher} object
	 * @deprecated see {@link #stripFilenameVersion(String)}
	 */
	@Deprecated
	public static Matcher matchVersionedFilename(final String filename) {
		return VERSION_PATTERN.matcher(filename);
	}

}
