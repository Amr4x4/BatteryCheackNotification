package com.example.batterycheck

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.batterycheck.ui.theme.BatteryCheckTheme

class MainActivity : ComponentActivity() {
    private lateinit var batteryLevelReceiver: BatteryLevelReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BatteryCheckTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BatteryStatusApp()
                }
            }
        }

        // Create notification channel
        createNotificationChannel(this)

        // Initialize and register the receiver
        batteryLevelReceiver = BatteryLevelReceiver { level ->
            if (level <= 5) {
                sendNotification(
                    this,
                    "Critical Battery Level",
                    "Battery level is critically low!"
                )
            } else if (level <= 20) {
                sendNotification(this, "Low Battery", "Battery level is low.")
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryLevelReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryLevelReceiver)
    }
}

private fun createNotificationChannel(context: Context) {
    val name = "Battery Notification"
    val descriptionText = "Notifications for battery status"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel("battery_channel", name, importance).apply {
        description = descriptionText
    }
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}


@Composable
fun BatteryStatusApp() {
    var batteryLevel by remember { mutableFloatStateOf(100f) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val receiver = BatteryLevelReceiver { level ->
            batteryLevel = level
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
    }

    Image(
        painter = painterResource(
            id = if (batteryLevel <= 20) R.drawable.battery_low else R.drawable.battery_full
        ),
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}

private fun sendNotification(context: Context, title: String, message: String) {
    val notification = NotificationCompat.Builder(context, "battery_channel")
        .setSmallIcon(R.drawable.ic_battery_alert) // Ensure you have an icon with this name
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
    NotificationManagerCompat.from(context).notify(1, notification)
}
