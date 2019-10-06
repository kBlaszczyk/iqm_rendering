package de.orchound.rendering.iqm

import de.orchound.rendering.uint
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets


/**
 * This interface represents a namespace.
 */
@ExperimentalUnsignedTypes
interface HeaderStructs {
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

	class Adjacency(data: ByteBuffer) {
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

	class Extension(data: ByteBuffer) {
		val name = data.uint
		val numData = data.uint
		val ofsData = data.uint
		val ofsExtensions = data.uint

		companion object {
			val sizeInBytes = 4u * 4u
		}
	}
}