/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Pure Java implementation of Argon2id password hashing per RFC 9106.
 * Uses BLAKE2b as the underlying hash function via reflection (Java 9+).
 */
class Argon2PasswordEncoder {
	
	private static final SecureRandom RANDOM = new SecureRandom();
	
	private final int memorySize;
	
	private final int iterations;
	
	private final int parallelism;
	
	private final int hashLength;
	
	private final int saltLength;
	
	Argon2PasswordEncoder(int saltLength, int hashLength, int parallelism, int memorySize, int iterations) {
		this.saltLength = saltLength;
		this.hashLength = hashLength;
		this.parallelism = parallelism;
		this.memorySize = memorySize;
		this.iterations = iterations;
	}
	
	String encode(String password) {
		byte[] salt = new byte[saltLength];
		RANDOM.nextBytes(salt);
		byte[] hash = argon2id(password.getBytes(StandardCharsets.UTF_8), salt, hashLength);
		return formatHash(salt, hash);
	}
	
	boolean matches(String rawPassword, String encodedHash) {
		String[] parts = parseHash(encodedHash);
		if (parts.length < 5) {
			return false;
		}
		int embeddedMemory = Integer.parseInt(parts[0]);
		int embeddedIterations = Integer.parseInt(parts[1]);
		int embeddedParallelism = Integer.parseInt(parts[2]);
		byte[] salt = Base64.getUrlDecoder().decode(parts[3]);
		byte[] expectedHash = Base64.getUrlDecoder().decode(parts[4]);
		byte[] actualHash = argon2idWithParams(rawPassword.getBytes(StandardCharsets.UTF_8), salt, expectedHash.length,
		    embeddedMemory, embeddedIterations, embeddedParallelism);
		return constantTimeEquals(expectedHash, actualHash);
	}
	
	private String formatHash(byte[] salt, byte[] hash) {
		return "$argon2id$v=19$m=" + memorySize + ",t=" + iterations + ",p=" + parallelism + "$"
		        + Base64.getUrlEncoder().withoutPadding().encodeToString(salt) + "$"
		        + Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
	}
	
	private String[] parseHash(String encodedHash) {
		if (encodedHash == null) {
			return new String[0];
		}
		String[] parts = encodedHash.split("\\$");
		if (parts.length != 6 || !"argon2id".equals(parts[1]) || !"v=19".equals(parts[2])) {
			return new String[0];
		}
		String[] params = parts[3].split(",");
		if (params.length != 3) {
			return new String[0];
		}
		int memory = Integer.parseInt(params[0].substring(2));
		int parsedIterations = Integer.parseInt(params[1].substring(2));
		int parsedParallelism = Integer.parseInt(params[2].substring(2));
		return new String[] { String.valueOf(memory), String.valueOf(parsedIterations), String.valueOf(parsedParallelism),
		        parts[4], parts[5] };
	}
	
	// ==================== Argon2id ====================
	
	private static final class ArgonContext {
		
		final int segmentLength;
		
		final int parallelism;
		
		final long[][] blocks;
		
		ArgonContext(int segmentLength, int parallelism, long[][] blocks) {
			this.segmentLength = segmentLength;
			this.parallelism = parallelism;
			this.blocks = blocks;
		}
	}
	
	private byte[] argon2id(byte[] password, byte[] salt, int desiredLength) {
		return argon2idWithParams(password, salt, desiredLength, memorySize, iterations, parallelism);
	}
	
	private byte[] argon2idWithParams(byte[] password, byte[] salt, int desiredLength, int mem, int iter, int para) {
		int segmentLength = Math.max(mem / (para * 4), 2);
		int totalBlocks = para * segmentLength * 4;
		
		long[][] blocks = new long[totalBlocks][128];
		byte[] h0 = h0HashWithParams(password, salt, desiredLength, mem, iter, para);
		
		initializeBlocks(h0, blocks, segmentLength, para);
		performPasses(blocks, segmentLength, para, totalBlocks, iter);
		long[] finalBlock = finalizeBlocks(blocks, para, segmentLength);
		
		return hPrime(longsToBytes(finalBlock, 1024), desiredLength);
	}
	
	private void initializeBlocks(byte[] h0, long[][] blocks, int segmentLength, int para) {
		for (int i = 0; i < para; i++) {
			blocks[i * segmentLength * 4] = toLongs(computeBlockHash(h0, 1, i));
			blocks[i * segmentLength * 4 + 1] = toLongs(computeBlockHash(h0, 2, i));
		}
	}
	
