
package org.scijava.stl;

import com.sun.javafx.geom.Vec3f;

/**
 * A helper class to store a facet read from a STL file
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public final class STLFacet {

	public final Vec3f normal;
	public final Vec3f vertex0;
	public final Vec3f vertex1;
	public final Vec3f vertex2;
	public final short attributeByteCount;

	public STLFacet(final Vec3f normal, final Vec3f vertex0, final Vec3f vertex1,
		final Vec3f vertex2, final short attributeByteCount)
	{
		this.normal = normal;
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.attributeByteCount = attributeByteCount;
	}
}
