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
import zernikalos.action.ZSkeletalAction
import zernikalos.context.ZContext
import zernikalos.events.touch.ZTouchEventType
import zernikalos.loader.ZKo
import zernikalos.loader.loadFromAssets
import zernikalos.logger.ZLogLevel
import zernikalos.math.ZVector3
import zernikalos.objects.ZCamera
import zernikalos.objects.ZModel
import zernikalos.objects.ZObject
import zernikalos.objects.ZScene
import zernikalos.scenestatehandler.ZSceneStateHandler
import zernikalos.search.findFirstModel
import zernikalos.ui.ZernikalosView
import dev.zernikalos.zernikalosdemoapp.EngineDemoControlsBar
import dev.zernikalos.zernikalosdemoapp.R

/**
 * Standalone demo: loads the Stormtrooper asset from the Collada-derived path, builds a minimal
 * scene (model + camera only), offsets the camera and rolls the mesh for a sensible default pose,
 * then drives skeletal actions from the spinner. Rotate buttons spin the root around Z for inspection.
 */
class StormtrooperSampleFragment : Fragment() {

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

        engine.settings.logLevel = ZLogLevel.DEBUG

        val handler = object : ZSceneStateHandler {
            override fun onReady(context: ZContext, done: () -> Unit) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val loaded: ZKo = runCatching {
                        withContext(Dispatchers.IO) {
                            loadFromAssets(
                                requireContext(),
                                "collada/stormtrooper/stormtrooper.zko",
                            )
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

                        // --- Minimal scene: no bundled lights here; only mesh hierarchy plus a fresh camera. ---
                        val root = loaded.root
                        val scene = ZScene()
                        val camera = ZCamera()
                        scene.addChild(root)
                        scene.addChild(camera)
                        val dragAxis = ZVector3.Up
                        val degreesPerPixel = 0.35f
                        root.events.addTouchListener { obj, event ->
                            if (event.pointerId != 0) return@addTouchListener
                            if (event.type != ZTouchEventType.MOVE) return@addTouchListener

                            val delta = event.deltaX * degreesPerPixel
                            obj.transform.rotateDegrees(delta, dragAxis)
                        }
                        context.activeCamera = camera
                        context.scene = scene

                        val mainObj = findFirstModel(scene)

                        // --- Push camera back and roll the mesh so the default view matches the asset orientation. ---
                        context.activeCamera?.transform?.rotateDegrees(180f, 0f, 0f, 1f)
                        context.activeCamera?.transform?.rotateDegrees(180f, 0f, 1f, 0f)
                        context.activeCamera?.transform?.translate(1f, 1f, -10f)

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

        // Stormtrooper demo uses roll (Z) for inspection instead of yaw (Y).
        demoControls.setOnRotateNegativeClick { rotateRootZ(-2f) }
        demoControls.setOnRotatePositiveClick { rotateRootZ(2f) }
    }

    private fun rotateRootZ(degrees: Float) {
        val root = loadedRoot ?: return
        root.transform.rotateDegrees(degrees, 0f, 0f, 1f)
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
