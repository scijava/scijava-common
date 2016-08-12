
package org.scijava.stl;

import com.sun.javafx.geom.Vec3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link STLFormat} implementation for standard binary STL files
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class BinarySTLFormat extends AbstractBinarySTLFormat {

	@Override
	public List<STLFacet> readFacets(final byte[] data) {
		final List<STLFacet> facets = new ArrayList<>();

		if (data.length < FACET_START) {
			return facets;
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data).order(
			ByteOrder.LITTLE_ENDIAN);
		final int facetCount = buffer.getInt(HEADER_BYTES);
		final int expectedSize = HEADER_BYTES + COUNT_BYTES + facetCount *
			FACET_BYTES;
		if (expectedSize != buffer.capacity()) {
			return facets;
		}

		buffer.position(FACET_START);
		for (int offset = FACET_START; offset < buffer.capacity(); offset +=
			FACET_BYTES)
		{
			STLFacet facet = readFacet(buffer);
			facets.add(facet);
		}

		return facets;
	}

	@Override
	public byte[] write(final List<STLFacet> facets) {
		final int facetCount = facets == null ? 0 : facets.size();
		final int bytes = HEADER_BYTES + COUNT_BYTES + facetCount * FACET_BYTES;
		final ByteBuffer buffer = ByteBuffer.allocate(bytes).order(
			ByteOrder.LITTLE_ENDIAN);

		buffer.put(HEADER.getBytes());
		buffer.putInt(facetCount);

		if (facets == null) {
			return buffer.array();
		}

		facets.forEach(f -> writeFacet(buffer, f));

		return buffer.array();
	}

	private static void writeFacet(final ByteBuffer buffer,
		final STLFacet facet)
	{
		writeVector(buffer, facet.normal);
		writeVector(buffer, facet.vertex0);
		writeVector(buffer, facet.vertex1);
		writeVector(buffer, facet.vertex2);
		buffer.putShort((short) 0); // Attribute byte count
	}

	private static void writeVector(final ByteBuffer buffer, final Vec3f vector) {
		buffer.putFloat(vector.x);
		buffer.putFloat(vector.y);
		buffer.putFloat(vector.z);
	}

	private static STLFacet readFacet(final ByteBuffer buffer) {
		final Vec3f normal = readVector(buffer);
		final Vec3f vertex0 = readVector(buffer);
		final Vec3f vertex1 = readVector(buffer);
		final Vec3f vertex2 = readVector(buffer);
		final short attributeByteCount = buffer.getShort();

		return new STLFacet(normal, vertex0, vertex1, vertex2, attributeByteCount);
	}

	private static Vec3f readVector(final ByteBuffer buffer) {
		final float x = buffer.getFloat();
		final float y = buffer.getFloat();
		final float z = buffer.getFloat();

		return new Vec3f(x, y, z);
	}
}
