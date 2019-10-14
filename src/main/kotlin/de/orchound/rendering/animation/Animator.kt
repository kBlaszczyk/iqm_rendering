package de.orchound.rendering.animation

import de.orchound.rendering.Time
import de.orchound.rendering.iqm.IqmData
import de.orchound.rendering.iqm.IqmTypes
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.min


@ExperimentalUnsignedTypes
class Animator(iqmData: IqmData) {

	var animationDone: Boolean = false
		private set
	var activeAnimation = ""
		private set

	private val baseJointTransformationBuffer = Matrix4f()
	private val frameJointTransformationBuffer = Matrix4f()

	/**
	 * base joint transformations relative to the model transformation
	 */
	private val baseJoints = loadBaseJoints(iqmData)
	private val frameJoints = baseJoints.map { Joint(it.id) }
	private val animations = loadAnimations(iqmData)

	private var animationTime = 0f

	init {
		baseJoints.zip(frameJoints).forEach {
			val baseJoint = it.first
			val frameJoint = it.second
			baseJoint.parent?.id?.let { parentId ->
				frameJoint.parent = frameJoints[parentId]
			}
		}

		println("animator loaded, available animations: ${animations.keys}")
	}

	fun startAnimation(animation: String) {
		animationTime = 0f
		animationDone = false
		activeAnimation = animation
	}

	fun update() {
		animations[activeAnimation]?.let { animation ->
			animationTime += Time.deltaTime / 1000f
			if (animation.loop) {
				animationTime %= animation.duration
			} else {
				animationTime = min(animationTime, animation.duration)
				if (animationTime == animation.duration)
					animationDone = true
			}
		}
	}

	/**
	 * Writes the joint matrices into the provided Matrix4f-array.
	 * The array must contain as many matrices as there are joints in the model,
	 * this Animator was initialized with.
	 */
	fun getFrame(dest: Array<Matrix4f>) {
		return animations[activeAnimation]?.let { animation ->
			animation.getFrame(animationTime, frameJoints)
			baseJoints.zip(frameJoints).withIndex().forEach {
				val baseJoint = it.value.first
				val frameJoint = it.value.second

				frameJoint.getAbsoluteTransformation(frameJointTransformationBuffer)
					.mul(baseJoint.getInverseTransformation(baseJointTransformationBuffer))

				dest[it.index].set(frameJointTransformationBuffer)
			}
		} ?: baseJoints.withIndex().forEach {
			it.value.getAbsoluteTransformation(dest[it.index])
		}
	}

	private fun loadBaseJoints(iqmData: IqmData): List<Joint> {
		val joints = iqmData.joints.mapIndexed { index, _ -> Joint(index) }

		return iqmData.joints.zip(joints).map {
			val jointData = it.first
			val joint = it.second

			with(jointData.translation) { joint.setTranslation(Vector3f(x, y, z)) }
			with(jointData.rotation) { joint.setRotation(Quaternionf(x, y, z, w)) }
			with(jointData.scale) { joint.setScale(Vector3f(x, y, z)) }

			joint.parent = jointData.parentId?.let { parentId ->
				joints[parentId]
			}

			joint
		}
	}

	private fun loadAnimations(iqmData: IqmData): Map<String, Animation> {
		return iqmData.animations.map { animationData ->
			getAnimation(animationData, iqmData.poses)
		}.associateBy(Animation::name)
	}

	private fun getAnimation(animationData: IqmTypes.Animation, poses: Collection<IqmTypes.Pose>): Animation {
		val frameValues = animationData.frameValues.toList()

		val keyFrames = ArrayList<Frame>()
		var frameTime = 0f
		val frameOffsetTime = 1 / animationData.frameRate

		var frameValueIndex = 0
		for (i in 0 until(animationData.framesCount.toInt())) {
			val jointPoses = poses.mapIndexed { index, _ -> Joint(index) }
			poses.zip(jointPoses).forEach {
				val jointPoseData = it.first
				val jointPose = it.second

				var translationX = jointPoseData.translationOffset.x
				if (jointPoseData.mask and 0x01u != 0u)
					translationX += frameValues[frameValueIndex++].toFloat() * jointPoseData.translationScale.x
				var translationY = jointPoseData.translationOffset.y
				if (jointPoseData.mask and 0x02u != 0u)
					translationY += frameValues[frameValueIndex++].toFloat() * jointPoseData.translationScale.y
				var translationZ = jointPoseData.translationOffset.z
				if (jointPoseData.mask and 0x04u != 0u)
					translationZ += frameValues[frameValueIndex++].toFloat() * jointPoseData.translationScale.z
				var rotationX = jointPoseData.rotationOffset.x
				if (jointPoseData.mask and 0x08u != 0u)
					rotationX += frameValues[frameValueIndex++].toFloat() * jointPoseData.rotationScale.x
				var rotationY = jointPoseData.rotationOffset.y
				if (jointPoseData.mask and 0x10u != 0u)
					rotationY += frameValues[frameValueIndex++].toFloat() * jointPoseData.rotationScale.y
				var rotationZ = jointPoseData.rotationOffset.z
				if (jointPoseData.mask and 0x20u != 0u)
					rotationZ += frameValues[frameValueIndex++].toFloat() * jointPoseData.rotationScale.z
				var rotationW = jointPoseData.rotationOffset.w
				if (jointPoseData.mask and 0x40u != 0u)
					rotationW += frameValues[frameValueIndex++].toFloat() * jointPoseData.rotationScale.w
				var scaleX = jointPoseData.scaleOffset.x
				if (jointPoseData.mask and 0x80u != 0u)
					scaleX += frameValues[frameValueIndex++].toFloat() * jointPoseData.scaleScale.x
				var scaleY = jointPoseData.scaleOffset.y
				if (jointPoseData.mask and 0x100u != 0u)
					scaleY += frameValues[frameValueIndex++].toFloat() * jointPoseData.scaleScale.y
				var scaleZ = jointPoseData.scaleOffset.z
				if (jointPoseData.mask and 0x200u != 0u)
					scaleZ += frameValues[frameValueIndex++].toFloat() * jointPoseData.scaleScale.z

				jointPose.setTranslation(Vector3f(translationX, translationY, translationZ))
				jointPose.setRotation(Quaternionf(rotationX, rotationY, rotationZ, rotationW).normalize())
				jointPose.setScale(Vector3f(scaleX, scaleY, scaleZ))
				jointPose.parent = jointPoseData.parentId?.let { parentId ->
					jointPoses[parentId]
				}
			}

			keyFrames.add(Frame(frameTime, jointPoses))
			frameTime += frameOffsetTime
		}

		return Animation(animationData.name, keyFrames, animationData.flags == 1u)
	}
}