package top.tsk.utils;

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

}

