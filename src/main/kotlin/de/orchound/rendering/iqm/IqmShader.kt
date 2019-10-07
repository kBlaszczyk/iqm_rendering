package de.orchound.rendering.iqm

import org.joml.Matrix4f
import org.lwjgl.opengl.GL20.*


class IqmShader {

	private val handle: Int
	private val modelViewProjectionLocation: Int
	private val matrixBuffer = FloatArray(16)

	init {
		val vertexShaderSource = loadShaderSource("/shader/IqmShader_vs.glsl")
		val fragmentShaderSource = loadShaderSource("/shader/IqmShader_fs.glsl")

		handle = createShaderProgram(vertexShaderSource, fragmentShaderSource)
		modelViewProjectionLocation = getUniformLocation("model_view_projection")
	}

	/**
	 * Updates the Uniform variable for the ModelViewProjection matrix.
	 * This method needs to be called after the shader has been bound.
	 */
	fun updateModelViewProjection(matrix: Matrix4f) {
		matrix.get(matrixBuffer)
		glUniformMatrix4fv(modelViewProjectionLocation, false, matrixBuffer)
	}

	fun bind() = glUseProgram(handle)
	fun unbind() = glUseProgram(0)

	private fun createShaderProgram(vertexShaderSource: Array<String>, fragmentShaderSource: Array<String>): Int {
		val vertexShaderHandle = compileShader(vertexShaderSource, GL_VERTEX_SHADER)
		val fragmentShaderHandle = compileShader(fragmentShaderSource, GL_FRAGMENT_SHADER)

		val programHandle = glCreateProgram()
		glAttachShader(programHandle, vertexShaderHandle)
		glAttachShader(programHandle, fragmentShaderHandle)

		glBindAttribLocation(programHandle, 0, "position_ms")
		glBindAttribLocation(programHandle, 1, "normal_ms")
		glBindAttribLocation(programHandle, 2, "tangent_ms")
		glBindAttribLocation(programHandle, 3, "texcoords")
		glBindAttribLocation(programHandle, 4, "color")
		glBindAttribLocation(programHandle, 5, "blend_index")
		glBindAttribLocation(programHandle, 6, "blend_weight")

		glLinkProgram(programHandle)

		glDetachShader(programHandle, vertexShaderHandle)
		glDetachShader(programHandle, fragmentShaderHandle)
		glDeleteShader(vertexShaderHandle)
		glDeleteShader(fragmentShaderHandle)

		validateShaderLinking(programHandle)
		validateShaderProgram(programHandle)

		return programHandle
	}

	private fun compileShader(shaderSource: Array<String>, type: Int): Int {
		val shaderId = glCreateShader(type)

		glShaderSource(shaderId, *shaderSource)
		glCompileShader(shaderId)

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
			val info = getShaderInfoLog(shaderId)
			val shaderType = if (type == GL_VERTEX_SHADER) "Vertex" else "Fragment"
			throw Exception("$shaderType shader compilation failed: $info")
		}

		return shaderId
	}

	private fun getProgramInfoLog(programId:Int):String {
		return glGetProgramInfoLog(programId, GL_INFO_LOG_LENGTH)
	}

	private fun getShaderInfoLog(shaderId:Int):String {
		return glGetShaderInfoLog(shaderId, GL_INFO_LOG_LENGTH)
	}

	private fun validateShaderProgram(programId:Int) {
		glValidateProgram(programId)

		val error = glGetError()
		if (error != 0)
			throw Exception("OpenGL shader creation failed")
	}

	private fun validateShaderLinking(programId: Int) {
		if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
			val info = getProgramInfoLog(programId)
			throw Exception("OpenGL shader linking failed: $info")
		}
	}

	private fun loadShaderSource(resource: String): Array<String> {
		return javaClass.getResourceAsStream(resource).use { inputStream ->
			inputStream.bufferedReader().readLines()
		}.map { "$it\n" }.toTypedArray()
	}

	private fun getUniformLocation(name: String): Int {
		return glGetUniformLocation(handle, name).let { location ->
			if (location != -1)
				location
			else
				throw Exception("Uniform variable '$name' could not be found in the shader.")
		}
	}
}
