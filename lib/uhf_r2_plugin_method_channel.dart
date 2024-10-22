import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

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
  Future<bool?> connect() async {
    final result = await methodChannel.invokeMethod('connect');
    return result;
  }

  @override
  Future<bool?> isConnected() async {
    final result = await methodChannel.invokeMethod('isConnected');
    return result;
  }
}
