package com.victorqueiroga.utils;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MyDateUtils {

	public static final String PATTERN_LOCALDATETIME = "yyyy-MM-dd'T'HH-mm-ss";
	public static final String PATTERN_LOCALDATETIMEFORMAL = "yyyy-MM-dd HH:mm:ss";

	public static final ZoneId zoneid1 = ZoneId.systemDefault();
	public static final Locale localeAtual = new Locale("pt", "BR");
	// public static final ZoneId zoneid1 = ZoneId.of("America/Sao_Paulo");

	public static String formatLocalDateTime(LocalDateTime localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_LOCALDATETIME);
		String formattedString = "";
		if (localDateTime != null)
			formattedString = localDateTime.format(formatter);
		return formattedString;
	}

	public static String formatLocalDateTime(LocalDateTime localDateTime, String pattern) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		String formattedString = "";
		if (localDateTime != null)
			formattedString = localDateTime.format(formatter);
		return formattedString;
	}

	public static String convertFileTimeToLocalDateTimeString(FileTime fileTime) {
		LocalDateTime localDateTime = fileTime.toInstant().atZone(zoneid1).toLocalDateTime();
		return formatLocalDateTime(localDateTime, PATTERN_LOCALDATETIMEFORMAL);
	}

}
