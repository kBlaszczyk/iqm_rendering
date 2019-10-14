package de.orchound.rendering.iqm

import de.orchound.rendering.iqm.HeaderStructs.*
import de.orchound.rendering.iqm.IqmTypes.Format
import de.orchound.rendering.limit
import de.orchound.rendering.position
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

@ExperimentalUnsignedTypes
class IqmParser(file: File) {

	val header: Header
	val texts: Map<UInt, String>
	val comments: Map<UInt, String>
	val meshes: Collection<Mesh>
	val vertexArrays: Collection<Pair<VertexArray, ByteArray>>
	val triangles: Collection<Triangle>
	val adjacency: Collection<Adjacency>
	val joints: Collection<Joint>
	val poses: Collection<Pose>
	val animations: Collection<Pair<Anim, Collection<UShort>>>
	val extensions: Collection<Pair<Extension, ByteArray>>

	init {
		val data = ByteBuffer.wrap(loadBinary(file)).order(ByteOrder.LITTLE_ENDIAN)

		data.limit(Header.sizeInBytes)
		header = Header(data)

		data.limit(header.ofsText + header.numText).position(header.ofsText)
		texts = parseStrings(data)

		data.limit(header.ofsComments + header.numComments).position(header.ofsComments)
		comments = parseStrings(data)

		data.limit(header.ofsMeshes + header.numMeshes * Mesh.sizeInBytes).position(header.ofsMeshes)
		meshes = parseElements(data, ::Mesh)

		data.limit(header.ofsVertexArrays + header.numVertexArrays * VertexArray.sizeInBytes)
			.position(header.ofsVertexArrays)
		vertexArrays = parseElements(data, ::VertexArray).map {
			val iqmFormat = Format.fromParsedValue(it.format)
			val bytesCount = header.numVertices * iqmFormat.size.toUInt() * it.size
			data.limit(it.offset + bytesCount).position(it.offset)
			val attributeData = parseAttributeBuffer(data, iqmFormat)
			Pair(it, attributeData)
		}

		data.limit(header.ofsTriangles + header.numTriangles * Triangle.sizeInBytes)
			.position(header.ofsTriangles)
		triangles = parseElements(data, ::Triangle)

		data.limit(header.ofsAdjacency + header.numTriangles * Adjacency.sizeInBytes)
			.position(header.ofsAdjacency)
		adjacency = parseElements(data, ::Adjacency)

		data.limit(header.ofsJoints + header.numJoints * Joint.sizeInBytes)
			.position(header.ofsJoints)
		joints = parseElements(data, ::Joint)

		data.limit(header.ofsPoses + header.numPoses * Pose.sizeInBytes)
			.position(header.ofsPoses)
		poses = parseElements(data, ::Pose)

		val frameValuesCount = poses.map {
			countSetBits(it.mask.toInt(), 0)
		}.sum()
		val framesCount = frameValuesCount * header.numFrames.toInt() * 2
		data.limit(header.ofsFrames.toInt() + framesCount).position(header.ofsFrames)
		val frames = parsePrimitives(data) { short.toUShort() }

		data.limit(header.ofsAnims + header.numAnims * Anim.sizeInBytes)
			.position(header.ofsAnims)
		animations = parseElements(data, ::Anim).map {
			val toFrameIndex = it.firstFrame.toInt() + it.numFrames.toInt() * frameValuesCount
			Pair(it, frames.subList(it.firstFrame.toInt(), toFrameIndex))
		}

		extensions = ArrayList()
		var extensionsCount = header.numExtensions
		var extensionOffset = header.ofsExtensions
		while (extensionsCount > 0u) {
			data.limit(extensionOffset + Extension.sizeInBytes).position(extensionOffset)
			val extension = Extension(data)
			data.limit(extension.ofsData + extension.numData).position(extension.ofsData)
			val extensionData = ByteArray(extension.numData.toInt())
			data.get(extensionData)
			(extensions as MutableList).add(Pair(extension, extensionData))
			extensionOffset = extension.ofsExtensions
			extensionsCount--
		}
	}

	private tailrec fun countSetBits(value: Int, sum: Int): Int {
		return if (value != 0)
			countSetBits(value shr 1, sum + (value and 1))
		else sum
	}

	private fun parseAttributeBuffer(data: ByteBuffer, type: Format): ByteArray {
		return when (type) {
			Format.BYTE -> {
				parsePrimitives(data, ByteBuffer::get).toByteArray()
			}
			Format.UBYTE -> {
				parsePrimitives(data, ByteBuffer::get).toByteArray()
			}
			Format.SHORT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getShort)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 2)
				attributeBuffer.asShortBuffer().put(attributes.toShortArray())
				attributeBuffer.array()
			}
			Format.USHORT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getShort)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 2)
				attributeBuffer.asShortBuffer().put(attributes.toShortArray())
				attributeBuffer.array()
			}
			Format.INT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getInt)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 4)
				attributeBuffer.asIntBuffer().put(attributes.toIntArray())
				attributeBuffer.array()
			}
			Format.UINT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getInt)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 4)
				attributeBuffer.asIntBuffer().put(attributes.toIntArray())
				attributeBuffer.array()
			}
			Format.HALF -> {
				TODO("read 2 bytes at a time and convert them to floating point")
			}
			Format.FLOAT -> {
				val attributes = parsePrimitives(data, ByteBuffer::getFloat)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 4)
				attributeBuffer.asFloatBuffer().put(attributes.toFloatArray())
				attributeBuffer.array()
			}
			Format.DOUBLE -> {
				val attributes = parsePrimitives(data, ByteBuffer::getDouble)
				val attributeBuffer = ByteBuffer.allocate(attributes.size * 8)
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
