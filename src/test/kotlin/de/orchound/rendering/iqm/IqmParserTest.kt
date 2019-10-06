package de.orchound.rendering.iqm

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File


@ExperimentalUnsignedTypes
class IqmParserTest : StringSpec() {
	private val parser = IqmParser(File("data/mrfixit.iqm"))
	private val header = parser.header

	init {
		"IQM version should be 2" {
			header.version shouldBe 2u
		}

		"Parser should contain all meshes" {
			parser.meshes.size.toUInt() shouldBe header.numMeshes
		}

		"Parser should contain all animations" {
			parser.animations.size.toUInt() shouldBe header.numAnims
		}

		"Parser should contain all joints" {
			parser.joints.size.toUInt() shouldBe header.numJoints
		}

		"Parser should contain all poses" {
			parser.poses.size.toUInt() shouldBe header.numPoses
		}

		"Parser should contain all texts" {
			parser.texts.size shouldBe 83
		}
	}
}
