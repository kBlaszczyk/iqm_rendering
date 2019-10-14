package de.orchound.rendering.animation


class Frame(val frameTime: Float, val jointPoses: List<Joint>) {
	val jointsCount = jointPoses.size
}