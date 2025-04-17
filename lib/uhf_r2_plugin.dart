
import 'uhf_r2_plugin_platform_interface.dart';

class UhfR2Plugin {
  Stream<List<Map<String, dynamic>>?> streamTagThread() {
    return UhfR2PluginPlatform.instance.streamTagThread();
  }
  Stream<bool?> streamIsTagging() {
    return UhfR2PluginPlatform.instance.streamIsTagging();
  }
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
  Future<void> clearData() {
    return UhfR2PluginPlatform.instance.clearData();
  }

  Future<bool?> setScanMode({required String scanMode}) {
    return UhfR2PluginPlatform.instance.setScanMode(scanMode: scanMode);
  }
  Future<bool?> setScanPower({required int scanPower}) {
    return UhfR2PluginPlatform.instance.setScanPower(scanPower: scanPower);
  }
  
  Future<Map<String, dynamic>?> connect({required String deviceAddress, required String deviceName}) {
    return UhfR2PluginPlatform.instance.connect(deviceAddress: deviceAddress, deviceName: deviceName);
  }
  Future<List<Map<String, dynamic>>?> tagSingle() {
    return UhfR2PluginPlatform.instance.tagSingle();
  }
  Future<List<Map<String, dynamic>>?> tagThread() {
    return UhfR2PluginPlatform.instance.tagThread();
  }
}
