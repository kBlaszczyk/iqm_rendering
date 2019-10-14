package de.orchound.rendering.animation


class Animation(val name: String, private val keyFrames: List<Frame>, val loop: Boolean) {

	val duration = keyFrames.last().frameTime

	init {
		require(keyFrames.isNotEmpty())
	}

	/**
	 * Returns the joint poses at the specified time.
	 * @param time has to be in interval [0, animation.length]
	 */
	fun getFrame(time: Float, dest: List<Joint>): List<Joint> {
		var keyFrameIndex = 0
		var firstKeyFrame = keyFrames[keyFrameIndex]
		var secondKeyFrame = keyFrames[keyFrameIndex + 1]

		while (time > secondKeyFrame.frameTime && keyFrameIndex < keyFrames.size - 1) {
			keyFrameIndex++
			firstKeyFrame = keyFrames[keyFrameIndex]
			secondKeyFrame = keyFrames[keyFrameIndex + 1]
		}

		val progress = (time - firstKeyFrame.frameTime) /
			(secondKeyFrame.frameTime - firstKeyFrame.frameTime)

		dest.zip(firstKeyFrame.jointPoses).onEach {
			it.first.set(it.second)
		}.map(Pair<Joint, Joint>::first).zip(secondKeyFrame.jointPoses).forEach {
			it.first.interpolate(it.second, progress)
		}

		return dest
	}
}