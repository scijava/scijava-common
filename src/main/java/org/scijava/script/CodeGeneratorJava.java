/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2025 SciJava developers.
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

/**
 * {@link CodeGenerator} for Java.
 * 
 * @author Grant Harris
 * @deprecated To be removed in SciJava Common 3.0.0.
 */
@Deprecated
public class CodeGeneratorJava implements CodeGenerator {

	static final String lsep = System.getProperty("line.separator");
	private final StringBuilder sb = new StringBuilder();

	@Override
	public String getResult() {
		return sb.toString();
	}

	@Override
	public void invokeStatementBegin() {
		sb.append("invoke(");
	}

	@Override
	public void addModuleCalled(final String moduleCalled) {
		sb.append("\"");
		sb.append(moduleCalled);
		sb.append("\"");
	}

	@Override
	public void addArgDelimiter() {
		sb.append(", ");
	}

	@Override
	public void addArgument(final ParameterObject parameterObject) {
		final StringBuilder sb1 = new StringBuilder();
		// Class<?> type = parameterObject.type;
		// String name = parameterObject.param;
		final Object value = parameterObject.value;
		if (value instanceof String) {
			sb1.append("\"");
			sb1.append(parameterObject.value.toString());
			sb1.append("\"");
		}
		else if (value instanceof Boolean) {
			if ((Boolean) value) sb1.append("true");
			else sb1.append("false");
		}
		else sb1.append(parameterObject.value.toString());
		sb.append(sb1.toString());
	}

	@Override
	public void statementTerminate() {
		sb.append(lsep);
	}

	@Override
	public void invokeStatementEnd() {
		sb.append(")");
	}

}
