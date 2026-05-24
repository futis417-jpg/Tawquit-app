package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import kotlin.math.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QiblaScreen(isSpanish: Boolean) {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }
    var targetQibla by remember { mutableFloatStateOf(100f) } // Default fallback

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val mecca = Location("Mecca")
                            mecca.latitude = 21.4225
                            mecca.longitude = 39.8262
                            targetQibla = location.bearingTo(mecca)
                            if (targetQibla < 0) targetQibla += 360f
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore for now, stick to fallback
            }
        }
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val _gravity = FloatArray(3)
        val _geomagnetic = FloatArray(3)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, _gravity, 0, event.values.size)
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, _geomagnetic, 0, event.values.size)
                }

                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, _gravity, _geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    val az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (az + 360) % 360
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    if (!locationPermissionsState.allPermissionsGranted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSpanish) "Se requiere ubicación para la Qibla" else "الموقع مطلوب للقبلة",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                Text(if (isSpanish) "Conceder Permisos" else "منح الأذونات")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSpanish) "QIBLA" else "القبلة",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = this.center
                val radius = size.minDimension / 2 - 20.dp.toPx()

                // Draw North
                rotate(degrees = -azimuth) {
                    drawCircle(
                        color = Color.Red,
                        radius = 8.dp.toPx(),
                        center = center.copy(y = center.y - radius)
                    )
                }
                
                // Draw Qibla (Kaaba)
                rotate(degrees = targetQibla - azimuth) {
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 12.dp.toPx(),
                        center = center.copy(y = center.y - radius)
                    )
                    drawLine(
                        color = Color(0xFF10B981),
                        start = center,
                        end = center.copy(y = center.y - radius),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            }
            
            Box(modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = if (isSpanish) "Alinea la línea verde con la parte superior de tu teléfono." else "قم بمحاذاة الخط الأخضر مع أعلى هاتفك.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
