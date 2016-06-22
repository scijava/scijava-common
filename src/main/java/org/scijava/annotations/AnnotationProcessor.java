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

package org.scijava.annotations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.scijava.annotations.AbstractIndexWriter.StreamFactory;

/**
 * The annotation processor for use with Java 6 and above.
 * 
 * @author Johannes Schindelin
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class AnnotationProcessor extends AbstractProcessor {

	private RoundEnvironment roundEnv;

	@Override
	public boolean process(final Set<? extends TypeElement> elements,
		final RoundEnvironment env)
	{
		roundEnv = env;

		final Writer writer = new Writer();
		for (final TypeElement element : elements) {
			writer.add(element);
		}
		try {
			writer.write(writer);
		}
		catch (final IOException e) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(out));
			try {
				out.close();
				processingEnv.getMessager().printMessage(Kind.ERROR, out.toString());
			}
			catch (final IOException e2) {
				processingEnv.getMessager().printMessage(Kind.ERROR,
					e2.getMessage() + " while printing " + e.getMessage());
			}
		}
		return false;
	}

	private class Writer extends AbstractIndexWriter implements StreamFactory {

		private final Map<String, List<Element>> originatingElements =
			new HashMap<>();
		private final Filer filer = processingEnv.getFiler();
		private final Elements utils = processingEnv.getElementUtils();
		private final Types typeUtils = processingEnv.getTypeUtils();

		public void add(final TypeElement element) {
			final AnnotationMirror mirror = getMirror(element);
			if (mirror != null) {
				final String annotationName = utils.getBinaryName(element).toString();

				// remember originating elements
				List<Element> originating = originatingElements.get(annotationName);
				if (originating == null) {
					originating = new ArrayList<>();
					originatingElements.put(annotationName, originating);
				}

				for (final Element annotated : roundEnv
					.getElementsAnnotatedWith(element))
				{
					switch (annotated.getKind()) {
						case ANNOTATION_TYPE:
						case CLASS:
						case ENUM:
						case INTERFACE:
							final String className =
								utils.getBinaryName((TypeElement) annotated).toString();
							final Map<String, Object> values =
								adapt(annotated.getAnnotationMirrors(), element.asType());
							super.add(values, annotationName, className);
							originating.add(annotated);
							break;
						default:
							processingEnv.getMessager().printMessage(
								Kind.ERROR,
								"Cannot handle annotated element of kind " +
									annotated.getKind());
					}
				}
			}
			//
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> adapt(
			final List<? extends AnnotationMirror> mirrors,
			final TypeMirror annotationType)
		{
			final Map<String, Object> result = new TreeMap<>();
			for (final AnnotationMirror mirror : mirrors) {
				if (typeUtils.isSameType(mirror.getAnnotationType(), annotationType)) {
					return (Map<String, Object>) adapt(mirror);
				}
			}
			return result;
		}

		@Override
		protected Object adapt(final Object o) {
			if (o instanceof AnnotationMirror) {
				final AnnotationMirror mirror = (AnnotationMirror) o;
				final Map<String, Object> result = new TreeMap<>();
				for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror
					.getElementValues().entrySet())
				{
					final String key = entry.getKey().getSimpleName().toString();
					final Object value = adapt(entry.getValue().getValue());
					result.put(key, value);
				}
				return result;
			}
			else if (o instanceof List) {
				final List<?> list = (List<?>) o;
				final List<Object> result = new ArrayList<>(list.size());
				for (final Object item : list) {
					result.add(adapt(item));
				}
				return result;
			}
			else if (o instanceof TypeMirror) {
				final TypeMirror mirror = (TypeMirror) o;
				return utils.getBinaryName((TypeElement) typeUtils.asElement(mirror))
					.toString();
			}
			else if (o instanceof VariableElement) {
				final VariableElement element = (VariableElement) o;
				final Map<String, Object> result = new TreeMap<>();
				final String enumName =
					utils.getBinaryName((TypeElement) element.getEnclosingElement())
						.toString();
				final String valueName = element.getSimpleName().toString();
				result.put("enum", enumName);
				result.put("value", valueName);
				return result;
			}
			else {
				return super.adapt(o);
			}
		}

		private AnnotationMirror getMirror(final TypeElement element) {
			for (final AnnotationMirror candidate : utils
				.getAllAnnotationMirrors(element))
			{
				final Name binaryName =
					utils.getBinaryName((TypeElement) candidate.getAnnotationType()
						.asElement());
				if (binaryName.contentEquals(Indexable.class.getName())) {
					return candidate;
				}
			}
			return null;
		}

		@Override
		public InputStream openInput(final String annotationName)
			throws IOException
		{
			try {
				return filer.getResource(StandardLocation.CLASS_OUTPUT, "",
					Index.INDEX_PREFIX + annotationName).openInputStream();
			}
			catch (final FileNotFoundException e) {
				return null;
			}
		}

		@Override
		public OutputStream openOutput(final String annotationName)
			throws IOException
		{
			final List<Element> originating = originatingElements.get(annotationName);
			final String path = Index.INDEX_PREFIX + annotationName;
			final FileObject fileObject =
				filer.createResource(StandardLocation.CLASS_OUTPUT, "", path,
					originating.toArray(new Element[originating.size()]));

			// Verify that the generated file is in the META-INF/json/ subdirectory;
			// Despite our asking for it explicitly, the DefaultFileManager will
			// strip out the directory if javac was called without an explicit
			// output directory (i.e. without <code>-d</code> option).
			final String uri = fileObject.toUri().toString();
			if (uri != null && uri.endsWith("/" + path)) {
				return fileObject.openOutputStream();
			}
			final String prefix =
				uri == null ? "" : uri.substring(0, uri.length() -
					annotationName.length());
			final File file = new File(prefix + path);
			final File parent = file.getParentFile();
			if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
				throw new IOException("Could not create directory: " + parent);
			}
			return new FileOutputStream(file);
		}

		@Override
		public boolean isClassObsolete(final String className) {
			return false;
		}

	}
}
