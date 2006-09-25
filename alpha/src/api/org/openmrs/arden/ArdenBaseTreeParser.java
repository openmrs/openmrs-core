// $ANTLR 2.7.6 (2005-12-22): "ArdenRecognizer.g" -> "ArdenBaseTreeParser.java"$

package org.openmrs.arden;

import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.collections.AST;
import antlr.collections.impl.BitSet;


/*************************************************************************************/
public class ArdenBaseTreeParser extends antlr.TreeParser       implements ArdenBaseTreeParserTokenTypes
 {
public ArdenBaseTreeParser() {
	tokenNames = _tokenNames;
}

	public final String  data(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST data_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			AST __t322 = _t;
			AST tmp1_AST_in = (AST)_t;
			match(_t,DATA);
			_t = _t.getFirstChild();
			System.err.println("\n"); System.err.println("-------Starting Data--------");
			{
			_loop324:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					System.err.println("-----------Starting Read -------");
					s=readAST(_t,obj);
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End Read -------");
				}
				else if ((_t.getType()==EVENT||_t.getType()==ID)) {
					System.err.println("----------------Starting Event-------");
					eventAST(_t);
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End Event -------");
				}
				else {
					break _loop324;
				}
				
			} while (true);
			}
			{
			AST tmp2_AST_in = (AST)_t;
			match(_t,ENDBLOCK);
			_t = _t.getNextSibling();
			}
			System.err.println("\n");System.err.println("-----------End Data -------");
			_t = __t322;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  readAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST readAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST k = null;
		AST x = null;
		AST y = null;
		AST z = null;
		AST j = null;
		AST m = null;
		AST n = null;
		AST i = null;
		String a,b, ret_val="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case READ:
			{
				AST __t328 = _t;
				AST tmp3_AST_in = (AST)_t;
				match(_t,READ);
				_t = _t.getFirstChild();
				a=readAST(_t,obj);
				_t = _retTree;
				b=readAST(_t,obj);
				_t = _retTree;
				_t = __t328;
				_t = _t.getNextSibling();
				s += ret_val;
				break;
			}
			case LAST:
			case LATEST:
			{
				{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LAST:
				{
					AST tmp4_AST_in = (AST)_t;
					match(_t,LAST);
					_t = _t.getNextSibling();
					break;
				}
				case LATEST:
				{
					AST tmp5_AST_in = (AST)_t;
					match(_t,LATEST);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case INTLIT:
				{
					k = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					AST tmp6_AST_in = (AST)_t;
					match(_t,FROM);
					_t = _t.getNextSibling();
					break;
				}
				case COUNT:
				case READ:
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
				case ID:
				case ARDEN_CURLY_BRACKETS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;if(k != null) {
												System.err.println("ReadType = Last " + "How many? " + k.getText());
												}
											 else {
					  							 System.err.println("ReadType = Last " + "How many? 1" );	
											 }
											 
											
				break;
			}
			case FIRST:
			case EARLIEST:
			{
				{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case FIRST:
				{
					AST tmp7_AST_in = (AST)_t;
					match(_t,FIRST);
					_t = _t.getNextSibling();
					break;
				}
				case EARLIEST:
				{
					AST tmp8_AST_in = (AST)_t;
					match(_t,EARLIEST);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case INTLIT:
				{
					x = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					AST tmp9_AST_in = (AST)_t;
					match(_t,FROM);
					_t = _t.getNextSibling();
					break;
				}
				case COUNT:
				case READ:
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
				case ID:
				case ARDEN_CURLY_BRACKETS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;if(x != null) {
												System.err.println("ReadType = First " + "How many? " + x.getText());
												}
											 else {
					  							 System.err.println("ReadType = First " + "How many? 1" );	
											 }
											 
											
				break;
			}
			case MAXIMUM:
			case MAX:
			{
				{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case MAXIMUM:
				{
					AST tmp10_AST_in = (AST)_t;
					match(_t,MAXIMUM);
					_t = _t.getNextSibling();
					break;
				}
				case MAX:
				{
					AST tmp11_AST_in = (AST)_t;
					match(_t,MAX);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case INTLIT:
				{
					y = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					AST tmp12_AST_in = (AST)_t;
					match(_t,FROM);
					_t = _t.getNextSibling();
					break;
				}
				case COUNT:
				case READ:
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
				case ID:
				case ARDEN_CURLY_BRACKETS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;if(y != null) {
												System.err.println("ReadType = Maximum " + "How many? " + y.getText());
												}
											 else {
					  							 System.err.println("ReadType = Maximum " + "How many? 1" );	
											 }
											 
											
				break;
			}
			case MINIMUM:
			case MIN:
			{
				{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case MINIMUM:
				{
					AST tmp13_AST_in = (AST)_t;
					match(_t,MINIMUM);
					_t = _t.getNextSibling();
					break;
				}
				case MIN:
				{
					AST tmp14_AST_in = (AST)_t;
					match(_t,MIN);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case INTLIT:
				{
					z = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					AST tmp15_AST_in = (AST)_t;
					match(_t,FROM);
					_t = _t.getNextSibling();
					break;
				}
				case COUNT:
				case READ:
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
				case ID:
				case ARDEN_CURLY_BRACKETS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;if(z != null) {
												System.err.println("ReadType = Minimum " + "How many? " + z.getText());
												}
											 else {
					  							 System.err.println("ReadType = Min " + "How many? 1" );	
											 }
											 
											
				break;
			}
			case EXIST:
			case EXISTS:
			{
				{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EXIST:
				{
					AST tmp16_AST_in = (AST)_t;
					match(_t,EXIST);
					_t = _t.getNextSibling();
					break;
				}
				case EXISTS:
				{
					AST tmp17_AST_in = (AST)_t;
					match(_t,EXISTS);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Exist");
				break;
			}
			case AVG:
			case AVERAGE:
			{
				{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case AVERAGE:
				{
					AST tmp18_AST_in = (AST)_t;
					match(_t,AVERAGE);
					_t = _t.getNextSibling();
					break;
				}
				case AVG:
				{
					AST tmp19_AST_in = (AST)_t;
					match(_t,AVG);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Average");
				break;
			}
			case COUNT:
			{
				{
				AST tmp20_AST_in = (AST)_t;
				match(_t,COUNT);
				_t = _t.getNextSibling();
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Count");
				break;
			}
			case SUM:
			{
				{
				AST tmp21_AST_in = (AST)_t;
				match(_t,SUM);
				_t = _t.getNextSibling();
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Sum");
				break;
			}
			case MEDIAN:
			{
				{
				AST tmp22_AST_in = (AST)_t;
				match(_t,MEDIAN);
				_t = _t.getNextSibling();
				b=readAST(_t,obj);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Median");
				break;
			}
			case ARDEN_CURLY_BRACKETS:
			{
				{
				{
				j = (AST)_t;
				match(_t,ARDEN_CURLY_BRACKETS);
				_t = _t.getNextSibling();
				}
				/*s=b;*/System.err.println("Fetch this data - " + j.getText());
					 										s = j.getText(); obj.AddConcept(s);
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case WHERE:
				{
					{
					AST tmp23_AST_in = (AST)_t;
					match(_t,WHERE);
					_t = _t.getNextSibling();
					AST tmp24_AST_in = (AST)_t;
					match(_t,PAST);
					_t = _t.getNextSibling();
					}
					System.err.println("Where=TRUE");
					{
					m = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					n = _t==ASTNULL ? null : (AST)_t;
					duration_op(_t);
					_t = _retTree;
					}
					System.err.println("Duration Clause - " + m.getText() + " " + n.getText());
					break;
				}
				case 3:
				case COUNT:
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
				case ENDBLOCK:
				case ID:
				case ARDEN_CURLY_BRACKETS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				}
				break;
			}
			case ID:
			{
				i = (AST)_t;
				match(_t,ID);
				_t = _t.getNextSibling();
				System.err.println("Variable = " + i.getText()); a= i.getText(); obj.SetConceptVar(a);
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  eventAST(AST _t) throws RecognitionException {
		String s="";
		
		AST eventAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST i = null;
		String a,b;
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EVENT:
			{
				AST __t356 = _t;
				AST tmp25_AST_in = (AST)_t;
				match(_t,EVENT);
				_t = _t.getFirstChild();
				b=eventAST(_t);
				_t = _retTree;
				_t = __t356;
				_t = _t.getNextSibling();
				break;
			}
			case ID:
			{
				i = (AST)_t;
				match(_t,ID);
				_t = _t.getNextSibling();
				System.err.println("Event Variable = " + i.getText());
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final void duration_op(AST _t) throws RecognitionException {
		
		AST duration_op_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case YEAR:
			{
				AST tmp26_AST_in = (AST)_t;
				match(_t,YEAR);
				_t = _t.getNextSibling();
				break;
			}
			case YEARS:
			{
				AST tmp27_AST_in = (AST)_t;
				match(_t,YEARS);
				_t = _t.getNextSibling();
				break;
			}
			case MONTH:
			{
				AST tmp28_AST_in = (AST)_t;
				match(_t,MONTH);
				_t = _t.getNextSibling();
				break;
			}
			case MONTHS:
			{
				AST tmp29_AST_in = (AST)_t;
				match(_t,MONTHS);
				_t = _t.getNextSibling();
				break;
			}
			case WEEK:
			{
				AST tmp30_AST_in = (AST)_t;
				match(_t,WEEK);
				_t = _t.getNextSibling();
				break;
			}
			case WEEKS:
			{
				AST tmp31_AST_in = (AST)_t;
				match(_t,WEEKS);
				_t = _t.getNextSibling();
				break;
			}
			case DAY:
			{
				AST tmp32_AST_in = (AST)_t;
				match(_t,DAY);
				_t = _t.getNextSibling();
				break;
			}
			case DAYS:
			{
				AST tmp33_AST_in = (AST)_t;
				match(_t,DAYS);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_hour:
			{
				AST tmp34_AST_in = (AST)_t;
				match(_t,LITERAL_hour);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_hours:
			{
				AST tmp35_AST_in = (AST)_t;
				match(_t,LITERAL_hours);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_minute:
			{
				AST tmp36_AST_in = (AST)_t;
				match(_t,LITERAL_minute);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_minutes:
			{
				AST tmp37_AST_in = (AST)_t;
				match(_t,LITERAL_minutes);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_second:
			{
				AST tmp38_AST_in = (AST)_t;
				match(_t,LITERAL_second);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_seconds:
			{
				AST tmp39_AST_in = (AST)_t;
				match(_t,LITERAL_seconds);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
/********************LOGIC***********************************/
	public final String  logic(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST logic_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			AST __t358 = _t;
			AST tmp40_AST_in = (AST)_t;
			match(_t,LOGIC);
			_t = _t.getFirstChild();
			System.err.println("\n"); System.err.println("-------Starting LOGIC--------");
			{
			_loop364:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case IF:
				{
					System.err.println("-----------Starting IF -------");
					a=ifAST(_t,obj);
					_t = _retTree;
					{
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_1.member(_t.getType()))) {
						System.err.println("-----------Starting CONCLUDE -------");
						{
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==CONCLUDE)) {
							concludeAST(_t,obj, a);
							_t = _retTree;
						}
						else if ((_tokenSet_1.member(_t.getType()))) {
						}
						else {
							throw new NoViableAltException(_t);
						}
						
						}
						System.err.println("\n");System.err.println("-----------End CONCLUDE -------");
					}
					else if ((_tokenSet_2.member(_t.getType()))) {
						System.err.println("-----------Starting Logic Assignment -------");
						logicAssignmentAST(_t,obj, a);
						_t = _retTree;
						System.err.println("\n");System.err.println("-----------End logic assignment -------");
					}
					else {
						throw new NoViableAltException(_t);
					}
					
					}
					System.err.println("\n");System.err.println("-----------End IF -------");
					break;
				}
				case ELSE:
				case ELSEIF:
				case ENDIF:
				{
					System.err.println("-----------Starting ELSE - ELSEIF -------");
					a=logic_elseifAST(_t,obj);
					_t = _retTree;
					{
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_1.member(_t.getType()))) {
						System.err.println("-----------Starting CONCLUDE -------");
						{
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==CONCLUDE)) {
							concludeAST(_t,obj, a);
							_t = _retTree;
						}
						else if ((_tokenSet_1.member(_t.getType()))) {
						}
						else {
							throw new NoViableAltException(_t);
						}
						
						}
						System.err.println("\n");System.err.println("-----------End CONCLUDE -------");
					}
					else if ((_tokenSet_2.member(_t.getType()))) {
						System.err.println("-----------Starting Logic Assignment -------");
						logicAssignmentAST(_t,obj, a);
						_t = _retTree;
						System.err.println("\n");System.err.println("-----------End logic assignment -------");
					}
					else {
						throw new NoViableAltException(_t);
					}
					
					}
					System.err.println("\n");System.err.println("-----------End ELSE- ELSEIF -------");
					break;
				}
				case CONCLUDE:
				{
					System.err.println("-----------Starting CONCLUDE -------");obj.InitEvaluateList();
					concludeAST(_t,obj, "");
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End CONCLUDE -------");
					break;
				}
				default:
				{
					break _loop364;
				}
				}
			} while (true);
			}
			{
			AST tmp41_AST_in = (AST)_t;
			match(_t,ENDBLOCK);
			_t = _t.getNextSibling();
			}
			System.err.println("\n");System.err.println("-----------End Action -------");
			_t = __t358;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  ifAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST ifAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			{
			AST __t368 = _t;
			AST tmp42_AST_in = (AST)_t;
			match(_t,IF);
			_t = _t.getFirstChild();
			obj.ResetConceptVar(); obj.InitEvaluateList();
			s=exprAST(_t,obj);
			_t = _retTree;
			AST tmp43_AST_in = (AST)_t;
			match(_t,THEN);
			_t = _t.getNextSibling();
			_t = __t368;
			_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  concludeAST(AST _t,
		MLMObject obj, String key
	) throws RecognitionException {
		String s="";
		
		AST concludeAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			{
			AST __t391 = _t;
			AST tmp44_AST_in = (AST)_t;
			match(_t,CONCLUDE);
			_t = _t.getFirstChild();
			if(key == "") {
						a = "tmp_conclude";
						key = a;
						obj.SetConceptVar(a);
						obj.AddConcept(key);
						obj.AddToEvaluateList(a); obj.SetConceptVar(a);
						obj.SetDBAccess(false,a);
						} 
					
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case FALSE:
			{
				AST tmp45_AST_in = (AST)_t;
				match(_t,FALSE);
				_t = _t.getNextSibling();
				System.err.println("***CONCLUDE FALSE " );
								obj.SetConcludeVal(false, key);
				break;
			}
			case TRUE:
			{
				AST tmp46_AST_in = (AST)_t;
				match(_t,TRUE);
				_t = _t.getNextSibling();
				System.err.println("***CONCLUDE TRUE " );
					    			obj.SetConcludeVal(true, key);
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t391;
			_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  logicAssignmentAST(AST _t,
		MLMObject obj, String key
	) throws RecognitionException {
		String s="";
		
		AST logicAssignmentAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			exprStringAST(_t,obj, key);
			_t = _retTree;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  logic_elseifAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST logic_elseifAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			{
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ELSEIF:
			{
				AST __t396 = _t;
				AST tmp47_AST_in = (AST)_t;
				match(_t,ELSEIF);
				_t = _t.getFirstChild();
				obj.ResetConceptVar();
				s=exprAST(_t,obj);
				_t = _retTree;
				AST tmp48_AST_in = (AST)_t;
				match(_t,THEN);
				_t = _t.getNextSibling();
				_t = __t396;
				_t = _t.getNextSibling();
				break;
			}
			case ELSE:
			{
				AST __t397 = _t;
				AST tmp49_AST_in = (AST)_t;
				match(_t,ELSE);
				_t = _t.getFirstChild();
				obj.ResetConceptVar();
				s=exprAST(_t,obj);
				_t = _retTree;
				obj.AddConcept(s);obj.SetDBAccess(false,s);
				_t = __t397;
				_t = _t.getNextSibling();
				break;
			}
			case ENDIF:
			{
				AST __t398 = _t;
				AST tmp50_AST_in = (AST)_t;
				match(_t,ENDIF);
				_t = _t.getFirstChild();
				System.err.println("ENDIF FOUND");
				_t = __t398;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  exprAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST exprAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			{
			a=exprStringAST(_t,obj, "");
			_t = _retTree;
			s=a;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUALS:
			case LT:
			case GT:
			case LTE:
			case GTE:
			{
				simple_comp_opAST(_t,obj, a);
				_t = _retTree;
				b=exprStringAST(_t,obj, a);
				_t = _retTree;
				/*obj.SetAnswer(b, a);*/
				break;
			}
			case 3:
			case THEN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  exprStringAST(AST _t,
		MLMObject obj, String instr
	) throws RecognitionException {
		String s="";
		
		AST exprStringAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST ift = null;
		AST val = null;
		AST strlit = null;
		AST id = null;
		AST str = null;
		String a,b;
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				AST __t375 = _t;
				ift = _t==ASTNULL ? null :(AST)_t;
				match(_t,ID);
				_t = _t.getFirstChild();
				a = ift.getText(); System.err.println("IF text = " + a); 
							        if(instr.equals("")) {
							        		obj.AddToEvaluateList(a); obj.SetConceptVar(a);
								      //  	obj.RetrieveConcept(a); 
							        }
							        else { // if instr is not empty then we are evaluating RHS of an equation, it can be a non string literal
							        	obj.SetAnswer(a,instr);					
							        }
							        s= a;
							
				_t = __t375;
				_t = _t.getNextSibling();
				break;
			}
			case TRUE:
			case FALSE:
			{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case TRUE:
				{
					AST __t377 = _t;
					AST tmp51_AST_in = (AST)_t;
					match(_t,TRUE);
					_t = _t.getFirstChild();
					obj.SetAnswer(true, instr);
					_t = __t377;
					_t = _t.getNextSibling();
					break;
				}
				case FALSE:
				{
					AST __t378 = _t;
					AST tmp52_AST_in = (AST)_t;
					match(_t,FALSE);
					_t = _t.getFirstChild();
					obj.SetAnswer(false, instr);
					_t = __t378;
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				break;
			}
			case INTLIT:
			{
				{
				val = (AST)_t;
				match(_t,INTLIT);
				_t = _t.getNextSibling();
				
						  b = val.getText();
						  Integer i = Integer.parseInt(b);
						  obj.SetAnswer(i, instr);
						
				}
				break;
			}
			case STRING_LITERAL:
			{
				{
				strlit = (AST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				
							b = val.getText();
							obj.SetAnswer(b,instr);					
							
						
				}
				break;
			}
			case ACTION_OP:
			{
				AST __t381 = _t;
				AST tmp53_AST_in = (AST)_t;
				match(_t,ACTION_OP);
				_t = _t.getFirstChild();
				id = (AST)_t;
				match(_t,ID);
				_t = _t.getNextSibling();
				a = id.getText();
				AST tmp54_AST_in = (AST)_t;
				match(_t,ACTION_OP);
				_t = _t.getNextSibling();
				str = (AST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				b = str.getText(); 
								obj.SetUserVarVal(a, b, instr);
				_t = __t381;
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			case IF:
			case THEN:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case ENDBLOCK:
			case EQUALS:
			case LT:
			case GT:
			case LTE:
			case GTE:
			{
				{
				a = "tmp_01"; System.err.println("IF text = " + a); 
							        if(instr.equals(""))
							        	obj.AddToEvaluateList(a); obj.SetConceptVar(a);
							        	
							      //  	obj.RetrieveConcept(a); 
							        s= a;
				
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  simple_comp_opAST(AST _t,
		MLMObject obj, String key
	) throws RecognitionException {
		String s="";
		
		AST simple_comp_opAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUALS:
			{
				AST __t384 = _t;
				AST tmp55_AST_in = (AST)_t;
				match(_t,EQUALS);
				_t = _t.getFirstChild();
				
								System.err.println("Found = ");
								 obj.SetCompOperator(EQUALS, key);
							
				_t = __t384;
				_t = _t.getNextSibling();
				break;
			}
			case GTE:
			{
				AST __t385 = _t;
				AST tmp56_AST_in = (AST)_t;
				match(_t,GTE);
				_t = _t.getFirstChild();
				
								System.err.println("Found >= ");
								 obj.SetCompOperator(GTE, key);
							
				_t = __t385;
				_t = _t.getNextSibling();
				break;
			}
			case GT:
			{
				AST __t386 = _t;
				AST tmp57_AST_in = (AST)_t;
				match(_t,GT);
				_t = _t.getFirstChild();
				
								System.err.println("Found > ");
								 obj.SetCompOperator(GT, key);
							
				_t = __t386;
				_t = _t.getNextSibling();
				break;
			}
			case LT:
			{
				AST __t387 = _t;
				AST tmp58_AST_in = (AST)_t;
				match(_t,LT);
				_t = _t.getFirstChild();
				
								System.err.println("Found < ");
								 obj.SetCompOperator(LT, key);
							
				_t = __t387;
				_t = _t.getNextSibling();
				break;
			}
			case LTE:
			{
				AST __t388 = _t;
				AST tmp59_AST_in = (AST)_t;
				match(_t,LTE);
				_t = _t.getFirstChild();
				
								System.err.println("Found <= ");
								 obj.SetCompOperator(LTE, key);
							
				_t = __t388;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
/***********************ACTION*******************************************/
	public final String  action(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST action_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			AST __t400 = _t;
			AST tmp60_AST_in = (AST)_t;
			match(_t,ACTION);
			_t = _t.getFirstChild();
			System.err.println("\n"); System.err.println("-------Starting Action--------");
			{
			_loop402:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==WRITE)) {
					System.err.println("-----------Starting Write -------");
					s=writeAST(_t,obj);
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End Write -------");
				}
				else {
					break _loop402;
				}
				
			} while (true);
			}
			{
			AST tmp61_AST_in = (AST)_t;
			match(_t,ENDBLOCK);
			_t = _t.getNextSibling();
			}
			System.err.println("\n");System.err.println("-----------End Action -------");
			_t = __t400;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  writeAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST writeAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST id = null;
		AST i = null;
		String a="",b="";
		
		try {      // for error handling
			{
			AST __t406 = _t;
			AST tmp62_AST_in = (AST)_t;
			match(_t,WRITE);
			_t = _t.getFirstChild();
			{
			_loop410:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ACTION_OP:
				{
					{
					AST tmp63_AST_in = (AST)_t;
					match(_t,ACTION_OP);
					_t = _t.getNextSibling();
					id = (AST)_t;
					match(_t,ID);
					_t = _t.getNextSibling();
					a = id.getText(); 
												//b= obj.getUserVarVal(a);
												b = "||" + a + "||";
												s += b;
					AST tmp64_AST_in = (AST)_t;
					match(_t,ACTION_OP);
					_t = _t.getNextSibling();
					}
					break;
				}
				case STRING_LITERAL:
				{
					{
					i = (AST)_t;
					match(_t,STRING_LITERAL);
					_t = _t.getNextSibling();
					s += i.getText();
					}
					break;
				}
				default:
				{
					break _loop410;
				}
				}
			} while (true);
			}
			_t = __t406;
			_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  actionExprAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST actionExprAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST id = null;
		String a,b;
		
		try {      // for error handling
			{
			{
			id = (AST)_t;
			match(_t,ID);
			_t = _t.getNextSibling();
			a = id.getText(); s= obj.getUserVarVal(a);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  maintenance(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST maintenance_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a="",b = "";
		
		try {      // for error handling
			{
			AST __t416 = _t;
			AST tmp65_AST_in = (AST)_t;
			match(_t,MAINTENANCE);
			_t = _t.getFirstChild();
			{
			_loop421:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= AND && _t.getType() <= STRING_LITERAL))) {
					{
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==FILENAME)) {
						AST __t419 = _t;
						AST tmp66_AST_in = (AST)_t;
						match(_t,FILENAME);
						_t = _t.getFirstChild();
						AST tmp67_AST_in = (AST)_t;
						match(_t,COLON);
						_t = _t.getNextSibling();
						s += " Filename: ";
						b=textAST(_t,obj);
						_t = _retTree;
						obj.setClassName(b); s += b; s += "\n";
						_t = __t419;
						_t = _t.getNextSibling();
					}
					else if ((_t.getType()==MLMNAME)) {
						AST __t420 = _t;
						AST tmp68_AST_in = (AST)_t;
						match(_t,MLMNAME);
						_t = _t.getFirstChild();
						AST tmp69_AST_in = (AST)_t;
						match(_t,COLON);
						_t = _t.getNextSibling();
						s += " Filename: ";
						b=textAST(_t,obj);
						_t = _retTree;
						obj.setClassName(b); s += b; s += "\n";
						_t = __t420;
						_t = _t.getNextSibling();
					}
					else if (((_t.getType() >= AND && _t.getType() <= STRING_LITERAL))) {
						a=textAST(_t,obj);
						_t = _retTree;
						s += a;
						AST tmp70_AST_in = (AST)_t;
						match(_t,ENDBLOCK);
						_t = _t.getNextSibling();
						s += "\n";
					}
					else {
						throw new NoViableAltException(_t);
					}
					
					}
				}
				else {
					break _loop421;
				}
				
			} while (true);
			}
			_t = __t416;
			_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  textAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST textAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST str = null;
		String a="",b="";
		
		try {      // for error handling
			{
			{
			{
			_loop427:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_3.member(_t.getType()))) {
					{
					str = (AST)_t;
					match(_t,_tokenSet_3);
					_t = _t.getNextSibling();
					}
					a = " " + str.getText();s += a; /*System.err.println(s);*/
				}
				else {
					break _loop427;
				}
				
			} while (true);
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	public final String  library(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST library_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a="",b="";
		
		try {      // for error handling
			{
			AST __t430 = _t;
			AST tmp71_AST_in = (AST)_t;
			match(_t,LIBRARY);
			_t = _t.getFirstChild();
			{
			_loop432:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= AND && _t.getType() <= STRING_LITERAL))) {
					a=textAST(_t,obj);
					_t = _retTree;
					s += a;
					AST tmp72_AST_in = (AST)_t;
					match(_t,ENDBLOCK);
					_t = _t.getNextSibling();
					s += "\n";
				}
				else {
					break _loop432;
				}
				
			} while (true);
			}
			_t = __t430;
			_t = _t.getNextSibling();
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return s;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"and\"",
		"WEIRD_IDENT",
		"\"is\"",
		"\"are\"",
		"\"was\"",
		"\"were\"",
		"\"count\"",
		"\"in\"",
		"\"less\"",
		"\"the\"",
		"\"than\"",
		"\"from\"",
		"\"before\"",
		"\"after\"",
		"\"ago\"",
		"\"write\"",
		"\"at\"",
		"\"let\"",
		"\"now\"",
		"\"be\"",
		"\"year\"",
		"\"years\"",
		"\"if\"",
		"\"it\"",
		"\"they\"",
		"\"not\"",
		"\"or\"",
		"\"then\"",
		"\"read\"",
		"\"minimum\"",
		"\"min\"",
		"\"maximum\"",
		"\"max\"",
		"\"last\"",
		"\"first\"",
		"\"earliest\"",
		"\"lastest\"",
		"\"event\"",
		"\"where\"",
		"\"exist\"",
		"\"exists\"",
		"\"past\"",
		"\"days\"",
		"\"day\"",
		"\"month\"",
		"\"months\"",
		"\"week\"",
		"\"weeks\"",
		"\"avg\"",
		"\"average\"",
		"\"sum\"",
		"\"median\"",
		"\"conclude\"",
		"\"else\"",
		"\"elseif\"",
		"\"endif\"",
		"\"true\"",
		"\"false\"",
		"\"data\"",
		"\"logic\"",
		"\"action\"",
		"\"maintenance\"",
		"\"library\"",
		"\"filename\"",
		"\"mlmname\"",
		"\"of\"",
		"\"time\"",
		"\"within\"",
		"\"end\"",
		"COLON",
		"\"knowledge\"",
		"\"title\"",
		";;",
		"DOT",
		"MINUS",
		"UNDERSCORE",
		"\"arden\"",
		"\"ASTM-E\"",
		"INTLIT",
		"\"version\"",
		"DIGIT",
		"\"institution\"",
		"\"author\"",
		"SEMI",
		"\"specialist\"",
		"\"date\"",
		"\"validation\"",
		"\"production\"",
		"\"research\"",
		"\"testing\"",
		"\"expired\"",
		"TIMES",
		"an identifier",
		"LPAREN",
		"RPAREN",
		"\":\"",
		"\"T\"",
		"\"t\"",
		"\".\"",
		"\"+\"",
		"\"-\"",
		"\"Z\"",
		"\"z\"",
		"\"purpose\"",
		"\"explanation\"",
		"\"keywords\"",
		"\"citations\"",
		"\"SUPPORT\"",
		"\"REFUTE\"",
		"\"links\"",
		"SINGLE_QUOTE",
		"\"type\"",
		"\"data-driven\"",
		"\"data_driven\"",
		"COMMENT",
		"ML_COMMENT",
		"BECOMES",
		"COMMA",
		"\"EVENT\"",
		"\"Event\"",
		"ARDEN_CURLY_BRACKETS",
		"\"PRESENT\"",
		"\"NULL\"",
		"\"BOOLEAN\"",
		"\"NUMBER\"",
		"\"DURATION\"",
		"\"STRING\"",
		"\"LIST\"",
		"\"OBJECT\"",
		"\"GREATER\"",
		"\"EQUAL\"",
		"\"hour\"",
		"\"hours\"",
		"\"minute\"",
		"\"minutes\"",
		"\"second\"",
		"\"seconds\"",
		"\"OCCUR\"",
		"\"Occur\"",
		"\"occur\"",
		"\"OCCURS\"",
		"\"Occurs\"",
		"\"occurs\"",
		"\"OCCURRED\"",
		"\"Occurred\"",
		"\"priority\"",
		"\"evoke\"",
		"\"CALL\"",
		"\"Any\"",
		"EQUALS",
		"\"EQ\"",
		"LT",
		"\"LT\"",
		"GT",
		"\"GT\"",
		"LTE",
		"\"LE\"",
		"GTE",
		"\"GE\"",
		"NE",
		"\"NE\"",
		"ACTION_OP",
		"\"urgency\"",
		"\"MERGE\"",
		"\"SORT\"",
		"\"DATA\"",
		"\"SEQTO\"",
		"\"*\"",
		"\"/\"",
		"STRING_LITERAL"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 67582577417913344L, 1152921508901814272L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 1080863910636027904L, 4096L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 4539628424456568832L, 4295233536L, 35321811042304L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[8];
		data[0]=-16L;
		data[1]=-4097L;
		data[2]=70368744177663L;
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	}
	
