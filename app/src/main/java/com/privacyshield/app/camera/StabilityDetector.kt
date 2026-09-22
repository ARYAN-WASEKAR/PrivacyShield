package com.privacyshield.app.camera

import com.privacyshield.app.sensors.MotionSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StabilityDetector(
    private val motionSensor: MotionSensor,
    private val scope: CoroutineScope
) {

    data class StabilityState(
        val isStable: Boolean = false,
        val stabilityScore: Float = 0f, // 0.0 (shaking) to 1.0 (rock solid)
        val statusText: String = "Hold steady…"
    )

    private val _state = MutableStateFlow(StabilityState())
    val state: StateFlow<StabilityState> = _state.asStateFlow()

    private val jitterThreshold = 0.45f

    init {
        scope.launch {
            motionSensor.motionJitter.collectLatest { jitter ->
                val stability = (1.0f - (jitter / 1.2f)).coerceIn(0f, 1f)
                val isStable = jitter < jitterThreshold
                val text = if (isStable) "Ready" else "Hold steady…"

                _state.value = StabilityState(
                    isStable = isStable,
                    stabilityScore = stability,
                    statusText = text
                )
            }
        }
    }
}
