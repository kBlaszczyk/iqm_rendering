package de.orchound.rendering.iqm

import de.orchound.rendering.iqm.IqmTypes.*
import de.orchound.rendering.opengl.OpenGLMesh
import de.orchound.rendering.opengl.OpenGLTexture
import de.orchound.rendering.opengl.OpenGLType
import de.orchound.rendering.toRadians
import org.joml.Matrix4f
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel


@ExperimentalUnsignedTypes
class IqmSceneObject(iqmData: IqmData, val shader: IqmShader) {

	private inner class Model(val mesh: OpenGLMesh, val texture: OpenGLTexture) {
		fun draw() {
			shader.setTexture(texture.handle)
			mesh.draw()
		}
	}

	private val models: Collection<Model>

	private val axisCorrectionMatrix = Matrix4f()
	private val modelTransformation = Matrix4f()

	init {
		val angle = toRadians(90f)
		axisCorrectionMatrix.setRotationXYZ(-angle, 0f, -angle)

		val meshes = iqmData.meshes.map(::loadMesh)
		val textures = iqmData.meshes
			.map(Mesh::material)
			.map { File("data/$it") }.map(::loadTexture)

		models = meshes.zip(textures).map {
			Model(it.first, it.second)
		}
	}

	fun draw() = models.forEach(Model::draw)
	fun rotateY(degrees: Float): Matrix4f = modelTransformation.rotateY(toRadians(degrees))

	fun getTransformation(dest: Matrix4f): Matrix4f {
		return modelTransformation.mul(axisCorrectionMatrix, dest)
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

	private fun loadTexture(file: File): OpenGLTexture {
		return MemoryStack.stackPush().use { frame ->
			val byteBuffer = file.inputStream().use { inputStream ->
				inputStream.channel.use { fileChannel ->
					fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
				}
			}

			val width = frame.mallocInt(1)
			val height = frame.mallocInt(1)
			val components = frame.mallocInt(1)

			val data = stbi_load_from_memory(byteBuffer, width, height, components, 4) ?:
			throw Exception("Failed to load image data for file: $file")

			val texture = OpenGLTexture(width.get(), height.get(), data)
			stbi_image_free(data)
			texture
		}
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
