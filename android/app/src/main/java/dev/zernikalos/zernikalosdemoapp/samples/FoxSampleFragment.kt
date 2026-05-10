package dev.zernikalos.zernikalosdemoapp.samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zernikalos.Zernikalos
import zernikalos.action.ZActionPlayer
import zernikalos.components.light.ZDirectionalLamp
import zernikalos.context.ZContext
import zernikalos.events.touch.ZTouchEventType
import zernikalos.loader.ZKo
import zernikalos.loader.loadFromAssets
import zernikalos.logger.ZLogLevel
import zernikalos.math.ZVector3
import zernikalos.action.ZSkeletalAction
import zernikalos.objects.ZCamera
import zernikalos.objects.ZLight
import zernikalos.objects.ZModel
import zernikalos.objects.ZObject
import zernikalos.objects.ZScene
import zernikalos.scenestatehandler.ZSceneStateHandler
import zernikalos.search.findFirstModel
import zernikalos.ui.ZernikalosView
import dev.zernikalos.zernikalosdemoapp.EngineDemoControlsBar
import dev.zernikalos.zernikalosdemoapp.R

/**
 * Standalone demo: loads the Fox skeletal model from app assets, builds a custom scene with ambient
 * plus directional light, exposes bundled skeletal actions in the spinner, and nudges the root
 * around Y with the toolbar buttons for a quick orbit preview.
 */
class FoxSampleFragment : Fragment() {

    private val engine = Zernikalos()
    private val actionPlayer = ZActionPlayer()

    private var loadedRoot: ZObject? = null

    private lateinit var zView: ZernikalosView
    private lateinit var demoControls: EngineDemoControlsBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_sample, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        zView = view.findViewById(R.id.zernikalos_view)
        demoControls = view.findViewById(R.id.demo_controls)

        // Verbose engine logs while wiring a new screen; turn down for production builds.
        engine.settings.logLevel = ZLogLevel.DEBUG

        val handler = object : ZSceneStateHandler {
            override fun onReady(context: ZContext, done: () -> Unit) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val loaded: ZKo = runCatching {
                        withContext(Dispatchers.IO) {
                            loadFromAssets(requireContext(), "gltf/Fox.zko")
                        }
                    }.getOrElse { e ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.zko_load_failed, e.message ?: e.toString()),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                        done()
                        return@launch
                    }

                    withContext(Dispatchers.Main) {
                        loadedRoot = loaded.root

                        // --- Scene graph: root model, lights, and camera are siblings under one ZScene. ---
                        val root = loaded.root
                        val scene = ZScene()
                        val camera = ZCamera()
                        val ambientLight = ZLight.createAmbientLight().apply { intensity = 0.1f }
                        val light = ZLight().apply { lamp = ZDirectionalLamp() }
                        scene.addChild(root)
                        scene.addChild(ambientLight)
                        scene.addChild(light)
                        scene.addChild(camera)
                        // Tuned for touch deltas expressed in pixels.
                        val degreesPerPixel = 0.15f
                        val worldUp = ZVector3.Up
                        val center = ZVector3.Zero

                        root.events.addTouchListener { obj, event ->
                            if (event.pointerId != 0) return@addTouchListener
                            if (event.type != ZTouchEventType.MOVE) return@addTouchListener

                            val camTransform = context.activeCamera?.transform ?: return@addTouchListener

                            // Simple 3D viewer orbit: move camera position around world origin.
                            // Clamp occasional spikes (can happen with event batching / history).
                            val clampedDx = event.deltaX.coerceIn(-80f, 80f)
                            val clampedDy = event.deltaY.coerceIn(-80f, 80f)

                            val yaw = clampedDx * degreesPerPixel
                            val pitch = clampedDy * degreesPerPixel

                            // 1) Yaw around fixed world Up.
                            //camTransform.rotateAroundWorld(yaw, center, worldUp)

                            // 2) Pitch around camera right (world-space direction).
                            // Note: right is already a world direction; we use it as a world axis.
                            val rightAxis = ZVector3()
                            //rightAxis.copy(camTransform.right)
                            //camTransform.rotateAroundWorld(pitch, center, rightAxis)

                            // Keep camera oriented toward center.
                            //camTransform.lookAt(center, worldUp)
                        }
                        context.activeCamera = camera
                        context.scene = scene

                        // --- Framing: pick the first skinned mesh, scale it, then aim the camera. ---
                        val mainObj = findFirstModel(scene)
                        mainObj?.transform?.scale(0.1f)
                        context.activeCamera?.transform?.translate(1f, -5f, -30f)

                        val actions = loaded.actions.orEmpty()
                        demoControls.bindSkeletalActions(mainObj, actions, ::playSkeletalClip)
                        val skinned = mainObj
                        if (skinned != null && actions.isNotEmpty()) {
                            playSkeletalClip(skinned, actions[0])
                        }
                        done()
                    }
                }
            }

            override fun onUpdate(context: ZContext, done: () -> Unit) {
                actionPlayer.update()
                done()
            }

            override fun onRender(context: ZContext, done: () -> Unit) {
                done()
            }
        }

        engine.initialize(zView, handler)

        // Toolbar rotates the loaded root around world Y for a small orbit each tap.
        demoControls.setOnRotateNegativeClick { rotateRootY(-2f) }
        demoControls.setOnRotatePositiveClick { rotateRootY(2f) }
    }

    private fun rotateRootY(degrees: Float) {
        val root = loadedRoot ?: return
        root.transform.rotate(degrees, 0f, 1f, 0f)
    }

    /** Stops the current clip, binds [action] to [model]'s skeleton, then starts playback. */
    private fun playSkeletalClip(model: ZModel, action: ZSkeletalAction) {
        actionPlayer.stop()
        actionPlayer.setAction(model.skeleton!!, action)
        actionPlayer.play(true)
    }

    override fun onDestroyView() {
        engine.dispose()
        super.onDestroyView()
    }
}
