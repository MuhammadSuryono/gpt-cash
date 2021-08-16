package com.gpt.product.gpcash.retail.transaction.utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;

public class InstructionModeUtils {

	public static Timestamp getNextSomeDayFromTimestamp(Timestamp timestamp, int numberOfDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(timestamp.getTime()));
		cal.add(Calendar.DAY_OF_MONTH, numberOfDays);
		Date date = cal.getTime();
		return new Timestamp(date.getTime());
	}

	public static Timestamp getStandingInstructionDate(String style, int value, Timestamp inputDate) throws BusinessException {
		if (style.equals(ApplicationConstants.SI_RECURRING_TYPE_WEEKLY)) {
			if (value >= 1 && value <= 7) {
				int x = incrementDayWithNameFromToday(value, inputDate);
				return getNextSomeDayFromTimestamp(inputDate, x);
			} else {
				throw new BusinessException("GPT-0100144");
			}
		} else if (style.equals(ApplicationConstants.SI_RECURRING_TYPE_MONTHLY)) {
			int[] dt = getCurrentDateFromToday(value, inputDate);
			Calendar dateWanted = Calendar.getInstance();
			dateWanted.set(dt[2], dt[1], dt[0]);
			Date dtdate = dateWanted.getTime();
			return new Timestamp(dtdate.getTime());
		} else if (style.equals(ApplicationConstants.SI_RECURRING_TYPE_DAILY)) {
			return getNextSomeDayFromTimestamp(inputDate, value);
		} else if (style.equals(ApplicationConstants.SI_RECURRING_TYPE_ANNUALLY)) {
			int[] dt = getCurrentDateYearFromToday(value, inputDate);
			Calendar dateWanted = Calendar.getInstance();
			dateWanted.set(dt[2], dt[1], dt[0]);
			Date dtdate = dateWanted.getTime();
			return new Timestamp(dtdate.getTime());
		} else {
			return null;
		}
	}
	
	public static int incrementDayWithNameFromToday(int dayWanted, Timestamp inputDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(inputDate);

		int dayToday = cal.get(Calendar.DAY_OF_WEEK);
		int increment;

		if (dayWanted > dayToday) {
			increment = dayWanted - dayToday;
		} else if (dayWanted == dayToday) {
			increment = 7;
		} else {
			increment = 7 - dayToday + dayWanted;
		}

		return increment;
	}
	
	public static int[] getCurrentDateFromToday(int dateWanted, Timestamp inputDate) throws BusinessException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(inputDate);

		int dateNow = cal.get(Calendar.DATE);
		int monthNow = cal.get(Calendar.MONTH);
		int yearNow = cal.get(Calendar.YEAR);
		int lastDateInMonth = cal.getActualMaximum(Calendar.DATE);

		cal.add(Calendar.MONTH, 1);
		int lastDateInNextMonth = cal.getActualMaximum(Calendar.DATE);

		int[] arr = new int[3];
		if (dateWanted == 999 && dateNow != lastDateInMonth) {
			arr[0] = lastDateInMonth;
			arr[1] = monthNow;
			arr[2] = yearNow;
		} else if (dateWanted == 999 && dateNow == lastDateInMonth) {
			arr[0] = lastDateInNextMonth;
			arr[1] = monthNow + 1;
			arr[2] = yearNow;
		} else if (dateWanted > dateNow && dateWanted <= lastDateInMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow;
			arr[2] = yearNow;
		} else if (dateWanted <= dateNow && dateWanted <= lastDateInNextMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow + 1;
			arr[2] = yearNow;
		} else if (dateWanted <= dateNow && dateWanted > lastDateInNextMonth) {
			arr[0] = lastDateInNextMonth;
			arr[1] = monthNow + 1;
			arr[2] = yearNow;
		} else if (dateWanted > lastDateInMonth) {
			arr[0] = lastDateInNextMonth;
			arr[1] = monthNow + 1;
			arr[2] = yearNow;
		} else {
			throw new BusinessException("GPT-0100143");
		}

		return arr;
	}
	
	public static int[] getCurrentDateYearFromToday(int dateWanted, Timestamp inputDate) throws BusinessException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(inputDate);

		int dateNow = cal.get(Calendar.DATE);
		int monthNow = cal.get(Calendar.MONTH);
		int yearNow = cal.get(Calendar.YEAR);
		int lastDateInMonth = cal.getActualMaximum(Calendar.DATE);

		cal.add(Calendar.YEAR, 1);
		int lastDateInNextYear = cal.getActualMaximum(Calendar.DATE);

		int[] arr = new int[3];
		if (dateWanted > dateNow && dateWanted <= lastDateInMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow;
			arr[2] = yearNow;
		} else if (dateWanted <= dateNow && dateWanted <= lastDateInNextYear) {
			arr[0] = dateWanted;
			arr[1] = monthNow;
			arr[2] = yearNow + 1;
		} else if (dateWanted <= dateNow && dateWanted > lastDateInNextYear) {
			arr[0] = lastDateInNextYear;
			arr[1] = monthNow;
			arr[2] = yearNow + 1;
		} else if (dateWanted > lastDateInMonth) {
			arr[0] = lastDateInNextYear;
			arr[1] = monthNow;
			arr[2] = yearNow + 1;
		} else {
			throw new BusinessException("GPT-0100143");
		}

		return arr;
	}
	
	public static Timestamp getStandingInstructionDate(String style, int value) throws BusinessException {
		if (style.equals(ApplicationConstants.SI_RECURRING_TYPE_WEEKLY)) {
			if (value >= 1 && value <= 7) {
				int x = incrementDayWithNameFromToday(value);
				return nextSomeDayFromToday(x);
			} else {
				throw new BusinessException("GPT-0100144");
			}
		} else if (style.equals(ApplicationConstants.SI_RECURRING_TYPE_MONTHLY)) {
			int[] dt = getCurrentDateFromToday(value);
			Calendar dateWanted = Calendar.getInstance();
			dateWanted.set(dt[2], dt[1], dt[0]);
			Date dtdate = dateWanted.getTime();
			return new Timestamp(dtdate.getTime());
		} else if (style.equals(ApplicationConstants.SI_RECURRING_TYPE_DAILY)) {
			return nextSomeDayFromToday(value);
		} else if (style.equals(ApplicationConstants.SI_RECURRING_TYPE_ANNUALLY)) {
			int[] dt = getCurrentDateYearFromToday(value);
			Calendar dateWanted = Calendar.getInstance();
			dateWanted.set(dt[2], dt[1], dt[0]);
			Date dtdate = dateWanted.getTime();
			return new Timestamp(dtdate.getTime());
		} else {
			return null;
		}
	}
	
	public static int incrementDayWithNameFromToday(int dayWanted) {
		Calendar cal = Calendar.getInstance();
		cal.getTime();

		int dayToday = cal.get(Calendar.DAY_OF_WEEK);
		int increment;

		if (dayWanted > dayToday) {
			increment = dayWanted - dayToday;
		} else if (dayWanted == dayToday) {
			increment = 7;
		} else {
			increment = 7 - dayToday + dayWanted;
		}

		return increment;
	}
	
	public static Timestamp nextSomeDayFromToday(int x) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, x);
		Date date = cal.getTime();
		return new Timestamp(date.getTime());
	}
	
	public static int[] getCurrentDateYearFromToday(int dateWanted) throws BusinessException {
		Calendar cal = Calendar.getInstance();
		int dateNow = cal.get(Calendar.DATE);
		int monthNow = cal.get(Calendar.MONTH);
		int yearNow = cal.get(Calendar.YEAR);
		int lastDateInMonth = cal.getActualMaximum(Calendar.DATE);

		cal.add(Calendar.YEAR, 1);
		int lastDateInNextYear = cal.getActualMaximum(Calendar.DATE);

		int[] arr = new int[3];
		if (dateWanted > dateNow && dateWanted <= lastDateInMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow;
			arr[2] = yearNow;
		} else if (dateWanted <= dateNow && dateWanted <= lastDateInNextYear) {
			arr[0] = dateWanted;
			arr[1] = monthNow;
			arr[2] = yearNow + 1;
		} else if (dateWanted <= dateNow && dateWanted > lastDateInNextYear) {
			arr[0] = lastDateInNextYear;
			arr[1] = monthNow;
			arr[2] = yearNow + 1;
		} else if (dateWanted > lastDateInMonth) {
			arr[0] = lastDateInNextYear;
			arr[1] = monthNow;
			arr[2] = yearNow + 1;
		} else {
			throw new BusinessException("GPT-0100143");
		}

		return arr;
	}
	
	public static int[] getCurrentDateFromToday(int dateWanted) throws BusinessException {
		Calendar cal = Calendar.getInstance();
		int dateNow = cal.get(Calendar.DATE);
		int monthNow = cal.get(Calendar.MONTH);
		int yearNow = cal.get(Calendar.YEAR);
		int lastDateInMonth = cal.getActualMaximum(Calendar.DATE);

		cal.add(Calendar.MONTH, 1);
		int lastDateInNextMonth = cal.getActualMaximum(Calendar.DATE);

		int[] arr = new int[3];
		if (dateWanted == 999 && dateNow != lastDateInMonth) {
			arr[0] = lastDateInMonth;
			arr[1] = monthNow;
			arr[2] = yearNow;
		} else if (dateWanted == 999 && dateNow == lastDateInMonth) {
			arr[0] = lastDateInNextMonth;
			arr[1] = monthNow + 1;
			arr[2] = yearNow;
		} else if (dateWanted > dateNow && dateWanted <= lastDateInMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow;
			arr[2] = yearNow;
		} else if (dateWanted <= dateNow && dateWanted <= lastDateInNextMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow + 1;
			arr[2] = yearNow;
		} else if (dateWanted <= dateNow && dateWanted > lastDateInNextMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow + 2;
			arr[2] = yearNow;
			//
		} else if (dateWanted > lastDateInMonth && dateWanted > lastDateInNextMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow + 2;
			arr[2] = yearNow;
			//
		} else if (dateWanted > lastDateInMonth && dateWanted <= lastDateInNextMonth) {
			arr[0] = dateWanted;
			arr[1] = monthNow + 1;
			arr[2] = yearNow;
		} else {
			throw new BusinessException("GPT-0100143");
		}

		return arr;
	}
}
