package de.orchound.rendering

import de.orchound.engine.rendering.iqm.IqmLoader
import de.orchound.rendering.iqm.IqmSceneObject
import de.orchound.rendering.iqm.IqmShader
import org.joml.Matrix4f
import java.io.File


@ExperimentalUnsignedTypes
fun main() {
	val window = Window("IQM Demo", 1280, 720)
	val camera = Camera(window.aspectRatio, 90f)
	val iqmShader = IqmShader()
	val sceneObject = IqmSceneObject(
		IqmLoader.loadIqm(File("data/mrfixit.iqm"))
	)
	val modelViewProjection = Matrix4f()

	while (!window.shouldClose()) {
		window.prepareFrame()

		camera.getProjectionView(modelViewProjection)
			.mul(sceneObject.getTransformation(), modelViewProjection)

		iqmShader.bind()
		iqmShader.updateModelViewProjection(modelViewProjection)
		sceneObject.draw()
		iqmShader.unbind()

		window.finishFrame()
	}

	window.destroy()
}
