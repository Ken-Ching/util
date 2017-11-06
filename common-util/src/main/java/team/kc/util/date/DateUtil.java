package team.kc.util.date;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
	public static Date now () {	
		return new Date();
	}
	
	public static String nowStr () {
		return DateFormatHolder.formatter(
			DateFormatConstants.YYYYMMDDHHMMSS
		).format( now() );
	}
	
	/**
	 * Format the date with the pattern.
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format (Date date, String pattern) {
		return DateFormatHolder.formatter(pattern).format(date);
	}
	
	/**
	 * Parse the dateStr with the pattern
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static Date parse (String dateStr, String pattern) {
		try {
			return DateFormatHolder.formatter(pattern).parse(dateStr);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * Add the date with the field & amount
	 * @param date
	 * @param field
	 * @param amount
	 * @return
	 */
	public static Date add (Date date, int field, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime( date );
		cal.add( field, amount );
		
		return cal.getTime();
	}
}
