/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
// $ANTLR 2.7.6 (2005-12-22): "ArdenRecognizer.g" -> "ArdenBaseLexer.java"$

package org.openmrs.arden;

import java.io.*;

import antlr.CommonAST;
import antlr.collections.AST;
import antlr.*;
import org.openmrs.arden.MLMObject;
import org.openmrs.arden.MLMObjectElement;

import java.lang.Integer;

import org.openmrs.logic.*;
import org.openmrs.logic.op.*;

import java.util.GregorianCalendar;

import java.io.InputStream;

import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;

import java.io.Reader;
import java.util.Hashtable;

import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

/*************************************************************************************/
public class ArdenBaseLexer extends antlr.CharScanner implements ArdenBaseParserTokenTypes, TokenStream {
	
	public ArdenBaseLexer(InputStream in) {
		this(new ByteBuffer(in));
	}
	
	public ArdenBaseLexer(Reader in) {
		this(new CharBuffer(in));
	}
	
	public ArdenBaseLexer(InputBuffer ib) {
		this(new LexerSharedInputState(ib));
	}
	
	public ArdenBaseLexer(LexerSharedInputState state) {
		super(state);
		caseSensitiveLiterals = false;
		setCaseSensitive(false);
		literals = new Hashtable();
		literals.put(new ANTLRHashString("PRESENT", this), Integer.valueOf(141));
		literals.put(new ANTLRHashString("filename", this), Integer.valueOf(69));
		literals.put(new ANTLRHashString("type", this), Integer.valueOf(82));
		literals.put(new ANTLRHashString("now", this), Integer.valueOf(23));
		literals.put(new ANTLRHashString("priority", this), Integer.valueOf(74));
		literals.put(new ANTLRHashString("LIST", this), Integer.valueOf(147));
		literals.put(new ANTLRHashString("before", this), Integer.valueOf(17));
		literals.put(new ANTLRHashString("CALL", this), Integer.valueOf(163));
		literals.put(new ANTLRHashString("action", this), Integer.valueOf(65));
		literals.put(new ANTLRHashString("time", this), Integer.valueOf(87));
		literals.put(new ANTLRHashString("call", this), Integer.valueOf(89));
		literals.put(new ANTLRHashString("expired", this), Integer.valueOf(114));
		literals.put(new ANTLRHashString("specialist", this), Integer.valueOf(76));
		literals.put(new ANTLRHashString("endif", this), Integer.valueOf(60));
		literals.put(new ANTLRHashString("end", this), Integer.valueOf(100));
		literals.put(new ANTLRHashString("production", this), Integer.valueOf(112));
		literals.put(new ANTLRHashString("months", this), Integer.valueOf(50));
		literals.put(new ANTLRHashString("+", this), Integer.valueOf(178));
		literals.put(new ANTLRHashString("STRING", this), Integer.valueOf(146));
		literals.put(new ANTLRHashString("are", this), Integer.valueOf(7));
		literals.put(new ANTLRHashString("purpose", this), Integer.valueOf(77));
		literals.put(new ANTLRHashString("maximum", this), Integer.valueOf(36));
		literals.put(new ANTLRHashString("exist", this), Integer.valueOf(44));
		literals.put(new ANTLRHashString("knowledge", this), Integer.valueOf(67));
		literals.put(new ANTLRHashString("NUMBER", this), Integer.valueOf(144));
		literals.put(new ANTLRHashString("seconds", this), Integer.valueOf(154));
		literals.put(new ANTLRHashString("author", this), Integer.valueOf(73));
		literals.put(new ANTLRHashString("where", this), Integer.valueOf(43));
		literals.put(new ANTLRHashString("lastest", this), Integer.valueOf(41));
		literals.put(new ANTLRHashString("maintenance", this), Integer.valueOf(66));
		literals.put(new ANTLRHashString("minutes", this), Integer.valueOf(153));
		literals.put(new ANTLRHashString("OCCUR", this), Integer.valueOf(155));
		literals.put(new ANTLRHashString("write", this), Integer.valueOf(20));
		literals.put(new ANTLRHashString("past", this), Integer.valueOf(46));
		literals.put(new ANTLRHashString("*", this), Integer.valueOf(180));
		literals.put(new ANTLRHashString("then", this), Integer.valueOf(32));
		literals.put(new ANTLRHashString("be", this), Integer.valueOf(24));
		literals.put(new ANTLRHashString("Occur", this), Integer.valueOf(156));
		literals.put(new ANTLRHashString("present", this), Integer.valueOf(96));
		literals.put(new ANTLRHashString("to", this), Integer.valueOf(91));
		literals.put(new ANTLRHashString("than", this), Integer.valueOf(15));
		literals.put(new ANTLRHashString("explanation", this), Integer.valueOf(78));
		literals.put(new ANTLRHashString("and", this), Integer.valueOf(4));
		literals.put(new ANTLRHashString("not", this), Integer.valueOf(30));
		literals.put(new ANTLRHashString("occurs", this), Integer.valueOf(159));
		literals.put(new ANTLRHashString("less", this), Integer.valueOf(12));
		literals.put(new ANTLRHashString("NULL", this), Integer.valueOf(142));
		literals.put(new ANTLRHashString("date", this), Integer.valueOf(83));
		literals.put(new ANTLRHashString("validation", this), Integer.valueOf(111));
		literals.put(new ANTLRHashString("evoke", this), Integer.valueOf(162));
		literals.put(new ANTLRHashString("month", this), Integer.valueOf(49));
		literals.put(new ANTLRHashString("from", this), Integer.valueOf(16));
		literals.put(new ANTLRHashString("null", this), Integer.valueOf(99));
		literals.put(new ANTLRHashString("age_min", this), Integer.valueOf(84));
		literals.put(new ANTLRHashString("they", this), Integer.valueOf(29));
		literals.put(new ANTLRHashString("count", this), Integer.valueOf(10));
		literals.put(new ANTLRHashString("last", this), Integer.valueOf(38));
		literals.put(new ANTLRHashString("GT", this), Integer.valueOf(167));
		literals.put(new ANTLRHashString("arden", this), Integer.valueOf(106));
		literals.put(new ANTLRHashString("the", this), Integer.valueOf(14));
		literals.put(new ANTLRHashString("citations", this), Integer.valueOf(80));
		literals.put(new ANTLRHashString("urgency", this), Integer.valueOf(173));
		literals.put(new ANTLRHashString("institution", this), Integer.valueOf(72));
		literals.put(new ANTLRHashString("http", this), Integer.valueOf(98));
		literals.put(new ANTLRHashString("event", this), Integer.valueOf(42));
		literals.put(new ANTLRHashString("minimum", this), Integer.valueOf(34));
		literals.put(new ANTLRHashString("REFUTE", this), Integer.valueOf(132));
		literals.put(new ANTLRHashString("title", this), Integer.valueOf(71));
		literals.put(new ANTLRHashString("was", this), Integer.valueOf(8));
		literals.put(new ANTLRHashString("with", this), Integer.valueOf(90));
		literals.put(new ANTLRHashString("links", this), Integer.valueOf(81));
		literals.put(new ANTLRHashString("it", this), Integer.valueOf(28));
		literals.put(new ANTLRHashString("library", this), Integer.valueOf(68));
		literals.put(new ANTLRHashString("weeks", this), Integer.valueOf(52));
		literals.put(new ANTLRHashString("SEQTO", this), Integer.valueOf(177));
		literals.put(new ANTLRHashString("elseif", this), Integer.valueOf(59));
		literals.put(new ANTLRHashString("version", this), Integer.valueOf(75));
		literals.put(new ANTLRHashString("occur", this), Integer.valueOf(95));
		literals.put(new ANTLRHashString("at", this), Integer.valueOf(21));
		literals.put(new ANTLRHashString("of", this), Integer.valueOf(86));
		literals.put(new ANTLRHashString("is", this), Integer.valueOf(6));
		literals.put(new ANTLRHashString("DURATION", this), Integer.valueOf(145));
		literals.put(new ANTLRHashString("OBJECT", this), Integer.valueOf(148));
		literals.put(new ANTLRHashString("logic", this), Integer.valueOf(64));
		literals.put(new ANTLRHashString("years", this), Integer.valueOf(26));
		literals.put(new ANTLRHashString("or", this), Integer.valueOf(31));
		literals.put(new ANTLRHashString("MERGE", this), Integer.valueOf(174));
		literals.put(new ANTLRHashString("GE", this), Integer.valueOf(169));
		literals.put(new ANTLRHashString("any", this), Integer.valueOf(92));
		literals.put(new ANTLRHashString("BOOLEAN", this), Integer.valueOf(143));
		literals.put(new ANTLRHashString("if", this), Integer.valueOf(27));
		literals.put(new ANTLRHashString("greater", this), Integer.valueOf(13));
		literals.put(new ANTLRHashString("age_max", this), Integer.valueOf(85));
		literals.put(new ANTLRHashString("DATA", this), Integer.valueOf(176));
		literals.put(new ANTLRHashString("min", this), Integer.valueOf(35));
		literals.put(new ANTLRHashString("ASTM-E", this), Integer.valueOf(107));
		literals.put(new ANTLRHashString("first", this), Integer.valueOf(39));
		literals.put(new ANTLRHashString("EQ", this), Integer.valueOf(165));
		literals.put(new ANTLRHashString("OCCURRED", this), Integer.valueOf(160));
		literals.put(new ANTLRHashString("days", this), Integer.valueOf(47));
		literals.put(new ANTLRHashString("data", this), Integer.valueOf(63));
		literals.put(new ANTLRHashString("second", this), Integer.valueOf(94));
		literals.put(new ANTLRHashString("hour", this), Integer.valueOf(150));
		literals.put(new ANTLRHashString("keywords", this), Integer.valueOf(79));
		literals.put(new ANTLRHashString("hours", this), Integer.valueOf(151));
		literals.put(new ANTLRHashString("OCCURS", this), Integer.valueOf(157));
		literals.put(new ANTLRHashString("number", this), Integer.valueOf(97));
		literals.put(new ANTLRHashString("SUPPORT", this), Integer.valueOf(131));
		literals.put(new ANTLRHashString("Occurred", this), Integer.valueOf(161));
		literals.put(new ANTLRHashString("false", this), Integer.valueOf(62));
		literals.put(new ANTLRHashString("exists", this), Integer.valueOf(45));
		literals.put(new ANTLRHashString("conclude", this), Integer.valueOf(57));
		literals.put(new ANTLRHashString("average", this), Integer.valueOf(54));
		literals.put(new ANTLRHashString("LT", this), Integer.valueOf(166));
		literals.put(new ANTLRHashString("/", this), Integer.valueOf(181));
		literals.put(new ANTLRHashString("research", this), Integer.valueOf(93));
		literals.put(new ANTLRHashString("mlmname", this), Integer.valueOf(70));
		literals.put(new ANTLRHashString("minute", this), Integer.valueOf(152));
		literals.put(new ANTLRHashString("were", this), Integer.valueOf(9));
		literals.put(new ANTLRHashString("day", this), Integer.valueOf(48));
		literals.put(new ANTLRHashString("data-driven", this), Integer.valueOf(135));
		literals.put(new ANTLRHashString("max", this), Integer.valueOf(37));
		literals.put(new ANTLRHashString("year", this), Integer.valueOf(25));
		literals.put(new ANTLRHashString("sum", this), Integer.valueOf(55));
		literals.put(new ANTLRHashString("after", this), Integer.valueOf(18));
		literals.put(new ANTLRHashString("SORT", this), Integer.valueOf(175));
		literals.put(new ANTLRHashString("else", this), Integer.valueOf(58));
		literals.put(new ANTLRHashString("in", this), Integer.valueOf(11));
		literals.put(new ANTLRHashString("let", this), Integer.valueOf(22));
		literals.put(new ANTLRHashString("ago", this), Integer.valueOf(19));
		literals.put(new ANTLRHashString("avg", this), Integer.valueOf(53));
		literals.put(new ANTLRHashString("data_driven", this), Integer.valueOf(136));
		literals.put(new ANTLRHashString("median", this), Integer.valueOf(56));
		literals.put(new ANTLRHashString("earliest", this), Integer.valueOf(40));
		literals.put(new ANTLRHashString("week", this), Integer.valueOf(51));
		literals.put(new ANTLRHashString("true", this), Integer.valueOf(61));
		literals.put(new ANTLRHashString("within", this), Integer.valueOf(88));
		literals.put(new ANTLRHashString("-", this), Integer.valueOf(179));
		literals.put(new ANTLRHashString("read", this), Integer.valueOf(33));
		literals.put(new ANTLRHashString("Occurs", this), Integer.valueOf(158));
		literals.put(new ANTLRHashString("testing", this), Integer.valueOf(113));
		literals.put(new ANTLRHashString("NE", this), Integer.valueOf(171));
		literals.put(new ANTLRHashString("LE", this), Integer.valueOf(168));
	}
	
