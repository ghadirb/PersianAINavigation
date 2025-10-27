package ir.navigation.persian.ai.data

import ir.navigation.persian.ai.model.SpeedCamera
import ir.navigation.persian.ai.model.CameraType

object CameraData {
    fun getTehranCameras() = listOf(
        // اتوبان همت
        SpeedCamera("c1", 35.7580, 51.4089, CameraType.FIXED_CAMERA, 110, 90.0, true),
        SpeedCamera("c2", 35.7520, 51.4150, CameraType.AVERAGE_SPEED_CAMERA, 110, 90.0, true),
        // آزادگان
        SpeedCamera("c3", 35.6892, 51.3890, CameraType.FIXED_CAMERA, 100, 180.0, true),
        SpeedCamera("c4", 35.6850, 51.3950, CameraType.FIXED_CAMERA, 100, 0.0, true),
        // حکیم
        SpeedCamera("c5", 35.7100, 51.3500, CameraType.FIXED_CAMERA, 90, 270.0, true),
        // یادگار امام
        SpeedCamera("c6", 35.6500, 51.4200, CameraType.AVERAGE_SPEED_CAMERA, 110, 45.0, true),
        // رسالت
        SpeedCamera("c7", 35.7300, 51.4800, CameraType.FIXED_CAMERA, 100, 90.0, true),
        // سرعت‌گیرها
        SpeedCamera("b1", 35.7000, 51.4000, CameraType.SPEED_BUMP, 40, null, true),
        SpeedCamera("b2", 35.6800, 51.3800, CameraType.SPEED_BUMP, 40, null, true),
        SpeedCamera("b3", 35.7200, 51.4200, CameraType.SPEED_BUMP, 40, null, true),
        // میدان آزادی
        SpeedCamera("c8", 35.6997, 51.3380, CameraType.TRAFFIC_LIGHT, 50, null, true),
        // تهران-کرج
        SpeedCamera("c9", 35.7800, 51.0500, CameraType.AVERAGE_SPEED_CAMERA, 120, 270.0, true),
        // چمران
        SpeedCamera("c10", 35.7600, 51.4200, CameraType.FIXED_CAMERA, 90, 0.0, true)
    )
}
