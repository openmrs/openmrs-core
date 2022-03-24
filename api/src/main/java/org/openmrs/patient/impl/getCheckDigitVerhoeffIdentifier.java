package org.openmrs.patient.impl;

public class getCheckDigitVerhoeffIdentifier {
	
	private int[][] F = new int[8][];
	
	private static final int[][] op = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 },
	        { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 },
	        { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 },
	        { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };
	
	private static final int[] inv = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };
	
	protected int getCheckDigit(String undecoratedIdentifier) {
		int[] a = getBase(Integer.parseInt(undecoratedIdentifier), undecoratedIdentifier.length());
		insertCheck(a);
		return a[0];
	}
	
	private int[] getBase(int num, int length) {
		int[] a = new int[length + 1];
		int x = 1;
		for (int i = length; i-- > 0;) {
			int y = num / x;
			a[i + 1] = y % 10;
			x = x * 10;
		}
		return a;
	}
	
	private int insertCheck(int[] a) {
		int check = 0;
		for (int i = 1; i < a.length; i++) {
			check = op[check][F[i % 8][a[i]]];
		}
		a[0] = inv[check];
		return a[0];
	}
	
}
