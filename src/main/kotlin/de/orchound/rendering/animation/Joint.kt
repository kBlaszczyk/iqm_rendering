package de.orchound.rendering.animation

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f


class Joint(val id: Int, var parent: Joint? = null) {

	private val translation = Vector3f()
	private val rotation = Quaternionf()
	private val scale = Vector3f(1f)

	private val transformation = Matrix4f()
	private val absoluteTransformation = Matrix4f()
	private val inverseAbsoluteTransformation = Matrix4f()

	private var transformationChanged = true

	private val matrixBuffer = Matrix4f()

	fun setTranslation(vector: Vector3f) {
		translation.set(vector)
		transformationChanged = true
	}

	fun setRotation(quaternion: Quaternionf) {
		rotation.set(quaternion)
		transformationChanged = true
	}

	fun setScale(vector: Vector3f) {
		scale.set(vector)
		transformationChanged = true
	}

	fun set(other: Joint) {
		translation.set(other.translation)
		rotation.set(other.rotation)
		scale.set(other.scale)
		transformationChanged = true
	}

	fun interpolate(target: Joint, progress: Float) {
		translation.interpolate(target.translation, progress)
		rotation.nlerp(target.rotation, progress)
		scale.interpolate(target.scale, progress)
		transformationChanged = true
	}

	private fun Vector3f.interpolate(other: Vector3f, progress: Float) {
		this.set(
			this.x  + progress * (other.x - this.x),
			this.y  + progress * (other.y - this.y),
			this.z  + progress * (other.z - this.z)
		)
	}

	fun getTransformation(dest: Matrix4f): Matrix4f {
		if (transformationChanged)
			updateAbsoluteTransformations()

		return dest.set(transformation)
	}

	fun getAbsoluteTransformation(dest: Matrix4f): Matrix4f {
		if (transformationChanged)
			updateAbsoluteTransformations()

		return dest.set(absoluteTransformation)
	}

	fun getInverseTransformation(dest: Matrix4f): Matrix4f {
		if (transformationChanged)
			updateAbsoluteTransformations()

		return dest.set(inverseAbsoluteTransformation)
	}

	private fun updateAbsoluteTransformations() {
		transformation.translationRotateScale(translation, rotation, scale)
		absoluteTransformation.set(transformation)
		inverseAbsoluteTransformation.set(transformation).invert()

		parent?.let {
			parent!!.getAbsoluteTransformation(matrixBuffer)
			matrixBuffer.mul(absoluteTransformation, absoluteTransformation)

			parent!!.getInverseTransformation(matrixBuffer)
			inverseAbsoluteTransformation.mul(matrixBuffer)
		}

		transformationChanged = false
	}
}