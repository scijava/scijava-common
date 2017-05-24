
package org.scijava.stl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.scijava.stl.AbstractBinarySTLFormat.FACET_BYTES;
import static org.scijava.stl.BinarySTLFormat.COUNT_BYTES;
import static org.scijava.stl.BinarySTLFormat.HEADER;
import static org.scijava.stl.BinarySTLFormat.HEADER_BYTES;

import com.google.common.base.Strings;
import com.sun.javafx.geom.Vec3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests for {@link BinarySTLFormat}
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class BinarySTLFormatTest {

	private static final BinarySTLFormat format = new BinarySTLFormat();

	@Test
	public void testWriteNull() throws Exception {
		final byte[] bytes = format.write(null);

		assertNotNull(bytes);
		assertEquals(HEADER_BYTES + COUNT_BYTES, bytes.length);
	}

	@Test
	public void testWrite() throws Exception {
		final STLFacet facet = new STLFacet(new Vec3f(0, 0, 1), new Vec3f(1, 0, 0),
			new Vec3f(0, 1, 0), new Vec3f(0, 0, 0), (short) 0);
		final STLFacet facet2 = new STLFacet(new Vec3f(-1, 0, 0), new Vec3f(0, 0,
			1), new Vec3f(0, 1, 0), new Vec3f(0, 0, 0), (short) 0);
		List<STLFacet> facets = Arrays.asList(facet, facet2);
		final int expectedSize = HEADER_BYTES + COUNT_BYTES + facets.size() *
			FACET_BYTES;

		final byte[] data = format.write(facets);
		final ByteBuffer buffer = ByteBuffer.wrap(data).order(
			ByteOrder.LITTLE_ENDIAN);

		assertEquals("Size of STL data is incorrect", expectedSize, buffer
			.capacity());

		byte[] headerBytes = new byte[HEADER_BYTES];
		buffer.get(headerBytes, 0, HEADER_BYTES);
		final String header = new String(headerBytes);
		assertEquals("Header of STL data is incorrect", header, HEADER);

		final int facetCount = buffer.getInt();
		assertEquals("Wrong number of facets written", facets.size(), facetCount);

		facets.forEach(f -> assertFacet(f, buffer, 1e-12));
	}

	@Test
	public void testReadNull() throws Exception {
		final List<STLFacet> facets = format.read(null);

		assertNotNull(facets);
		assertEquals(0, facets.size());
	}

	@Test
	public void testReadBadSize() throws Exception {
		final byte[] data = new byte[61];
		final List<STLFacet> facets = format.readFacets(data);

		assertNotNull(facets);
		assertEquals(0, facets.size());
	}

	@Test
	public void testReadBadFacetCount() throws Exception {
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + COUNT_BYTES)
			.order(ByteOrder.LITTLE_ENDIAN);
		final byte[] header = Strings.padEnd("Header", 80, '.').getBytes();
		final int facetCount = 2;
		buffer.put(header);
		buffer.putInt(facetCount);

		final List<STLFacet> facets = format.readFacets(buffer.array());

		assertNotNull(facets);
		assertEquals(0, facets.size());
	}

	@Test
	public void testReadFacets() throws Exception {
		final int facetCount = 2;
		final short attributeByteCount = 0;
		final List<float[]> facet = Arrays.asList(new float[] { -2.0f, -1.0f,
			0.0f }, new float[] { 1.0f, 2.0f, 3.0f }, new float[] { 4.0f, 5.0f,
				6.0f }, new float[] { 7.0f, 8.0f, 9.0f });
		final List<float[]> facet1 = Arrays.asList(new float[] { 1.0f, 0.0f, 0.0f },
			new float[] { 1.0f, 0.0f, 0.0f }, new float[] { 1.0f, 0.0f, 0.0f },
			new float[] { 1.0f, 0.0f, 0.0f });
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + COUNT_BYTES +
			facetCount * FACET_BYTES).order(ByteOrder.LITTLE_ENDIAN);
		final byte[] header = Strings.padEnd("Header", 80, '.').getBytes();

		buffer.put(header);
		buffer.putInt(facetCount);
		writeFacet(buffer, facet, attributeByteCount);
		writeFacet(buffer, facet1, attributeByteCount);

		final List<STLFacet> facets = format.readFacets(buffer.array());

		assertFacet(facets.get(0), facet);
	}

	private static void assertFacet(final STLFacet expectedFacet,
		final List<float[]> actualFacet)
	{
		assertVector(expectedFacet.normal, actualFacet.get(0));
		assertVector(expectedFacet.vertex0, actualFacet.get(1));
		assertVector(expectedFacet.vertex1, actualFacet.get(2));
		assertVector(expectedFacet.vertex2, actualFacet.get(3));
	}

	private static void assertVector(final Vec3f expectedVector,
		final float[] actualVector)
	{
		assertEquals(expectedVector.x, actualVector[0], 1e-12);
		assertEquals(expectedVector.y, actualVector[1], 1e-12);
		assertEquals(expectedVector.z, actualVector[2], 1e-12);
	}

	private static void writeFacet(final ByteBuffer buffer, List<float[]> vectors,
		final short attributeByteCount)
	{
		vectors.forEach(v -> writeVector(buffer, v[0], v[1], v[2]));
		buffer.putShort(attributeByteCount);
	}

	private static void writeVector(final ByteBuffer buffer, final float x,
		final float y, final float z)
	{
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(x);
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(y);
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(z);
	}

	private static void assertFacet(STLFacet expected, ByteBuffer buffer,
		double delta)
	{
		assertVector(expected.normal, buffer, delta);
		assertVector(expected.vertex0, buffer, delta);
		assertVector(expected.vertex1, buffer, delta);
		assertVector(expected.vertex2, buffer, delta);

		final short attributeByteCount = buffer.getShort();
		assertEquals(expected.attributeByteCount, attributeByteCount);
	}

	private static void assertVector(final Vec3f expected,
		final ByteBuffer buffer, final double delta)
	{
		assertEquals(expected.x, buffer.getFloat(), delta);
		assertEquals(expected.y, buffer.getFloat(), delta);
		assertEquals(expected.z, buffer.getFloat(), delta);
	}
}
