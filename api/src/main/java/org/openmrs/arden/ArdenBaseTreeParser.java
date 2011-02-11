// $ANTLR 2.7.6 (2005-12-22): "ArdenRecognizer.g" -> "ArdenBaseTreeParser.java"$

package org.openmrs.arden;

import java.util.Calendar;
import java.util.GregorianCalendar;

import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.collections.AST;
import antlr.collections.impl.BitSet;

/*************************************************************************************/
public class ArdenBaseTreeParser extends antlr.TreeParser implements ArdenBaseTreeParserTokenTypes {
	
	public ArdenBaseTreeParser() {
		tokenNames = _tokenNames;
	}
	
	public final String data(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST data_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				_loop343: do {
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case IF: {
							System.err.println("----------------Starting Data If-------");
							dataIfAST(_t, obj);
							_t = _retTree;
							System.err.println("\n");
							System.err.println("-----------End Data If -------");
							break;
						}
						case CALL: {
							System.err.println("-----------Starting Data CALL -------");
							a = "";
							callSectionAST(_t, obj, a, "data");
							_t = _retTree;
							System.err.println("\n");
							System.err.println("-----------End CALL -------");
							break;
						}
						default:
							if (_t == null)
								_t = ASTNULL;
							if ((_tokenSet_0.member(_t.getType()))) {
								System.err.println("-----------Starting Read -------");
								obj.InitEvaluateList("data", null);
								s = readAST(_t, obj, s);
								_t = _retTree;
								obj.AddToEvaluateList("data", s);
								System.err.println("\n");
								System.err.println("-----------End Read -------");
							} else if ((_t.getType() == EVENT || _t.getType() == ID)) {
								System.err.println("----------------Starting Event-------");
								eventAST(_t);
								_t = _retTree;
								System.err.println("\n");
								System.err.println("-----------End Event -------");
							} else if ((_t.getType() == ENDIF)) {
								AST __t342 = _t;
								AST tmp1_AST_in = (AST) _t;
								match(_t, ENDIF);
								_t = _t.getFirstChild();
								System.err.println("ENDIF FOUND");
								a = "ENDIF";
								obj.AddToEvaluateList("data", a);
								_t = __t342;
								_t = _t.getNextSibling();
							} else if (((_t.getType() >= ELSE && _t.getType() <= ENDIF))) {
								System.err.println("----------------Starting Data Else If-------");
								data_elseifAST(_t, obj);
								_t = _retTree;
								System.err.println("\n");
								System.err.println("-----------End Data Else If -------");
							} else {
								break _loop343;
							}
					}
				} while (true);
			}
			{
				AST tmp2_AST_in = (AST) _t;
				match(_t, ENDBLOCK);
				_t = _t.getNextSibling();
			}
			System.err.println("\n");
			System.err.println("-----------End Data -------");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String readAST(AST _t, MLMObject obj, String instr) throws RecognitionException {
		String s = "";
		
		AST readAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST k = null;
		AST x = null;
		AST y = null;
		AST intlit = null;
		AST dop = null;
		AST z = null;
		AST j = null;
		AST i = null;
		AST t = null;
		AST f = null;
		String a = "", b = "", ret_val = "";
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case READ: {
						AST __t366 = _t;
						AST tmp3_AST_in = (AST) _t;
						match(_t, READ);
						_t = _t.getFirstChild();
						a = readAST(_t, obj, instr);
						_t = _retTree;
						b = readAST(_t, obj, a);
						_t = _retTree;
						_t = __t366;
						_t = _t.getNextSibling();
						s += a;
						break;
					}
					case LAST:
					case LATEST: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case LAST: {
										AST tmp4_AST_in = (AST) _t;
										match(_t, LAST);
										_t = _t.getNextSibling();
										break;
									}
									case LATEST: {
										AST tmp5_AST_in = (AST) _t;
										match(_t, LATEST);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == INTLIT)) {
									k = (AST) _t;
									match(_t, INTLIT);
									_t = _t.getNextSibling();
								} else if ((_tokenSet_0.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
							s += b;
							obj.setReadType("last");
							obj.setHowMany("1");
							if (k != null) {
								obj.setHowMany(k.getText());
								System.err.println("ReadType = Last " + "How many? " + k.getText());
							} else {
								System.err.println("ReadType = Last " + "How many? 1");
							}
							
							b = readAST(_t, obj, instr);
							_t = _retTree;
						}
						break;
					}
					case FIRST:
					case EARLIEST: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case FIRST: {
										AST tmp6_AST_in = (AST) _t;
										match(_t, FIRST);
										_t = _t.getNextSibling();
										break;
									}
									case EARLIEST: {
										AST tmp7_AST_in = (AST) _t;
										match(_t, EARLIEST);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == INTLIT)) {
									x = (AST) _t;
									match(_t, INTLIT);
									_t = _t.getNextSibling();
								} else if ((_tokenSet_0.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
							s += b;
							obj.setReadType("first");
							obj.setHowMany("1");
							if (x != null) {
								obj.setHowMany(x.getText());
								System.err.println("ReadType = First " + "How many? " + x.getText());
							} else {
								System.err.println("ReadType = First " + "How many? 1");
							}
							
							b = readAST(_t, obj, instr);
							_t = _retTree;
						}
						break;
					}
					case MAXIMUM:
					case MAX:
					case INTLIT: {
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case MAXIMUM:
								case MAX: {
									{
										if (_t == null)
											_t = ASTNULL;
										switch (_t.getType()) {
											case MAXIMUM: {
												AST tmp8_AST_in = (AST) _t;
												match(_t, MAXIMUM);
												_t = _t.getNextSibling();
												break;
											}
											case MAX: {
												AST tmp9_AST_in = (AST) _t;
												match(_t, MAX);
												_t = _t.getNextSibling();
												break;
											}
											default: {
												throw new NoViableAltException(_t);
											}
										}
									}
									{
										{
											if (_t == null)
												_t = ASTNULL;
											if ((_t.getType() == INTLIT)) {
												y = (AST) _t;
												match(_t, INTLIT);
												_t = _t.getNextSibling();
											} else if ((_tokenSet_0.member(_t.getType()))) {} else {
												throw new NoViableAltException(_t);
											}
											
										}
										s += b;
										obj.setReadType("max");
										obj.setHowMany("1");
										if (y != null) {
											obj.setHowMany(y.getText());
											System.err.println("ReadType = Maximum " + "How many? " + y.getText());
										} else {
											System.err.println("ReadType = Maximum " + "How many? 1");
										}
										
										b = readAST(_t, obj, instr);
										_t = _retTree;
									}
									break;
								}
								case INTLIT: {
									{
										{
											intlit = (AST) _t;
											match(_t, INTLIT);
											_t = _t.getNextSibling();
											dop = _t == ASTNULL ? null : (AST) _t;
											duration_op(_t);
											_t = _retTree;
										}
										System.err.println("Duration Clause - " + intlit.getText() + " " + dop.getText());
									}
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
						break;
					}
					case MINIMUM:
					case MIN: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case MINIMUM: {
										AST tmp10_AST_in = (AST) _t;
										match(_t, MINIMUM);
										_t = _t.getNextSibling();
										break;
									}
									case MIN: {
										AST tmp11_AST_in = (AST) _t;
										match(_t, MIN);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == INTLIT)) {
									z = (AST) _t;
									match(_t, INTLIT);
									_t = _t.getNextSibling();
								} else if ((_tokenSet_0.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
							s += b;
							obj.setReadType("min");
							obj.setHowMany("1");
							if (z != null) {
								obj.setHowMany(z.getText());
								System.err.println("ReadType = Minimum " + "How many? " + z.getText());
							} else {
								System.err.println("ReadType = Min " + "How many? 1");
							}
							
							b = readAST(_t, obj, instr);
							_t = _retTree;
						}
						break;
					}
					case EXIST:
					case EXISTS: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case EXIST: {
										AST tmp12_AST_in = (AST) _t;
										match(_t, EXIST);
										_t = _t.getNextSibling();
										break;
									}
									case EXISTS: {
										AST tmp13_AST_in = (AST) _t;
										match(_t, EXISTS);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							s += b;
							obj.setReadType("exist");
							obj.setHowMany("1");
							System.err.println("ReadType = Exist");
							b = readAST(_t, obj, instr);
							_t = _retTree;
						}
						break;
					}
					case AVG:
					case AVERAGE: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case AVERAGE: {
										AST tmp14_AST_in = (AST) _t;
										match(_t, AVERAGE);
										_t = _t.getNextSibling();
										break;
									}
									case AVG: {
										AST tmp15_AST_in = (AST) _t;
										match(_t, AVG);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							b = readAST(_t, obj, instr);
							_t = _retTree;
						}
						s += b;
						obj.setReadType("average");
						System.err.println("ReadType = Average");
						break;
					}
					case COUNT: {
						{
							AST tmp16_AST_in = (AST) _t;
							match(_t, COUNT);
							_t = _t.getNextSibling();
							b = readAST(_t, obj, instr);
							_t = _retTree;
						}
						s += b;
						obj.setReadType("count");
						System.err.println("ReadType = Count");
						break;
					}
					case SUM: {
						{
							AST tmp17_AST_in = (AST) _t;
							match(_t, SUM);
							_t = _t.getNextSibling();
							b = readAST(_t, obj, instr);
							_t = _retTree;
						}
						s += b;
						System.err.println("ReadType = Sum");
						break;
					}
					case MEDIAN: {
						{
							AST tmp18_AST_in = (AST) _t;
							match(_t, MEDIAN);
							_t = _t.getNextSibling();
							b = readAST(_t, obj, instr);
							_t = _retTree;
						}
						s += b;
						System.err.println("ReadType = Median");
						break;
					}
					case TRUE:
					case FALSE:
					case ID:
					case ARDEN_CURLY_BRACKETS: {
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case ARDEN_CURLY_BRACKETS: {
									{
										j = (AST) _t;
										match(_t, ARDEN_CURLY_BRACKETS);
										_t = _t.getNextSibling();
									}
									/*s=b;*/System.err.println("Fetch this data - " + j.getText());
									s = j.getText();
									obj.AddConcept(s);
									
									{
										if (_t == null)
											_t = ASTNULL;
										switch (_t.getType()) {
											case WHERE: {
												AST tmp19_AST_in = (AST) _t;
												match(_t, WHERE);
												_t = _t.getNextSibling();
												System.err.println("Where=TRUE");
												where_it_occurredAST(_t, obj, instr);
												_t = _retTree;
												break;
											}
											case 3:
											case COUNT:
											case IF:
											case READ:
											case MINIMUM:
											case MIN:
											case MAXIMUM:
											case MAX:
											case LAST:
											case FIRST:
											case EARLIEST:
											case LATEST:
											case EVENT:
											case EXIST:
											case EXISTS:
											case AVG:
											case AVERAGE:
											case SUM:
											case MEDIAN:
											case ELSE:
											case ELSEIF:
											case ENDIF:
											case TRUE:
											case FALSE:
											case CALL:
											case ENDBLOCK:
											case INTLIT:
											case ID:
											case ARDEN_CURLY_BRACKETS: {
												break;
											}
											default: {
												throw new NoViableAltException(_t);
											}
										}
									}
									break;
								}
								case ID: {
									i = (AST) _t;
									match(_t, ID);
									_t = _t.getNextSibling();
									System.err.println("Variable = " + i.getText());
									a = i.getText();
									s = a;
									obj.SetConceptVar(a);
									break;
								}
								case TRUE: {
									t = (AST) _t;
									match(_t, TRUE);
									_t = _t.getNextSibling();
									s = "true";
									
									break;
								}
								case FALSE: {
									f = (AST) _t;
									match(_t, FALSE);
									_t = _t.getNextSibling();
									s = "false";
									
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
						break;
					}
					default: {
						throw new NoViableAltException(_t);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String eventAST(AST _t) throws RecognitionException {
		String s = "";
		
		AST eventAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST i = null;
		String a, b;
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case EVENT: {
						AST __t421 = _t;
						AST tmp20_AST_in = (AST) _t;
						match(_t, EVENT);
						_t = _t.getFirstChild();
						b = eventAST(_t);
						_t = _retTree;
						_t = __t421;
						_t = _t.getNextSibling();
						break;
					}
					case ID: {
						i = (AST) _t;
						match(_t, ID);
						_t = _t.getNextSibling();
						System.err.println("Event Variable = " + i.getText());
						break;
					}
					default: {
						throw new NoViableAltException(_t);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String dataIfAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST dataIfAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				AST __t347 = _t;
				AST tmp21_AST_in = (AST) _t;
				match(_t, IF);
				_t = _t.getFirstChild();
				obj.InitEvaluateList("data", "IF");
				obj.AddToEvaluateList("data", "IF");
				s = exprAST(_t, "data", obj);
				_t = _retTree;
				AST tmp22_AST_in = (AST) _t;
				match(_t, THEN);
				_t = _t.getNextSibling();
				obj.AddToEvaluateList("data", "THEN");
				{
					_loop349: do {
						if (_t == null)
							_t = ASTNULL;
						if ((_tokenSet_0.member(_t.getType()))) {
							System.err.println("-----------Starting Read -------");
							s = readAST(_t, obj, s);
							_t = _retTree;
							obj.AddToEvaluateList("data", s);
							System.err.println("\n");
							System.err.println("-----------End Read -------");
						} else if ((_t.getType() == EVENT || _t.getType() == ID)) {
							System.err.println("----------------Starting Event-------");
							eventAST(_t);
							_t = _retTree;
							System.err.println("\n");
							System.err.println("-----------End Event -------");
						} else if ((_t.getType() == CALL)) {
							System.err.println("-----------Starting Data CALL -------");
							a = "";
							callSectionAST(_t, obj, a, "data");
							_t = _retTree;
							System.err.println("\n");
							System.err.println("-----------End CALL -------");
						} else {
							break _loop349;
						}
						
					} while (true);
				}
				_t = __t347;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String data_elseifAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST data_elseifAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				{
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case ELSEIF: {
							AST __t353 = _t;
							AST tmp23_AST_in = (AST) _t;
							match(_t, ELSEIF);
							_t = _t.getFirstChild();
							obj.AddToEvaluateList("data", "ELSEIF");
							s = exprAST(_t, "data", obj);
							_t = _retTree;
							AST tmp24_AST_in = (AST) _t;
							match(_t, THEN);
							_t = _t.getNextSibling();
							obj.AddToEvaluateList("data", "THEN");
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_tokenSet_0.member(_t.getType()))) {
									System.err.println("-----------Starting Read -------");
									s = readAST(_t, obj, s);
									_t = _retTree;
									obj.AddToEvaluateList("data", s);
									System.err.println("\n");
									System.err.println("-----------End Read -------");
								} else if ((_t.getType() == EVENT || _t.getType() == ID)) {
									System.err.println("----------------Starting Event-------");
									eventAST(_t);
									_t = _retTree;
									System.err.println("\n");
									System.err.println("-----------End Event -------");
								} else if ((_t.getType() == CALL)) {
									System.err.println("-----------Starting Data CALL -------");
									a = "";
									callSectionAST(_t, obj, a, "data");
									_t = _retTree;
									System.err.println("\n");
									System.err.println("-----------End CALL -------");
								} else {
									throw new NoViableAltException(_t);
								}
								
							}
							_t = __t353;
							_t = _t.getNextSibling();
							break;
						}
						case ELSE: {
							AST __t355 = _t;
							AST tmp25_AST_in = (AST) _t;
							match(_t, ELSE);
							_t = _t.getFirstChild();
							obj.AddToEvaluateList("data", "ELSE");
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_tokenSet_0.member(_t.getType()))) {
									System.err.println("-----------Starting Read -------");
									s = readAST(_t, obj, s);
									_t = _retTree;
									obj.AddToEvaluateList("data", s);
									System.err.println("\n");
									System.err.println("-----------End Read -------");
								} else if ((_t.getType() == EVENT || _t.getType() == ID)) {
									System.err.println("----------------Starting Event-------");
									eventAST(_t);
									_t = _retTree;
									System.err.println("\n");
									System.err.println("-----------End Event -------");
								} else if ((_t.getType() == CALL)) {
									System.err.println("-----------Starting Data CALL -------");
									a = "";
									callSectionAST(_t, obj, a, "data");
									_t = _retTree;
									System.err.println("\n");
									System.err.println("-----------End CALL -------");
								} else {
									throw new NoViableAltException(_t);
								}
								
							}
							_t = __t355;
							_t = _t.getNextSibling();
							break;
						}
						case ENDIF: {
							AST __t357 = _t;
							AST tmp26_AST_in = (AST) _t;
							match(_t, ENDIF);
							_t = _t.getFirstChild();
							System.err.println("ENDIF FOUND");
							_t = __t357;
							_t = _t.getNextSibling();
							break;
						}
						default: {
							throw new NoViableAltException(_t);
						}
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String callSectionAST(AST _t, MLMObject obj, String key, String section) throws RecognitionException {
		String s = "";
		
		AST callSectionAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a = "", b = "";
		
		try { // for error handling
			{
				AST __t439 = _t;
				AST tmp27_AST_in = (AST) _t;
				match(_t, CALL);
				_t = _t.getFirstChild();
				{
					a = callStringAST(_t, section, obj, key);
					_t = _retTree;
				}
				{
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case TRUE:
						case FALSE:
						case NULL:
						case INTLIT:
						case ID:
						case STRING_LITERAL:
						case ACTION_OP:
						case TERM_LITERAL: {
							b = callStringAST(_t, section, obj, a);
							_t = _retTree;
							break;
						}
						case 3:
						case WITH: {
							break;
						}
						default: {
							throw new NoViableAltException(_t);
						}
					}
				}
				
				if (b.equals("")) {
					b = a;
					a = "";
				}
				obj.AddToEvaluateList(section, "call");
				obj.addCall(section, a, b);
				
				{
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case WITH: {
							{
								AST tmp28_AST_in = (AST) _t;
								match(_t, WITH);
								_t = _t.getNextSibling();
								b = callStringAST(_t, section, obj, a);
								_t = _retTree;
							}
							{
								_loop445: do {
									if (_t == null)
										_t = ASTNULL;
									if ((_tokenSet_1.member(_t.getType()))) {
										a = callStringAST(_t, section, obj, a);
										_t = _retTree;
									} else {
										break _loop445;
									}
									
								} while (true);
							}
							break;
						}
						case 3: {
							break;
						}
						default: {
							throw new NoViableAltException(_t);
						}
					}
				}
				_t = __t439;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String exprAST(AST _t, String section, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST exprAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				expr_orAST(_t, section, obj);
				_t = _retTree;
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String where_it_occurredAST(AST _t, MLMObject obj, String key) throws RecognitionException {
		String s = "";
		
		AST where_it_occurredAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST m = null;
		AST n = null;
		AST i = null;
		String a, b, ret_val = "";
		
		try { // for error handling
			if (_t == null)
				_t = ASTNULL;
			switch (_t.getType()) {
				case COUNT:
				case NOT:
				case MINIMUM:
				case MIN:
				case MAXIMUM:
				case MAX:
				case LAST:
				case FIRST:
				case EARLIEST:
				case LATEST:
				case EXIST:
				case EXISTS:
				case AVG:
				case AVERAGE:
				case SUM:
				case MEDIAN:
				case TRUE:
				case FALSE:
				case OF:
				case WITHIN:
				case ANY:
				case NULL:
				case INTLIT:
				case ID:
				case STRING_LITERAL:
				case ACTION_OP:
				case TERM_LITERAL: {
					{
						if (_t == null)
							_t = ASTNULL;
						switch (_t.getType()) {
							case WITHIN: {
								AST tmp29_AST_in = (AST) _t;
								match(_t, WITHIN);
								_t = _t.getNextSibling();
								obj.setWhere("withinPreceding", key);
								{
									AST tmp30_AST_in = (AST) _t;
									match(_t, PAST);
									_t = _t.getNextSibling();
								}
								{
									m = (AST) _t;
									match(_t, INTLIT);
									_t = _t.getNextSibling();
									n = _t == ASTNULL ? null : (AST) _t;
									duration_op(_t);
									_t = _retTree;
								}
								obj.setDuration("past", m.getText(), n.getText(), key);
								System.err.println("Duration Clause - " + m.getText() + " " + n.getText());
								break;
							}
							case COUNT:
							case NOT:
							case MINIMUM:
							case MIN:
							case MAXIMUM:
							case MAX:
							case LAST:
							case FIRST:
							case EARLIEST:
							case LATEST:
							case EXIST:
							case EXISTS:
							case AVG:
							case AVERAGE:
							case SUM:
							case MEDIAN:
							case TRUE:
							case FALSE:
							case OF:
							case ANY:
							case NULL:
							case INTLIT:
							case ID:
							case STRING_LITERAL:
							case ACTION_OP:
							case TERM_LITERAL: {
								a = exprAST(_t, "data", obj);
								_t = _retTree;
								AST tmp31_AST_in = (AST) _t;
								match(_t, TO);
								_t = _t.getNextSibling();
								b = exprAST(_t, "data", obj);
								_t = _retTree;
								break;
							}
							default: {
								throw new NoViableAltException(_t);
							}
						}
					}
					break;
				}
				case AFTER: {
					{
						AST tmp32_AST_in = (AST) _t;
						match(_t, AFTER);
						_t = _t.getNextSibling();
					}
					{
						i = (AST) _t;
						match(_t, ID);
						_t = _t.getNextSibling();
					}
					System.err.println("Variable = " + i.getText());
					break;
				}
				default: {
					throw new NoViableAltException(_t);
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final void duration_op(AST _t) throws RecognitionException {
		
		AST duration_op_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		
		try { // for error handling
			if (_t == null)
				_t = ASTNULL;
			switch (_t.getType()) {
				case YEAR: {
					AST tmp33_AST_in = (AST) _t;
					match(_t, YEAR);
					_t = _t.getNextSibling();
					break;
				}
				case YEARS: {
					AST tmp34_AST_in = (AST) _t;
					match(_t, YEARS);
					_t = _t.getNextSibling();
					break;
				}
				case MONTH: {
					AST tmp35_AST_in = (AST) _t;
					match(_t, MONTH);
					_t = _t.getNextSibling();
					break;
				}
				case MONTHS: {
					AST tmp36_AST_in = (AST) _t;
					match(_t, MONTHS);
					_t = _t.getNextSibling();
					break;
				}
				case WEEK: {
					AST tmp37_AST_in = (AST) _t;
					match(_t, WEEK);
					_t = _t.getNextSibling();
					break;
				}
				case WEEKS: {
					AST tmp38_AST_in = (AST) _t;
					match(_t, WEEKS);
					_t = _t.getNextSibling();
					break;
				}
				case DAY: {
					AST tmp39_AST_in = (AST) _t;
					match(_t, DAY);
					_t = _t.getNextSibling();
					break;
				}
				case DAYS: {
					AST tmp40_AST_in = (AST) _t;
					match(_t, DAYS);
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_hour: {
					AST tmp41_AST_in = (AST) _t;
					match(_t, LITERAL_hour);
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_hours: {
					AST tmp42_AST_in = (AST) _t;
					match(_t, LITERAL_hours);
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_minute: {
					AST tmp43_AST_in = (AST) _t;
					match(_t, LITERAL_minute);
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_minutes: {
					AST tmp44_AST_in = (AST) _t;
					match(_t, LITERAL_minutes);
					_t = _t.getNextSibling();
					break;
				}
				case SECOND: {
					AST tmp45_AST_in = (AST) _t;
					match(_t, SECOND);
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_seconds: {
					AST tmp46_AST_in = (AST) _t;
					match(_t, LITERAL_seconds);
					_t = _t.getNextSibling();
					break;
				}
				default: {
					throw new NoViableAltException(_t);
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
	}
	
	public final String from_of_func_opAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST from_of_func_opAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST k = null;
		AST x = null;
		AST y = null;
		AST z = null;
		String a, b, ret_val = "";
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case TRUE:
					case FALSE:
					case OF:
					case NULL:
					case INTLIT:
					case ID:
					case STRING_LITERAL:
					case ACTION_OP:
					case TERM_LITERAL: {
						break;
					}
					case LAST:
					case LATEST: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case LAST: {
										AST tmp47_AST_in = (AST) _t;
										match(_t, LAST);
										_t = _t.getNextSibling();
										break;
									}
									case LATEST: {
										AST tmp48_AST_in = (AST) _t;
										match(_t, LATEST);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == INTLIT)) {
									k = (AST) _t;
									match(_t, INTLIT);
									_t = _t.getNextSibling();
									AST tmp49_AST_in = (AST) _t;
									match(_t, FROM);
									_t = _t.getNextSibling();
								} else if ((_tokenSet_2.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
						}
						if (k != null) {
							System.err.println("ReadType = Last " + "How many? " + k.getText());
						} else {
							System.err.println("ReadType = Last " + "How many? 1");
						}
						
						break;
					}
					case FIRST:
					case EARLIEST: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case FIRST: {
										AST tmp50_AST_in = (AST) _t;
										match(_t, FIRST);
										_t = _t.getNextSibling();
										break;
									}
									case EARLIEST: {
										AST tmp51_AST_in = (AST) _t;
										match(_t, EARLIEST);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == INTLIT)) {
									x = (AST) _t;
									match(_t, INTLIT);
									_t = _t.getNextSibling();
									AST tmp52_AST_in = (AST) _t;
									match(_t, FROM);
									_t = _t.getNextSibling();
								} else if ((_tokenSet_2.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
						}
						if (x != null) {
							System.err.println("ReadType = First " + "How many? " + x.getText());
						} else {
							System.err.println("ReadType = First " + "How many? 1");
						}
						
						break;
					}
					case MAXIMUM:
					case MAX: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case MAXIMUM: {
										AST tmp53_AST_in = (AST) _t;
										match(_t, MAXIMUM);
										_t = _t.getNextSibling();
										break;
									}
									case MAX: {
										AST tmp54_AST_in = (AST) _t;
										match(_t, MAX);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == INTLIT)) {
									y = (AST) _t;
									match(_t, INTLIT);
									_t = _t.getNextSibling();
									AST tmp55_AST_in = (AST) _t;
									match(_t, FROM);
									_t = _t.getNextSibling();
								} else if ((_tokenSet_2.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
						}
						if (y != null) {
							System.err.println("ReadType = Maximum " + "How many? " + y.getText());
						} else {
							System.err.println("ReadType = Maximum " + "How many? 1");
						}
						
						break;
					}
					case MINIMUM:
					case MIN: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case MINIMUM: {
										AST tmp56_AST_in = (AST) _t;
										match(_t, MINIMUM);
										_t = _t.getNextSibling();
										break;
									}
									case MIN: {
										AST tmp57_AST_in = (AST) _t;
										match(_t, MIN);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == INTLIT)) {
									z = (AST) _t;
									match(_t, INTLIT);
									_t = _t.getNextSibling();
									AST tmp58_AST_in = (AST) _t;
									match(_t, FROM);
									_t = _t.getNextSibling();
								} else if ((_tokenSet_2.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
						}
						if (z != null) {
							System.err.println("ReadType = Minimum " + "How many? " + z.getText());
						} else {
							System.err.println("ReadType = Min " + "How many? 1");
						}
						
						break;
					}
					default: {
						throw new NoViableAltException(_t);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String of_read_func_opAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST of_read_func_opAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b, ret_val = "";
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case TRUE:
					case FALSE:
					case OF:
					case NULL:
					case INTLIT:
					case ID:
					case STRING_LITERAL:
					case ACTION_OP:
					case TERM_LITERAL: {
						break;
					}
					case EXIST:
					case EXISTS: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case EXIST: {
										AST tmp59_AST_in = (AST) _t;
										match(_t, EXIST);
										_t = _t.getNextSibling();
										break;
									}
									case EXISTS: {
										AST tmp60_AST_in = (AST) _t;
										match(_t, EXISTS);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
						}
						obj.AddToEvaluateList("data", "EXIST");
						System.err.println("ReadType = Exist");
						break;
					}
					case AVG:
					case AVERAGE: {
						{
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case AVERAGE: {
										AST tmp61_AST_in = (AST) _t;
										match(_t, AVERAGE);
										_t = _t.getNextSibling();
										break;
									}
									case AVG: {
										AST tmp62_AST_in = (AST) _t;
										match(_t, AVG);
										_t = _t.getNextSibling();
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
						}
						obj.AddToEvaluateList("data", "AVG");
						System.err.println("ReadType = Average");
						break;
					}
					case COUNT: {
						{
							AST tmp63_AST_in = (AST) _t;
							match(_t, COUNT);
							_t = _t.getNextSibling();
						}
						obj.AddToEvaluateList("data", "COUNT");
						System.err.println("ReadType = Count");
						break;
					}
					case SUM: {
						{
							AST tmp64_AST_in = (AST) _t;
							match(_t, SUM);
							_t = _t.getNextSibling();
						}
						obj.AddToEvaluateList("data", "SUM");
						System.err.println("ReadType = Sum");
						break;
					}
					case MEDIAN: {
						{
							AST tmp65_AST_in = (AST) _t;
							match(_t, MEDIAN);
							_t = _t.getNextSibling();
						}
						obj.AddToEvaluateList("data", "MEDIAN");
						System.err.println("ReadType = Median");
						break;
					}
					default: {
						throw new NoViableAltException(_t);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String of_noread_func_opAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST of_noread_func_opAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b, ret_val = "";
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case TRUE:
					case FALSE:
					case OF:
					case NULL:
					case INTLIT:
					case ID:
					case STRING_LITERAL:
					case ACTION_OP:
					case TERM_LITERAL: {
						break;
					}
					case ANY: {
						{
							AST tmp66_AST_in = (AST) _t;
							match(_t, ANY);
							_t = _t.getNextSibling();
						}
						obj.AddToEvaluateList("data", "ANY");
						System.err.println("Any of");
						break;
					}
					default: {
						throw new NoViableAltException(_t);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	/******************** LOGIC ***********************************/
	public final String logic(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST logic_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		Integer i = 1;
		
		try { // for error handling
			{
				_loop435: do {
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case IF: {
							System.err.println("-----------Starting IF -------");
							a = ifAST(_t, obj);
							_t = _retTree;
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case TRUE:
									case FALSE:
									case NULL:
									case INTLIT:
									case ID:
									case STRING_LITERAL:
									case ACTION_OP:
									case TERM_LITERAL: {
										System.err.println("-----------Starting Logic Assignment -------");
										logicAssignmentAST(_t, obj, a);
										_t = _retTree;
										System.err.println("\n");
										System.err.println("-----------End logic assignment -------");
										break;
									}
									case IF:
									case CONCLUDE:
									case ELSE:
									case ELSEIF:
									case ENDIF:
									case CALL:
									case ENDBLOCK: {
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_tokenSet_3.member(_t.getType()))) {
									System.err.println("-----------Starting CONCLUDE -------");
									{
										if (_t == null)
											_t = ASTNULL;
										if ((_t.getType() == CONCLUDE)) {
											concludeAST(_t, obj, a);
											_t = _retTree;
										} else if ((_tokenSet_3.member(_t.getType()))) {} else {
											throw new NoViableAltException(_t);
										}
										
									}
									System.err.println("\n");
									System.err.println("-----------End CONCLUDE -------");
								} else if ((_tokenSet_3.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_tokenSet_3.member(_t.getType()))) {
									System.err.println("-----------Starting CALL -------");
									{
										if (_t == null)
											_t = ASTNULL;
										if ((_t.getType() == CALL)) {
											callSectionAST(_t, obj, a, "logic");
											_t = _retTree;
										} else if ((_tokenSet_3.member(_t.getType()))) {} else {
											throw new NoViableAltException(_t);
										}
										
									}
									System.err.println("\n");
									System.err.println("-----------End CALL -------");
								} else if ((_tokenSet_3.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
							System.err.println("\n");
							System.err.println("-----------End IF -------");
							break;
						}
						case ELSE:
						case ELSEIF: {
							System.err.println("-----------Starting ELSE - ELSEIF -------");
							a = logic_elseifAST(_t, obj, i);
							_t = _retTree;
							{
								if (_t == null)
									_t = ASTNULL;
								switch (_t.getType()) {
									case TRUE:
									case FALSE:
									case NULL:
									case INTLIT:
									case ID:
									case STRING_LITERAL:
									case ACTION_OP:
									case TERM_LITERAL: {
										System.err.println("-----------Starting Logic Assignment -------");
										logicAssignmentAST(_t, obj, a);
										_t = _retTree;
										System.err.println("\n");
										System.err.println("-----------End logic assignment -------");
										break;
									}
									case IF:
									case CONCLUDE:
									case ELSE:
									case ELSEIF:
									case ENDIF:
									case CALL:
									case ENDBLOCK: {
										break;
									}
									default: {
										throw new NoViableAltException(_t);
									}
								}
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_tokenSet_3.member(_t.getType()))) {
									System.err.println("-----------Starting CONCLUDE -------");
									{
										if (_t == null)
											_t = ASTNULL;
										if ((_t.getType() == CONCLUDE)) {
											concludeAST(_t, obj, a);
											_t = _retTree;
										} else if ((_tokenSet_3.member(_t.getType()))) {} else {
											throw new NoViableAltException(_t);
										}
										
									}
									System.err.println("\n");
									System.err.println("-----------End CONCLUDE -------");
								} else if ((_tokenSet_3.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_tokenSet_3.member(_t.getType()))) {
									System.err.println("-----------Starting CALL -------");
									{
										if (_t == null)
											_t = ASTNULL;
										if ((_t.getType() == CALL)) {
											callSectionAST(_t, obj, a, "logic");
											_t = _retTree;
										} else if ((_tokenSet_3.member(_t.getType()))) {} else {
											throw new NoViableAltException(_t);
										}
										
									}
									System.err.println("\n");
									System.err.println("-----------End CALL -------");
								} else if ((_tokenSet_3.member(_t.getType()))) {} else {
									throw new NoViableAltException(_t);
								}
								
							}
							System.err.println("\n");
							System.err.println("-----------End ELSE- ELSEIF -------");
							break;
						}
						case ENDIF: {
							AST __t434 = _t;
							AST tmp67_AST_in = (AST) _t;
							match(_t, ENDIF);
							_t = _t.getFirstChild();
							System.err.println("ENDIF FOUND");
							a = "ENDIF";
							obj.AddToEvaluateList("logic", a);
							_t = __t434;
							_t = _t.getNextSibling();
							break;
						}
						case CONCLUDE: {
							System.err.println("-----------Starting CONCLUDE -------");
							obj.InitEvaluateList("logic", null);
							a = "Conclude_" + Integer.toString(i);
							concludeAST(_t, obj, a);
							_t = _retTree;
							System.err.println("\n");
							System.err.println("-----------End CONCLUDE -------");
							break;
						}
						case CALL: {
							System.err.println("-----------Starting CALL -------");
							obj.InitEvaluateList("logic", null);
							a = "";
							callSectionAST(_t, obj, a, "logic");
							_t = _retTree;
							System.err.println("\n");
							System.err.println("-----------End CALL -------");
							i++;
							break;
						}
						default: {
							break _loop435;
						}
					}
				} while (true);
			}
			{
				AST tmp68_AST_in = (AST) _t;
				match(_t, ENDBLOCK);
				_t = _t.getNextSibling();
			}
			System.err.println("\n");
			System.err.println("-----------End LOGIC -------");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String ifAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST ifAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				AST __t448 = _t;
				AST tmp69_AST_in = (AST) _t;
				match(_t, IF);
				_t = _t.getFirstChild();
				obj.InitEvaluateList("logic", "IF");
				obj.AddToEvaluateList("logic", "IF");
				s = exprAST(_t, "logic", obj);
				_t = _retTree;
				AST tmp70_AST_in = (AST) _t;
				match(_t, THEN);
				_t = _t.getNextSibling();
				obj.AddToEvaluateList("logic", "THEN");
				_t = __t448;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String logicAssignmentAST(AST _t, MLMObject obj, String key) throws RecognitionException {
		String s = "";
		
		AST logicAssignmentAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		
		try { // for error handling
			exprStringAST(_t, "logic", obj, "CTX"/*key Do not use key- depends on context so CTX*/);
			_t = _retTree;
			obj.AddToEvaluateList("logic", "Logic_Assignment");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String concludeAST(AST _t, MLMObject obj, String key) throws RecognitionException {
		String s = "";
		
		AST concludeAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				AST __t519 = _t;
				AST tmp71_AST_in = (AST) _t;
				match(_t, CONCLUDE);
				_t = _t.getFirstChild();
				
				{
					a = "Conclude";
					//	key = a;
					if (key.startsWith("Conclude_")) { // Simply Conclude
						obj.AddToEvaluateList("logic", key);
					} else { // Associate with the Else before
						obj.AddToEvaluateList("logic", a);
					}
				}
				
				{
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case FALSE: {
							AST tmp72_AST_in = (AST) _t;
							match(_t, FALSE);
							_t = _t.getNextSibling();
							System.err.println("***CONCLUDE FALSE ");
							obj.addConcludeVal(false);
							break;
						}
						case TRUE: {
							AST tmp73_AST_in = (AST) _t;
							match(_t, TRUE);
							_t = _t.getNextSibling();
							System.err.println("***CONCLUDE TRUE ");
							obj.addConcludeVal(true);
							break;
						}
						default: {
							throw new NoViableAltException(_t);
						}
					}
				}
				_t = __t519;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String logic_elseifAST(AST _t, MLMObject obj, Integer i) throws RecognitionException {
		String s = "";
		
		AST logic_elseifAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				{
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case ELSEIF: {
							AST __t524 = _t;
							AST tmp74_AST_in = (AST) _t;
							match(_t, ELSEIF);
							_t = _t.getFirstChild();
							a = "ELSEIF";
							System.err.println("ELSEIF");
							obj.AddToEvaluateList("logic", a);
							
							s = exprAST(_t, "logic", obj);
							_t = _retTree;
							AST tmp75_AST_in = (AST) _t;
							match(_t, THEN);
							_t = _t.getNextSibling();
							obj.AddToEvaluateList("logic", "THEN");
							_t = __t524;
							_t = _t.getNextSibling();
							break;
						}
						case ELSE: {
							AST __t525 = _t;
							AST tmp76_AST_in = (AST) _t;
							match(_t, ELSE);
							_t = _t.getFirstChild();
							a = "ELSE_";
							s = a + Integer.toString(i);
							System.err.println("ELSE");
							obj.AddToEvaluateList("logic", s);
							
							_t = __t525;
							_t = _t.getNextSibling();
							break;
						}
						default: {
							throw new NoViableAltException(_t);
						}
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String callStringAST(AST _t, String section, MLMObject obj, String instr) throws RecognitionException {
		String s = "";
		
		AST callStringAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST ift = null;
		AST val = null;
		AST val2 = null;
		AST strlit = null;
		AST termlit = null;
		AST nulllit = null;
		AST id = null;
		AST str = null;
		String a = "", b = "";
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case ID: {
						AST __t479 = _t;
						ift = _t == ASTNULL ? null : (AST) _t;
						match(_t, ID);
						_t = _t.getFirstChild();
						a = ift.getText();
						System.err.println("text = " + a);
						s = a;
						
						_t = __t479;
						_t = _t.getNextSibling();
						break;
					}
					case TRUE:
					case FALSE: {
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case TRUE: {
									AST __t481 = _t;
									AST tmp77_AST_in = (AST) _t;
									match(_t, TRUE);
									_t = _t.getFirstChild();
									_t = __t481;
									_t = _t.getNextSibling();
									break;
								}
								case FALSE: {
									AST __t482 = _t;
									AST tmp78_AST_in = (AST) _t;
									match(_t, FALSE);
									_t = _t.getFirstChild();
									_t = __t482;
									_t = _t.getNextSibling();
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
						break;
					}
					case INTLIT: {
						{
							val = (AST) _t;
							match(_t, INTLIT);
							_t = _t.getNextSibling();
							
							b = val.getText();
							obj.addParameter(section, b);
							
							{
								{
									if (_t == null)
										_t = ASTNULL;
									switch (_t.getType()) {
										case DOT: {
											AST tmp79_AST_in = (AST) _t;
											match(_t, DOT);
											_t = _t.getNextSibling();
											val2 = (AST) _t;
											match(_t, INTLIT);
											_t = _t.getNextSibling();
											
											a = val2.getText();
											obj.addParameter(section, a);
											
											break;
										}
										case 3:
										case TRUE:
										case FALSE:
										case WITH:
										case NULL:
										case INTLIT:
										case ID:
										case STRING_LITERAL:
										case ACTION_OP:
										case TERM_LITERAL: {
											break;
										}
										default: {
											throw new NoViableAltException(_t);
										}
									}
								}
							}
						}
						break;
					}
					case STRING_LITERAL: {
						{
							strlit = (AST) _t;
							match(_t, STRING_LITERAL);
							_t = _t.getNextSibling();
							
							b = strlit.getText();
							obj.addParameter(section, b);
							
						}
						break;
					}
					case TERM_LITERAL: {
						{
							termlit = (AST) _t;
							match(_t, TERM_LITERAL);
							_t = _t.getNextSibling();
							
							b = termlit.getText();
							obj.addParameter(section, b);
							
						}
						break;
					}
					case NULL: {
						{
							nulllit = (AST) _t;
							match(_t, NULL);
							_t = _t.getNextSibling();
							
							b = nulllit.getText();
							obj.addParameter(section, b);
							
						}
						break;
					}
					case ACTION_OP: {
						AST __t489 = _t;
						AST tmp80_AST_in = (AST) _t;
						match(_t, ACTION_OP);
						_t = _t.getFirstChild();
						id = (AST) _t;
						match(_t, ID);
						_t = _t.getNextSibling();
						a = id.getText();
						AST tmp81_AST_in = (AST) _t;
						match(_t, ACTION_OP);
						_t = _t.getNextSibling();
						str = (AST) _t;
						match(_t, STRING_LITERAL);
						_t = _t.getNextSibling();
						b = str.getText();
						
						_t = __t489;
						_t = _t.getNextSibling();
						break;
					}
					default: {
						throw new NoViableAltException(_t);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String exprStringAST(AST _t, String section, MLMObject obj, String instr) throws RecognitionException {
		String s = "";
		
		AST exprStringAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST ift = null;
		AST val = null;
		AST val2 = null;
		AST strlit = null;
		AST termlit = null;
		AST nulllit = null;
		AST id = null;
		AST str = null;
		String a = "", b = "";
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case ID: {
						AST __t492 = _t;
						ift = _t == ASTNULL ? null : (AST) _t;
						match(_t, ID);
						_t = _t.getFirstChild();
						a = ift.getText();
						System.err.println("text = " + a);
						if (instr.equals("")) {
							obj.AddToEvaluateList(section, a);
							s = a;
							
							//  	obj.RetrieveConcept(a); 
						} else if (instr.startsWith("__Temp__")) {
							obj.SetAnswerListKey(section, a); // adds key only if a previously formed list found...
							obj.AddToEvaluateList(section, a);// Add key to evaluate list if none found before
							
						} else if (instr.equals("CTX")) {
							s = a;
							// do nothing for now
						} else if (instr.equals("notnull")) {
							obj.AddToEvaluateList(section, a);
							if (obj.GetMLMObjectElement(a) == null) {
								s = "Func_1"; // Func like Exist..          
							} else {
								s = a;
							}
							
						} else { // if instr is not empty then we are evaluating RHS of an equation, it can be a non string literal
							obj.SetAnswer(section, a, instr);
							s = a;
						}
						
						_t = __t492;
						_t = _t.getNextSibling();
						break;
					}
					case TRUE:
					case FALSE: {
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case TRUE: {
									AST __t494 = _t;
									AST tmp82_AST_in = (AST) _t;
									match(_t, TRUE);
									_t = _t.getFirstChild();
									obj.SetAnswer(section, true, instr);
									_t = __t494;
									_t = _t.getNextSibling();
									break;
								}
								case FALSE: {
									AST __t495 = _t;
									AST tmp83_AST_in = (AST) _t;
									match(_t, FALSE);
									_t = _t.getFirstChild();
									obj.SetAnswer(section, false, instr);
									_t = __t495;
									_t = _t.getNextSibling();
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
						break;
					}
					case INTLIT: {
						{
							val = (AST) _t;
							match(_t, INTLIT);
							_t = _t.getNextSibling();
							
							b = val.getText();
							Integer i = Integer.parseInt(b);
							Double idbl = null;
							
							{
								{
									if (_t == null)
										_t = ASTNULL;
									switch (_t.getType()) {
										case DOT: {
											AST tmp84_AST_in = (AST) _t;
											match(_t, DOT);
											_t = _t.getNextSibling();
											val2 = (AST) _t;
											match(_t, INTLIT);
											_t = _t.getNextSibling();
											
											a = val2.getText();
											String dbl = b + "." + a;
											idbl = Double.parseDouble(dbl);
											
											break;
										}
										case 3:
										case AND:
										case COUNT:
										case IN:
										case LESS:
										case GREATER:
										case IF:
										case OR:
										case THEN:
										case READ:
										case MINIMUM:
										case MIN:
										case MAXIMUM:
										case MAX:
										case LAST:
										case FIRST:
										case EARLIEST:
										case LATEST:
										case EVENT:
										case EXIST:
										case EXISTS:
										case AVG:
										case AVERAGE:
										case SUM:
										case MEDIAN:
										case CONCLUDE:
										case ELSE:
										case ELSEIF:
										case ENDIF:
										case TRUE:
										case FALSE:
										case CALL:
										case TO:
										case NULL:
										case ENDBLOCK:
										case INTLIT:
										case ID:
										case GT:
										case GTE:
										case LT:
										case LTE:
										case COMMA:
										case STRING_LITERAL:
										case ARDEN_CURLY_BRACKETS:
										case EQUALS:
										case ACTION_OP:
										case TERM_LITERAL: {
											break;
										}
										default: {
											throw new NoViableAltException(_t);
										}
									}
								}
							}
							
							if (idbl == null) {
								obj.SetAnswer(section, i, instr);
							} else {
								obj.SetAnswer(section, idbl, instr);
							}
							
						}
						break;
					}
					case STRING_LITERAL: {
						{
							strlit = (AST) _t;
							match(_t, STRING_LITERAL);
							_t = _t.getNextSibling();
							
							b = strlit.getText();
							obj.SetAnswer(section, b, instr);
							
						}
						break;
					}
					case TERM_LITERAL: {
						{
							termlit = (AST) _t;
							match(_t, TERM_LITERAL);
							_t = _t.getNextSibling();
							
							if (instr.equals("")) {
								// LHS , example - 'ABC' in Variable
								b = termlit.getText();
								s = obj.SetAnswerList(section, b, instr);
							} else if (instr.equals("CTX")) {
								s = a;
								// do nothing for now
							} else if (instr.equals("notnull")) {

							} else {
								b = termlit.getText();
								obj.SetAnswer(section, b, instr);
							}
							
						}
						break;
					}
					case NULL: {
						{
							nulllit = (AST) _t;
							match(_t, NULL);
							_t = _t.getNextSibling();
							
							b = nulllit.getText();
							obj.SetAnswer(section, null, instr);
							
						}
						break;
					}
					case ACTION_OP: {
						AST __t502 = _t;
						AST tmp85_AST_in = (AST) _t;
						match(_t, ACTION_OP);
						_t = _t.getFirstChild();
						id = (AST) _t;
						match(_t, ID);
						_t = _t.getNextSibling();
						a = id.getText();
						AST tmp86_AST_in = (AST) _t;
						match(_t, ACTION_OP);
						_t = _t.getNextSibling();
						str = (AST) _t;
						match(_t, STRING_LITERAL);
						_t = _t.getNextSibling();
						b = str.getText();
						obj.addLogicAssignment(a, b);
						_t = __t502;
						_t = _t.getNextSibling();
						break;
					}
					default: {
						throw new NoViableAltException(_t);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String expr_comparisonAST(AST _t, String section, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST expr_comparisonAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				if ((_tokenSet_1.member(_t.getType()))) {
					{
						a = exprStringAST(_t, section, obj, "");
						_t = _retTree;
						s = a;
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case IN:
								case LESS:
								case GREATER:
								case GT:
								case GTE:
								case LT:
								case LTE:
								case EQUALS: {
									{
										if (_t == null)
											_t = ASTNULL;
										if ((_tokenSet_4.member(_t.getType()))) {
											simple_comp_opAST(_t, section, obj, a);
											_t = _retTree;
										} else if ((_tokenSet_5.member(_t.getType()))) {
											binary_comp_opAST(_t, section, obj, a);
											_t = _retTree;
										} else {
											throw new NoViableAltException(_t);
										}
										
									}
									{
										_loop456: do {
											if (_t == null)
												_t = ASTNULL;
											if ((_tokenSet_1.member(_t.getType()))) {
												b = exprStringAST(_t, section, obj, a);
												_t = _retTree;
											} else {
												break _loop456;
											}
											
										} while (true);
									}
									break;
								}
								case 3:
								case AND:
								case COUNT:
								case IF:
								case OR:
								case THEN:
								case READ:
								case MINIMUM:
								case MIN:
								case MAXIMUM:
								case MAX:
								case LAST:
								case FIRST:
								case EARLIEST:
								case LATEST:
								case EVENT:
								case EXIST:
								case EXISTS:
								case AVG:
								case AVERAGE:
								case SUM:
								case MEDIAN:
								case ELSE:
								case ELSEIF:
								case ENDIF:
								case TRUE:
								case FALSE:
								case CALL:
								case TO:
								case ENDBLOCK:
								case INTLIT:
								case ID:
								case ARDEN_CURLY_BRACKETS: {
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
					}
				} else if ((_tokenSet_6.member(_t.getType()))) {
					expr_functionAST(_t, obj);
					_t = _retTree;
					{
						a = exprStringAST(_t, section, obj, "notnull");
						_t = _retTree;
						s = a;
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case IN:
								case LESS:
								case GREATER:
								case EQUALS: {
									{
										binary_comp_opAST(_t, section, obj, a);
										_t = _retTree;
									}
									b = exprStringAST(_t, section, obj, a);
									_t = _retTree;
									{
										_loop461: do {
											if (_t == null)
												_t = ASTNULL;
											if ((_t.getType() == COMMA)) {
												AST tmp87_AST_in = (AST) _t;
												match(_t, COMMA);
												_t = _t.getNextSibling();
												exprStringAST(_t, section, obj, a);
												_t = _retTree;
												s = a;
											} else {
												break _loop461;
											}
											
										} while (true);
									}
									break;
								}
								case 3:
								case AND:
								case COUNT:
								case IF:
								case OR:
								case THEN:
								case READ:
								case MINIMUM:
								case MIN:
								case MAXIMUM:
								case MAX:
								case LAST:
								case FIRST:
								case EARLIEST:
								case LATEST:
								case EVENT:
								case EXIST:
								case EXISTS:
								case AVG:
								case AVERAGE:
								case SUM:
								case MEDIAN:
								case ELSE:
								case ELSEIF:
								case ENDIF:
								case TRUE:
								case FALSE:
								case CALL:
								case TO:
								case ENDBLOCK:
								case INTLIT:
								case ID:
								case ARDEN_CURLY_BRACKETS: {
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
					}
				} else {
					throw new NoViableAltException(_t);
				}
				
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String simple_comp_opAST(AST _t, String section, MLMObject obj, String key) throws RecognitionException {
		String s = "";
		
		AST simple_comp_opAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			if (_t == null)
				_t = ASTNULL;
			switch (_t.getType()) {
				case EQUALS: {
					AST __t504 = _t;
					AST tmp88_AST_in = (AST) _t;
					match(_t, EQUALS);
					_t = _t.getFirstChild();
					
					System.err.println("Found = ");
					obj.addCompOperator(section, EQUALS, key);
					
					_t = __t504;
					_t = _t.getNextSibling();
					break;
				}
				case GTE: {
					AST __t505 = _t;
					AST tmp89_AST_in = (AST) _t;
					match(_t, GTE);
					_t = _t.getFirstChild();
					
					System.err.println("Found >= ");
					obj.addCompOperator(section, GTE, key);
					
					_t = __t505;
					_t = _t.getNextSibling();
					break;
				}
				case GT: {
					AST __t506 = _t;
					AST tmp90_AST_in = (AST) _t;
					match(_t, GT);
					_t = _t.getFirstChild();
					
					System.err.println("Found > ");
					obj.addCompOperator(section, GT, key);
					
					_t = __t506;
					_t = _t.getNextSibling();
					break;
				}
				case LT: {
					AST __t507 = _t;
					AST tmp91_AST_in = (AST) _t;
					match(_t, LT);
					_t = _t.getFirstChild();
					
					System.err.println("Found < ");
					obj.addCompOperator(section, LT, key);
					
					_t = __t507;
					_t = _t.getNextSibling();
					break;
				}
				case LTE: {
					AST __t508 = _t;
					AST tmp92_AST_in = (AST) _t;
					match(_t, LTE);
					_t = _t.getFirstChild();
					
					System.err.println("Found <= ");
					obj.addCompOperator(section, LTE, key);
					
					_t = __t508;
					_t = _t.getNextSibling();
					AST __t509 = _t;
					AST tmp93_AST_in = (AST) _t;
					match(_t, NE);
					_t = _t.getFirstChild();
					
					System.err.println("Found <> ");
					obj.addCompOperator(section, NE, key);
					
					_t = __t509;
					_t = _t.getNextSibling();
					break;
				}
				default: {
					throw new NoViableAltException(_t);
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String binary_comp_opAST(AST _t, String section, MLMObject obj, String key) throws RecognitionException {
		String s = "";
		
		AST binary_comp_opAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			if (_t == null)
				_t = ASTNULL;
			switch (_t.getType()) {
				case EQUALS: {
					AST __t511 = _t;
					AST tmp94_AST_in = (AST) _t;
					match(_t, EQUALS);
					_t = _t.getFirstChild();
					
					System.err.println("Found = ");
					obj.addCompOperator(section, EQUALS, key);
					
					_t = __t511;
					_t = _t.getNextSibling();
					break;
				}
				case GREATER: {
					AST __t512 = _t;
					AST tmp95_AST_in = (AST) _t;
					match(_t, GREATER);
					_t = _t.getFirstChild();
					AST tmp96_AST_in = (AST) _t;
					match(_t, THAN);
					_t = _t.getNextSibling();
					
					System.err.println("Found > ");
					obj.addCompOperator(section, GT, key);
					
					{
						if (_t == null)
							_t = ASTNULL;
						switch (_t.getType()) {
							case OR: {
								AST tmp97_AST_in = (AST) _t;
								match(_t, OR);
								_t = _t.getNextSibling();
								AST tmp98_AST_in = (AST) _t;
								match(_t, EQUAL);
								_t = _t.getNextSibling();
								break;
							}
							case 3: {
								break;
							}
							default: {
								throw new NoViableAltException(_t);
							}
						}
					}
					
					System.err.println("Found >= ");
					obj.addCompOperator(section, GTE, key);
					
					_t = __t512;
					_t = _t.getNextSibling();
					break;
				}
				case IN: {
					AST __t516 = _t;
					AST tmp99_AST_in = (AST) _t;
					match(_t, IN);
					_t = _t.getFirstChild();
					
					System.err.println("Found IN ");
					obj.addCompOperator(section, IN, key);
					
					_t = __t516;
					_t = _t.getNextSibling();
					break;
				}
				default:
					if (_t == null)
						_t = ASTNULL;
					if ((_t.getType() == LESS)) {
						AST __t514 = _t;
						AST tmp100_AST_in = (AST) _t;
						match(_t, LESS);
						_t = _t.getFirstChild();
						AST tmp101_AST_in = (AST) _t;
						match(_t, THAN);
						_t = _t.getNextSibling();
						
						System.err.println("Found < ");
						obj.addCompOperator(section, LT, key);
						
						_t = __t514;
						_t = _t.getNextSibling();
					} else if ((_t.getType() == LESS)) {
						AST __t515 = _t;
						AST tmp102_AST_in = (AST) _t;
						match(_t, LESS);
						_t = _t.getFirstChild();
						AST tmp103_AST_in = (AST) _t;
						match(_t, THAN);
						_t = _t.getNextSibling();
						AST tmp104_AST_in = (AST) _t;
						match(_t, OR);
						_t = _t.getNextSibling();
						AST tmp105_AST_in = (AST) _t;
						match(_t, EQUAL);
						_t = _t.getNextSibling();
						
						System.err.println("Found <= ");
						obj.addCompOperator(section, LTE, key);
						
						_t = __t515;
						_t = _t.getNextSibling();
					} else {
						throw new NoViableAltException(_t);
					}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String expr_functionAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST expr_functionAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				if ((_tokenSet_7.member(_t.getType()))) {
					from_of_func_opAST(_t, obj);
					_t = _retTree;
					{
						if (_t == null)
							_t = ASTNULL;
						switch (_t.getType()) {
							case OF: {
								AST tmp106_AST_in = (AST) _t;
								match(_t, OF);
								_t = _t.getNextSibling();
								break;
							}
							case TRUE:
							case FALSE:
							case NULL:
							case INTLIT:
							case ID:
							case STRING_LITERAL:
							case ACTION_OP:
							case TERM_LITERAL: {
								break;
							}
							default: {
								throw new NoViableAltException(_t);
							}
						}
					}
				} else if ((_tokenSet_8.member(_t.getType()))) {
					of_read_func_opAST(_t, obj);
					_t = _retTree;
					{
						if (_t == null)
							_t = ASTNULL;
						switch (_t.getType()) {
							case OF: {
								AST tmp107_AST_in = (AST) _t;
								match(_t, OF);
								_t = _t.getNextSibling();
								break;
							}
							case TRUE:
							case FALSE:
							case NULL:
							case INTLIT:
							case ID:
							case STRING_LITERAL:
							case ACTION_OP:
							case TERM_LITERAL: {
								break;
							}
							default: {
								throw new NoViableAltException(_t);
							}
						}
					}
				} else if ((_tokenSet_9.member(_t.getType()))) {
					of_noread_func_opAST(_t, obj);
					_t = _retTree;
					{
						if (_t == null)
							_t = ASTNULL;
						switch (_t.getType()) {
							case OF: {
								AST tmp108_AST_in = (AST) _t;
								match(_t, OF);
								_t = _t.getNextSibling();
								break;
							}
							case TRUE:
							case FALSE:
							case NULL:
							case INTLIT:
							case ID:
							case STRING_LITERAL:
							case ACTION_OP:
							case TERM_LITERAL: {
								break;
							}
							default: {
								throw new NoViableAltException(_t);
							}
						}
					}
				} else {
					throw new NoViableAltException(_t);
				}
				
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String expr_notAST(AST _t, String section, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST expr_notAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		
		try { // for error handling
			if (_t == null)
				_t = ASTNULL;
			switch (_t.getType()) {
				case COUNT:
				case MINIMUM:
				case MIN:
				case MAXIMUM:
				case MAX:
				case LAST:
				case FIRST:
				case EARLIEST:
				case LATEST:
				case EXIST:
				case EXISTS:
				case AVG:
				case AVERAGE:
				case SUM:
				case MEDIAN:
				case TRUE:
				case FALSE:
				case OF:
				case ANY:
				case NULL:
				case INTLIT:
				case ID:
				case STRING_LITERAL:
				case ACTION_OP:
				case TERM_LITERAL: {
					expr_comparisonAST(_t, section, obj);
					_t = _retTree;
					break;
				}
				case NOT: {
					{
						AST tmp109_AST_in = (AST) _t;
						match(_t, NOT);
						_t = _t.getNextSibling();
						obj.AddToEvaluateList(section, "NOT");
						expr_comparisonAST(_t, section, obj);
						_t = _retTree;
					}
					break;
				}
				default: {
					throw new NoViableAltException(_t);
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String expr_andAST(AST _t, String section, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST expr_andAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		
		try { // for error handling
			expr_notAST(_t, section, obj);
			_t = _retTree;
			{
				_loop466: do {
					if (_t == null)
						_t = ASTNULL;
					if ((_t.getType() == AND)) {
						AST tmp110_AST_in = (AST) _t;
						match(_t, AND);
						_t = _t.getNextSibling();
						obj.AddToEvaluateList(section, "AND");
						expr_notAST(_t, section, obj);
						_t = _retTree;
					} else {
						break _loop466;
					}
					
				} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String expr_orAST(AST _t, String section, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST expr_orAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		
		try { // for error handling
			expr_andAST(_t, section, obj);
			_t = _retTree;
			{
				_loop469: do {
					if (_t == null)
						_t = ASTNULL;
					if ((_t.getType() == OR)) {
						AST tmp111_AST_in = (AST) _t;
						match(_t, OR);
						_t = _t.getNextSibling();
						obj.AddToEvaluateList(section, "OR");
						expr_andAST(_t, section, obj);
						_t = _retTree;
					} else {
						break _loop469;
					}
					
				} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	/*********************** ACTION *******************************************/
	public final String action(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST action_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a, b;
		
		try { // for error handling
			{
				_loop528: do {
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case WRITE: {
							System.err.println("-----------Starting Write -------");
							s = writeAST(_t, obj);
							_t = _retTree;
							System.err.println("\n");
							System.err.println("-----------End Write -------");
							break;
						}
						case CALL: {
							System.err.println("-----------Starting CALL -------");
							obj.InitEvaluateList("action", null);
							a = "";
							callSectionAST(_t, obj, a, "action");
							_t = _retTree;
							System.err.println("\n");
							System.err.println("-----------End CALL -------");
							break;
						}
						default: {
							break _loop528;
						}
					}
				} while (true);
			}
			{
				AST tmp112_AST_in = (AST) _t;
				match(_t, ENDBLOCK);
				_t = _t.getNextSibling();
			}
			System.err.println("\n");
			System.err.println("-----------End Action -------");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String writeAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST writeAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST id = null;
		AST i = null;
		AST idat = null;
		String a = "", b = "";
		
		try { // for error handling
			{
				AST __t532 = _t;
				AST tmp113_AST_in = (AST) _t;
				match(_t, WRITE);
				_t = _t.getFirstChild();
				{
					_loop536: do {
						if (_t == null)
							_t = ASTNULL;
						switch (_t.getType()) {
							case ACTION_OP: {
								{
									AST tmp114_AST_in = (AST) _t;
									match(_t, ACTION_OP);
									_t = _t.getNextSibling();
									id = (AST) _t;
									match(_t, ID);
									_t = _t.getNextSibling();
									a = id.getText();
									//b= obj.getUserVarVal(a);
									b = "||" + a + "||";
									s += b;
									AST tmp115_AST_in = (AST) _t;
									match(_t, ACTION_OP);
									_t = _t.getNextSibling();
								}
								break;
							}
							case STRING_LITERAL: {
								{
									i = (AST) _t;
									match(_t, STRING_LITERAL);
									_t = _t.getNextSibling();
									s += i.getText();
								}
								break;
							}
							default: {
								break _loop536;
							}
						}
					} while (true);
				}
				obj.addAction(s);
				{
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case AT: {
							AST tmp116_AST_in = (AST) _t;
							match(_t, AT);
							_t = _t.getNextSibling();
							idat = (AST) _t;
							match(_t, ID);
							_t = _t.getNextSibling();
							obj.setAt(idat.getText());
							break;
						}
						case 3: {
							break;
						}
						default: {
							throw new NoViableAltException(_t);
						}
					}
				}
				_t = __t532;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	/*********************** KNOWLEDGE *******************************************/
	public final String knowledge(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST knowledge_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a = "", b = "";
		
		try { // for error handling
			{
				AST __t540 = _t;
				AST tmp117_AST_in = (AST) _t;
				match(_t, KNOWLEDGE);
				_t = _t.getFirstChild();
				{
					_loop549: do {
						if (_t == null)
							_t = ASTNULL;
						if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == DATA)) {
									AST __t543 = _t;
									AST tmp118_AST_in = (AST) _t;
									match(_t, DATA);
									_t = _t.getFirstChild();
									AST tmp119_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									System.err.println("-----------Starting Data -------");
									s = data(_t, obj);
									_t = _retTree;
									System.err.println("\n");
									System.err.println("-----------End Data -------");
									_t = __t543;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == PRIORITY)) {
									AST __t544 = _t;
									AST tmp120_AST_in = (AST) _t;
									match(_t, PRIORITY);
									_t = _t.getFirstChild();
									AST tmp121_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Priority: ";
									b = doubleAST(_t, obj);
									_t = _retTree;
									obj.setPriority(b);
									s += b;
									s += "\n";
									_t = __t544;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == LOGIC)) {
									AST __t545 = _t;
									AST tmp122_AST_in = (AST) _t;
									match(_t, LOGIC);
									_t = _t.getFirstChild();
									AST tmp123_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									System.err.println("-----------Starting Logic -------");
									s = logic(_t, obj);
									_t = _retTree;
									System.err.println("\n");
									System.err.println("-----------End Logic -------");
									_t = __t545;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == ACTION)) {
									AST __t546 = _t;
									AST tmp124_AST_in = (AST) _t;
									match(_t, ACTION);
									_t = _t.getFirstChild();
									AST tmp125_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									System.err.println("-----------Starting ACTION -------");
									s = action(_t, obj);
									_t = _retTree;
									System.err.println("\n");
									System.err.println("-----------End Action -------");
									_t = __t546;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == AGE_MIN)) {
									AST __t547 = _t;
									AST tmp126_AST_in = (AST) _t;
									match(_t, AGE_MIN);
									_t = _t.getFirstChild();
									AST tmp127_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Age_Min: ";
									b = doubleAST(_t, obj);
									_t = _retTree;
									obj.setAgeMin(b);
									s += b;
									s += "\n";
									_t = __t547;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == AGE_MAX)) {
									AST __t548 = _t;
									AST tmp128_AST_in = (AST) _t;
									match(_t, AGE_MAX);
									_t = _t.getFirstChild();
									AST tmp129_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Age_Max: ";
									b = doubleAST(_t, obj);
									_t = _retTree;
									obj.setAgeMax(b);
									s += b;
									s += "\n";
									_t = __t548;
									_t = _t.getNextSibling();
								} else if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
									a = textAST(_t, obj);
									_t = _retTree;
									s += a;
									AST tmp130_AST_in = (AST) _t;
									match(_t, ENDBLOCK);
									_t = _t.getNextSibling();
									s += "\n";
								} else {
									throw new NoViableAltException(_t);
								}
								
							}
						} else {
							break _loop549;
						}
						
					} while (true);
				}
				_t = __t540;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String doubleAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST doubleAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST str = null;
		String a = "", b = "";
		
		try { // for error handling
			{
				{
					{
						_loop587: do {
							if (_t == null)
								_t = ASTNULL;
							if ((_tokenSet_10.member(_t.getType()))) {
								{
									str = (AST) _t;
									match(_t, _tokenSet_10);
									_t = _t.getNextSibling();
								}
								a = str.getText();
								s += a; /*System.err.println(s);*/
							} else {
								break _loop587;
							}
							
						} while (true);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String textAST(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST textAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST str = null;
		String a = "", b = "";
		
		try { // for error handling
			{
				{
					{
						_loop581: do {
							if (_t == null)
								_t = ASTNULL;
							if ((_tokenSet_10.member(_t.getType()))) {
								{
									str = (AST) _t;
									match(_t, _tokenSet_10);
									_t = _t.getNextSibling();
								}
								a = " " + str.getText();
								s += a; /*System.err.println(s);*/
							} else {
								break _loop581;
							}
							
						} while (true);
					}
				}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	/*********************** KNOWLEDGE TEXT- To return data, logic and action text to populate the DB *******************************************/
	public final String knowledge_text(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST knowledge_text_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a = "", b = "";
		
		try { // for error handling
			{
				AST __t552 = _t;
				AST tmp131_AST_in = (AST) _t;
				match(_t, KNOWLEDGE);
				_t = _t.getFirstChild();
				{
					_loop561: do {
						if (_t == null)
							_t = ASTNULL;
						if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == DATA)) {
									AST __t555 = _t;
									AST tmp132_AST_in = (AST) _t;
									match(_t, DATA);
									_t = _t.getFirstChild();
									AST tmp133_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setData(b);
									s += b;
									s += "\n";
									_t = __t555;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == PRIORITY)) {
									AST __t556 = _t;
									AST tmp134_AST_in = (AST) _t;
									match(_t, PRIORITY);
									_t = _t.getFirstChild();
									AST tmp135_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									b = doubleAST(_t, obj);
									_t = _retTree;
									obj.setPriority(b);
									s += b;
									s += "\n";
									_t = __t556;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == LOGIC)) {
									AST __t557 = _t;
									AST tmp136_AST_in = (AST) _t;
									match(_t, LOGIC);
									_t = _t.getFirstChild();
									AST tmp137_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setLogic(b);
									s += b;
									s += "\n";
									_t = __t557;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == ACTION)) {
									AST __t558 = _t;
									AST tmp138_AST_in = (AST) _t;
									match(_t, ACTION);
									_t = _t.getFirstChild();
									AST tmp139_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setAction(b);
									s += b;
									s += "\n";
									_t = __t558;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == AGE_MIN)) {
									AST __t559 = _t;
									AST tmp140_AST_in = (AST) _t;
									match(_t, AGE_MIN);
									_t = _t.getFirstChild();
									AST tmp141_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									b = doubleAST(_t, obj);
									_t = _retTree;
									obj.setAgeMin(b);
									s += b;
									s += "\n";
									_t = __t559;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == AGE_MAX)) {
									AST __t560 = _t;
									AST tmp142_AST_in = (AST) _t;
									match(_t, AGE_MAX);
									_t = _t.getFirstChild();
									AST tmp143_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									b = doubleAST(_t, obj);
									_t = _retTree;
									obj.setAgeMax(b);
									s += b;
									s += "\n";
									_t = __t560;
									_t = _t.getNextSibling();
								} else if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
									a = textAST(_t, obj);
									_t = _retTree;
									s += a;
									AST tmp144_AST_in = (AST) _t;
									match(_t, ENDBLOCK);
									_t = _t.getNextSibling();
									s += "\n";
								} else {
									throw new NoViableAltException(_t);
								}
								
							}
						} else {
							break _loop561;
						}
						
					} while (true);
				}
				_t = __t552;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	/*********************** MAINTENANCE *******************************************/
	public final String maintenance(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST maintenance_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a = "", b = "";
		
		try { // for error handling
			{
				AST __t564 = _t;
				AST tmp145_AST_in = (AST) _t;
				match(_t, MAINTENANCE);
				_t = _t.getFirstChild();
				{
					_loop575: do {
						if (_t == null)
							_t = ASTNULL;
						if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
							{
								if (_t == null)
									_t = ASTNULL;
								if ((_t.getType() == FILENAME)) {
									AST __t567 = _t;
									AST tmp146_AST_in = (AST) _t;
									match(_t, FILENAME);
									_t = _t.getFirstChild();
									AST tmp147_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Filename: ";
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setClassName(b);
									s += b;
									s += "\n";
									_t = __t567;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == MLMNAME)) {
									AST __t568 = _t;
									AST tmp148_AST_in = (AST) _t;
									match(_t, MLMNAME);
									_t = _t.getFirstChild();
									AST tmp149_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Filename: ";
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setClassName(b);
									s += b;
									s += "\n";
									_t = __t568;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == VERSION)) {
									AST __t569 = _t;
									AST tmp150_AST_in = (AST) _t;
									match(_t, VERSION);
									_t = _t.getFirstChild();
									AST tmp151_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Version: ";
									b = doubleAST(_t, obj);
									_t = _retTree;
									obj.setVersion(b);
									s += b;
									s += "\n";
									_t = __t569;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == TITLE)) {
									AST __t570 = _t;
									AST tmp152_AST_in = (AST) _t;
									match(_t, TITLE);
									_t = _t.getFirstChild();
									AST tmp153_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Title: ";
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setTitle(b);
									s += b;
									s += "\n";
									_t = __t570;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == AUTHOR)) {
									AST __t571 = _t;
									AST tmp154_AST_in = (AST) _t;
									match(_t, AUTHOR);
									_t = _t.getFirstChild();
									AST tmp155_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Author: ";
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setAuthor(b);
									s += b;
									s += "\n";
									_t = __t571;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == SPECIALIST)) {
									AST __t572 = _t;
									AST tmp156_AST_in = (AST) _t;
									match(_t, SPECIALIST);
									_t = _t.getFirstChild();
									AST tmp157_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Specialist: ";
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setSpecialist(b);
									s += b;
									s += "\n";
									_t = __t572;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == DATE)) {
									AST __t573 = _t;
									AST tmp158_AST_in = (AST) _t;
									match(_t, DATE);
									_t = _t.getFirstChild();
									AST tmp159_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Date: ";
									b = doubleAST(_t, obj);
									_t = _retTree;
									obj.setDate(b);
									s += b;
									s += "\n";
									_t = __t573;
									_t = _t.getNextSibling();
								} else if ((_t.getType() == INSTITUTION)) {
									AST __t574 = _t;
									AST tmp160_AST_in = (AST) _t;
									match(_t, INSTITUTION);
									_t = _t.getFirstChild();
									AST tmp161_AST_in = (AST) _t;
									match(_t, COLON);
									_t = _t.getNextSibling();
									s += " Institution: ";
									b = textAST(_t, obj);
									_t = _retTree;
									obj.setInstitution(b);
									s += b;
									s += "\n";
									_t = __t574;
									_t = _t.getNextSibling();
								} else if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
									a = textAST(_t, obj);
									_t = _retTree;
									s += a;
									AST tmp162_AST_in = (AST) _t;
									match(_t, ENDBLOCK);
									_t = _t.getNextSibling();
									s += "\n";
								} else {
									throw new NoViableAltException(_t);
								}
								
							}
						} else {
							break _loop575;
						}
						
					} while (true);
				}
				_t = __t564;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	/*********************** LIBRARY *******************************************/
	public final String library(AST _t, MLMObject obj) throws RecognitionException {
		String s = "";
		
		AST library_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a = "", b = "";
		
		try { // for error handling
			{
				AST __t590 = _t;
				AST tmp163_AST_in = (AST) _t;
				match(_t, LIBRARY);
				_t = _t.getFirstChild();
				{
					_loop599: do {
						if (_t == null)
							_t = ASTNULL;
						switch (_t.getType()) {
							case PURPOSE: {
								AST __t592 = _t;
								AST tmp164_AST_in = (AST) _t;
								match(_t, PURPOSE);
								_t = _t.getFirstChild();
								AST tmp165_AST_in = (AST) _t;
								match(_t, COLON);
								_t = _t.getNextSibling();
								s += " Purpose: ";
								b = textAST(_t, obj);
								_t = _retTree;
								obj.setPurpose(b);
								s += b;
								s += "\n";
								_t = __t592;
								_t = _t.getNextSibling();
								break;
							}
							case EXPLANATION: {
								AST __t593 = _t;
								AST tmp166_AST_in = (AST) _t;
								match(_t, EXPLANATION);
								_t = _t.getFirstChild();
								AST tmp167_AST_in = (AST) _t;
								match(_t, COLON);
								_t = _t.getNextSibling();
								s += " Explanation: ";
								b = textAST(_t, obj);
								_t = _retTree;
								obj.setExplanation(b);
								s += b;
								s += "\n";
								_t = __t593;
								_t = _t.getNextSibling();
								break;
							}
							case KEYWORDS: {
								AST __t594 = _t;
								AST tmp168_AST_in = (AST) _t;
								match(_t, KEYWORDS);
								_t = _t.getFirstChild();
								AST tmp169_AST_in = (AST) _t;
								match(_t, COLON);
								_t = _t.getNextSibling();
								s += " Keywords: ";
								b = textAST(_t, obj);
								_t = _retTree;
								obj.setKeywords(b);
								s += b;
								s += "\n";
								_t = __t594;
								_t = _t.getNextSibling();
								break;
							}
							case CITATIONS: {
								AST __t595 = _t;
								AST tmp170_AST_in = (AST) _t;
								match(_t, CITATIONS);
								_t = _t.getFirstChild();
								AST tmp171_AST_in = (AST) _t;
								match(_t, COLON);
								_t = _t.getNextSibling();
								s += " Citations: ";
								b = textAST(_t, obj);
								_t = _retTree;
								obj.setCitations(b);
								s += b;
								s += "\n";
								_t = __t595;
								_t = _t.getNextSibling();
								break;
							}
							case LINKS: {
								AST __t596 = _t;
								AST tmp172_AST_in = (AST) _t;
								match(_t, LINKS);
								_t = _t.getFirstChild();
								AST tmp173_AST_in = (AST) _t;
								match(_t, COLON);
								_t = _t.getNextSibling();
								s += " Links: ";
								{
									if (_t == null)
										_t = ASTNULL;
									if ((_t.getType() == HTTP)) {
										AST __t598 = _t;
										AST tmp174_AST_in = (AST) _t;
										match(_t, HTTP);
										_t = _t.getFirstChild();
										s += "http://";
										_t = __t598;
										_t = _t.getNextSibling();
									} else if ((_tokenSet_11.member(_t.getType()))) {
										a = textAST(_t, obj);
										_t = _retTree;
										s += a;
										s += "\n";
									} else if ((_tokenSet_11.member(_t.getType()))) {} else {
										throw new NoViableAltException(_t);
									}
									
								}
								b = textAST(_t, obj);
								_t = _retTree;
								obj.setLinks(b);
								s += b;
								s += "\n";
								_t = __t596;
								_t = _t.getNextSibling();
								break;
							}
							default: {
								break _loop599;
							}
						}
					} while (true);
				}
				_t = __t590;
				_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public final String dateAST(AST _t, GregorianCalendar calendar) throws RecognitionException {
		String s = "";
		
		AST dateAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST tyear = null;
		AST tmonth = null;
		AST tday = null;
		String year = "", month = "", day = "";
		
		try { // for error handling
			AST __t601 = _t;
			AST tmp175_AST_in = (AST) _t;
			match(_t, MINUS);
			_t = _t.getFirstChild();
			AST __t602 = _t;
			AST tmp176_AST_in = (AST) _t;
			match(_t, MINUS);
			_t = _t.getFirstChild();
			calendar.clear();
			{
				tyear = (AST) _t;
				match(_t, INTLIT);
				_t = _t.getNextSibling();
				year = tyear.getText();
				s += year;
				s += "-";
				calendar.set(Calendar.YEAR, Integer.valueOf(year));
				
				tmonth = (AST) _t;
				match(_t, INTLIT);
				_t = _t.getNextSibling();
				month = tmonth.getText();
				s += month;
				s += "-";
				calendar.set(Calendar.MONTH, Integer.valueOf(month) - 1); // Month is 0 -11 in the Calendar class 
				
			}
			_t = __t602;
			_t = _t.getNextSibling();
			tday = (AST) _t;
			match(_t, INTLIT);
			_t = _t.getNextSibling();
			day = tday.getText();
			s += day;
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
			
			_t = __t601;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return s;
	}
	
	public static final String[] _tokenNames = { "<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "\"and\"", "WEIRD_IDENT",
	        "\"is\"", "\"are\"", "\"was\"", "\"were\"", "\"count\"", "\"in\"", "\"less\"", "\"greater\"", "\"the\"",
	        "\"than\"", "\"from\"", "\"before\"", "\"after\"", "\"ago\"", "\"write\"", "\"at\"", "\"let\"", "\"now\"",
	        "\"be\"", "\"year\"", "\"years\"", "\"if\"", "\"it\"", "\"they\"", "\"not\"", "\"or\"", "\"then\"", "\"read\"",
	        "\"minimum\"", "\"min\"", "\"maximum\"", "\"max\"", "\"last\"", "\"first\"", "\"earliest\"", "\"lastest\"",
	        "\"event\"", "\"where\"", "\"exist\"", "\"exists\"", "\"past\"", "\"days\"", "\"day\"", "\"month\"",
	        "\"months\"", "\"week\"", "\"weeks\"", "\"avg\"", "\"average\"", "\"sum\"", "\"median\"", "\"conclude\"",
	        "\"else\"", "\"elseif\"", "\"endif\"", "\"true\"", "\"false\"", "\"data\"", "\"logic\"", "\"action\"",
	        "\"maintenance\"", "\"knowledge\"", "\"library\"", "\"filename\"", "\"mlmname\"", "\"title\"",
	        "\"institution\"", "\"author\"", "\"priority\"", "\"version\"", "\"specialist\"", "\"purpose\"",
	        "\"explanation\"", "\"keywords\"", "\"citations\"", "\"links\"", "\"type\"", "\"date\"", "\"age_min\"",
	        "\"age_max\"", "\"of\"", "\"time\"", "\"within\"", "\"call\"", "\"with\"", "\"to\"", "\"any\"", "\"research\"",
	        "\"second\"", "\"occur\"", "\"present\"", "\"number\"", "\"http\"", "\"null\"", "\"end\"", "COLON", ";;", "DOT",
	        "MINUS", "UNDERSCORE", "\"arden\"", "\"ASTM-E\"", "INTLIT", "DIGIT", "SEMI", "\"validation\"", "\"production\"",
	        "\"testing\"", "\"expired\"", "TIMES", "an identifier", "APOSTROPHE", "AMPERSAND", "PERCENT", "GT", "GTE", "LT",
	        "LTE", "POUND", "COMMA", "DIV", "STRING_LITERAL", "LPAREN", "RPAREN", "PLUS", "\"SUPPORT\"", "\"REFUTE\"",
	        "NOT_COMMENT", "SINGLE_QUOTE", "\"data-driven\"", "\"data_driven\"", "COMMENT", "ML_COMMENT", "BECOMES",
	        "ARDEN_CURLY_BRACKETS", "\"PRESENT\"", "\"NULL\"", "\"BOOLEAN\"", "\"NUMBER\"", "\"DURATION\"", "\"STRING\"",
	        "\"LIST\"", "\"OBJECT\"", "EQUAL", "\"hour\"", "\"hours\"", "\"minute\"", "\"minutes\"", "\"seconds\"",
	        "\"OCCUR\"", "\"Occur\"", "\"OCCURS\"", "\"Occurs\"", "\"occurs\"", "\"OCCURRED\"", "\"Occurred\"", "\"evoke\"",
	        "\"CALL\"", "EQUALS", "\"EQ\"", "\"LT\"", "\"GT\"", "\"LE\"", "\"GE\"", "NE", "\"NE\"", "ACTION_OP",
	        "\"urgency\"", "\"MERGE\"", "\"SORT\"", "\"DATA\"", "\"SEQTO\"", "\"+\"", "\"-\"", "\"*\"", "\"/\"",
	        "TERM_LITERAL" };
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 7052694182476907520L, 4521191813414912L, 4096L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	private static final long[] mk_tokenSet_1() {
		long[] data = { 6917529027641081856L, -9218850810681622528L, 18031990695526400L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	private static final long[] mk_tokenSet_2() {
		long[] data = { 6917529027641081856L, -9218850810677428224L, 18031990695526400L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	private static final long[] mk_tokenSet_3() {
		long[] data = { 2161727821272055808L, 274911461376L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	
	private static final long[] mk_tokenSet_4() {
		long[] data = { 0L, 1080863910568919040L, 68719476736L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	
	private static final long[] mk_tokenSet_5() {
		long[] data = { 14336L, 0L, 68719476736L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	
	private static final long[] mk_tokenSet_6() {
		long[] data = { 7052694173886972928L, -9218850810408992768L, 18031990695526400L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	
	private static final long[] mk_tokenSet_7() {
		long[] data = { 6917533408507723776L, -9218850810677428224L, 18031990695526400L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	
	private static final long[] mk_tokenSet_8() {
		long[] data = { 7052689793020331008L, -9218850810677428224L, 18031990695526400L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	
	private static final long[] mk_tokenSet_9() {
		long[] data = { 6917529027641081856L, -9218850810408992768L, 18031990695526400L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[8];
		data[0] = -16L;
		data[1] = -274877906945L;
		data[2] = 36028797018963967L;
		return data;
	}
	
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	
	private static final long[] mk_tokenSet_11() {
		long[] data = new long[8];
		data[0] = -8L;
		data[1] = -274877906945L;
		data[2] = 36028797018963967L;
		return data;
	}
	
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
}
