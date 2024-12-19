package com.amastsales.uhf_r2_plugin

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.amastsales.uhf_r2_plugin.UhfR2Plugin
import com.amastsales.uhf_r2_plugin.helper.UhfR2Listener
import com.amastsales.uhf_r2_plugin.helper.Uhfr2Helper
import com.amastsales.uhf_r2_plugin.helpers.MyDevice
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener

import io.reactivex.subjects.PublishSubject;
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.ArrayList

/** UhfR2Plugin */
//class UhfR2Plugin: FlutterPlugin, MethodCallHandler, ActivityAware,  RequestPermissionsResultListener, EventChannel.StreamHandler {
class UhfR2Plugin: FlutterPlugin, MethodCallHandler, ActivityAware,  RequestPermissionsResultListener {

  private var connectedStatus : PublishSubject<Boolean> = PublishSubject.create()
  private var tagsStatus : PublishSubject<String> = PublishSubject.create()

  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  private var activity: Activity? = null
  private var eventSink:  EventChannel.EventSink? = null

  var isScanning: Boolean = false
  var isKeyDownUp: Boolean = false

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "uhf_r2_plugin")
    context = flutterPluginBinding.applicationContext
    channel.setMethodCallHandler(this)

//    Uhfr2Helper().getInstance().init(context)

    val uhfR2Listener = object : UhfR2Listener {
      override fun onRead(tagsJson: String) {
        tagsJson?.let { tagsStatus.onNext(it) }
      }

      override fun onConnect(isConnected: Boolean, powerLevel: Int) {
        connectedStatus.onNext(isConnected)
      }
    }

    Uhfr2Helper().getInstance().setUhfListener(uhfR2Listener)

    val tagThreadEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, CHANNEL_TagThreadEvent)
    tagThreadEventChannel.setStreamHandler(EventChannelHandler(this, CHANNEL_TagThreadEvent))

    val isTaggingEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, CHANNEL_IsTaggingEvent)
    isTaggingEventChannel.setStreamHandler(EventChannelHandler(this, CHANNEL_IsTaggingEvent))

  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    handleMethods(call, result)
  }

  private fun handleMethods(call: MethodCall, result: Result) = GlobalScope.async {
    when (call.method) {
      CHANNEL_GetPlatformVersion -> result.success("Android " + Build.VERSION.RELEASE)

//      CHANNEL_IsStarted -> result.success(UHFHelper.getInstance().isStarted())
//      CHANNEL_StartSingle -> result.success(UHFHelper.getInstance().start(true))
//      CHANNEL_StartContinuous -> result.success(UHFHelper.getInstance().start(false))
//      CHANNEL_Stop -> result.success(UHFHelper.getInstance().stop())

      CHANNEL_ClearData -> {
          try {
              Uhfr2Helper().getInstance().clearData()
              result.success(true)
          } catch (error: Exception) {
              result.error("CHANNEL_ClearData :: ", "Error: ", error)
          }
      }

      CHANNEL_TagSingle -> {
        try {
          val mutableTagList: MutableList<HashMap<String, String>> = Uhfr2Helper().getInstance().tagSingle()

          result.success(mutableTagList.toList().toString())
        } catch (error: Exception) {
          result.error("CHANNEL_TagSingle :: ", "Error: ", error)
        }
      }

      CHANNEL_TagThread -> {
        try {
          val mutableTagList: MutableList<HashMap<String, String>> = Uhfr2Helper().getInstance().tagThread(this@UhfR2Plugin)

          result.success(mutableTagList.toList().toString())
        } catch (error: Exception) {
          result.error("CHANNEL_TagThread :: ", "Error: ", error)
        }
      }

      CHANNEL_StartScan -> {
        try {
          val deviceList: List<MyDevice> = async()
            { Uhfr2Helper().getInstance().startScan(context, activity!!) }.await().toList()

          result.success(deviceList.toString())

        } catch (error: Exception) {

          result.error("CHANNEL_StartScan :: ", "Error: ", error.toString())
          Log.d("error", error.toString())

        }
      }

      CHANNEL_StopScan -> {
        Uhfr2Helper().getInstance().stopScan()
        result.success(true)
      }

      CHANNEL_Connect -> {
          try {

            val response: Int = async()
            {
              Uhfr2Helper().getInstance()
                .connect(context, call.argument<String>("deviceAddress").toString())
            }.await()

            Uhfr2Helper().getInstance().tagThread(this@UhfR2Plugin)
//            val tagThreadEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "tagThreadEvent"); // timeHandlerEvent event name
//            tagThreadEventChannel.setStreamHandler(this@UhfR2Plugin)

            result.success(response)

          } catch (e: Exception) {

            result.error("CHANNEL_Connect :: ", "Error: ", e.toString())
            Log.d("Error:", e.toString())

          }
      }

      CHANNEL_Disconnect -> {
          try {
              Uhfr2Helper().getInstance().disconnect()
              result.success(true)
          } catch (e: Exception) {
              result.error("CHANNEL_Disconnect", "Error: ", e.toString())
          }
      }

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


      CHANNEL_TestConnect -> result.success(Uhfr2Helper().getInstance().init(context))
      CHANNEL_IsConnected -> result.success(Uhfr2Helper().getInstance().isConnected())
      CHANNEL_GetConnectionStatus -> {
          try {
            val res: String = Uhfr2Helper().getInstance().getConnectionStatus()

            result.success(res)
          } catch (e: Exception) {

            result.error("CHANNEL_GetConnectionStatus :: ", "Error: ", e.toString())

          }

      }

      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String?>,
    grantResults: IntArray
  ): Boolean {
//    TODO("Not yet implemented")
    return grantResults[0] == PackageManager.PERMISSION_GRANTED
  }

  fun getContext() : UhfR2Plugin{
    return this
  }

  // ** Event Channel Handler**
  class EventChannelHandler(private var context: UhfR2Plugin, private var eventChannel: String) : EventChannel.StreamHandler{

    private var eventSink: EventChannel.EventSink? = null

    override fun onListen(
      arguments: Any?,
      events: EventChannel.EventSink?
    ) {
      eventSink = events

      try {
        when (eventChannel) {
          CHANNEL_TagThreadEvent -> {
            val mutableTagList: MutableList<HashMap<String, String>> = Uhfr2Helper().getInstance().tagThread(context)

            eventSink?.success(mutableTagList.toList().toString())
          }

          CHANNEL_IsTaggingEvent -> {
            val isTaggingResponse: Boolean = Uhfr2Helper().getInstance().isTaggingThread()

            eventSink?.success(isTaggingResponse)
          }
        }

      } catch (error: Exception) {
//      eventSink?.error("EVENT_TagThread :: ", "Error: ", error)
        Log.d(eventChannel, error.toString())
      }
    }

    override fun onCancel(arguments: Any?) {
      eventSink = null
    }
  }

  companion object {
    private const val CHANNEL_GetPlatformVersion : String = "getPlatformVersion"

    private const val CHANNEL_StartScan : String = "startScan"
    private const val CHANNEL_StopScan : String = "stopScan"
    private const val CHANNEL_Connect : String = "connect"
    private const val CHANNEL_Disconnect : String = "disconnect"

    private const val CHANNEL_TagSingle : String = "tagSingle"
    private const val CHANNEL_TagThread : String = "tagThread"
    private const val CHANNEL_ClearData : String = "clearData"

    // debugging purposes
    private const val CHANNEL_TestConnect : String = "testConnect"
    private const val CHANNEL_GetConnectionStatus : String = "getConnectionStatus"
    private const val CHANNEL_IsConnected : String = "isConnected"

    // event Channel
    private const val CHANNEL_TagThreadEvent : String = "tagThreadEvent"
    private const val CHANNEL_IsTaggingEvent : String = "isTaggingEvent"
  }
}