	public Token nextToken() throws TokenStreamException {
		Token theRetToken = null;
		tryAgain: for (;;) {
			Token _token = null;
			int _ttype = Token.INVALID_TYPE;
			resetText();
			try { // for char stream error handling
				try { // for lexical error handling
					switch (LA(1)) {
						case '}': {
							mRCURLY(true);
							theRetToken = _returnToken;
							break;
						}
						case ',': {
							mCOMMA(true);
							theRetToken = _returnToken;
							break;
						}
						case ')': {
							mRPAREN(true);
							theRetToken = _returnToken;
							break;
						}
						case '"': {
							mSTRING_LITERAL(true);
							theRetToken = _returnToken;
							break;
						}
						case '\t':
						case '\n':
						case '\u000c':
						case '\r':
						case ' ': {
							mWS(true);
							theRetToken = _returnToken;
							break;
						}
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
						case 'G':
						case 'H':
						case 'I':
						case 'J':
						case 'K':
						case 'L':
						case 'M':
						case 'N':
						case 'O':
						case 'P':
						case 'Q':
						case 'R':
						case 'S':
						case 'T':
						case 'U':
						case 'V':
						case 'W':
						case 'X':
						case 'Y':
						case 'Z':
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
						case 'g':
						case 'h':
						case 'i':
						case 'j':
						case 'k':
						case 'l':
						case 'm':
						case 'n':
						case 'o':
						case 'p':
						case 'q':
						case 'r':
						case 's':
						case 't':
						case 'u':
						case 'v':
						case 'w':
						case 'x':
						case 'y':
						case 'z': {
							mID(true);
							theRetToken = _returnToken;
							break;
						}
						case '-': {
							mMINUS(true);
							theRetToken = _returnToken;
							break;
						}
						case '_': {
							mUNDERSCORE(true);
							theRetToken = _returnToken;
							break;
						}
						case '@': {
							mAT(true);
							theRetToken = _returnToken;
							break;
						}
						case '=': {
							mEQUALS(true);
							theRetToken = _returnToken;
							break;
						}
						case '[': {
							mLBRACKET(true);
							theRetToken = _returnToken;
							break;
						}
						case ']': {
							mRBRACKET(true);
							theRetToken = _returnToken;
							break;
						}
						case '+': {
							mPLUS(true);
							theRetToken = _returnToken;
							break;
						}
						case '*': {
							mTIMES(true);
							theRetToken = _returnToken;
							break;
						}
						case '&': {
							mAMPERSAND(true);
							theRetToken = _returnToken;
							break;
						}
						case '%': {
							mPERCENT(true);
							theRetToken = _returnToken;
							break;
						}
						case '#': {
							mPOUND(true);
							theRetToken = _returnToken;
							break;
						}
						case '?': {
							mQUESTION(true);
							theRetToken = _returnToken;
							break;
						}
						case '|': {
							mACTION_OP(true);
							theRetToken = _returnToken;
							break;
						}
						default:
							if ((LA(1) == '{') && ((LA(2) >= '\u0000' && LA(2) <= '\u00ff'))) {
								mARDEN_CURLY_BRACKETS(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == ':') && (LA(2) == '/')) {
								mNOT_COMMENT(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '/') && (LA(2) == '/')) {
								mCOMMENT(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '/') && (LA(2) == '*')) {
								mML_COMMENT(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == ':') && (LA(2) == '=')) {
								mBECOMES(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '.') && (LA(2) == '.')) {
								mDOTDOT(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '/') && (LA(2) == '=')) {
								mNOT_EQUALS(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '<') && (LA(2) == '=')) {
								mLTE(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '>') && (LA(2) == '=')) {
								mGTE(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '<') && (LA(2) == '>')) {
								mNE(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == ';') && (LA(2) == ';')) {
								mENDBLOCK(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '{') && (true)) {
								mLCURLY(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '(') && (true)) {
								mLPAREN(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '\'') && (true)) {
								mTERM_LITERAL(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '.') && (true)) {
								mDOT(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '/') && (true)) {
								mDIV(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == ':') && (true)) {
								mCOLON(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == ';') && (true)) {
								mSEMI(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '<') && (true)) {
								mLT(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '>') && (true)) {
								mGT(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '\'') && (true)) {
								mAPOSTROPHE(true);
								theRetToken = _returnToken;
							} else if ((LA(1) == '\'') && (true)) {
								mSINGLE_QUOTE(true);
								theRetToken = _returnToken;
							} else {
								mINTLIT(true);
								theRetToken = _returnToken;
							}
					}
					if (_returnToken == null) {
						continue tryAgain; // found SKIP token
					}
					_ttype = _returnToken.getType();
					_returnToken.setType(_ttype);
					return _returnToken;
				}
				catch (RecognitionException e) {
					throw new TokenStreamRecognitionException(e);
				}
			}
			catch (CharStreamException cse) {
				if (cse instanceof CharStreamIOException) {
					throw new TokenStreamIOException(((CharStreamIOException) cse).io);
				} else {
					throw new TokenStreamException(cse.getMessage());
				}
			}
		}
	}
	
