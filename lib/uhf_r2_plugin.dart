
import 'uhf_r2_plugin_platform_interface.dart';

class UhfR2Plugin {
  Future<String?> getPlatformVersion() {
    return UhfR2PluginPlatform.instance.getPlatformVersion();
  }
  Future<bool?> connect() {
    return UhfR2PluginPlatform.instance.connect();
  }
  Future<bool?> isConnected() {
    return UhfR2PluginPlatform.instance.isConnected();
  }
}
