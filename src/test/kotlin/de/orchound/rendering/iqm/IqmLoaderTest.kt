package de.orchound.rendering.iqm

import de.orchound.engine.rendering.iqm.IqmLoader
import io.kotlintest.matchers.haveSize
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File


@ExperimentalUnsignedTypes
class IqmLoaderTest : StringSpec() {
	private val data = IqmLoader.loadIqm(File("data/mrfixit.iqm"))

	init {
		"IQM version should be 2" {
			data.version shouldBe 2u
		}

		"IqmData should contain all meshes" {
			data.meshes should haveSize(2)
		}

		"IqmData should contain all animations" {
			data.animations should haveSize(1)
		}

		"IqmData should contain all joints" {
			data.joints should haveSize(75)
		}

		"IqmData should contain all poses" {
			data.poses should haveSize(75)
		}
	}
}
