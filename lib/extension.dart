extension StringExtension on String {
  List<Map<String, dynamic>> cleanFromMyDevice() {

    RegExp deviceRegExp = RegExp(
      r'MyDevice\(address=([A-F0-9:]+), name=([A-Za-z0-9_ ]+), bondState=(\d+), rssi=(-?\d+)\)');

    List<Map<String, dynamic>> devicesList = deviceRegExp.allMatches(this).map((match) {
      return {
        'address': match.group(1),
        'name': match.group(2),
        'bondState': int.parse(match.group(3)!),
        'rssi': double.parse(match.group(4)!),
      };
    }).toList();

    return devicesList;
  }
  List<Map<String, dynamic>> cleanFromTags() {

    RegExp tagRegExp = RegExp(
      r'tagData=([\w\d]+), tagEpc=([\w\d]+), tagCount=(\d+), tagUser=([\w]+|null), tagRssi=(-?\d+\.\d+), tagTid=([\w]+|null)');
      
    List<Map<String, dynamic>> tagLists = tagRegExp.allMatches(this).map((match) {
      return {
        'tagData': match.group(1),
        'tagEpc': match.group(2),
        'tagCount': int.parse(match.group(3)!),
        'tagUser': match.group(4) == 'null' ? null : match.group(4),
        'tagRssi': double.parse(match.group(5)!),
        'tagTid': match.group(6) == 'null' ? null : match.group(6),
      };
    }).toList();

    return tagLists;
  }
}