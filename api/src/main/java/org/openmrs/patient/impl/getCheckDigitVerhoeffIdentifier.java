/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.patient.impl;

public class getCheckDigitVerhoeffIdentifier {


	private static final int[][] op = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
		{2, 3, 4, 0, 1, 7, 8, 9, 5, 6}, {3, 4, 0, 1, 2, 8, 9, 5, 6, 7}, {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
		{5, 9, 8, 7, 6, 0, 4, 3, 2, 1}, {6, 5, 9, 8, 7, 1, 0, 4, 3, 2}, {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
		{8, 7, 6, 5, 9, 3, 2, 1, 0, 4}, {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}};

	private static final int[] inv = {0, 4, 3, 2, 1, 5, 6, 7, 8, 9};
	private static final int[] F0 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

	private static final int[] F1 = {1, 5, 7, 6, 2, 8, 3, 0, 9, 4};

	private int[][] checkArray = new int[8][];

	public getCheckDigitVerhoeffIdentifier() {

		checkArray[0] = F0;
		checkArray[1] = F1;
		for (int i = 2; i < 8; i++) {
			checkArray[i] = new int[10];
			for (int j = 0; j < 10; j++) {
				checkArray[i][j] = checkArray[i - 1][checkArray[1][j]];
			}
		}
	}

	protected int getCheckDigit(String undecoratedIdentifier) {
		int[] getBaseArray = getBase(Integer.parseInt(undecoratedIdentifier), undecoratedIdentifier.length());
		insertCheck(getBaseArray);
		return getBaseArray[0];
	}

	private int[] getBase(int num, int length) {
		int[] a = new int[length + 1];
		int x = 1;
		for (int i = length; i-- > 0; ) {
			int y = num / x;
			a[i + 1] = y % 10;
			x = x * 10;
		}
		return a;
	}

	private int insertCheck(int[] a) {
		int check = 0;
		for (int i = 1; i < a.length; i++) {
			check = op[check][checkArray[i % 8][a[i]]];
		}
		a[0] = inv[check];
		return a[0];
	}

}
