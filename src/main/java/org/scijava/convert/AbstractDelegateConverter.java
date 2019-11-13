
package org.scijava.convert;

import org.scijava.plugin.Parameter;

/**
 * Abstract superclass for {@link Converter} plugins that delegate to other
 * converters to chain two conversion steps together.
 * 
 * @author Jan Eglinger
 * @param <I> the input type
 * @param <D> the delegate type
 * @param <O> the output type
 */
public abstract class AbstractDelegateConverter<I, D, O> extends
	AbstractConverter<I, O>
{

	@Parameter
	private ConvertService convertService;

	@Override
	public <T> T convert(Object src, Class<T> dest) {
		D delegate = convertService.convert(src, getDelegateType());
		return convertService.convert(delegate, dest);
	}

	protected abstract Class<D> getDelegateType();
}
