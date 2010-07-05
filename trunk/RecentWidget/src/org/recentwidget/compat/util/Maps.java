package org.recentwidget.compat.util;

import java.util.HashMap;

/**
 * Provides static methods for creating mutable {@code Maps} instances easily.
 * 
 * http://www.google.co.in/codesearch/p?hl=en#uX1GffpyOZk/core/java/com/google/
 * android
 * /collect/Maps.java&q=maps.java&exact_package=git://android.git.kernel.org
 * /platform/frameworks/base.git&sa=N&cd=1&ct=rc
 */
public class Maps {
	/**
	 * Creates a {@code HashMap} instance.
	 * 
	 * @return a newly-created, initially-empty {@code HashMap}
	 */
	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}
}