	private byte[] computeBlockHash(byte[] h0, int type, int lane) {
		byte[] input = new byte[h0.length + 12];
		System.arraycopy(h0, 0, input, 0, h0.length);
		le32(input, h0.length, type);
		le32(input, h0.length + 4, lane);
		le32(input, h0.length + 8, 1);
		return hPrime(input, 1024);
	}
	
	private void performPasses(long[][] blocks, int segmentLength, int parallelism, int totalBlocks, int numIterations) {
		ArgonContext ctx = new ArgonContext(segmentLength, parallelism, blocks);
		for (int pass = 0; pass < numIterations; pass++) {
			for (int slice = 0; slice < 4; slice++) {
				for (int lane = 0; lane < parallelism; lane++) {
					for (int index = 0; index < segmentLength; index++) {
						processBlock(pass, slice, lane, index, totalBlocks, ctx);
					}
				}
			}
		}
	}
	
	private void processBlock(int pass, int slice, int lane, int index, int totalBlocks, ArgonContext ctx) {
		int segmentLength = ctx.segmentLength;
		int curIdx = lane * segmentLength * 4 + slice * segmentLength + index;
		int prevIdx = computePrevIdx(pass, slice, lane, index, segmentLength);
		
		int refIdx = getReferenceIndex(pass, slice, lane, index, curIdx, ctx);
		refIdx = Math.abs(refIdx) % totalBlocks;
		
		long[] cur = ctx.blocks[curIdx];
		long[] prev = ctx.blocks[prevIdx];
		long[] ref = ctx.blocks[refIdx];
		
		for (int j = 0; j < 128; j++) {
			cur[j] = prev[j] ^ ref[j];
		}
		
		f(cur);
	}
	
	private int computePrevIdx(int pass, int slice, int lane, int index, int segmentLength) {
		int laneStart = lane * segmentLength * 4;
		if (index > 0) {
			return laneStart + slice * segmentLength + index - 1;
		}
		if (pass == 0 || slice == 0) {
			return laneStart + segmentLength * 4 - 1;
		}
		return laneStart + slice * segmentLength - 1;
	}
	
	private long[] finalizeBlocks(long[][] blocks, int parallelism, int segmentLength) {
		long[] finalBlock = new long[128];
		for (int i = 0; i < parallelism; i++) {
			int lastIdx = i * segmentLength * 4 + segmentLength * 4 - 1;
			for (int j = 0; j < 128; j++) {
				finalBlock[j] ^= blocks[lastIdx][j];
			}
		}
		return finalBlock;
	}
	
	private int getReferenceIndex(int pass, int slice, int lane, int index, int curIdx, ArgonContext ctx) {
		int segmentLength = ctx.segmentLength;
		int ctxParallelism = ctx.parallelism;
		long[][] blocks = ctx.blocks;
		int laneLength = segmentLength * 4;
		
		long pseudoRand = (index == 0) ? blocks[curIdx][0] : blocks[curIdx - 1][0];
		
		int referenceAreaSize;
		if (pass == 0) {
			referenceAreaSize = (slice == 0) ? lane * laneLength + index : slice * segmentLength + index;
		} else {
			referenceAreaSize = 3 * segmentLength + index + 1;
		}
		referenceAreaSize = Math.max(referenceAreaSize, 1);
		
		pseudoRand = Long.remainderUnsigned(pseudoRand, referenceAreaSize);
		pseudoRand = Long.remainderUnsigned(pseudoRand, ctxParallelism);
		
		int refLane = (pass == 0 && slice == 0) ? lane : (int) pseudoRand;
		
		int refStart;
		if (pass == 0) {
			refStart = (slice == 0) ? 0 : slice * segmentLength;
		} else {
			refStart = laneLength - segmentLength;
		}
		
		pseudoRand = Long.remainderUnsigned(blocks[curIdx - (index == 0 ? 0 : 1)][0]
		        ^ ((long) pass << 32 | (long) lane << 16 | index), referenceAreaSize);
		int refIdx = refStart + (int) pseudoRand;
		if (refIdx >= laneLength) {
			refIdx = refIdx % laneLength;
		}
		
		return refLane * laneLength + refIdx;
	}
	
	/**
	 * Argon2 block function F: applies BLAKE2b-based compression over blocks.
	 */
	private void f(long[] block) {
		long[] r = new long[128];
		System.arraycopy(block, 0, r, 0, 128);
		
		for (int i = 0; i < 128; i += 16) {
			long[] q = new long[16];
			System.arraycopy(r, i, q, 0, 16);
			applyCompression(q);
			for (int j = 0; j < 16; j++) {
				r[i + j] = q[j];
			}
		}
		
		for (int i = 0; i < 128; i++) {
			block[i] ^= r[i];
		}
	}
	
