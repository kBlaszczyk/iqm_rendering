package de.orchound.rendering.iqm

import de.orchound.rendering.limit
import de.orchound.rendering.position
import de.orchound.rendering.uint
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets


@ExperimentalUnsignedTypes
class IqmParser(file: File) {

	enum class IqmAttributeTypes() {
		POSITION,
		TEXCOORD,
		NORMAL,
		TANGENT,
		BLENDINDEXES,
		BLENDWEIGHTS,
		COLOR,
		CUSTOM;

		companion object {
			fun fromIndex(index: UInt) = when (index) {
				0u -> POSITION
				1u -> TEXCOORD
				2u -> NORMAL
				3u -> TANGENT
				4u -> BLENDINDEXES
				5u -> BLENDWEIGHTS
				6u -> COLOR
				0x10u -> CUSTOM
				else -> throw RuntimeException("Unknown IQM vertex attribute type.")
			}
		}
	}

	enum class IqmFormats(val sizeInBytes: Int) {
		BYTE(1),
		UBYTE(1),
		SHORT(2),
		USHORT(2),
		INT (4),
		UINT(4),
		HALF(2),
		FLOAT(4),
		DOUBLE(8);

		companion object {
			fun fromIndex(index: UInt) = when (index) {
				0u -> BYTE
				1u -> UBYTE
				2u -> SHORT
				3u -> USHORT
				4u -> INT
				5u -> UINT
				6u -> HALF
				7u -> FLOAT
				8u -> DOUBLE
				else -> throw RuntimeException("Unknown IQM data type.")
			}
		}
	}

	class Header(data: ByteBuffer) {
		val magic: String
		val version: UInt
		val filesize: UInt
		val flags: UInt
		val numText: UInt; val ofsText: UInt
		val numMeshes: UInt; val ofsMeshes: UInt
		val numVertexArrays: UInt; val numVertices: UInt; val ofsVertexArrays: UInt
		val numTriangles: UInt; val ofsTriangles: UInt; val ofsAdjacency: UInt
		val numJoints: UInt; val ofsJoints: UInt
		val numPoses: UInt; val ofsPoses: UInt
		val numAnims: UInt; val ofsAnims: UInt
		val numFrames: UInt; val numFrameChannels: UInt; val ofsFrames: UInt; val ofsBounds: UInt
		val numComments: UInt; val ofsComments: UInt
		val numExtensions: UInt; val ofsExtensions: UInt

		init {
			val magicBytes = ByteArray(16).apply { data.get(this) }
			magic = String(magicBytes, StandardCharsets.US_ASCII)
			check(magic == "INTERQUAKEMODEL\u0000")

			version = data.uint
			filesize = data.uint
			flags = data.uint
			numText = data.uint
			ofsText = data.uint
			numMeshes = data.uint
			ofsMeshes = data.uint
			numVertexArrays = data.uint
			numVertices = data.uint
			ofsVertexArrays = data.uint
			numTriangles = data.uint
			ofsTriangles = data.uint
			ofsAdjacency = data.uint
			numJoints = data.uint
			ofsJoints = data.uint
			numPoses = data.uint
			ofsPoses = data.uint
			numAnims = data.uint
			ofsAnims = data.uint
			numFrames = data.uint
			numFrameChannels = data.uint
			ofsFrames = data.uint
			ofsBounds = data.uint
			numComments = data.uint
			ofsComments = data.uint
			numExtensions = data.uint
			ofsExtensions = data.uint
		}

		companion object {
			val sizeInBytes = 124
		}
	}

	class Mesh(data: ByteBuffer) {
		val name = data.uint
		val material = data.uint
		val firstVertex = data.uint
		val numVertices = data.uint
		val firstTriangle = data.uint
		val numTriangles = data.uint

		companion object {
			val sizeInBytes = 6u * 4u
		}
	}

	class Triangle(data: ByteBuffer) {
		val vertex1 = data.uint
		val vertex2 = data.uint
		val vertex3 = data.uint

		companion object {
			val sizeInBytes = 3u * 4u
		}
	}

	class Adjacency(val data: ByteBuffer) {
		val triangle1 = data.uint
		val triangle2 = data.uint
		val triangle3 = data.uint

		companion object {
			val sizeInBytes = 3u * 4u
		}
	}

	class Joint(data: ByteBuffer) {
		val name: UInt
		val parent: Int
		val translation: FloatArray
		val rotation: FloatArray
		val scale: FloatArray

		init {
			name = data.uint
			parent = data.int
			translation = floatArrayOf(data.float, data.float, data.float)
			rotation = floatArrayOf(data.float, data.float, data.float, data.float)
			scale = floatArrayOf(data.float, data.float, data.float)
		}

		companion object {
			val sizeInBytes = 12u * 4u
		}
	}

	class Pose(data: ByteBuffer) {
		val parent: Int
		val mask: UInt
		val channelOffset: FloatArray
		val channelScale: FloatArray

		init {
			parent = data.int
			mask = data.uint

			channelOffset = parsePrimitives(data, 10) { this.float }.toFloatArray()
			channelScale = parsePrimitives(data, 10) { this.float }.toFloatArray()
		}

		companion object {
			val sizeInBytes = 22u * 4u
		}
	}

	class Anim(data: ByteBuffer) {
		val name: UInt
		val firstFrame: UInt
		val numFrames: UInt
		val frameRate: Float
		val flags: UInt

		init {
			name = data.uint
			firstFrame = data.uint
			numFrames = data.uint
			frameRate = data.float
			flags = data.uint
		}

