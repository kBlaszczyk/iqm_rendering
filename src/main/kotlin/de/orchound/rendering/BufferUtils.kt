package de.orchound.rendering

import java.nio.Buffer
import java.nio.ByteBuffer


@ExperimentalUnsignedTypes
fun Buffer.limit(value: UInt): Buffer = this.limit(value.toInt())
@ExperimentalUnsignedTypes
fun Buffer.position(value: UInt) = this.position(value.toInt())
@ExperimentalUnsignedTypes
val ByteBuffer.uint
	get() = this.int.toUInt()
