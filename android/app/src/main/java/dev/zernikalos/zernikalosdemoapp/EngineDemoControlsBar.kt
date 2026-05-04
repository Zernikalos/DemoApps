package dev.zernikalos.zernikalosdemoapp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import zernikalos.action.ZSkeletalAction
import zernikalos.objects.ZModel

/**
 * Shared bottom bar for engine samples: nudge rotation plus a skeletal action picker. Each demo
 * supplies rotation steps via [setOnRotateNegativeClick] / [setOnRotatePositiveClick]; while a
 * rotate button is held, that step is applied on a short interval so the user does not need
 * repeated taps. Skeletal playback is delegated via [bindSkeletalActions] callbacks so engine
 * wiring stays in the host screen.
 */
class EngineDemoControlsBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val btnRotateNeg: ImageButton
    private val btnRotatePos: ImageButton
    private val spinnerActions: Spinner

    private val handler = Handler(Looper.getMainLooper())
    private var rotateNegativeAction: (() -> Unit)? = null
    private var rotatePositiveAction: (() -> Unit)? = null

    private val repeatNegative = object : Runnable {
        override fun run() {
            rotateNegativeAction?.invoke()
            handler.postDelayed(this, REPEAT_INTERVAL_MS)
        }
    }

    private val repeatPositive = object : Runnable {
        override fun run() {
            rotatePositiveAction?.invoke()
            handler.postDelayed(this, REPEAT_INTERVAL_MS)
        }
    }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_engine_demo_controls, this, true)
        btnRotateNeg = findViewById(R.id.btn_rotate_neg)
        btnRotatePos = findViewById(R.id.btn_rotate_pos)
        spinnerActions = findViewById(R.id.spinner_actions)
    }

    fun setOnRotateNegativeClick(block: () -> Unit) {
        rotateNegativeAction = block
        btnRotateNeg.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    stopRepeating()
                    rotateNegativeAction?.invoke()
                    handler.postDelayed(repeatNegative, REPEAT_INTERVAL_MS)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(repeatNegative)
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun setOnRotatePositiveClick(block: () -> Unit) {
        rotatePositiveAction = block
        btnRotatePos.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    stopRepeating()
                    rotatePositiveAction?.invoke()
                    handler.postDelayed(repeatPositive, REPEAT_INTERVAL_MS)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(repeatPositive)
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun stopRepeating() {
        handler.removeCallbacks(repeatNegative)
        handler.removeCallbacks(repeatPositive)
    }

    override fun onDetachedFromWindow() {
        stopRepeating()
        super.onDetachedFromWindow()
    }

    /**
     * Populates the action spinner. On each selection, invokes [onSkeletalActionSelected] with the
     * skinned [ZModel] and chosen [ZSkeletalAction]. The host should run its usual clip switch
     * sequence (stop current clip, setAction on the model’s skeleton, play). Hides the spinner when there is no skinned
     * target or no actions; rotate buttons stay usable.
     */
    fun bindSkeletalActions(
        model: ZModel?,
        actions: List<ZSkeletalAction>,
        onSkeletalActionSelected: (ZModel, ZSkeletalAction) -> Unit,
    ) {
        if (actions.isEmpty() || model == null) {
            spinnerActions.visibility = View.GONE
            spinnerActions.onItemSelectedListener = null
            spinnerActions.adapter = null
            return
        }
        spinnerActions.visibility = View.VISIBLE
        val labels = actions.map { it.name }
        spinnerActions.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            labels,
        )
        spinnerActions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                onSkeletalActionSelected(model, actions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    companion object {
        private const val REPEAT_INTERVAL_MS = 40L
    }
}
