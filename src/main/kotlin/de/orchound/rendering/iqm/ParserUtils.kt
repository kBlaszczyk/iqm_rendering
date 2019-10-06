package de.orchound.rendering.iqm

import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.HashMap


@ExperimentalUnsignedTypes
fun parseStrings(data: ByteBuffer): Map<UInt, String> {
	val stringsByByteIndex = HashMap<UInt, String>()

	val stringBuilder = StringBuilder()
	var currentIndex = 0
	var stringIndex = 0u
	while (data.hasRemaining()) {
		val byte = data.get()

		if (byte != 0.toByte()) {
			stringBuilder.append(byte.toChar())
		} else {
			stringsByByteIndex[stringIndex] = stringBuilder.toString()
			stringBuilder.clear()
			stringIndex = currentIndex.toUInt() + 1u
		}

		currentIndex++
	}

	if (stringBuilder.isNotEmpty())
		stringsByByteIndex[stringIndex] = stringBuilder.toString()

	return stringsByByteIndex
}

fun <T> parseElements(data: ByteBuffer, parseSingleElement: (ByteBuffer) -> T): Collection<T> {
	val elements = ArrayList<T>()
	while (data.hasRemaining())
		elements.add(parseSingleElement(data))

	return elements
}

fun <T> parsePrimitives(data: ByteBuffer, getValue: ByteBuffer.() -> T): List<T> {
	val primitives = ArrayList<T>()
	while (data.hasRemaining())
		primitives.add(data.getValue())

	return primitives
}

fun <T> parsePrimitives(data: ByteBuffer, primivesCount: Int, getValue: ByteBuffer.() -> T): List<T> {
	val primitives = ArrayList<T>()
	for (i in 0 until primivesCount)
		primitives.add(data.getValue())

	return primitives
}
