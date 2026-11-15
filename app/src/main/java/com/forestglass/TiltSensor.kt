package com.forestglass.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface

class TiltSensor(
    context: Context,
    private val onTiltChanged: (Float, Float) -> Unit
) : SensorEventListener {

    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val displayManager = appContext.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    private val filter = TiltFilter()
    private val rotMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var lastTimestampNs = 0L

    fun start() {
        val sm = sensorManager ?: return
        val sensor = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: return

        filter.reset()
        lastTimestampNs = 0L
        runCatching {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        runCatching { sensorManager?.unregisterListener(this) }
        lastTimestampNs = 0L
    }

    override fun onSensorChanged(event: SensorEvent) {
        runCatching {
            val dt = if (lastTimestampNs == 0L) {
                1f / 60f
            } else {
                ((event.timestamp - lastTimestampNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            }
            lastTimestampNs = event.timestamp

            var rawX = 0f
            var rawY = 0f

            when (event.sensor.type) {
                Sensor.TYPE_GAME_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR -> {
                    SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                    val rotation = currentDisplayRotation()
                    val (axisX, axisY) = when (rotation) {
                        Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                        Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                        Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                        else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                    }
                    SensorManager.remapCoordinateSystem(rotMatrix, axisX, axisY, remappedMatrix)
                    SensorManager.getOrientation(remappedMatrix, orientation)
                    rawX = orientation[2]
                    rawY = orientation[1]
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    rawX = (-event.values[0] / 9.81f).coerceIn(-1f, 1f) * 0.5f
                    rawY = (event.values[1] / 9.81f).coerceIn(-1f, 1f) * 0.5f
                }
            }

            val state = filter.step(rawX, rawY, dt)
            onTiltChanged(state.x, state.y)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun currentDisplayRotation(): Int = runCatching {
        displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
    }.getOrDefault(Surface.ROTATION_0)
}
