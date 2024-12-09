import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:uhf_r2_plugin/extension.dart';

import 'uhf_r2_plugin_platform_interface.dart';

/// An implementation of [UhfR2PluginPlatform] that uses method channels.
class MethodChannelUhfR2Plugin extends UhfR2PluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('uhf_r2_plugin');

  static const eventChannel = EventChannel('tagThreadEvent');
  
  @override
  Stream<List<Map<String, dynamic>>?> streamTagThread() {

    return eventChannel
      .receiveBroadcastStream()
      .map((event) {
        
        debugPrint("streamTagThread event: ${event.toString()}");
        debugPrint("streamTagThread event MAP: ${event.toString().cleanFromTags().toString()}");

        return event.toString().cleanFromTags();
      });

  }

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> getConnectionStatus() async {
    final version = await methodChannel.invokeMethod<String>('getConnectionStatus');
    return version;
  }

  @override
  Future<bool?> testConnect() async {
    final result = await methodChannel.invokeMethod('testConnect');
    return result;
  }

  @override
  Future<bool?> isConnected() async {
    final result = await methodChannel.invokeMethod('isConnected');
    return result;
  }

  @override
  Future<List<Map<String, dynamic>>?> startScan() async {
    final String result = await methodChannel.invokeMethod('startScan');

      debugPrint("result in package: $result");
      debugPrint("result in package: ${result.cleanFromMyDevice().toString()}");

    return result.cleanFromMyDevice();
  }

  @override
  Future<bool?> stopScan() async {
    final result = await methodChannel.invokeMethod('stopScan');
    return result;
  }


  @override
  Future<void> disconnect() async {
    await methodChannel.invokeMethod('disconnect');
  }
  @override
  Future<void> clearData() async {
    await methodChannel.invokeMethod('clearData');
  }

  

  @override
  Future<Map<String, dynamic>?> connect({required String deviceAddress}) async {
    Map<String, dynamic> result = {};

    final response = await methodChannel.invokeMethod('connect',
      {
        "deviceAddress" : deviceAddress
      }
    );

    if (response is int) {
      switch (response) {
        case 1:
          result = {
            "connect": true,
            "message": "Connected to $deviceAddress"
          };
          break;
        case 2:
          result = {
            "connect": false,
            "message": "Invalid device to connect."
          };
          break;
        case 3:
          result = {
            "connect": false,
            "message": "Disconnected from $deviceAddress"
          };
          break;
        default:
          result = {
            "connect": false,
            "message": "Internal Error"
          };
      }
    } else {
      result = {
        "connect": false,
        "message": "Error: ${response.toString()}"
      };
    }

    return result;
  }

  @override
  Future<List<Map<String, dynamic>>?> tagSingle() async {
    final String result = await methodChannel.invokeMethod('tagSingle');

      debugPrint("tagSingle result: $result");
      debugPrint("tagSingle result MAP: ${result.cleanFromTags().toString()}");

    return result.cleanFromTags();
  }

  @override
  Future<List<Map<String, dynamic>>?> tagThread() async {
    final String result = await methodChannel.invokeMethod('tagThread');

      debugPrint("tagThread result: $result");
      debugPrint("tagThread result MAP: ${result.cleanFromTags().toString()}");

    return result.cleanFromTags();
  }
}