	public final void mARDEN_CURLY_BRACKETS(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = ARDEN_CURLY_BRACKETS;
		int _saveIndex;
		
		mLCURLY(false);
		{
			_loop606: do {
				// nongreedy exit test
				if ((LA(1) == '}') && (true)) {
					break _loop606;
				}
				if (((LA(1) >= '\u0000' && LA(1) <= '\u00ff')) && ((LA(2) >= '\u0000' && LA(2) <= '\u00ff'))) {
					matchNot(EOF_CHAR);
				} else {
					break _loop606;
				}
				
			} while (true);
		}
		mRCURLY(false);
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mLCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = LCURLY;
		int _saveIndex;
		
		match('{');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mRCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = RCURLY;
		int _saveIndex;
		
		match('}');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = NOT_COMMENT;
		int _saveIndex;
		
		match("://");
		{
			_loop610: do {
				if ((_tokenSet_0.member(LA(1)))) {
					{
						match(_tokenSet_0);
					}
				} else {
					break _loop610;
				}
				
			} while (true);
		}
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = COMMENT;
		int _saveIndex;
		
		match("//");
		{
			_loop614: do {
				if ((_tokenSet_1.member(LA(1)))) {
					{
						match(_tokenSet_1);
					}
				} else {
					break _loop614;
				}
				
			} while (true);
		}
		_ttype = Token.SKIP;
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mML_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = ML_COMMENT;
		int _saveIndex;
		
		match("/*");
		{
			_loop618: do {
				if (((LA(1) == '*') && ((LA(2) >= '\u0000' && LA(2) <= '\u00ff'))) && (LA(2) != '/')) {
					match('*');
				} else if ((LA(1) == '\r') && (LA(2) == '\n')) {
					match('\r');
					match('\n');
					newline();
				} else if ((LA(1) == '\r') && ((LA(2) >= '\u0000' && LA(2) <= '\u00ff'))) {
					match('\r');
					newline();
				} else if ((LA(1) == '\n')) {
					match('\n');
					newline();
				} else if ((_tokenSet_2.member(LA(1)))) {
					{
						match(_tokenSet_2);
					}
				} else {
					break _loop618;
				}
				
			} while (true);
		}
		match("*/");
		_ttype = Token.SKIP;
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = DIGIT;
		int _saveIndex;
		
		matchRange('0', '9');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mINTLIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = INTLIT;
		int _saveIndex;
		
		{
			{
				_loop626: do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						{
							int _cnt624 = 0;
							_loop624: do {
								if (((LA(1) >= '0' && LA(1) <= '9')) && (true)) {
									mDIGIT(false);
								} else {
									if (_cnt624 >= 1) {
										break _loop624;
									} else {
										throw new NoViableAltForCharException((char) LA(1), getFilename(), getLine(),
										        getColumn());
									}
								}
								
								_cnt624++;
							} while (true);
						}
						{
							if ((LA(1) == ',')) {
								mCOMMA(false);
							} else {}
							
						}
					} else {
						break _loop626;
					}
					
				} while (true);
			}
			{
				if ((LA(1) == '(')) {
					{
						mLPAREN(false);
						{
							int _cnt630 = 0;
							_loop630: do {
								if (((LA(1) >= '0' && LA(1) <= '9'))) {
									mDIGIT(false);
								} else {
									if (_cnt630 >= 1) {
										break _loop630;
									} else {
										throw new NoViableAltForCharException((char) LA(1), getFilename(), getLine(),
										        getColumn());
									}
								}
								
								_cnt630++;
							} while (true);
						}
						mRPAREN(false);
					}
				} else {}
				
			}
		}
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mLPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTRING_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = STRING_LITERAL;
		int _saveIndex;
		
		_saveIndex = text.length();
		match('"');
		text.setLength(_saveIndex);
		{
			_loop634: do {
				if ((LA(1) == '"') && (LA(2) == '"')) {
					match('"');
					_saveIndex = text.length();
					match('"');
					text.setLength(_saveIndex);
				} else if ((_tokenSet_3.member(LA(1)))) {
					{
						match(_tokenSet_3);
					}
				} else {
					break _loop634;
				}
				
			} while (true);
		}
		{
			if ((LA(1) == '"')) {
				_saveIndex = text.length();
				match('"');
				text.setLength(_saveIndex);
			} else {}
			
		}
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mTERM_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = TERM_LITERAL;
		int _saveIndex;
		
		_saveIndex = text.length();
		match('\'');
		text.setLength(_saveIndex);
		{
			_loop639: do {
				if ((LA(1) == '\'') && (LA(2) == '\'')) {
					match('\'');
					_saveIndex = text.length();
					match('\'');
					text.setLength(_saveIndex);
				} else if ((_tokenSet_4.member(LA(1)))) {
					{
						match(_tokenSet_4);
					}
				} else {
					break _loop639;
				}
				
			} while (true);
		}
		{
			if ((LA(1) == '\'')) {
				_saveIndex = text.length();
				match('\'');
				text.setLength(_saveIndex);
			} else {}
			
		}
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
			switch (LA(1)) {
				case ' ': {
					match(' ');
					break;
				}
				case '\t': {
					match('\t');
					break;
				}
				case '\u000c': {
					match('\f');
					break;
				}
				case '\n':
				case '\r': {
					{
						if ((LA(1) == '\r') && (LA(2) == '\n')) {
							match("\r\n");
						} else if ((LA(1) == '\r') && (true)) {
							match('\r');
						} else if ((LA(1) == '\n')) {
							match('\n');
						} else {
							throw new NoViableAltForCharException((char) LA(1), getFilename(), getLine(), getColumn());
						}
						
					}
					newline();
					break;
				}
				default: {
					throw new NoViableAltForCharException((char) LA(1), getFilename(), getLine(), getColumn());
				}
			}
		}
		_ttype = Token.SKIP;
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mID(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = ID;
		int _saveIndex;
		
		{
			{
				switch (LA(1)) {
					case 'a':
					case 'b':
					case 'c':
					case 'd':
					case 'e':
					case 'f':
					case 'g':
					case 'h':
					case 'i':
					case 'j':
					case 'k':
					case 'l':
					case 'm':
					case 'n':
					case 'o':
					case 'p':
					case 'q':
					case 'r':
					case 's':
					case 't':
					case 'u':
					case 'v':
					case 'w':
					case 'x':
					case 'y':
					case 'z': {
						matchRange('a', 'z');
						break;
					}
					case 'A':
					case 'B':
					case 'C':
					case 'D':
					case 'E':
					case 'F':
					case 'G':
					case 'H':
					case 'I':
					case 'J':
					case 'K':
					case 'L':
					case 'M':
					case 'N':
					case 'O':
					case 'P':
					case 'Q':
					case 'R':
					case 'S':
					case 'T':
					case 'U':
					case 'V':
					case 'W':
					case 'X':
					case 'Y':
					case 'Z': {
						matchRange('A', 'Z');
						break;
					}
					default: {
						throw new NoViableAltForCharException((char) LA(1), getFilename(), getLine(), getColumn());
					}
				}
			}
			{
				_loop648: do {
					switch (LA(1)) {
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
						case 'g':
						case 'h':
						case 'i':
						case 'j':
						case 'k':
						case 'l':
						case 'm':
						case 'n':
						case 'o':
						case 'p':
						case 'q':
						case 'r':
						case 's':
						case 't':
						case 'u':
						case 'v':
						case 'w':
						case 'x':
						case 'y':
						case 'z': {
							matchRange('a', 'z');
							break;
						}
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
						case 'G':
						case 'H':
						case 'I':
						case 'J':
						case 'K':
						case 'L':
						case 'M':
						case 'N':
						case 'O':
						case 'P':
						case 'Q':
						case 'R':
						case 'S':
						case 'T':
						case 'U':
						case 'V':
						case 'W':
						case 'X':
						case 'Y':
						case 'Z': {
							matchRange('A', 'Z');
							break;
						}
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9': {
							matchRange('0', '9');
							break;
						}
						case '-': {
							mMINUS(false);
							break;
						}
						case ',': {
							mCOMMA(false);
							break;
						}
						case '.': {
							mDOT(false);
							break;
						}
						case '/': {
							mDIV(false);
							break;
						}
						case '_': {
							mUNDERSCORE(false);
							break;
						}
						case '@': {
							mAT(false);
							break;
						}
						default: {
							break _loop648;
						}
					}
				} while (true);
			}
		}
		_ttype = testLiteralsTable(_ttype);
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		match('-');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = DOT;
		int _saveIndex;
		
		match('.');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mDIV(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = DIV;
		int _saveIndex;
		
		match('/');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mUNDERSCORE(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = UNDERSCORE;
		int _saveIndex;
		
		match("_");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = AT;
		int _saveIndex;
		
		match("@");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mBECOMES(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = BECOMES;
		int _saveIndex;
		
		match(":=");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = COLON;
		int _saveIndex;
		
		match(':');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEMI(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = SEMI;
		int _saveIndex;
		
		match(';');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQUALS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = EQUALS;
		int _saveIndex;
		
		match('=');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mLBRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = LBRACKET;
		int _saveIndex;
		
		match('[');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mRBRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = RBRACKET;
		int _saveIndex;
		
		match(']');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mDOTDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = DOTDOT;
		int _saveIndex;
		
		match("..");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_EQUALS(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = NOT_EQUALS;
		int _saveIndex;
		
		match("/=");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mLT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = LT;
		int _saveIndex;
		
		match('<');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mLTE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = LTE;
		int _saveIndex;
		
		match("<=");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mGT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = GT;
		int _saveIndex;
		
		match('>');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mGTE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = GTE;
		int _saveIndex;
		
		match(">=");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mTIMES(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = TIMES;
		int _saveIndex;
		
		match('*');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mNE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = NE;
		int _saveIndex;
		
		match("<>");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mAPOSTROPHE(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = APOSTROPHE;
		int _saveIndex;
		
		match('\'');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mAMPERSAND(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = AMPERSAND;
		int _saveIndex;
		
		match('&');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mPERCENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = PERCENT;
		int _saveIndex;
		
		match('%');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mPOUND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = POUND;
		int _saveIndex;
		
		match('#');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mQUESTION(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = QUESTION;
		int _saveIndex;
		
		match('?');
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mENDBLOCK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = ENDBLOCK;
		int _saveIndex;
		
		match(";;");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mACTION_OP(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = ACTION_OP;
		int _saveIndex;
		
		match("||");
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	public final void mSINGLE_QUOTE(boolean _createToken) throws RecognitionException, CharStreamException,
	        TokenStreamException {
		int _ttype;
		Token _token = null;
		int _begin = text.length();
		_ttype = SINGLE_QUOTE;
		int _saveIndex;
		
		_saveIndex = text.length();
		match('\'');
		text.setLength(_saveIndex);
		if (_createToken && _token == null && _ttype != Token.SKIP) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length() - _begin));
		}
		_returnToken = _token;
	}
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[8];
		data[0] = -576460752303432705L;
		for (int i = 1; i <= 3; i++) {
			data[i] = -1L;
		}
		return data;
	}
	
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[8];
		data[0] = -9217L;
		for (int i = 1; i <= 3; i++) {
			data[i] = -1L;
		}
		return data;
	}
	
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[8];
		data[0] = -4398046520321L;
		for (int i = 1; i <= 3; i++) {
			data[i] = -1L;
		}
		return data;
	}
	
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[8];
		data[0] = -17179878401L;
		for (int i = 1; i <= 3; i++) {
			data[i] = -1L;
		}
		return data;
	}
	
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[8];
		data[0] = -549755823105L;
		for (int i = 1; i <= 3; i++) {
			data[i] = -1L;
		}
		return data;
	}
	
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	
}
