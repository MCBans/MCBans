package com.mcbans.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeTools {
  public static Long convertStringToDate(String time){
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    parse(c, time);
    return c.getTimeInMillis();
  }

  public static String[] parse(Calendar calendar, String dateString){
    Pattern pattern = Pattern.compile("([\\-0-9]+)([ a-zA-Z]+)");
    Matcher matcher = pattern.matcher(dateString);
    while(matcher.find()){
      MatchResult t = matcher.toMatchResult();
      String time = t.group(1);
      String unit = t.group(2).trim();
      if(unit.contains("minute") || unit.equals("m")){
        calendar.add(Calendar.MINUTE, Integer.valueOf(time));
      }else if(unit.contains("second") || unit.equals("s")){
        calendar.add(Calendar.SECOND, Integer.valueOf(time));
      }else if(unit.contains("hour") || unit.equals("h")){
        calendar.add(Calendar.HOUR, Integer.valueOf(time));
      }else if(unit.contains("day") || unit.equals("d")){
        calendar.add(Calendar.HOUR, 24*Integer.valueOf(time));
      }else if(unit.contains("week") || unit.equals("w")){
        calendar.add(Calendar.HOUR, 7*24*Integer.valueOf(time));
      }
    }
    return null;
  }

  public static String countdown(long time){
    long timeLeft = time;
    long days = timeLeft/(1000*60*60*24); timeLeft %= (1000*60*60*24);
    long hours = timeLeft/(1000*60*60); timeLeft %= (1000*60*60);
    long minutes = timeLeft/(1000*60); timeLeft %= (1000*60);
    long seconds = timeLeft/(1000); timeLeft %= (1000);
    return ((days>0)?" "+days+" day"+((days>1)?"s":""):"") +
      ((hours>0)?" "+hours+" hour"+((hours>1)?"s":""):"") +
      ((minutes>0)?" "+minutes+" minute"+((minutes>1)?"s":""):"") +
      ((seconds>0)?" "+seconds+" second"+((seconds>1)?"s":""):"");
  }
  public static String countDownForBanSync(long time){
    long timeLeft = time;
    long days = timeLeft/(1000*60*60*24); timeLeft %= (1000*60*60*24);
    long hours = timeLeft/(1000*60*60); timeLeft %= (1000*60*60);
    long minutes = timeLeft/(1000*60); timeLeft %= (1000*60);
    long seconds = timeLeft/(1000);
    return ((days>0)?" "+days+"d":"") +
      ((hours>0)?" "+hours+"h":"") +
      ((minutes>0)?" "+minutes+"m":"") +
      ((seconds>0)?" "+seconds+"s":"");
  }
}
