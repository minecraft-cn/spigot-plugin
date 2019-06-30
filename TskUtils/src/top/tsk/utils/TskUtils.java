package top.tsk.utils;

import java.util.LinkedList;
import java.util.List;

public class TskUtils {

  public static String CombineStrings(String[] args) {

    if (args.length == 0) {
      return "";
    }

    StringBuilder s = new StringBuilder();
    for (String arg : args) {
      s.append(" ").append(arg);
    }
    return s.toString().substring(1);
  }

  public static List<String> FilterByPrefix(List<String> list, String prefix) {

    LinkedList<String> linkedList = new LinkedList<>();

    for (String s : list) {
      if (s.startsWith(prefix)) {
        linkedList.add(s);
      }
    }

    return linkedList;
  }
}

