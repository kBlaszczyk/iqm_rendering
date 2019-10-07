package de.orchound.rendering.opengl;

import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays
import java.nio.ByteBuffer


class OpenGLMesh {

	private var vao = glGenVertexArrays()
	private var indicesCount: Int = 0
	private var drawMode = GL_TRIANGLES

	fun setVertexAttribute(
		data: ByteBuffer, shaderLocation: Int, type: OpenGLType,
		attributeComponentsCount: Int, normalized: Boolean
	) {
		val vbo = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, vbo)
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
		glBindBuffer(GL_ARRAY_BUFFER, 0)

		glBindVertexArray(vao)
		glBindBuffer(GL_ARRAY_BUFFER, vbo)
		glVertexAttribPointer(shaderLocation, attributeComponentsCount, type.value, normalized, 0, 0)
		glEnableVertexAttribArray(shaderLocation)
		glBindVertexArray(0)
	}

	fun setIndices(indices: IntArray) {
		indicesCount = indices.size

		val ebo = glGenBuffers()
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

		glBindVertexArray(vao)
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
		glBindVertexArray(0)
	}

	fun draw() {
		glBindVertexArray(vao)
		glDrawElements(drawMode, indicesCount, GL_UNSIGNED_INT, 0)
		glBindVertexArray(0)
	}
}
