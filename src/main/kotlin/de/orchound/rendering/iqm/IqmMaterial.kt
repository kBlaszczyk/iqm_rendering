package de.orchound.rendering.iqm

import de.orchound.shaderutility.OpenGLShader
import de.orchound.shaderutility.UniformJomlHelper
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer


class IqmMaterial(private val shader: OpenGLShader) {

	private val jomlHelper = UniformJomlHelper(shader)

	private val modelViewSetter = jomlHelper.getMat4Setter("model_view")
	private val modelViewProjectionSetter = jomlHelper.getMat4Setter("model_view_projection")
	private val jointsSetter = shader.getMat4ArraySetter("joints")

	private val animationFrameBuffer: FloatBuffer = BufferUtils.createFloatBuffer(16 * MAX_JOINTS)

	init {
		shader.bind()
		shader.getSamplerSetter("texture_sampler")(0)
		shader.unbind()
	}

	/**
	 * Sets the Uniform variable for the Model matrix.
	 * This method needs to be called after the shader has been bound.
	 */
	fun setModelView(matrix: Matrix4f) = modelViewSetter(matrix)
	fun setModelViewProjection(matrix: Matrix4f) = modelViewProjectionSetter(matrix)
	fun setTexture(textureHandle: Int) = shader.setTexture(textureHandle)

	fun setFrame(frame: Array<Matrix4f>) {
		animationFrameBuffer.clear()
		frame.forEachIndexed { index, matrix ->
			matrix.get(index * 16, animationFrameBuffer)
		}
		animationFrameBuffer.limit(frame.size * 16)
		jointsSetter(animationFrameBuffer)
	}

	companion object {
		const val MAX_JOINTS = 80
	}
}
