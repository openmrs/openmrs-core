/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util

import java.sql.Timestamp
import java.util.Calendar
import java.util.Date

/**
 * Kotlin extension functions for OpenMRS utilities.
 *
 * These extensions provide idiomatic Kotlin alternatives to the static methods
 * in OpenmrsUtil.java. They can be used alongside the Java utilities during
 * the migration period.
 *
 * Usage example:
 * ```kotlin
 * // Instead of: OpenmrsUtil.nullSafeEquals(a, b)
 * a.nullSafeEquals(b)
 *
 * // Instead of: OpenmrsUtil.collectionContains(collection, obj)
 * collection.safeContains(obj)
 * ```
 */

// ============================================================================
// Null-Safe Comparison Extensions
// ============================================================================

/**
 * Null-safe equality check that handles Date/Timestamp comparison specially.
 *
 * For Date objects, uses [compareDates] to handle Timestamp nanosecond issues.
 * For other objects, uses standard equals().
 *
 * @param other the object to compare with
 * @return true if both are null, or if they are equal
 */
fun Any?.nullSafeEquals(other: Any?): Boolean {
    return when {
        this == null -> other == null
        other == null -> false
        this is Date && other is Date -> this.compareDates(other) == 0
        else -> this == other
    }
}

/**
 * Null-safe case-insensitive string equality check.
 *
 * @param other the string to compare with
 * @return true if both are null, or if they are equal ignoring case
 */
fun String?.nullSafeEqualsIgnoreCase(other: String?): Boolean {
    return when {
        this == null -> other == null
        other == null -> false
        else -> this.equals(other, ignoreCase = true)
    }
}

// ============================================================================
// Date Extensions
// ============================================================================

/**
 * Compares two Date objects, handling java.sql.Timestamp correctly.
 *
 * Timestamp is not directly comparable to Date due to nanosecond precision,
 * so this drops the nanoseconds for comparison.
 *
 * @param other the date to compare with
 * @return negative if this < other, zero if equal, positive if this > other
 */
fun Date.compareDates(other: Date): Int {
    val d1 = if (this is Timestamp) Date(this.time) else this
    val d2 = if (other is Timestamp) Date(other.time) else other
    return d1.compareTo(d2)
}

/**
 * Compares dates treating null as the earliest possible date.
 *
 * @param other the date to compare with (can be null)
 * @return negative if this < other, zero if equal, positive if this > other
 */
fun Date?.compareWithNullAsEarliest(other: Date?): Int {
    return when {
        this == null && other == null -> 0
        this == null -> -1
        other == null -> 1
        else -> this.compareDates(other)
    }
}

/**
 * Compares dates treating null as the latest possible date.
 *
 * @param other the date to compare with (can be null)
 * @return negative if this < other, zero if equal, positive if this > other
 */
fun Date?.compareWithNullAsLatest(other: Date?): Int {
    return when {
        this == null && other == null -> 0
        this == null -> 1
        other == null -> -1
        else -> this.compareDates(other)
    }
}

/**
 * Gets the first moment (00:00:00.000) of the day for this date.
 *
 * @return a new Date at the start of the day, or null if this is null
 */
