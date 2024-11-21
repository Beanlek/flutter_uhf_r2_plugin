
import 'uhf_r2_plugin_platform_interface.dart';

class UhfR2Plugin {
  Future<String?> getPlatformVersion() {
    return UhfR2PluginPlatform.instance.getPlatformVersion();
  }
  Future<String?> getConnectionStatus() {
    return UhfR2PluginPlatform.instance.getConnectionStatus();
  }
  Future<bool?> testConnect() {
    return UhfR2PluginPlatform.instance.testConnect();
  }
  Future<bool?> isConnected() {
    return UhfR2PluginPlatform.instance.isConnected();
  }
  Future<List<Map<String, dynamic>>?> startScan() {
    return UhfR2PluginPlatform.instance.startScan();
  }
  Future<bool?> stopScan() {
    return UhfR2PluginPlatform.instance.stopScan();
  }
  Future<void> disconnect() {
    return UhfR2PluginPlatform.instance.disconnect();
  }
  Future<void> clear() {
    return UhfR2PluginPlatform.instance.clear();
  }
  
  Future<Map<String, dynamic>?> connect({required String deviceAddress}) {
    return UhfR2PluginPlatform.instance.connect(deviceAddress: deviceAddress);
  }
  Future<List<Map<String, dynamic>>?> tagSingle() {
    return UhfR2PluginPlatform.instance.tagSingle();
  }
}
