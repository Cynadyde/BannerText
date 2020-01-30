package me.cynadyde.bannertext;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public class Utilities {

	/** Safely attempts to convert the given string into an integer */
	public static int intValueOf(final String string, final int defaultVal) {
		try { return (int) Math.floor(Double.parseDouble(string)); } 
		catch (NumberFormatException e) { return defaultVal; }
	}
	
	/** Safely attempts to retrieve an object from the given array */
	public static <T> T arrayGet(final T[] array, int index, final T defaultVal) {
		if (array.length == 0) {
			return defaultVal;
		}
		if (index < 0) {
			index += array.length;
		}
		if (!((0 <= index) && (index < array.length))) {
			return defaultVal;
		}
		return array[index];
	}
	
	/** Finds the index of an object or -1 within the given array */
	public static <T> int arrayIndexOf(final T[] array, final T v) {
		
		if (array == null || array.length == 0) {
			return -1;
		}
		
		for (int i = 0; i < array.length; i++) {
			T e = array[i];
			if (Objects.equals(e, v)) {
				return i;
			}
		}
	    return -1;
	}
	
	/** Safely returns a slice of the given array */
	@SuppressWarnings("unchecked")
	public static <T> T[] arraySlice(final T[] array, Integer from, Integer to) {
		
		if (array.length == 0) {
			return array;
		}
		if (from == null) {
			from = 0;
		}
		if (to == null) {
			to = array.length;
		}
		if (from < 0) {
			from = Math.max(0, from + array.length);
		}
		if (to < 0) {
			to += array.length;
		}
		if (to > array.length) {
			to = (array.length);
		}
		if ((array.length <= from) | (to <= from)) {
			return (T[])Array.newInstance(array.getClass().getComponentType(), 0);
		}
		return Arrays.copyOfRange(array, from, to);
	}
}
