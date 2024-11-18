extension StringExtension on String {
  List<Map<String, dynamic>> cleanFromMyDevice() {

    RegExp deviceRegExp = RegExp(
      r'MyDevice\(address=([A-F0-9:]+), name=([A-Za-z0-9_ ]+), bondState=(\d+)\)');

    List<Map<String, dynamic>> devicesList = deviceRegExp.allMatches(this).map((match) {
      return {
        'address': match.group(1),
        'name': match.group(2),
        'bondState': int.parse(match.group(3)!),
      };
    }).toList();

    return devicesList;
  }
}