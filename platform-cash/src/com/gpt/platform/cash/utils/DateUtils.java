package com.gpt.platform.cash.utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.FastDateFormat;

public class DateUtils {
	public static java.sql.Date getCurrentDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(calendar.getTime().getTime());
	}

	// java.sql.Date only keeps Time, this method name is misleading
	public static java.sql.Date getCurrentDateTime() {
		return new java.sql.Date(System.currentTimeMillis());
	}

//	public static java.sql.Date getCurrentSqlDate() {
//		return new java.sql.Date(System.currentTimeMillis());
//	}

	public static Timestamp getCurrentTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	public static Timestamp getTimestamp(java.sql.Date date) {
		if (date != null)
			return new Timestamp(date.getTime());
		return null;
	}

	public static java.sql.Date getSQLDate(Timestamp timestamp) {
		if (timestamp != null)
			return new java.sql.Date(timestamp.getTime());
		return null;
	}

	public static String[] getDateTimeOnStringArray(java.sql.Date date) {
		if (date == null)
			return null;

		String[] values = new String[6];

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int yearValue = calendar.get(Calendar.YEAR);
		int monthValue = calendar.get(Calendar.MONTH) + 1;
		int dateValue = calendar.get(Calendar.DATE);
		int hourValue = calendar.get(Calendar.HOUR_OF_DAY);
		int minuteValue = calendar.get(Calendar.MINUTE);
		int secondValue = calendar.get(Calendar.SECOND);

		values[0] = "" + yearValue;
		values[1] = "0" + monthValue;
		values[2] = "0" + dateValue;
		values[3] = "0" + hourValue;
		values[4] = "0" + minuteValue;
		values[5] = "0" + secondValue;

		return values;
	}

	public static java.sql.Date roll(Calendar calendar, int period) {
		calendar.roll(Calendar.DAY_OF_YEAR, period);
		return new java.sql.Date(calendar.getTime().getTime());
	}

	public static Calendar getToday() {
		Calendar c = Calendar.getInstance();
		c.setTime(new java.util.Date());
		return c;
	}

	public static Calendar getCalendar(java.util.Date date) {
		if (date == null)
			return null;

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public static int getMonth(Calendar calendar) {
		return (calendar.get(Calendar.MONTH) + 1);
	}

	public static int getYear(Calendar calendar) {
		return calendar.get(Calendar.YEAR);
	}
	
	public static Timestamp getInstructionDateBySessionTime(String sessionTime, Timestamp instructionDate) {
		Calendar calInsDate = Calendar.getInstance();
		calInsDate.setTime(instructionDate);
		String[] sessionTimeRequest = sessionTime.split("\\:");
		int sessionTimeHour = Integer.valueOf(sessionTimeRequest[0]);
		int sessionTimeMinute = Integer.valueOf(sessionTimeRequest[1]);
		calInsDate.set(Calendar.HOUR_OF_DAY, sessionTimeHour);
		calInsDate.set(Calendar.MINUTE, sessionTimeMinute);
		calInsDate.set(Calendar.SECOND, 0);
		calInsDate.set(Calendar.MILLISECOND, 0);
		
		return new Timestamp(calInsDate.getTimeInMillis());
	}
	
	public static String getSessionTimeByInstructionDate(Timestamp instructionDate) {
		return FastDateFormat.getInstance("HH:mm").format(instructionDate);
	}
	
	/**
	 *
	 * @param date
	 * @return the next earliest date, e.g: if date is 30/11/2017 12:00:00 then the next earliest date is 01/12/2017 00:00:00 
	 */
	public static Calendar getNextEarliestDate(Date date) {
		Calendar calInsDate = Calendar.getInstance();
		calInsDate.setTime(date);
		calInsDate.set(Calendar.HOUR_OF_DAY, 0);
		calInsDate.set(Calendar.MINUTE, 0);
		calInsDate.set(Calendar.SECOND, 0);
		calInsDate.set(Calendar.MILLISECOND, 0);
		calInsDate.add(Calendar.DATE, 1);
		return calInsDate;
	}

	/**
	 * 
	 * @param date
	 * @return the earliest date, e.g: if date is 30/11/2017 12:45:10 then the earliest date is 30/11/2017 00:00:00 
	 */
	public static Calendar getEarliestDate(Date date) {
		Calendar calInsDate = Calendar.getInstance();
		calInsDate.setTime(date);
		calInsDate.set(Calendar.HOUR_OF_DAY, 0);
		calInsDate.set(Calendar.MINUTE, 0);
		calInsDate.set(Calendar.SECOND, 0);
		calInsDate.set(Calendar.MILLISECOND, 0);
		return calInsDate;
	}
	
	public static Calendar getNextSessionTime(Timestamp futureDate, int sessionTimeValue,List<String> sessionTimeList ) {
		Calendar calTo = DateUtils.getEarliestDate(futureDate); 
		
		if(sessionTimeValue == 1) {
			
			String[] sessionTimeEnd = sessionTimeList.get(sessionTimeValue -1).split("\\:");
			int sessionTimeHourEnd = Integer.valueOf(sessionTimeEnd[0]);
			int sessionTimeMinuteEnd = Integer.valueOf(sessionTimeEnd[1]);
			
			calTo.set(Calendar.HOUR_OF_DAY, sessionTimeHourEnd);
			calTo.set(Calendar.MINUTE, sessionTimeMinuteEnd);
			calTo.set(Calendar.SECOND, 0);
			calTo.set(Calendar.MILLISECOND, 0);
		} else if (sessionTimeValue > 1) {
			
			String[] sessionTimeEnd = sessionTimeList.get(sessionTimeValue -1).split("\\:");
			int sessionTimeHourEnd = Integer.valueOf(sessionTimeEnd[0]);
			int sessionTimeMinuteEnd = Integer.valueOf(sessionTimeEnd[1]);
			
			calTo.set(Calendar.HOUR_OF_DAY, sessionTimeHourEnd);
			calTo.set(Calendar.MINUTE, sessionTimeMinuteEnd);
			calTo.set(Calendar.SECOND, 0);
			calTo.set(Calendar.MILLISECOND, 0);
		}
		return calTo;
	}
	
}
