import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'uhf_r2_plugin_method_channel.dart';

abstract class UhfR2PluginPlatform extends PlatformInterface {
  /// Constructs a UhfR2PluginPlatform.
  UhfR2PluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static UhfR2PluginPlatform _instance = MethodChannelUhfR2Plugin();

  /// The default instance of [UhfR2PluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelUhfR2Plugin].
  static UhfR2PluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [UhfR2PluginPlatform] when
  /// they register themselves.
  static set instance(UhfR2PluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
  Future<String?> getConnectionStatus() {
    throw UnimplementedError('getConnectionStatus() has not been implemented.');
  }
  Future<bool?> testConnect() {
    throw UnimplementedError('testConnect() has not been implemented.');
  }
  Future<bool?> isConnected() {
    throw UnimplementedError('isConnected() has not been implemented.');
  }
  Future<List<Map<String, dynamic>>?> startScan() {
    throw UnimplementedError('startScan() has not been implemented.');
  }
  Future<bool?> stopScan() {
    throw UnimplementedError('stopScan() has not been implemented.');
  }
  Future<void> disconnect() {
    throw UnimplementedError('disconnect() has not been implemented.');
  }
  Future<void> clearData() {
    throw UnimplementedError('clearData() has not been implemented.');
  }

  Future<Map<String, dynamic>?> connect({required String deviceAddress}) {
    throw UnimplementedError('connect() has not been implemented.');
  }
  Future<List<Map<String, dynamic>>?> tagSingle() {
    throw UnimplementedError('tagSingle() has not been implemented.');
  }
}
