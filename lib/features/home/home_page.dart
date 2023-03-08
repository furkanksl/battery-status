// ignore_for_file: library_private_types_in_public_api, must_be_immutable

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';

import 'state/battery_state.dart';

class HomePage extends StatelessWidget {
  HomePage({super.key});

  final BatteryLevelState batteryLevelState = Get.put(BatteryLevelState());

  MethodChannel methodChannel = const MethodChannel('battery-status');
  EventChannel eventChannel = const EventChannel('battery-stream-status');

  Future<void> _getBatteryLevel() async {
    String batteryLevel = '';

    try {
      final int result = await methodChannel.invokeMethod('getBatteryLevel');
      batteryLevel = 'Battery level: $result%';
    } on PlatformException {
      batteryLevel = 'Failed to get battery level.';
    }

    batteryLevelState.setBatteryLevel(batteryLevel);
  }

  void _onEvent(Object event) {
    batteryLevelState.setBatteryLevelLive(event as int);
    if (event.runtimeType == int && event < 25) {
      Get.snackbar(
        'Battery',
        'Hey, battery is under 25%',
        colorText: Colors.white,
        icon: const Icon(
          Icons.warning_amber_rounded,
          size: 40,
          color: Colors.red,
        ),
      );
    }
  }

  void _onError(Object error) => batteryLevelState.setBatteryLevelLive('unknown');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Savvy Navvy'),
        backgroundColor: const Color(0xff21295c),
      ),
      body: GetBuilder(
        init: BatteryLevelState(),
        initState: (state) {
          eventChannel.receiveBroadcastStream().listen((e) => _onEvent(e), onError: _onError);
        },
        builder: (_) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: <Widget>[
                Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    Text(batteryLevelState.batteryLevel),
                    Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: TextButton(
                        style: ButtonStyle(backgroundColor: MaterialStateProperty.all(const Color(0xff21295c))),
                        onPressed: _getBatteryLevel,
                        child: const Text(
                          'Check Battery Level',
                          style: TextStyle(
                            color: Colors.white,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
                Text(batteryLevelState.batteryLevelLive),
              ],
            ),
          );
        },
      ),
    );
  }
}