	private void applyCompression(long[] m) {
		long[] v = new long[16];
		System.arraycopy(IV, 0, v, 0, 8);
		System.arraycopy(IV, 0, v, 8, 8);
		v[12] ^= 1024L;
		v[14] ^= 0xFFFFFFFFFFFFFFFFL;
		
		for (int i = 0; i < 12; i++) {
			int[] s = SIGMA[i];
			g(v, 0, 4, 8, 12, m[s[0]], m[s[1]]);
			g(v, 1, 5, 9, 13, m[s[2]], m[s[3]]);
			g(v, 2, 6, 10, 14, m[s[4]], m[s[5]]);
			g(v, 3, 7, 11, 15, m[s[6]], m[s[7]]);
			g(v, 0, 5, 10, 15, m[s[8]], m[s[9]]);
			g(v, 1, 6, 11, 12, m[s[10]], m[s[11]]);
			g(v, 2, 7, 8, 13, m[s[12]], m[s[13]]);
			g(v, 3, 4, 9, 14, m[s[14]], m[s[15]]);
		}
		
		for (int i = 0; i < 8; i++) {
			m[i] = v[i] ^ v[i + 8] ^ IV[i];
			m[i + 8] = v[i + 8] ^ IV[i];
		}
	}
	
	private static final long[] IV = { 0x6a09e667f3bcc908L, 0xbb67ae8584caa73bL, 0x3c6ef372fe94f82bL, 0xa54ff53a5f1d36f1L,
	        0x510e527fade682d1L, 0x9b05688c2b3e6c1fL, 0x1f83d9abfb41bd6bL, 0x5be0cd19137e2179L };
	