fun Date?.firstMomentOfDay(): Date? {
    return this?.let {
        Calendar.getInstance().apply {
            time = it
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
}

/**
 * Gets the last moment (23:59:59.999) of the day for this date.
 *
 * @return a new Date at the end of the day, or null if this is null
 */
fun Date?.lastMomentOfDay(): Date? {
    return this?.let {
        Calendar.getInstance().apply {
            time = it
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }
}

/**
 * Creates a safe copy of this date.
 *
 * @return a new Date with the same time value
 */
fun Date.safeCopy(): Date = Date(this.time)

/**
 * Checks if this date is yesterday relative to the current date.
 *
 * @return true if this date is yesterday
 */
fun Date?.isYesterday(): Boolean {
    if (this == null) return false

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    val thisDate = Calendar.getInstance().apply {
        time = this@isYesterday
    }

    return yesterday.get(Calendar.ERA) == thisDate.get(Calendar.ERA) &&
            yesterday.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == thisDate.get(Calendar.DAY_OF_YEAR)
}

// ============================================================================
// Collection Extensions
// ============================================================================

/**
 * Checks if the collection contains the element using only equals() comparison.
 *
 * This is important for SortedSet collections where compareTo() might differ
 * from equals(). OpenMRS entity classes often have compareTo() optimized for
 * sorting rather than equality.
 *
 * @param element the element to search for
 * @return true if any element equals() the given element
 */
fun <T> Collection<T>?.safeContains(element: T?): Boolean {
    if (this == null || element == null) return false
    return this.any { it == element }
}

/**
 * Checks if this collection contains any element from the given collection.
 *
 * @param elements the elements to check for
 * @return true if any element is found in this collection
 */
fun <T> Collection<T>.containsAny(elements: Collection<T>): Boolean {
    return elements.any { this.contains(it) }
}

/**
 * Adds an element to a Set within a Map, creating the Set if necessary.
 *
 * @param key the map key
 * @param value the value to add to the set
 */
fun <K, V> MutableMap<K, MutableSet<V>>.addToSet(key: K, value: V) {
    this.getOrPut(key) { mutableSetOf() }.add(value)
}

/**
 * Adds an element to a List within a Map, creating the List if necessary.
 *
 * @param key the map key
 * @param value the value to add to the list
 */
fun <K, V> MutableMap<K, MutableList<V>>.addToList(key: K, value: V) {
    this.getOrPut(key) { mutableListOf() }.add(value)
}

// ============================================================================
// String Extensions
// ============================================================================

/**
 * Checks if this string is in the given array.
 *
 * @param array the array to search in
 * @return true if this string is found in the array
 */
fun String?.isInArray(array: Array<String>?): Boolean {
    if (this == null || array == null) return false
    return this in array
}

/**
 * Checks if this string starts with any of the given prefixes.
 *
 * @param prefixes the prefixes to check
 * @return true if this string starts with any prefix
 */
fun String.startsWithAny(vararg prefixes: String): Boolean {
    return prefixes.any { this.startsWith(it) }
}

/**
 * Checks if this string contains both uppercase and lowercase characters.
 *
 * @return true if the string contains mixed case
 */
fun String?.containsUpperAndLowerCase(): Boolean {
    if (this == null) return false
    return Regex("^(?=.*?[A-Z])(?=.*?[a-z])[\\w|\\W]*\$").matches(this)
}

/**
 * Checks if this string contains only digit characters.
 *
 * @return true if all characters are digits
 */
fun String?.containsOnlyDigits(): Boolean {
    if (this.isNullOrEmpty()) return false
    return this.all { it.isDigit() }
}

/**
 * Checks if this string contains at least one digit.
 *
 * @return true if any character is a digit
 */
fun String?.containsDigit(): Boolean {
    if (this == null) return false
    return this.any { it.isDigit() }
}

/**
 * Shortens a stack trace by removing Spring framework and reflection lines.
 *
 * Reduces stack trace length by approximately 60% for easier viewing.
 *
 * @return shortened stack trace, or null if input is null
 */
fun String?.shortenedStackTrace(): String? {
    if (this == null) return null

    val excludePattern = Regex("(org\\.springframework\\.|java\\.lang\\.reflect\\.Method\\.invoke|sun\\.reflect\\.)")
    val results = mutableListOf<String>()
    var foundExcluded = false

    for (line in this.split("\n")) {
        if (excludePattern.containsMatchIn(line)) {
            foundExcluded = true
        } else {
            if (foundExcluded) {
                foundExcluded = false
                results.add("\tat [ignored] ...")
            }
            results.add(line)
        }
    }

    return results.joinToString("\n")
}

/**
 * Parses a parameter list string like "size=compact|order=date" into a Map.
 *
 * @return map of parameter names to values
 * @throws IllegalArgumentException if the format is invalid
 */
fun String?.parseParameterList(): Map<String, String> {
    if (this.isNullOrEmpty()) return emptyMap()

    return this.split("|").associate { param ->
        val index = param.indexOf('=')
        require(index > 0) {
            "Misformed argument in dynamic page specification string: '$param' is not 'key=value'."
        }
        param.substring(0, index) to param.substring(index + 1)
    }
}

/**
 * Parses a delimited string of integers.
 *
 * @param delimiter the delimiter between integers
 * @return list of parsed integers
 */
fun String.toIntList(delimiter: String = ","): List<Int> {
    return this.split(delimiter)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.toInt() }
}

// ============================================================================
// Comparable Extensions
// ============================================================================

/**
 * Compares two Comparable values, treating null as the lowest value.
 *
 * @param other the value to compare with
 * @return comparison result
 */
fun <T : Comparable<T>> T?.compareWithNullAsLowest(other: T?): Int {
    return when {
        this == null && other == null -> 0
        this == null -> -1
        other == null -> 1
        else -> this.compareTo(other)
    }
}

/**
 * Compares two Comparable values, treating null as the greatest value.
 *
 * @param other the value to compare with
 * @return comparison result
 */
fun <T : Comparable<T>> T?.compareWithNullAsGreatest(other: T?): Int {
    return when {
        this == null && other == null -> 0
        this == null -> 1
        other == null -> -1
        else -> this.compareTo(other)
    }
}

// ============================================================================
// Number Extensions
// ============================================================================

/**
 * Safely converts a Long to an Integer, throwing if the value doesn't fit.
 *
 * @return the integer value
 * @throws IllegalArgumentException if the value doesn't fit in an Integer
 */
fun Long.toIntSafe(): Int {
    require(this in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
        "$this cannot be cast to Integer without changing its value."
    }
    return this.toInt()
}
