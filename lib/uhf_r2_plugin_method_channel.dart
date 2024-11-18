import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:uhf_r2_plugin/extension.dart';

import 'uhf_r2_plugin_platform_interface.dart';

/// An implementation of [UhfR2PluginPlatform] that uses method channels.
class MethodChannelUhfR2Plugin extends UhfR2PluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('uhf_r2_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
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
}
