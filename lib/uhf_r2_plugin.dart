
import 'uhf_r2_plugin_platform_interface.dart';

class UhfR2Plugin {
  Future<String?> getPlatformVersion() {
    return UhfR2PluginPlatform.instance.getPlatformVersion();
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
}
