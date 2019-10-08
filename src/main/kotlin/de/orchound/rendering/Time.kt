package de.orchound.rendering


object Time {
	var currentTime = System.currentTimeMillis()
		private set
	var deltaTime: Long = 0
		private set

	fun update() {
		val previousTime = currentTime;
		currentTime = System.currentTimeMillis()
		deltaTime = currentTime - previousTime
	}
}
