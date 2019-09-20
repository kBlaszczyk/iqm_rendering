package de.orchound.rendering.iqm

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File


@ExperimentalUnsignedTypes
class IqmParserTest : StringSpec() {
	private val iqmParser = IqmParser(File("data/mrfixit.iqm"))
	private val header = iqmParser.header

	init {
		"IQM version should be 2" {
			header.version shouldBe 2u
		}

		"Parser should contain all meshes" {
			iqmParser.meshes.size.toUInt() shouldBe header.numMeshes
		}

		"Parser should contain all animations" {
			iqmParser.animations.size.toUInt() shouldBe header.numAnims
		}

		"Parser should contain all joints" {
			iqmParser.joints.size.toUInt() shouldBe header.numJoints
		}

		"Parser should contain all poses" {
			iqmParser.poses.size.toUInt() shouldBe header.numPoses
		}

		"Parser should contain all frames" {
			iqmParser.frames.size.toUInt() shouldBe header.numFrames
		}

		"Parser should contain all texts" {
			iqmParser.texts.size shouldBe 83
		}
	}
}
