package com.amastsales.uhf_r2_plugin

import android.content.Context
import android.os.Build
import com.amastsales.uhf_r2_plugin.helper.Uhfr2Helper
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** UhfR2Plugin */
class UhfR2Plugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "uhf_r2_plugin")
    context = flutterPluginBinding.applicationContext
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
//    if (call.method == "getPlatformVersion") {
//      result.success("Android ${android.os.Build.VERSION.RELEASE}")
//    } else {
//      handleMethods(call, result);
//    }
    handleMethods(call, result);
  }

  private fun handleMethods(call: MethodCall, result: Result) {
    when (call.method) {
      CHANNEL_GetPlatformVersion -> result.success("Android " + Build.VERSION.RELEASE)
//      CHANNEL_IsStarted -> result.success(UHFHelper.getInstance().isStarted())
//      CHANNEL_StartSingle -> result.success(UHFHelper.getInstance().start(true))
//      CHANNEL_StartContinuous -> result.success(UHFHelper.getInstance().start(false))
//      CHANNEL_Stop -> result.success(UHFHelper.getInstance().stop())
//      CHANNEL_ClearData -> {
//        UHFHelper.getInstance().clearData()
//        result.success(true)
//      }
//
//      CHANNEL_IsEmptyTags -> result.success(UHFHelper.getInstance().isEmptyTags())
//      CHANNEL_Close -> {
//        UHFHelper.getInstance().close()
//        result.success(true)
//      }
//      CHANNEL_SETPOWERLEVEL -> {
//        val powerLevel = call.argument<String>("value")
//        result.success(UHFHelper.getInstance().setPowerLevel(powerLevel))
//      }
//
//      CHANNEL_SETWORKAREA -> {
//        val workArea = call.argument<String>("value")
//        result.success(UHFHelper.getInstance().setWorkArea(workArea))
//      }

      CHANNEL_Connect -> result.success(Uhfr2Helper().getInstance().connect(context))

      CHANNEL_IsConnected -> result.success(Uhfr2Helper().getInstance().isConnected())

      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  companion object {
    private const val CHANNEL_GetPlatformVersion : String = "getPlatformVersion"
    private const val CHANNEL_Connect : String = "connect"
    private const val CHANNEL_IsConnected : String = "isConnected"
  }
}
