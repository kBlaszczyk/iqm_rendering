package de.orchound.rendering.iqm

import java.nio.ByteBuffer
import java.util.*


fun parseStrings(data: ByteBuffer): List<String> {
	val strings = ArrayList<String>()

	val stringBuilder = StringBuilder()
	while (data.hasRemaining()) {
		val byte = data.get()

		if (byte != 0.toByte()) {
			stringBuilder.append(byte.toChar())
		} else {
			strings.add(stringBuilder.toString())
			stringBuilder.clear()
		}
	}

	if (stringBuilder.isNotEmpty())
		strings.add(stringBuilder.toString())

	return strings
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
