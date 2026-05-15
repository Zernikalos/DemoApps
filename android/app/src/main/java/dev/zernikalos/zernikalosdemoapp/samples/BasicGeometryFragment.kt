package dev.zernikalos.zernikalosdemoapp.samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import zernikalos.Zernikalos
import zernikalos.components.light.ZDirectionalLamp
import zernikalos.components.material.ZMaterial
import zernikalos.components.material.ZPhongMaterialData
import zernikalos.context.ZContext
import zernikalos.events.touch.ZTouchEventType
import zernikalos.geometries.ZCube
import zernikalos.geometries.ZPlane
import zernikalos.logger.ZLogLevel
import zernikalos.math.ZColor
import zernikalos.math.ZVector3
import zernikalos.objects.ZCamera
import zernikalos.objects.ZGroup
import zernikalos.objects.ZLight
import zernikalos.objects.ZScene
import zernikalos.scenestatehandler.ZSceneStateHandler
import zernikalos.ui.ZernikalosView
import dev.zernikalos.zernikalosdemoapp.R

/**
 * Minimal scene: a large ground plane (XZ) and a unit cube resting on it, Phong materials, orbit by drag.
 */
class BasicGeometryFragment : Fragment() {

    private val engine = Zernikalos()

    private lateinit var zView: ZernikalosView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_basic_geometry, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        zView = view.findViewById(R.id.zernikalos_view)
        engine.settings.logLevel = ZLogLevel.DEBUG

        val handler = object : ZSceneStateHandler {
            override fun onReady(context: ZContext, done: () -> Unit) {
                val scene = ZScene()
                // Clear color lives on the scene viewport (engine default: dark grey in [ZViewportData]).
                scene.viewport.clearColor = ZColor(0.06f, 0.07f, 0.12f, 1f)

                val camera = ZCamera()
                val ambient = ZLight.createAmbientLight().apply { intensity = 0.25f }
                val sun = ZLight().apply {
                    lamp = ZDirectionalLamp()
                    intensity = 0.9f
                }

                val plane = ZPlane(3f).apply {
                    name = "Ground"
                    material = phongGroundMaterial()
                }

                val cube = ZCube(0.5f).apply {
                    name = "Cube"
                    material = phongCubeMaterial()
                    transform.translate(0f, 0.5f, 0f)
                }

                val content = ZGroup().apply {
                    name = "BasicGeometryRoot"
                    addChild(plane)
                    addChild(cube)
                }

                scene.addChild(content)
                scene.addChild(ambient)
                scene.addChild(sun)
                scene.addChild(camera)

                val degreesPerPixel = 0.15f
                val worldUp = ZVector3.Up
                val center = ZVector3(0f, 0.35f, 0f)

                content.events.addTouchListener { _, event ->
                    if (event.pointerId != 0) return@addTouchListener
                    if (event.type != ZTouchEventType.MOVE) return@addTouchListener

                    val camTransform = context.activeCamera?.transform ?: return@addTouchListener
                    val clampedDx = event.deltaX.coerceIn(-80f, 80f)
                    val clampedDy = event.deltaY.coerceIn(-80f, 80f)
                    val yaw = clampedDx * degreesPerPixel
                    val pitch = clampedDy * degreesPerPixel

                    camTransform.rotateAroundWorldDegrees(yaw, center, worldUp)
                    camTransform.rotateAroundWorldDegrees(pitch, center, ZVector3.Right)
                    //camTransform.lookAt(center, worldUp)
                }

                context.activeCamera = camera
                context.scene = scene

                camera.transform.translate(0f, -2f, -7.5f)
                //camera.transform.lookAt(center, worldUp)

                done()
            }

            override fun onUpdate(context: ZContext, done: () -> Unit) {
                done()
            }

            override fun onRender(context: ZContext, done: () -> Unit) {
                done()
            }
        }

        engine.initialize(zView, handler)
    }

    override fun onDestroyView() {
        engine.dispose()
        super.onDestroyView()
    }

    /** Ground: warm tan so it cannot be mistaken for the clear color or a flat mid-grey. */
    private fun phongGroundMaterial(): ZMaterial = ZMaterial().apply {
        phong = ZPhongMaterialData(
            diffuse = ZColor(0.62f, 0.52f, 0.38f),
            ambient = ZColor(0.2f, 0.16f, 0.12f),
            specular = ZColor(0.35f, 0.32f, 0.26f),
            shiny = 24f,
        )
    }

    /** Cube: saturated accent vs background and floor. */
    private fun phongCubeMaterial(): ZMaterial = ZMaterial().apply {
        phong = ZPhongMaterialData(
            diffuse = ZColor(0.15f, 0.72f, 0.55f),
            ambient = ZColor(0.04f, 0.18f, 0.14f),
            specular = ZColor(0.5f, 0.55f, 0.5f),
            shiny = 56f,
        )
    }
}
