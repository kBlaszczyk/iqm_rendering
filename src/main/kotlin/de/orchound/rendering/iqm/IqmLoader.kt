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
			val type = AttributeType.fromParsedValue(vertexArray.type)
			val format = Format.fromParsedValue(vertexArray.format)
			VertexAttributeBuffer(type, vertexArray.flags, it.second, format, vertexArray.size.toInt())
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

		val joints = parser.joints.map { jointDataToIqmJoint(it, texts) }
		val poses = parser.poses.map { poseDataToIqmPose(it) }

		val animations = parser.animations.map {
			val anim = it.first
			Animation(texts[anim.name].orEmpty(), anim.flags, anim.frameRate, anim.numFrames, it.second)
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

	private fun jointDataToIqmJoint(joint: HeaderStructs.Joint, texts: Map<UInt, String>): Joint {
		val parentId = if (joint.parent >= 0)
			joint.parent
		else null

		val translation = Translation(joint.translation[0], joint.translation[1], joint.translation[2])
		val rotation = Rotation(joint.rotation[0], joint.rotation[1], joint.rotation[2], joint.rotation[3])
		val scale = Scale(joint.scale[0], joint.scale[1], joint.scale[2])

		return Joint(texts[joint.name].orEmpty(), parentId, translation, rotation, scale)
	}

	private fun poseDataToIqmPose(poseData: HeaderStructs.Pose): Pose {
		val parentId = if (poseData.parent >= 0)
			poseData.parent
		else null

		val channelOffset = poseData.channelOffset
		val translationOffset = Translation(channelOffset[0], channelOffset[1], channelOffset[2])
		val rotationOffset = Rotation(channelOffset[3], channelOffset[4], channelOffset[5], channelOffset[6])
		val scaleOffset = Scale(channelOffset[7], channelOffset[8], channelOffset[9])
		val channelScale = poseData.channelScale
		val translationScale = Translation(channelScale[0], channelScale[1], channelScale[2])
		val rotationScale = Rotation(channelScale[3], channelScale[4], channelScale[5], channelScale[6])
		val scaleScale = Scale(channelScale[7], channelScale[8], channelScale[9])

		return Pose(
			parentId, poseData.mask, translationOffset, rotationOffset, scaleOffset,
			translationScale, rotationScale, scaleScale
		)
	}
}