package de.orchound.rendering.iqm


/**
 * This interface represents a namespace.
 */
@ExperimentalUnsignedTypes
interface IqmTypes {
	enum class AttributeType {
		POSITION,
		TEXCOORD,
		NORMAL,
		TANGENT,
		BLENDINDEXES,
		BLENDWEIGHTS,
		COLOR,
		CUSTOM;

		companion object {
			fun fromParsedValue(value: UInt) = when(value) {
				0u -> POSITION
				1u -> TEXCOORD
				2u -> NORMAL
				3u -> TANGENT
				4u -> BLENDINDEXES
				5u -> BLENDWEIGHTS
				6u -> COLOR
				0x10u -> CUSTOM
				else -> { throw Exception("Unknown IQM attribute value.") }
			}
		}
	}

	enum class Format(val size: Int) {
		BYTE(1),
		UBYTE(1),
		SHORT(2),
		USHORT(2),
		INT(4),
		UINT(4),
		HALF(2),
		FLOAT(4),
		DOUBLE(8);

		companion object {
			fun fromParsedValue(value: UInt) = when(value) {
				0u -> BYTE
				1u -> UBYTE
				2u -> SHORT
				3u -> USHORT
				4u -> INT
				5u -> UINT
				6u -> HALF
				7u -> FLOAT
				8u -> DOUBLE
				else -> { throw Exception("Unknown IQM format value.") }
			}
		}
	}

	class Translation(
		val x: Float, val y: Float, val z: Float
	)

	class Rotation(
		val x: Float, val y: Float, val z: Float, val w: Float
	)

	class Scale(
		val x: Float, val y: Float, val z: Float
	)

	class Triangle(
		val vertex1: UInt,
		val vertex2: UInt,
		val vertex3: UInt
	)

	class Adjacency(
		val triangle1: Triangle?,
		val triangle2: Triangle?,
		val triangle3: Triangle?
	)

	class VertexAttributeBuffer(
		val type: AttributeType,
		val flags: UInt,
		val data: ByteArray,
		val format: Format,
		val componentSize: Int
	)

	class Mesh(
		val name: String,
		val material: String,
		val verticesCount: UInt,
		val vertexAttributes: Collection<VertexAttributeBuffer>,
		val triangles: Collection<Triangle>
	)

	class Joint(
		val name: String,
		val parentId: Int?,
		val translation: Translation,
		val rotation: Rotation,
		val scale: Scale
	)

	class Pose(
		val parentId: Int?,
		val mask: UInt,
		val translationOffset: Translation,
		val rotationOffset: Rotation,
		val scaleOffset: Scale,
		val translationScale: Translation,
		val rotationScale: Rotation,
		val scaleScale: Scale
	)

	class Animation(
		val name: String,
		val flags: UInt,
		val frameRate: Float,
		val framesCount: UInt,
		val frameValues: Collection<UShort>
	)

	class Bound(
		bbMin: FloatArray,
		bbMax: FloatArray,
		radius: Float,
		xyRadius: Float
	)

	class Extension(
		val name: String,
		val data: ByteArray
	)
}
