package de.orchound.engine.rendering.iqm

import de.orchound.rendering.iqm.HeaderStructs
import de.orchound.rendering.iqm.IqmData
import de.orchound.rendering.iqm.IqmParser
import de.orchound.rendering.iqm.IqmTypes.*
import java.io.File


@ExperimentalUnsignedTypes
object IqmLoader {

	fun loadIqm(file: File): IqmData {
		val parser = IqmParser(file)

		val texts = parser.texts

		val triangles = parser.triangles.map {
			Triangle(it.vertex1, it.vertex2, it.vertex3)
		}

		val vertexAttributeBuffers = parser.vertexArrays.map {
			val vertexArray = it.first
			val format = Format.fromParsedValue(vertexArray.format)
			VertexAttributeBuffer(vertexArray.flags, it.second, format, vertexArray.size.toInt())
		}

		val meshes = parser.meshes.map {
			val meshTriangles = triangles
				.subList(it.firstTriangle.toInt(), it.firstTriangle.toInt() + it.numTriangles.toInt())
			Mesh(
				texts[it.name].orEmpty(), texts[it.material].orEmpty(),
				it.numVertices, vertexAttributeBuffers, meshTriangles
			)
		}

		val adjacency = parser.adjacency.map {
			val triangleIndex1 = it.triangle1.toInt()
			val triangle1 = if (triangleIndex1 != -1) triangles[triangleIndex1] else null
			val triangleIndex2 = it.triangle2.toInt()
			val triangle2 = if (triangleIndex2 != -1) triangles[triangleIndex2] else null
			val triangleIndex3 = it.triangle3.toInt()
			val triangle3 = if (triangleIndex3 != -1) triangles[triangleIndex3] else null

			Adjacency(triangle1, triangle2, triangle3)
		}

		val joints = createIqmJoints(parser.joints.withIndex(), texts)
		val poses = createIqmPoses(parser.poses.withIndex())

		val animations = parser.animations.map {
			Animation(texts[it.first.name].orEmpty(), it.second, it.first.frameRate, it.first.flags)
		}

		val extensions = parser.extensions.map {
			Extension(texts[it.first.name].orEmpty(), it.second)
		}

		val header = parser.header
		return IqmData(
			header.version, header.filesize, header.flags, meshes,
			joints, poses, animations, parser.comments.values, extensions
		)
	}

	private fun createIqmJoints(
		jointsData: Iterable<IndexedValue<HeaderStructs.Joint>>, texts: Map<UInt, String>
	): Collection<Joint> {
		val jointsByIndex = HashMap<Int, Joint>()
		addJointChildrenToMap(null, jointsData, jointsByIndex, texts)
		return jointsByIndex.values
	}

	private fun addJointChildrenToMap(
		parentIndex: Int?, jointsData: Iterable<IndexedValue<HeaderStructs.Joint>>,
		jointsByIndex: MutableMap<Int, Joint>, texts: Map<UInt, String>
	) {
		val childJointsData = if (parentIndex == null)
			jointsData.filter { it.value.parent < 0 }
		else
			jointsData.filter { it.value.parent == parentIndex }

		childJointsData.forEach {
			jointsByIndex[it.index] = jointDataToIqmJoint(it.value, jointsByIndex, texts)
			addJointChildrenToMap(it.index, jointsData, jointsByIndex, texts)
		}
	}

	private fun jointDataToIqmJoint(joint: HeaderStructs.Joint, jointsByIndex: Map<Int, Joint>, texts: Map<UInt, String>): Joint {
		val translation = Translation(joint.translation[0], joint.translation[1], joint.translation[2])
		val rotation = Rotation(joint.rotation[0], joint.rotation[1], joint.rotation[2], joint.rotation[3])
		val scale = Scale(joint.scale[0], joint.scale[1], joint.scale[2])
		return Joint(texts[joint.name].orEmpty(), jointsByIndex[joint.parent], translation, rotation, scale)
	}

	private fun createIqmPoses(posesData: Iterable<IndexedValue<HeaderStructs.Pose>>): Collection<Pose> {
		val posesByIndex = HashMap<Int, Pose>()
		addPoseChildrenToMap(null, posesData, posesByIndex)
		return posesByIndex.values
	}

	private fun addPoseChildrenToMap(
		parentIndex: Int?, posesData: Iterable<IndexedValue<HeaderStructs.Pose>>,
		posesByIndex: MutableMap<Int, Pose>
	) {
		val childPosesData = if (parentIndex == null)
			posesData.filter { it.value.parent < 0 }
		else
			posesData.filter { it.value.parent == parentIndex }

		childPosesData.forEach {
			posesByIndex[it.index] = poseDataToIqmPose(it.value, posesByIndex)
			addPoseChildrenToMap(it.index, posesData, posesByIndex)
		}
	}

	private fun poseDataToIqmPose(pose: HeaderStructs.Pose, posesByIndex: Map<Int, Pose>): Pose {
		val channelOffset = pose.channelOffset
		val translationOffset = Translation(channelOffset[0], channelOffset[1], channelOffset[2])
		val rotationOffset = Rotation(channelOffset[3], channelOffset[4], channelOffset[5], channelOffset[6])
		val scaleOffset = Scale(channelOffset[7], channelOffset[8], channelOffset[9])
		val channelScale = pose.channelScale
		val translationScale = Translation(channelScale[0], channelScale[1], channelScale[2])
		val rotationScale = Rotation(channelScale[3], channelScale[4], channelScale[5], channelScale[6])
		val scaleScale = Scale(channelScale[7], channelScale[8], channelScale[9])

		return Pose(
			posesByIndex[pose.parent], pose.mask, translationOffset, rotationOffset, scaleOffset,
			translationScale, rotationScale, scaleScale
		)
	}
}