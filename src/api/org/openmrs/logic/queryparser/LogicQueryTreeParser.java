// $ANTLR 2.7.6 (2005-12-22): "LogicQueryParser.g" -> "LogicQueryTreeParser.java"$

package org.openmrs.logic.queryparser;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.op.Operator;

import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.collections.AST;
import antlr.collections.impl.BitSet;

/*************************************************************************************/
public class LogicQueryTreeParser extends antlr.TreeParser implements LogicQueryTreeParserTokenTypes {
	
	public LogicQueryTreeParser() {
		tokenNames = _tokenNames;
	}
	
	public final LogicCriteria query_AST(AST _t) throws RecognitionException {
		LogicCriteria lc_return = null;
		
		AST query_AST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST ift = null;
		AST ifst = null;
		AST val = null;
		AST idt = null;
		AST idstr = null;
		AST valstr = null;
		AST val1 = null;
		AST val2 = null;
		String a = "", b = "";
		Operator transform = null, comp_op = null, temporal_op = null;
		boolean lcFormed = false;
		LogicCriteria lc = null;
		
		try { // for error handling
			{
				{
					if (_t == null)
						_t = ASTNULL;
					switch (_t.getType()) {
						case LAST:
						case FIRST:
						case EARLIEST:
						case LATEST:
						case EXIST:
						case EXISTS: {
							transform = of_from_AST(_t);
							_t = _retTree;
							break;
						}
						case ID:
						case INTLIT:
						case STRING_LITERAL: {
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
					switch (_t.getType()) {
						case ID: {
							AST __t124 = _t;
							ift = _t == ASTNULL ? null : (AST) _t;
							match(_t, ID);
							_t = _t.getFirstChild();
							a = ift.getText(); //System.err.println("text = " + a);
							
							_t = __t124;
							_t = _t.getNextSibling();
							break;
						}
						case STRING_LITERAL: {
							AST __t125 = _t;
							ifst = _t == ASTNULL ? null : (AST) _t;
							match(_t, STRING_LITERAL);
							_t = _t.getFirstChild();
							a = ifst.getText(); //System.err.println("text = " + a);
							
							_t = __t125;
							_t = _t.getNextSibling();
							break;
						}
						case INTLIT: {
							AST __t126 = _t;
							val = _t == ASTNULL ? null : (AST) _t;
							match(_t, INTLIT);
							_t = _t.getFirstChild();
							a = val.getText(); //System.err.println("text = " + a);
							// This is an error on LHS of an expr
							
							_t = __t126;
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
					if ((_tokenSet_0.member(_t.getType()))) {
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case EQUALS:
								case LT:
								case GT:
								case LTE:
								case GTE: {
									comp_op = simple_comp_op(_t);
									_t = _retTree;
									{
										if (_t == null)
											_t = ASTNULL;
										switch (_t.getType()) {
											case ID: {
												AST __t130 = _t;
												idt = _t == ASTNULL ? null : (AST) _t;
												match(_t, ID);
												_t = _t.getFirstChild();
												b += idt.getText(); //System.err.println("text = " + b);
												lc = new LogicCriteria(null, a);
												lc.appendExpression(comp_op, b);
												//lc_return = lc.applyTransform(transform);
												lcFormed = true;
												
												_t = __t130;
												_t = _t.getNextSibling();
												break;
											}
											case STRING_LITERAL: {
												AST __t131 = _t;
												idstr = _t == ASTNULL ? null : (AST) _t;
												match(_t, STRING_LITERAL);
												_t = _t.getFirstChild();
												b += idstr.getText(); //System.err.println("text = " + b);
												lc = new LogicCriteria(null, a);
												lc.appendExpression(comp_op, b);
												//lc_return = lc.applyTransform(transform);
												lcFormed = true;
												
												_t = __t131;
												_t = _t.getNextSibling();
												break;
											}
											case INTLIT: {
												AST __t132 = _t;
												valstr = _t == ASTNULL ? null : (AST) _t;
												match(_t, INTLIT);
												_t = _t.getFirstChild();
												b += valstr.getText(); //System.err.println("text = " + b);
												Integer i = null;
												
												i = Integer.parseInt(b);
												lc = new LogicCriteria(null, a);
												
												lc.appendExpression(comp_op, i);
												//lc_return = lc.applyTransform(transform);
												lcFormed = true;
												
												_t = __t132;
												_t = _t.getNextSibling();
												break;
											}
											case DOT: {
												AST __t133 = _t;
												AST tmp240_AST_in = (AST) _t;
												match(_t, DOT);
												_t = _t.getFirstChild();
												val1 = (AST) _t;
												match(_t, INTLIT);
												_t = _t.getNextSibling();
												
												b = val1.getText();
												
												val2 = (AST) _t;
												match(_t, INTLIT);
												_t = _t.getNextSibling();
												
												Double idbl = null;
												
												String dbl = b + "." + val2.getText();
												idbl = Double.parseDouble(dbl);
												
												lc = new LogicCriteria(null, a);
												
												lc.appendExpression(comp_op, idbl);
												//lc_return = lc.applyTransform(transform);
												lcFormed = true;
												
												_t = __t133;
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
										switch (_t.getType()) {
											case BEFORE:
											case AFTER: {
												temporal_op = temporal_comp_op(_t);
												_t = _retTree;
												{
													GregorianCalendar gc = new GregorianCalendar();
													b = dateAST(_t, gc);
													_t = _retTree;
													
													lc.appendExpression(temporal_op, gc.getTime());
													//lc_return = lc.applyTransform(transform);
													lcFormed = true;
													
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
					} else if ((_t.getType() == 3 || _t.getType() == BEFORE || _t.getType() == AFTER)) {
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case BEFORE:
								case AFTER: {
									temporal_op = temporal_comp_op(_t);
									_t = _retTree;
									{
										GregorianCalendar gc = new GregorianCalendar();
										b = dateAST(_t, gc);
										_t = _retTree;
										
										lc = new LogicCriteria(null, a);
										lc.appendExpression(temporal_op, gc.getTime());
										//lc_return = lc.applyTransform(transform);
										lcFormed = true;
										
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
					} else {
						throw new NoViableAltException(_t);
					}
					
				}
				
				if (lcFormed == false) // just a terminal symbol like CD4 COUNT
				{
					lc_return = new LogicCriteria(null, a);
				} else {
					lc_return = lc.applyTransform(transform);
				}
				return lc_return;
				
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t != null) {
				_t = _t.getNextSibling();
			}
		}
		_retTree = _t;
		return lc_return;
	}
	
	public final Operator of_from_AST(AST _t) throws RecognitionException {
		Operator s = null;
		
		AST of_from_AST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		
		try { // for error handling
			if (_t == null)
				_t = ASTNULL;
			switch (_t.getType()) {
				case LAST:
				case LATEST: {
					{
						{
							if (_t == null)
								_t = ASTNULL;
							switch (_t.getType()) {
								case LAST: {
									AST tmp241_AST_in = (AST) _t;
									match(_t, LAST);
									_t = _t.getNextSibling();
									break;
								}
								case LATEST: {
									AST tmp242_AST_in = (AST) _t;
									match(_t, LATEST);
									_t = _t.getNextSibling();
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
						s = Operator.LAST;
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
									AST tmp243_AST_in = (AST) _t;
									match(_t, FIRST);
									_t = _t.getNextSibling();
									break;
								}
								case EARLIEST: {
									AST tmp244_AST_in = (AST) _t;
									match(_t, EARLIEST);
									_t = _t.getNextSibling();
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
						s = Operator.FIRST;
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
									AST tmp245_AST_in = (AST) _t;
									match(_t, EXIST);
									_t = _t.getNextSibling();
									break;
								}
								case EXISTS: {
									AST tmp246_AST_in = (AST) _t;
									match(_t, EXISTS);
									_t = _t.getNextSibling();
									break;
								}
								default: {
									throw new NoViableAltException(_t);
								}
							}
						}
						s = Operator.EXISTS;
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
	
	public final Operator simple_comp_op(AST _t) throws RecognitionException {
		Operator s = null;
		
		AST simple_comp_op_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a = "", b = "";
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case EQUALS: {
						AST __t146 = _t;
						AST tmp247_AST_in = (AST) _t;
						match(_t, EQUALS);
						_t = _t.getFirstChild();
						
						//System.err.println("Found = ");
						s = Operator.EQUALS;
						
						_t = __t146;
						_t = _t.getNextSibling();
						break;
					}
					case GTE: {
						AST __t147 = _t;
						AST tmp248_AST_in = (AST) _t;
						match(_t, GTE);
						_t = _t.getFirstChild();
						
						//System.err.println("Found >= ");
						s = Operator.GTE;
						
						_t = __t147;
						_t = _t.getNextSibling();
						break;
					}
					case GT: {
						AST __t148 = _t;
						AST tmp249_AST_in = (AST) _t;
						match(_t, GT);
						_t = _t.getFirstChild();
						
						//System.err.println("Found > ");
						s = Operator.GT;
						
						_t = __t148;
						_t = _t.getNextSibling();
						break;
					}
					case LT: {
						AST __t149 = _t;
						AST tmp250_AST_in = (AST) _t;
						match(_t, LT);
						_t = _t.getFirstChild();
						
						//System.err.println("Found < ");
						s = Operator.LT;
						
						_t = __t149;
						_t = _t.getNextSibling();
						break;
					}
					case LTE: {
						AST __t150 = _t;
						AST tmp251_AST_in = (AST) _t;
						match(_t, LTE);
						_t = _t.getFirstChild();
						
						//System.err.println("Found <= ");
						s = Operator.LTE;
						
						_t = __t150;
						_t = _t.getNextSibling();
						AST __t151 = _t;
						AST tmp252_AST_in = (AST) _t;
						match(_t, NE);
						_t = _t.getFirstChild();
						
						//System.err.println("Found <> ");
						s = Operator.NOT_EXISTS;
						
						_t = __t151;
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
	
	public final Operator temporal_comp_op(AST _t) throws RecognitionException {
		Operator s = null;
		
		AST temporal_comp_op_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		String a = "", b = "";
		
		try { // for error handling
			{
				if (_t == null)
					_t = ASTNULL;
				switch (_t.getType()) {
					case BEFORE: {
						AST __t154 = _t;
						AST tmp253_AST_in = (AST) _t;
						match(_t, BEFORE);
						_t = _t.getFirstChild();
						
						//System.err.println("Found BEFORE ");
						s = Operator.BEFORE;
						
						_t = __t154;
						_t = _t.getNextSibling();
						break;
					}
					case AFTER: {
						AST __t155 = _t;
						AST tmp254_AST_in = (AST) _t;
						match(_t, AFTER);
						_t = _t.getFirstChild();
						
						//System.err.println("Found AFTER ");
						s = Operator.AFTER;
						
						_t = __t155;
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
	
	public final String dateAST(AST _t, GregorianCalendar calendar) throws RecognitionException {
		String s = "";
		
		AST dateAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST tyear = null;
		AST tmonth = null;
		AST tday = null;
		String year = "", month = "", day = "";
		
		try { // for error handling
			AST __t141 = _t;
			AST tmp255_AST_in = (AST) _t;
			match(_t, MINUS);
			_t = _t.getFirstChild();
			AST __t142 = _t;
			AST tmp256_AST_in = (AST) _t;
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
			_t = __t142;
			_t = _t.getNextSibling();
			tday = (AST) _t;
			match(_t, INTLIT);
			_t = _t.getNextSibling();
			day = tday.getText();
			s += day;
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
			
			_t = __t141;
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
	
	public final String doubleAST(AST _t) throws RecognitionException {
		String s = "";
		
		AST doubleAST_AST_in = (_t == ASTNULL) ? null : (AST) _t;
		AST val2 = null;
		String a = "", b = "";
		
		try { // for error handling
			AST __t139 = _t;
			AST tmp257_AST_in = (AST) _t;
			match(_t, DOT);
			_t = _t.getFirstChild();
			val2 = (AST) _t;
			match(_t, INTLIT);
			_t = _t.getNextSibling();
			
			s = val2.getText();
			
			_t = __t139;
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
	        "\"second\"", "\"occur\"", "\"present\"", "\"number\"", "\"http\"", "\"null\"", "TIMES", "an identifier",
	        "INTLIT", "MINUS", "COMMA", "DOT", "DIV", "UNDERSCORE", "STRING_LITERAL", "LPAREN", "RPAREN", ";;", "\":\"",
	        "\"T\"", "\"t\"", "\".\"", "DIGIT", "\"+\"", "\"-\"", "\"Z\"", "\"z\"", "\"PRESENT\"", "\"NULL\"",
	        "\"BOOLEAN\"", "\"NUMBER\"", "\"DURATION\"", "\"STRING\"", "\"LIST\"", "\"OBJECT\"", "EQUAL", "\"hour\"",
	        "\"hours\"", "\"minute\"", "\"minutes\"", "\"seconds\"", "EQUALS", "\"EQ\"", "LT", "\"LT\"", "GT", "\"GT\"",
	        "LTE", "\"LE\"", "GTE", "\"GE\"", "NE", "\"NE\"", "\"OCCUR\"", "\"Occur\"", "\"OCCURS\"", "\"Occurs\"",
	        "\"occurs\"", "\"OCCURRED\"", "\"Occurred\"", "\"MERGE\"", "\"SORT\"", "\"DATA\"", "\"SEQTO\"", "ACTION_OP",
	        "\"*\"", "\"/\"", "TERM_LITERAL", "SEMI" };
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 8L, 0L, 43648L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
}