		companion object {
			val sizeInBytes = 5u * 4u
		}
	}

	class VertexArray(data: ByteBuffer) {
		val type = data.uint
		val flags = data.uint
		val format = data.uint
		val size = data.uint
		val offset = data.uint

		companion object {
			val sizeInBytes = 5u * 4u
		}
	}

	class Bounds(data: ByteBuffer) {
		val bbMin: FloatArray = floatArrayOf(data.float, data.float, data.float)
		val bbMax: FloatArray = floatArrayOf(data.float, data.float, data.float)
		val xyRadius: Float = data.float
		val radius: Float = data.float

		companion object {
			val sizeInBytes = 8u * 4u
		}
	}

	val header: Header
	val texts: Collection<String>
	val comments: Collection<String>
	val meshes: Collection<Mesh>
	val vertexArrays: Collection<Pair<VertexArray, ByteArray>>
	val triangles: Collection<Triangle>
	val adjacency: Collection<Adjacency>
	val joints: Collection<Joint>
	val poses: Collection<Pose>
	val animations: Collection<Anim>
	val frames: Collection<UShort>
	val extensions: ByteArray

	init {
		val data = ByteBuffer.wrap(loadBinary(file)).order(ByteOrder.LITTLE_ENDIAN)

		data.limit(Header.sizeInBytes)
		header = Header(data)
		check(!data.hasRemaining())

		data.limit(header.ofsText + header.numText).position(header.ofsText)
		texts = parseStrings(data)

		data.limit(header.ofsComments + header.numComments).position(header.ofsComments)
		comments = parseStrings(data)

		data.limit(header.ofsMeshes + header.numMeshes * Mesh.sizeInBytes)
			.position(header.ofsMeshes)
		meshes = parseElements(data, ::Mesh)

		data.limit(header.ofsVertexArrays + header.numVertexArrays * VertexArray.sizeInBytes)
			.position(header.ofsVertexArrays)
		val vertexArrayDescriptors = parseElements(data, ::VertexArray)
		vertexArrays = vertexArrayDescriptors.map {
			val iqmFormat = IqmFormats.fromIndex(it.format)
			val bytesCount = header.numVertices * iqmFormat.sizeInBytes.toUInt() * it.size
			data.limit(it.offset + bytesCount).position(it.offset)
			Pair(it, parseAttributeBuffer(data, IqmFormats.fromIndex(it.format)))
		}

		data.limit(header.ofsTriangles + header.numTriangles * Triangle.sizeInBytes)
			.position(header.ofsTriangles)
		triangles = parseElements(data, ::Triangle)

		data.limit(header.ofsAdjacency + header.numTriangles * 3u * Adjacency.sizeInBytes)
			.position(header.ofsAdjacency)
		adjacency = parseElements(data, ::Adjacency)

		data.limit(header.ofsJoints + header.numJoints * Joint.sizeInBytes)
			.position(header.ofsJoints)
		joints = parseElements(data, ::Joint)

		data.limit(header.ofsPoses + header.numPoses * Pose.sizeInBytes)
			.position(header.ofsPoses)
		poses = parseElements(data, ::Pose)

		data.limit(header.ofsAnims + header.numAnims * Anim.sizeInBytes)
			.position(header.ofsAnims)
		animations = parseElements(data, ::Anim)

		data.limit(header.ofsFrames + header.numFrames * 2u).position(header.ofsFrames)
		frames = parsePrimitives(data) { short.toUShort() }

		data.limit(header.ofsExtensions + header.numExtensions).position(header.ofsExtensions)
		extensions = ByteArray(header.numExtensions.toInt())
		data.get(extensions)
	}

	private fun parseAttributeBuffer(data: ByteBuffer, type: IqmFormats): ByteArray {
		return when (type) {
			IqmFormats.BYTE -> {
				parsePrimitives(data, ByteBuffer::get).toByteArray()
			}
			IqmFormats.UBYTE -> {
				parsePrimitives(data, ByteBuffer::get).toByteArray()
			}
			IqmFormats.SHORT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getShort)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 2)
				attributeBuffer.asShortBuffer().put(attributes.toShortArray())
				attributeBuffer.array()
			}
			IqmFormats.USHORT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getShort)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 2)
				attributeBuffer.asShortBuffer().put(attributes.toShortArray())
				attributeBuffer.array()
			}
			IqmFormats.INT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getInt)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 4)
				attributeBuffer.asIntBuffer().put(attributes.toIntArray())
				attributeBuffer.array()
			}
			IqmFormats.UINT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getInt)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 4)
				attributeBuffer.asIntBuffer().put(attributes.toIntArray())
				attributeBuffer.array()
			}
			IqmFormats.HALF -> {
				TODO("read 2 bytes at a time and convert them to floating point")
			}
			IqmFormats.FLOAT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getFloat)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 4)
				attributeBuffer.asFloatBuffer().put(attributes.toFloatArray())
				attributeBuffer.array()
			}
			IqmFormats.DOUBLE -> {
				val attributes = parsePrimitives(data, ByteBuffer::getDouble)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * type.sizeInBytes)
				attributeBuffer.asDoubleBuffer().put(attributes.toDoubleArray())
				attributeBuffer.array()
			}
		}
	}

	private fun loadBinary(file: File): ByteArray {
		try {
			return RandomAccessFile(file, "r").use {
				val bytes = ByteArray(it.channel.size().toInt())
				it.readFully(bytes)
				bytes
			}
		} catch (ex: IOException) {
			throw Exception("IQM parsing failed for file '$file'.", ex)
		}
	}
}
