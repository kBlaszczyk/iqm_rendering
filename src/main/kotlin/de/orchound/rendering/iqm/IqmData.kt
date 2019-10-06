package de.orchound.rendering.iqm

import de.orchound.rendering.iqm.IqmTypes.*


@ExperimentalUnsignedTypes
class IqmData(
	val version: UInt,
	val fileSize: UInt,
	val flags: UInt,

	val meshes: Collection<Mesh>,
	val joints: Collection<Joint>,
	val poses: Collection<Pose>,
	val animations: Collection<Animation>,
	val comments: Collection<String>,
	val extensions: Collection<Extension>
)