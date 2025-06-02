package com.example.myapplication

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs

class DraggableFloatingActionButton
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FloatingActionButton(context, attrs, defStyleAttr), View.OnTouchListener {

    private var dX = 0f
    private var dY = 0f
    private var isDragging = false
    private var startClickTime = 0L
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialX = 0f
    private var initialY = 0f
    private var hasMovedEnough = false

    // Listener for position changes
    var onPositionChangedListener: ((x: Float, y: Float) -> Unit)? = null

    init {
        setOnTouchListener(this)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = false
                hasMovedEnough = false
                startClickTime = System.currentTimeMillis()
                dX = view.x - event.rawX
                dY = view.y - event.rawY
                initialX = event.rawX
                initialY = event.rawY                // Start long press detection
                postDelayed(
                        {
                            if (!hasMovedEnough && !isDragging) {
                                isDragging = true
                                performHapticFeedback(View.HAPTIC_FEEDBACK_ENABLED)
                                alpha = 0.7f
                                scaleX = 1.1f
                                scaleY = 1.1f
                                elevation = elevation * 2
                            }
                        },
                        longPressTimeout
                )

                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(event.rawX - initialX)
                val deltaY = abs(event.rawY - initialY)

                if (deltaX > touchSlop || deltaY > touchSlop) {
                    hasMovedEnough = true
                }

                if (isDragging ||
                                (hasMovedEnough &&
                                        System.currentTimeMillis() - startClickTime >
                                                longPressTimeout)
                ) {                    if (!isDragging) {
                        isDragging = true
                        performHapticFeedback(View.HAPTIC_FEEDBACK_ENABLED)
                        alpha = 0.7f
                        scaleX = 1.1f
                        scaleY = 1.1f
                        elevation = elevation * 2
                    }

                    val newX = event.rawX + dX
                    val newY = event.rawY + dY
                    // Get parent bounds
                    val parentView = parent as? View
                    if (parentView != null) {
                        val maxX = parentView.width - width.toFloat()
                        val maxY = parentView.height - height.toFloat()

                        // Constrain to parent bounds with some margin
                        val margin = 16f
                        val constrainedX = newX.coerceIn(margin, maxX - margin)
                        val constrainedY = newY.coerceIn(margin, maxY - margin)

                        view.x = constrainedX
                        view.y = constrainedY
                    }

                    return true
                }

                return false
            }
            MotionEvent.ACTION_UP -> {
                val clickDuration = System.currentTimeMillis() - startClickTime

                // Reset visual state
                animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(200).start()

                if (isDragging) {
                    // Save position and snap to edge
                    onPositionChangedListener?.invoke(view.x, view.y)
                    snapToNearestEdge()

                    // Reset dragging state after animation
                    postDelayed({ isDragging = false }, 300)

                    return true
                } else if (!hasMovedEnough && clickDuration < longPressTimeout) {
                    // This was a quick tap, trigger click
                    performClick()
                    return true
                }

                isDragging = false
                return false
            }
            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                hasMovedEnough = false
                alpha = 1.0f
                scaleX = 1.0f
                scaleY = 1.0f
                return false
            }
        }

        return false
    }
    private fun snapToNearestEdge() {
        val parentView = parent as? View ?: return
        val parentWidth = parentView.width
        val margin = 16f

        // Determine which edge is closer
        val centerX = x + width / 2
        val snapToRight = centerX > parentWidth / 2

        val targetX =
                if (snapToRight) {
                    parentWidth - width - margin
                } else {
                    margin
                }

        // Animate to edge
        animate()
                .x(targetX)
                .setDuration(300)
                .withEndAction { onPositionChangedListener?.invoke(x, y) }
                .start()
    }

    fun setPosition(x: Float, y: Float) {
        post {
            this.x = x
            this.y = y
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