	private static final int[][] SIGMA = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
	        { 14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3 },
	        { 11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4 },
	        { 7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8 },
	        { 9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13 },
	        { 2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9 },
	        { 12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11 },
	        { 13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10 },
	        { 6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5 },
	        { 10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0 },
	        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
	        { 14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3 } };
	
	private static void g(long[] v, int a, int b, int c, int d, long x, long y) {
		v[a] = v[a] + v[b] + x;
		v[d] = (v[d] ^ v[a]) >>> 32 | (v[d] ^ v[a]) << 32;
		v[c] = v[c] + v[d];
		v[b] = (v[b] ^ v[c]) >>> 24 | (v[b] ^ v[c]) << 40;
		v[a] = v[a] + v[b] + y;
		v[d] = (v[d] ^ v[a]) >>> 16 | (v[d] ^ v[a]) << 48;
		v[c] = v[c] + v[d];
		v[b] = (v[b] ^ v[c]) >>> 63 | (v[b] ^ v[c]) << 1;
	}
	
	// ==================== H0 and H' ====================
	
	private byte[] h0HashWithParams(byte[] password, byte[] salt, int hashLen, int mem, int iter, int para) {
		byte[] paramBlock = new byte[72];
		paramBlock[0] = (byte) 64;
		paramBlock[1] = 0;
		paramBlock[2] = 1;
		paramBlock[3] = 1;
		
		int totalLen = password.length + salt.length + 4 + 4 + 4 + 4 + 4 + 4;
		byte[] fullInput = new byte[totalLen];
		int pos = 0;
		System.arraycopy(password, 0, fullInput, pos, password.length);
		pos += password.length;
		System.arraycopy(salt, 0, fullInput, pos, salt.length);
		pos += salt.length;
		le32(fullInput, pos, 1);
		pos += 4;
		le32(fullInput, pos, hashLen);
		pos += 4;
		le32(fullInput, pos, mem);
		pos += 4;
		le32(fullInput, pos, iter);
		pos += 4;
		le32(fullInput, pos, 2);
		pos += 4;
		le32(fullInput, pos, para);
		pos += 4;
		
		return hPrime(fullInput, 64);
	}
	
	private byte[] hPrime(byte[] input, int desiredLength) {
		if (desiredLength <= 64) {
			return blake2b(input, desiredLength);
		}
		
		int n = (desiredLength + 31) / 32;
		byte[] v0 = blake2b(input, 64);
		byte[] result = new byte[desiredLength];
		int remaining = desiredLength;
		
		for (int i = 0; i < n && remaining > 0; i++) {
			byte[] b = new byte[65];
			System.arraycopy(v0, 0, b, 0, 64);
			b[64] = (byte) (i + 1);
			byte[] vi = blake2b(b, 64);
			int toCopy = Math.min(32, remaining);
			System.arraycopy(vi, 0, result, desiredLength - remaining, toCopy);
			remaining -= toCopy;
		}
		return result;
	}
	
	// ==================== BLAKE2b ====================
	
	private byte[] blake2b(byte[] input, int outlen) {
		return blake2bImpl(new byte[0], input, outlen);
	}
	
	private byte[] blake2bImpl(byte[] key, byte[] input, int outlen) {
		long[] h = IV.clone();
		h[0] ^= 0x01010000L ^ ((long) key.length << 8) ^ outlen;
		
		byte[] paddedInput = input;
		if (key.length > 0) {
			int pad = ((key.length + 127) / 128) * 128;
			paddedInput = new byte[pad + input.length];
			System.arraycopy(key, 0, paddedInput, 0, key.length);
			System.arraycopy(input, 0, paddedInput, pad, input.length);
		}
		
		long t = 0;
		int offset = 0;
		int remaining = paddedInput.length;
		
		while (remaining > 128) {
			long[] m = toLongs(paddedInput, offset);
			t += 128;
			compressBlake2b(h, m, t, false);
			offset += 128;
			remaining -= 128;
		}
		
		long[] lastM = new long[16];
		if (remaining > 0) {
			byte[] lastBytes = new byte[128];
			System.arraycopy(paddedInput, offset, lastBytes, 0, remaining);
			lastM = toLongs(lastBytes);
		}
		t += remaining;
		compressBlake2b(h, lastM, t, true);
		
		byte[] out = new byte[outlen];
		byte[] hBytes = longsToBytes(h, 64);
		System.arraycopy(hBytes, 0, out, 0, Math.min(outlen, 64));
		return out;
	}
	
	private void compressBlake2b(long[] h, long[] m, long t, boolean last) {
		long[] v = new long[16];
		System.arraycopy(h, 0, v, 0, 8);
		System.arraycopy(IV, 0, v, 8, 8);
		v[12] ^= t;
		if (last) {
			v[14] ^= 0xFFFFFFFFFFFFFFFFL;
		}
		
		for (int i = 0; i < 12; i++) {
			int[] s = SIGMA[i];
			g(v, 0, 4, 8, 12, m[s[0]], m[s[1]]);
			g(v, 1, 5, 9, 13, m[s[2]], m[s[3]]);
			g(v, 2, 6, 10, 14, m[s[4]], m[s[5]]);
			g(v, 3, 7, 11, 15, m[s[6]], m[s[7]]);
			g(v, 0, 5, 10, 15, m[s[8]], m[s[9]]);
			g(v, 1, 6, 11, 12, m[s[10]], m[s[11]]);
			g(v, 2, 7, 8, 13, m[s[12]], m[s[13]]);
			g(v, 3, 4, 9, 14, m[s[14]], m[s[15]]);
		}
		
		for (int i = 0; i < 8; i++) {
			h[i] ^= v[i] ^ v[i + 8];
		}
	}
	
	// ==================== Utilities ====================
	
	private static void le32(byte[] arr, int offset, int value) {
		arr[offset] = (byte) value;
		arr[offset + 1] = (byte) (value >>> 8);
		arr[offset + 2] = (byte) (value >>> 16);
		arr[offset + 3] = (byte) (value >>> 24);
	}
	
	private static long[] toLongs(byte[] bytes) {
		return toLongs(bytes, 0);
	}
	
	private static long[] toLongs(byte[] bytes, int offset) {
		int len = (bytes.length - offset) / 8;
		long[] longs = new long[len];
		for (int i = 0; i < len; i++) {
			int b = offset + i * 8;
			longs[i] = (bytes[b] & 0xFFL) | ((bytes[b + 1] & 0xFFL) << 8) | ((bytes[b + 2] & 0xFFL) << 16)
			        | ((bytes[b + 3] & 0xFFL) << 24) | ((bytes[b + 4] & 0xFFL) << 32) | ((bytes[b + 5] & 0xFFL) << 40)
			        | ((bytes[b + 6] & 0xFFL) << 48) | ((bytes[b + 7] & 0xFFL) << 56);
		}
		return longs;
	}
	
	private static byte[] longsToBytes(long[] longs, int length) {
		byte[] bytes = new byte[length];
		for (int i = 0; i < length && i / 8 < longs.length; i++) {
			bytes[i] = (byte) (longs[i / 8] >>> (8 * (i % 8)));
		}
		return bytes;
	}
	
	private static boolean constantTimeEquals(byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}
		int diff = 0;
		for (int i = 0; i < a.length; i++) {
			diff |= a[i] ^ b[i];
		}
		return diff == 0;
	}
}
