package com.nta.component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateUtils {
  private static final List<String> DATE_FORMATS_WITHOUT_TIMEZONE =
      List.of(
          "yyyy-MM-dd HH:mm:ss",
          "yyyy-MM-dd HH:mm:ss.0",
          "MMM d, yyyy, h:mm:ss a",
          "dd MMM yyyy",
          "MMM dd, yyyy hh:mm:ss a",
          "dd-MMM-yyyy HH:mm:ss",
          "ss",
          "yyyy/MM/dd HH:mm:ss");

  private DateUtils() {
    super();
  }

  public static Date parseDate(final String dateString) throws ParseException {
    final String trimmedDateString = dateString.trim();
    Date date = parseDate("yyyy-MM-dd'T'HH:mm:ss'Z'", trimmedDateString);
    if (date == null) {
      for (final String format : DATE_FORMATS_WITHOUT_TIMEZONE) {
        date = parseDate(format, trimmedDateString);
        if (date != null) {
          break;
        }
      }
    }
    if (date == null) {
      throw new ParseException("Failed to parse date string: " + dateString, 0);
    }
    return date;
  }

  private static Date parseDate(final String format, final String dateString) {
    try {
      final DateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
      return dateFormat.parse(dateString);
    } catch (ParseException e) {
      return null;
    }
  }

  public static Date parseUsingDefaultFormat(final String date) throws ParseException {
    final DateFormat df = getDefaultDateFormat();
    return df.parse(date);
  }

  public static Date getFormattedDate(final String date) throws ParseException {
    final SimpleDateFormat format;
    if (date.contains("PM") || date.contains("AM")) {
      format = new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.getDefault());
    } else if (date.contains("/")) {
      format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
    } else if (date.contains(":") && date.contains("-") && date.contains("T")) {
      format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
    } else if (date.contains(":") && date.contains("-")) {
      format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
    } else {
      format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }
    return format.parse(date.trim());
  }

  public static SimpleDateFormat getDefaultDateFormat() {
    return new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.getDefault());
  }

  public static SimpleDateFormat getDefaultDateFormat(final String dateString) {
    return dateString.toUpperCase(Locale.ENGLISH).contains("AM")
            || dateString.toUpperCase(Locale.ENGLISH).contains("PM")
        ? new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.getDefault())
        : new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
  }

  public static String getFormattedDateString(final String dateString, final DateFormat format)
      throws ParseException {
    final DateFormat fmt = getDefaultDateFormat(dateString);

    return format.format(fmt.parse(dateString));
  }

  public static String formatDate(final Date date) throws ParseException {
    return getDefaultDateFormat().format(date);
  }
}
