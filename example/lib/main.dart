import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:uhf_r2_plugin/uhf_r2_plugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _uhfR2Plugin = UhfR2Plugin();

  bool connect = false;
  bool isConnected = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await _uhfR2Plugin.getPlatformVersion() ?? 'Unknown platform version';

      connect = await _uhfR2Plugin.connect() ?? false;
      isConnected = await _uhfR2Plugin.isConnected() ?? false;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column( mainAxisAlignment: MainAxisAlignment.center, crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Padding( padding: const EdgeInsets.only(bottom: 8.0),
                child: Text('Running on: $_platformVersion\n'),
              ),
              Padding( padding: const EdgeInsets.only(bottom: 8.0),
                child: Row( mainAxisAlignment: MainAxisAlignment.start, crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Icon(Icons.circle, color: connect ? Colors.green : Colors.red,), const SizedBox(width: 12),
                    Text('Connect: $connect\n'),
                  ],
                ),
              ),
              Padding( padding: const EdgeInsets.only(bottom: 8.0),
                child: Row( mainAxisAlignment: MainAxisAlignment.start, crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Icon(Icons.circle, color: isConnected ? Colors.green : Colors.red,), const SizedBox(width: 12),
                    Text('Is Connected: $isConnected\n'),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
