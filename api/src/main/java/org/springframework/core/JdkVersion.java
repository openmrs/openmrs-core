/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core;

/**
 * Internal helper class used to find the Java/JVM version that Spring is
 * operating on, to allow for automatically adapting to the present platform's
 * capabilities.
 *
 * <p>Note that Spring requires JVM 1.6 or higher, as of Spring 4.0.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rick Evans
 * @author Sam Brannen
 */
public abstract class JdkVersion {
	
	/**
	 * Constant identifying the 1.3.x JVM (JDK 1.3).
	 */
	public static final int JAVA_13 = 0;
	
	/**
	 * Constant identifying the 1.4.x JVM (J2SE 1.4).
	 */
	public static final int JAVA_14 = 1;
	
	/**
	 * Constant identifying the 1.5 JVM (Java 5).
	 */
	public static final int JAVA_15 = 2;
	
	/**
	 * Constant identifying the 1.6 JVM (Java 6).
	 */
	public static final int JAVA_16 = 3;
	
	/**
	 * Constant identifying the 1.7 JVM (Java 7).
	 */
	public static final int JAVA_17 = 4;
	
	/**
	 * Constant identifying the 1.8 JVM (Java 8).
	 */
	public static final int JAVA_18 = 5;
	
	/**
	 * Constant identifying the 1.9 JVM (Java 9).
	 */
	public static final int JAVA_19 = 6;
	
	/**
	 * Constant identifying the 10.0.x JVM (Java 10).
	 */
	public static final int JAVA_100 = 7;
	
	/**
	 * Constant identifying the 11.0.x JVM (Java 11).
	 */
	public static final int JAVA_110 = 8;
	
	/**
	 * Constant identifying the 12.0.x JVM (Java 12).
	 */
	public static final int JAVA_120 = 9;
	
	/**
	 * Constant identifying the 13.0.x JVM (Java 13).
	 */
	public static final int JAVA_130 = 10;
	
	/**
	 * Constant identifying the 14.0.x JVM (Java 14).
	 */
	public static final int JAVA_140 = 11;
	
	private static final String javaVersion;
	
	private static final int majorJavaVersion;
	
	static {
		javaVersion = System.getProperty("java.version");
		// in versions 9 and lower		- version String should look like "1.4.2_10"
		// in versions 10 and higher 	- version String should look like "11.0.7"
		if (javaVersion.contains("14.0.")) {
			majorJavaVersion = JAVA_140;
		} else if (javaVersion.contains("13.0.")) {
			majorJavaVersion = JAVA_130;
		} else if (javaVersion.contains("12.0.")) {
			majorJavaVersion = JAVA_120;
		} else if (javaVersion.contains("11.0.")) {
			majorJavaVersion = JAVA_110;
		} else if (javaVersion.contains("10.0.")) {
			majorJavaVersion = JAVA_100;
		} else if (javaVersion.contains("1.9.")) {
			majorJavaVersion = JAVA_19;
		} else if (javaVersion.contains("1.8.")) {
			majorJavaVersion = JAVA_18;
		} else if (javaVersion.contains("1.7.")) {
			majorJavaVersion = JAVA_17;
		} else {
			// else leave 1.6 as default (it's either 1.6 or unknown)
			majorJavaVersion = JAVA_16;
		}
	}
	
	/**
	 * Private constructor to override the default constructor to prevent it from being instantiated.
	 */
	private JdkVersion() {
	}
	
	/**
	 * Return the full Java version string, as returned by
	 * {@code System.getProperty("java.version")}.
	 * @return the full Java version string
	 * @see System#getProperty(String)
	 */
	public static String getJavaVersion() {
		return javaVersion;
	}
	
	/**
	 * Get the major version code. This means we can do things like
	 * {@code if (getMajorJavaVersion() >= JAVA_17)}.
	 * @return a code comparable to the {@code JAVA_XX} codes in this class
	 * @see #JAVA_16
	 * @see #JAVA_17
	 * @see #JAVA_18
	 * @see #JAVA_19
	 * @see #JAVA_100
	 * @see #JAVA_110
	 * @see #JAVA_120
	 * @see #JAVA_130
	 * @see #JAVA_140
	 */
	public static int getMajorJavaVersion() {
		return majorJavaVersion;
	}
	
}
