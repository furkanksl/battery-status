package com.example.battery_status

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodChannel

public class MainActivity : FlutterActivity() {
    private val BATTERY_CHANNEL = "battery-status"
    private val CHARGING_CHANNEL = "battery-stream-status"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BATTERY_CHANNEL)
                .setMethodCallHandler { call, result ->
                    if (call.method == "getBatteryLevel") {
                        val batteryLevel = getBatteryLevel()

                        if (batteryLevel != -1) {
                            result.success(batteryLevel)
                        } else {
                            result.error("UNAVAILABLE", "Battery level not available.", null)
                        }
                    } else {
                        result.notImplemented()
                    }
                }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, CHARGING_CHANNEL)
                .setStreamHandler(
                        object : EventChannel.StreamHandler {
                            private var chargingStateChangeReceiver: BroadcastReceiver? = null
                            override fun onListen(arguments: Any?, events: EventSink?) {
                                if (events !== null) {
                                    chargingStateChangeReceiver =
                                            createChargingStateChangeReceiver(events)
                                    registerReceiver(
                                            chargingStateChangeReceiver,
                                            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                                    )
                                }
                            }

                            override fun onCancel(arguments: Any?) {
                                unregisterReceiver(chargingStateChangeReceiver)
                                chargingStateChangeReceiver = null
                            }
                        }
                )

        // .setStreamHandler(
        //     object : EventChannel.StreamHandler {
        //         private var chargingStateChangeReceiver: BroadcastReceiver? = null
        //         override fun onListen(arguments: Any, events: EventSink) {
        //             chargingStateChangeReceiver =
        //                     createChargingStateChangeReceiver(events)
        //             registerReceiver(
        //                     chargingStateChangeReceiver,
        //                     IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        //             )
        //         }

        //         override fun onCancel(arguments: Any) {
        //             unregisterReceiver(chargingStateChangeReceiver)
        //             chargingStateChangeReceiver = null
        //         }
        //     }
        // )

    }

    private fun createChargingStateChangeReceiver(events: EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                // if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
                //     events.error("UNAVAILABLE", "Charging status unavailable", null)
                // } else {
                //     val isCharging =
                //             status == BatteryManager.BATTERY_STATUS_CHARGING ||
                //                     status == BatteryManager.BATTERY_STATUS_FULL
                //     events.success(if (isCharging) "charging" else "discharging")
                // }
                events.success(getBatteryLevel())
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryLevel: Int
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent =
                    ContextWrapper(applicationContext)
                            .registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryLevel =
                    intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 /
                            intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }

        return batteryLevel
    }
}
