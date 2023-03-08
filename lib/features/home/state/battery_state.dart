import 'package:get/get.dart';

class BatteryLevelState extends GetxController {
  String batteryLevel = "";
  String batteryLevelLive = "";
  List<dynamic> searchList = [];

  setBatteryLevel(String newLevel) {
    batteryLevel = newLevel;
    update();
  }

  setBatteryLevelLive(dynamic newLevel) {
    if (newLevel.runtimeType == int) {
      batteryLevelLive = "Live battery level: $newLevel%";
    } else {
      batteryLevelLive = "Live battery level: unknown";
    }

    update();
  }
}
