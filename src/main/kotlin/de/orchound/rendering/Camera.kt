package de.orchound.rendering

import org.joml.Matrix4f
import org.joml.Vector3f

class Camera(aspectRatio: Float, fov: Float) {

	private val up = Vector3f(0f, 1f, 0f)
	private val center = Vector3f(0f, 4f, 0f)
	private val position = Vector3f(0f, 8f, 10f)

	private val fovY = toRadians(fov / aspectRatio)
	private val nearPlane = 0.5f
	private val farPlane = 100f

	private val viewMatrix = Matrix4f().setLookAt(position, center, up)
	private val projectionMatrix = Matrix4f()
		.setPerspective(fovY, aspectRatio, nearPlane, farPlane)

	fun getProjectionView(dest: Matrix4f): Matrix4f {
		return projectionMatrix.mul(viewMatrix, dest)
	}
}
