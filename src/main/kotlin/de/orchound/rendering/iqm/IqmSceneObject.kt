package de.orchound.rendering.iqm

import de.orchound.rendering.iqm.IqmTypes.*
import de.orchound.rendering.opengl.OpenGLMesh
import de.orchound.rendering.opengl.OpenGLType
import de.orchound.rendering.toRadians
import org.joml.Matrix4f
import java.nio.ByteBuffer


@ExperimentalUnsignedTypes
class IqmSceneObject(iqmData: IqmData) {

	val meshes = iqmData.meshes.map(::loadMesh)

	private val transformation = Matrix4f()
	private val axisCorrectionMatrix = Matrix4f()
	private val modelTransformation = Matrix4f()

	init {
		val angle = Math.toRadians(90.0).toFloat()
		axisCorrectionMatrix.setRotationXYZ(-angle, 0f, -angle)
	}

	fun getTransformation(): Matrix4f {
		return modelTransformation.mul(axisCorrectionMatrix, transformation)
	}

	fun draw() {
		meshes.forEach(OpenGLMesh::draw)
	}

	fun rotateY(degrees: Float) {
		modelTransformation.rotateY(toRadians(degrees))
	}

	private fun loadMesh(meshData: Mesh): OpenGLMesh {
		val mesh = OpenGLMesh()

		val indices = meshData.triangles.flatMap {
			listOf(it.vertex1.toInt(), it.vertex2.toInt(), it.vertex3.toInt())
		}.toIntArray()

		mesh.setIndices(indices)

		for (vertexAttribute in meshData.vertexAttributes) {
			val shaderLocation = attributeToShaderLocation(vertexAttribute.type)
			val data = vertexAttribute.data
			val format = iqmFormatToOpenGLType(vertexAttribute.format)
			val byteBuffer = byteArrayToInvertedEndiannessByteBuffer(data, format.size)

			// FixMe normalize blend weight
			mesh.setVertexAttribute(byteBuffer, shaderLocation, format, vertexAttribute.componentSize, false)
		}

		return mesh
	}

	private fun byteArrayToInvertedEndiannessByteBuffer(data: ByteArray, formatSize: Int): ByteBuffer {
		val byteBuffer = ByteBuffer.allocateDirect(data.size)
		for (element in data.indices step formatSize) {
			for (index in formatSize - 1 downTo 0) {
				byteBuffer.put(data[element + index])
			}
		}
		byteBuffer.flip()
		return byteBuffer
	}

	private fun attributeToShaderLocation(attributeType: AttributeType): Int = when (attributeType) {
		AttributeType.POSITION -> 0
		AttributeType.NORMAL -> 1
		AttributeType.TANGENT -> 2
		AttributeType.TEXCOORD -> 3
		AttributeType.COLOR -> 4
		AttributeType.BLENDINDEXES -> 5
		AttributeType.BLENDWEIGHTS -> 6
		else -> throw Exception("Unsupported attribute type: ${attributeType.name}.")
	}

	private fun iqmFormatToOpenGLType(iqmFormat: Format) = when (iqmFormat) {
		Format.BYTE -> OpenGLType.BYTE
		Format.UBYTE -> OpenGLType.UNSIGNED_BYTE
		Format.SHORT -> OpenGLType.SHORT
		Format.USHORT -> OpenGLType.UNSIGNED_SHORT
		Format.INT -> OpenGLType.INT
		Format.HALF -> OpenGLType.HALF_FLOAT
		Format.FLOAT -> OpenGLType.FLOAT
		Format.DOUBLE -> OpenGLType.DOUBLE
		else -> throw Exception("Unsupported IQM Format: ${iqmFormat.name}")
	}
}
