/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
// $ANTLR 2.7.6 (2005-12-22): "ArdenRecognizer.g" -> "ArdenBaseTreeParser.java"$

package org.openmrs.arden;

import java.io.*;
import antlr.CommonAST;
import antlr.collections.AST;
import antlr.*;
import org.openmrs.arden.MLMObject;
import org.openmrs.arden.MLMObjectElement;
import java.lang.Integer;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;


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
			AST __t325 = _t;
			AST tmp1_AST_in = (AST)_t;
			match(_t,DATA);
			_t = _t.getFirstChild();
			System.err.println("\n"); System.err.println("-------Starting Data--------");
			{
			_loop327:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case IF:
				{
					System.err.println("----------------Starting Data If-------");
					dataIfAST(_t,obj);
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End Data If -------");
					break;
				}
				case ELSE:
				case ELSEIF:
				case ENDIF:
				{
					System.err.println("----------------Starting Data Else If-------");
					data_elseifAST(_t,obj);
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End Data Else If -------");
					break;
				}
				default:
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_0.member(_t.getType()))) {
						System.err.println("-----------Starting Read -------");
						s=readAST(_t,obj, s);
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
					break _loop327;
				}
				}
			} while (true);
			}
			{
			AST tmp2_AST_in = (AST)_t;
			match(_t,ENDBLOCK);
			_t = _t.getNextSibling();
			}
			System.err.println("\n");System.err.println("-----------End Data -------");
			_t = __t325;
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
		MLMObject obj, String instr
	) throws RecognitionException {
		String s="";
		
		AST readAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
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
		String a="",b="", ret_val="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case READ:
			{
				AST __t346 = _t;
				AST tmp3_AST_in = (AST)_t;
				match(_t,READ);
				_t = _t.getFirstChild();
				a=readAST(_t,obj, instr);
				_t = _retTree;
				b=readAST(_t,obj, a);
				_t = _retTree;
				_t = __t346;
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
				if ((_t.getType()==INTLIT)) {
					k = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
				}
				else if ((_tokenSet_0.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				s+=b;obj.setReadType("last"); obj.setHowMany("1");
					  							if(k != null) {
					   								obj.setHowMany(k.getText());
					   								System.err.println("ReadType = Last " + "How many? " + k.getText());
					  							}
					  							 else {
						  							 System.err.println("ReadType = Last " + "How many? 1" );	
					  							 }
					  							 
					  							
				b=readAST(_t,obj, instr);
				_t = _retTree;
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
					AST tmp6_AST_in = (AST)_t;
					match(_t,FIRST);
					_t = _t.getNextSibling();
					break;
				}
				case EARLIEST:
				{
					AST tmp7_AST_in = (AST)_t;
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
				if ((_t.getType()==INTLIT)) {
					x = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
				}
				else if ((_tokenSet_0.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				s+=b;obj.setReadType("first"); obj.setHowMany("1");
					  								if(x != null) {
					   									obj.setHowMany(x.getText());
					   									System.err.println("ReadType = First " + "How many? " + x.getText());
					  								}
					  							 else {
						  							 System.err.println("ReadType = First " + "How many? 1" );	
					  							 }
					  							 
					  							
				b=readAST(_t,obj, instr);
				_t = _retTree;
				}
				break;
			}
			case MAXIMUM:
			case MAX:
			case INTLIT:
			{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case MAXIMUM:
				case MAX:
				{
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case MAXIMUM:
					{
						AST tmp8_AST_in = (AST)_t;
						match(_t,MAXIMUM);
						_t = _t.getNextSibling();
						break;
					}
					case MAX:
					{
						AST tmp9_AST_in = (AST)_t;
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
					{
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==INTLIT)) {
						y = (AST)_t;
						match(_t,INTLIT);
						_t = _t.getNextSibling();
					}
					else if ((_tokenSet_0.member(_t.getType()))) {
					}
					else {
						throw new NoViableAltException(_t);
					}
					
					}
					s+=b;obj.setReadType("max"); obj.setHowMany("1");
						  							if(y != null) {
						   									obj.setHowMany(y.getText());
						   									System.err.println("ReadType = Maximum " + "How many? " + y.getText());
						  								}
						  							 else {
							  							 System.err.println("ReadType = Maximum " + "How many? 1" );	
						  							 }
						  							 
						  							
					b=readAST(_t,obj, instr);
					_t = _retTree;
					}
					break;
				}
				case INTLIT:
				{
					{
					{
					intlit = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					dop = _t==ASTNULL ? null : (AST)_t;
					duration_op(_t);
					_t = _retTree;
					}
					System.err.println("Duration Clause - " + intlit.getText() + " " + dop.getText());
					}
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
			case MINIMUM:
			case MIN:
			{
				{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case MINIMUM:
				{
					AST tmp10_AST_in = (AST)_t;
					match(_t,MINIMUM);
					_t = _t.getNextSibling();
					break;
				}
				case MIN:
				{
					AST tmp11_AST_in = (AST)_t;
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
				if ((_t.getType()==INTLIT)) {
					z = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
				}
				else if ((_tokenSet_0.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				s+=b;obj.setReadType("min"); obj.setHowMany("1");
					  							if(z != null) {
						   								obj.setHowMany(z.getText());
						   								System.err.println("ReadType = Minimum " + "How many? " + z.getText());
					  								}
					  							 else {
						  							 System.err.println("ReadType = Min " + "How many? 1" );	
					  							 }
					  							 
					  							
				b=readAST(_t,obj, instr);
				_t = _retTree;
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
					AST tmp12_AST_in = (AST)_t;
					match(_t,EXIST);
					_t = _t.getNextSibling();
					break;
				}
				case EXISTS:
				{
					AST tmp13_AST_in = (AST)_t;
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
				s+=b;obj.setReadType("last"); obj.setHowMany("1");System.err.println("ReadType = Exist");
				b=readAST(_t,obj, instr);
				_t = _retTree;
				}
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
					AST tmp14_AST_in = (AST)_t;
					match(_t,AVERAGE);
					_t = _t.getNextSibling();
					break;
				}
				case AVG:
				{
					AST tmp15_AST_in = (AST)_t;
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
				b=readAST(_t,obj, instr);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Average");
				break;
			}
			case COUNT:
			{
				{
				AST tmp16_AST_in = (AST)_t;
				match(_t,COUNT);
				_t = _t.getNextSibling();
				b=readAST(_t,obj, instr);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Count");
				break;
			}
			case SUM:
			{
				{
				AST tmp17_AST_in = (AST)_t;
				match(_t,SUM);
				_t = _t.getNextSibling();
				b=readAST(_t,obj, instr);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Sum");
				break;
			}
			case MEDIAN:
			{
				{
				AST tmp18_AST_in = (AST)_t;
				match(_t,MEDIAN);
				_t = _t.getNextSibling();
				b=readAST(_t,obj, instr);
				_t = _retTree;
				}
				s+=b;System.err.println("ReadType = Median");
				break;
			}
			case TRUE:
			case FALSE:
			case ID:
			case ARDEN_CURLY_BRACKETS:
			{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ARDEN_CURLY_BRACKETS:
				{
					{
					j = (AST)_t;
					match(_t,ARDEN_CURLY_BRACKETS);
					_t = _t.getNextSibling();
					}
					/*s=b;*/System.err.println("Fetch this data - " + j.getText());
							  	 										s = j.getText(); 
							  	 										obj.AddConcept(s);
							  	 									
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case WHERE:
					{
						AST tmp19_AST_in = (AST)_t;
						match(_t,WHERE);
						_t = _t.getNextSibling();
						System.err.println("Where=TRUE");
						where_it_occurredAST(_t,obj, instr);
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
					case ENDBLOCK:
					case INTLIT:
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
					break;
				}
				case ID:
				{
					i = (AST)_t;
					match(_t,ID);
					_t = _t.getNextSibling();
					System.err.println("Variable = " + i.getText()); a= i.getText(); s=a; obj.SetConceptVar(a);
					break;
				}
				case TRUE:
				{
					t = (AST)_t;
					match(_t,TRUE);
					_t = _t.getNextSibling();
					s="true";
						            obj.AddConcept(s);obj.SetDBAccess(false,instr);
						
					break;
				}
				case FALSE:
				{
					f = (AST)_t;
					match(_t,FALSE);
					_t = _t.getNextSibling();
					s="false";
						 			 obj.AddConcept(s);obj.SetDBAccess(false,instr);
						 			
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
				AST __t401 = _t;
				AST tmp20_AST_in = (AST)_t;
				match(_t,EVENT);
				_t = _t.getFirstChild();
				b=eventAST(_t);
				_t = _retTree;
				_t = __t401;
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
	
	public final String  dataIfAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST dataIfAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a, b;
		
		try {      // for error handling
			{
			AST __t331 = _t;
			AST tmp21_AST_in = (AST)_t;
			match(_t,IF);
			_t = _t.getFirstChild();
			obj.ResetConceptVar(); obj.InitEvaluateList();
			s=exprAST(_t,obj);
			_t = _retTree;
			AST tmp22_AST_in = (AST)_t;
			match(_t,THEN);
			_t = _t.getNextSibling();
			_t = __t331;
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
	
	public final String  data_elseifAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST data_elseifAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			{
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ELSEIF:
			{
				AST __t335 = _t;
				AST tmp23_AST_in = (AST)_t;
				match(_t,ELSEIF);
				_t = _t.getFirstChild();
				obj.ResetConceptVar();
				s=exprAST(_t,obj);
				_t = _retTree;
				AST tmp24_AST_in = (AST)_t;
				match(_t,THEN);
				_t = _t.getNextSibling();
				_t = __t335;
				_t = _t.getNextSibling();
				break;
			}
			case ELSE:
			{
				AST __t336 = _t;
				AST tmp25_AST_in = (AST)_t;
				match(_t,ELSE);
				_t = _t.getFirstChild();
				obj.ResetConceptVar();
				s=exprAST(_t,obj);
				_t = _retTree;
				obj.AddConcept(s);obj.SetDBAccess(false,s);
				_t = __t336;
				_t = _t.getNextSibling();
				break;
			}
			case ENDIF:
			{
				AST __t337 = _t;
				AST tmp26_AST_in = (AST)_t;
				match(_t,ENDIF);
				_t = _t.getFirstChild();
				System.err.println("ENDIF FOUND");
				_t = __t337;
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
			if (_t==null) _t=ASTNULL;
			if ((_tokenSet_1.member(_t.getType()))) {
				{
				a=exprStringAST(_t,obj, "");
				_t = _retTree;
				s=a;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case IN:
				case LESS:
				case GREATER:
				case EQUALS:
				case LT:
				case GT:
				case LTE:
				case GTE:
				{
					{
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_2.member(_t.getType()))) {
						simple_comp_opAST(_t,obj, a);
						_t = _retTree;
					}
					else if ((_tokenSet_3.member(_t.getType()))) {
						binary_comp_opAST(_t,obj, a);
						_t = _retTree;
					}
					else {
						throw new NoViableAltException(_t);
					}
					
					}
					b=exprStringAST(_t,obj, a);
					_t = _retTree;
					break;
				}
				case 3:
				case AND:
				case COUNT:
				case IF:
				case NOT:
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
				case TO:
				case ENDBLOCK:
				case INTLIT:
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
			}
			else if ((_tokenSet_4.member(_t.getType()))) {
				expr_functionAST(_t,obj);
				_t = _retTree;
				{
				a=exprStringAST(_t,obj, "notnull");
				_t = _retTree;
				s=a;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case IN:
				case LESS:
				case GREATER:
				case EQUALS:
				{
					{
					binary_comp_opAST(_t,obj, a);
					_t = _retTree;
					}
					b=exprStringAST(_t,obj, a);
					_t = _retTree;
					{
					_loop435:
					do {
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==COMMA)) {
							AST tmp27_AST_in = (AST)_t;
							match(_t,COMMA);
							_t = _t.getNextSibling();
							exprStringAST(_t,obj, a);
							_t = _retTree;
							s=a;
						}
						else {
							break _loop435;
						}
						
					} while (true);
					}
					/*obj.SetAnswer(b, a);*/
					break;
				}
				case 3:
				case AND:
				case COUNT:
				case IF:
				case NOT:
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
				case TO:
				case ENDBLOCK:
				case INTLIT:
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
			}
			else {
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
	
	public final String  where_it_occurredAST(AST _t,
		MLMObject obj, String key
	) throws RecognitionException {
		String s="";
		
		AST where_it_occurredAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST m = null;
		AST n = null;
		AST i = null;
		String a,b, ret_val="";
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
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
			case WITHIN:
			case ANY:
			case INTLIT:
			case ID:
			case ACTION_OP:
			case STRING_LITERAL:
			case TERM_LITERAL:
			{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case WITHIN:
				{
					AST tmp28_AST_in = (AST)_t;
					match(_t,WITHIN);
					_t = _t.getNextSibling();
					obj.setWhere("withinPreceding", key);
					{
					AST tmp29_AST_in = (AST)_t;
					match(_t,PAST);
					_t = _t.getNextSibling();
					}
					{
					m = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					n = _t==ASTNULL ? null : (AST)_t;
					duration_op(_t);
					_t = _retTree;
					}
					obj.setDuration("past",m.getText(),n.getText(),key); System.err.println("Duration Clause - " + m.getText() + " " + n.getText());
					break;
				}
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
				case INTLIT:
				case ID:
				case ACTION_OP:
				case STRING_LITERAL:
				case TERM_LITERAL:
				{
					a=exprAST(_t,obj);
					_t = _retTree;
					AST tmp30_AST_in = (AST)_t;
					match(_t,TO);
					_t = _t.getNextSibling();
					b=exprAST(_t,obj);
					_t = _retTree;
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
			case AFTER:
			{
				{
				AST tmp31_AST_in = (AST)_t;
				match(_t,AFTER);
				_t = _t.getNextSibling();
				}
				{
				i = (AST)_t;
				match(_t,ID);
				_t = _t.getNextSibling();
				}
				System.err.println("Variable = " + i.getText());
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
	
	public final void duration_op(AST _t) throws RecognitionException {
		
		AST duration_op_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case YEAR:
			{
				AST tmp32_AST_in = (AST)_t;
				match(_t,YEAR);
				_t = _t.getNextSibling();
				break;
			}
			case YEARS:
			{
				AST tmp33_AST_in = (AST)_t;
				match(_t,YEARS);
				_t = _t.getNextSibling();
				break;
			}
			case MONTH:
			{
				AST tmp34_AST_in = (AST)_t;
				match(_t,MONTH);
				_t = _t.getNextSibling();
				break;
			}
			case MONTHS:
			{
				AST tmp35_AST_in = (AST)_t;
				match(_t,MONTHS);
				_t = _t.getNextSibling();
				break;
			}
			case WEEK:
			{
				AST tmp36_AST_in = (AST)_t;
				match(_t,WEEK);
				_t = _t.getNextSibling();
				break;
			}
			case WEEKS:
			{
				AST tmp37_AST_in = (AST)_t;
				match(_t,WEEKS);
				_t = _t.getNextSibling();
				break;
			}
			case DAY:
			{
				AST tmp38_AST_in = (AST)_t;
				match(_t,DAY);
				_t = _t.getNextSibling();
				break;
			}
			case DAYS:
			{
				AST tmp39_AST_in = (AST)_t;
				match(_t,DAYS);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_hour:
			{
				AST tmp40_AST_in = (AST)_t;
				match(_t,LITERAL_hour);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_hours:
			{
				AST tmp41_AST_in = (AST)_t;
				match(_t,LITERAL_hours);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_minute:
			{
				AST tmp42_AST_in = (AST)_t;
				match(_t,LITERAL_minute);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_minutes:
			{
				AST tmp43_AST_in = (AST)_t;
				match(_t,LITERAL_minutes);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_second:
			{
				AST tmp44_AST_in = (AST)_t;
				match(_t,LITERAL_second);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_seconds:
			{
				AST tmp45_AST_in = (AST)_t;
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
	
	public final String  from_of_func_opAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST from_of_func_opAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST k = null;
		AST x = null;
		AST y = null;
		AST z = null;
		String a,b, ret_val="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case TRUE:
			case FALSE:
			case OF:
			case INTLIT:
			case ID:
			case ACTION_OP:
			case STRING_LITERAL:
			case TERM_LITERAL:
			{
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
					AST tmp46_AST_in = (AST)_t;
					match(_t,LAST);
					_t = _t.getNextSibling();
					break;
				}
				case LATEST:
				{
					AST tmp47_AST_in = (AST)_t;
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
				if ((_t.getType()==INTLIT)) {
					k = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					AST tmp48_AST_in = (AST)_t;
					match(_t,FROM);
					_t = _t.getNextSibling();
				}
				else if ((_tokenSet_5.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				}
				if(k != null) {
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
					AST tmp49_AST_in = (AST)_t;
					match(_t,FIRST);
					_t = _t.getNextSibling();
					break;
				}
				case EARLIEST:
				{
					AST tmp50_AST_in = (AST)_t;
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
				if ((_t.getType()==INTLIT)) {
					x = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					AST tmp51_AST_in = (AST)_t;
					match(_t,FROM);
					_t = _t.getNextSibling();
				}
				else if ((_tokenSet_5.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				}
				if(x != null) {
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
					AST tmp52_AST_in = (AST)_t;
					match(_t,MAXIMUM);
					_t = _t.getNextSibling();
					break;
				}
				case MAX:
				{
					AST tmp53_AST_in = (AST)_t;
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
				if ((_t.getType()==INTLIT)) {
					y = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					AST tmp54_AST_in = (AST)_t;
					match(_t,FROM);
					_t = _t.getNextSibling();
				}
				else if ((_tokenSet_5.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				}
				if(y != null) {
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
					AST tmp55_AST_in = (AST)_t;
					match(_t,MINIMUM);
					_t = _t.getNextSibling();
					break;
				}
				case MIN:
				{
					AST tmp56_AST_in = (AST)_t;
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
				if ((_t.getType()==INTLIT)) {
					z = (AST)_t;
					match(_t,INTLIT);
					_t = _t.getNextSibling();
					AST tmp57_AST_in = (AST)_t;
					match(_t,FROM);
					_t = _t.getNextSibling();
				}
				else if ((_tokenSet_5.member(_t.getType()))) {
				}
				else {
					throw new NoViableAltException(_t);
				}
				
				}
				}
				if(z != null) {
												System.err.println("ReadType = Minimum " + "How many? " + z.getText());
												}
											 else {
					  							 System.err.println("ReadType = Min " + "How many? 1" );	
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
	
	public final String  of_read_func_opAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST of_read_func_opAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b, ret_val="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case TRUE:
			case FALSE:
			case OF:
			case INTLIT:
			case ID:
			case ACTION_OP:
			case STRING_LITERAL:
			case TERM_LITERAL:
			{
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
					AST tmp58_AST_in = (AST)_t;
					match(_t,EXIST);
					_t = _t.getNextSibling();
					break;
				}
				case EXISTS:
				{
					AST tmp59_AST_in = (AST)_t;
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
				}
				obj.AddToEvaluateList("EXIST");System.err.println("ReadType = Exist");
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
					AST tmp60_AST_in = (AST)_t;
					match(_t,AVERAGE);
					_t = _t.getNextSibling();
					break;
				}
				case AVG:
				{
					AST tmp61_AST_in = (AST)_t;
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
				}
				obj.AddToEvaluateList("AVG");System.err.println("ReadType = Average");
				break;
			}
			case COUNT:
			{
				{
				AST tmp62_AST_in = (AST)_t;
				match(_t,COUNT);
				_t = _t.getNextSibling();
				}
				obj.AddToEvaluateList("COUNT");System.err.println("ReadType = Count");
				break;
			}
			case SUM:
			{
				{
				AST tmp63_AST_in = (AST)_t;
				match(_t,SUM);
				_t = _t.getNextSibling();
				}
				obj.AddToEvaluateList("SUM");System.err.println("ReadType = Sum");
				break;
			}
			case MEDIAN:
			{
				{
				AST tmp64_AST_in = (AST)_t;
				match(_t,MEDIAN);
				_t = _t.getNextSibling();
				}
				obj.AddToEvaluateList("MEDIAN");System.err.println("ReadType = Median");
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
	
	public final String  of_noread_func_opAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST of_noread_func_opAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b, ret_val="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case TRUE:
			case FALSE:
			case OF:
			case INTLIT:
			case ID:
			case ACTION_OP:
			case STRING_LITERAL:
			case TERM_LITERAL:
			{
				break;
			}
			case ANY:
			{
				{
				AST tmp65_AST_in = (AST)_t;
				match(_t,ANY);
				_t = _t.getNextSibling();
				}
				obj.AddToEvaluateList("ANY");System.err.println("Any of");
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
	
/********************LOGIC***********************************/
	public final String  logic(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST logic_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b; Integer i = 1;
		
		try {      // for error handling
			AST __t403 = _t;
			AST tmp66_AST_in = (AST)_t;
			match(_t,LOGIC);
			_t = _t.getFirstChild();
			System.err.println("\n"); System.err.println("-------Starting LOGIC--------");
			{
			_loop412:
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
					switch ( _t.getType()) {
					case TRUE:
					case FALSE:
					case INTLIT:
					case ID:
					case ACTION_OP:
					case STRING_LITERAL:
					case TERM_LITERAL:
					{
						System.err.println("-----------Starting Logic Assignment -------");
						logicAssignmentAST(_t,obj, a);
						_t = _retTree;
						System.err.println("\n");System.err.println("-----------End logic assignment -------");
						break;
					}
					case IF:
					case CONCLUDE:
					case ELSE:
					case ELSEIF:
					case ENDIF:
					case CALL:
					case ENDBLOCK:
					{
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
					if ((_tokenSet_6.member(_t.getType()))) {
						System.err.println("-----------Starting CONCLUDE -------");
						{
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==CONCLUDE)) {
							concludeAST(_t,obj, a);
							_t = _retTree;
						}
						else if ((_tokenSet_6.member(_t.getType()))) {
						}
						else {
							throw new NoViableAltException(_t);
						}
						
						}
						System.err.println("\n");System.err.println("-----------End CONCLUDE -------");
					}
					else if ((_tokenSet_6.member(_t.getType()))) {
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
				{
					System.err.println("-----------Starting ELSE - ELSEIF -------");
					a=logic_elseifAST(_t,obj, i);
					_t = _retTree;
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case TRUE:
					case FALSE:
					case INTLIT:
					case ID:
					case ACTION_OP:
					case STRING_LITERAL:
					case TERM_LITERAL:
					{
						System.err.println("-----------Starting Logic Assignment -------");
						logicAssignmentAST(_t,obj, a);
						_t = _retTree;
						System.err.println("\n");System.err.println("-----------End logic assignment -------");
						break;
					}
					case IF:
					case CONCLUDE:
					case ELSE:
					case ELSEIF:
					case ENDIF:
					case CALL:
					case ENDBLOCK:
					{
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
					if ((_tokenSet_6.member(_t.getType()))) {
						System.err.println("-----------Starting CONCLUDE -------");
						{
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==CONCLUDE)) {
							concludeAST(_t,obj, a);
							_t = _retTree;
						}
						else if ((_tokenSet_6.member(_t.getType()))) {
						}
						else {
							throw new NoViableAltException(_t);
						}
						
						}
						System.err.println("\n");System.err.println("-----------End CONCLUDE -------");
					}
					else if ((_tokenSet_6.member(_t.getType()))) {
					}
					else {
						throw new NoViableAltException(_t);
					}
					
					}
					System.err.println("\n");System.err.println("-----------End ELSE- ELSEIF -------");
					break;
				}
				case ENDIF:
				{
					AST __t411 = _t;
					AST tmp67_AST_in = (AST)_t;
					match(_t,ENDIF);
					_t = _t.getFirstChild();
					System.err.println("ENDIF FOUND");a = "ENDIF"; obj.AddToEvaluateList(a);obj.SetConceptVar(a);
					_t = __t411;
					_t = _t.getNextSibling();
					break;
				}
				case CONCLUDE:
				{
					System.err.println("-----------Starting CONCLUDE -------");obj.InitEvaluateList(); a = "Conclude_" + Integer.toString(i);
					concludeAST(_t,obj, a);
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End CONCLUDE -------");
					break;
				}
				case CALL:
				{
					System.err.println("-----------Starting CALL -------");obj.InitEvaluateList(); a = "" ;
					callAST(_t,obj, a);
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End CALL -------");
					i++;
					break;
				}
				default:
				{
					break _loop412;
				}
				}
			} while (true);
			}
			{
			AST tmp68_AST_in = (AST)_t;
			match(_t,ENDBLOCK);
			_t = _t.getNextSibling();
			}
			System.err.println("\n");System.err.println("-----------End LOGIC -------");
			_t = __t403;
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
			AST __t419 = _t;
			AST tmp69_AST_in = (AST)_t;
			match(_t,IF);
			_t = _t.getFirstChild();
			obj.ResetConceptVar(); obj.InitEvaluateList(); obj.AddToEvaluateList("IF");
			s=exprAST(_t,obj);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case AND:
			case NOT:
			case OR:
			{
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case OR:
				{
					AST tmp70_AST_in = (AST)_t;
					match(_t,OR);
					_t = _t.getNextSibling();
					obj.AddToEvaluateList("OR");
					break;
				}
				case AND:
				{
					AST tmp71_AST_in = (AST)_t;
					match(_t,AND);
					_t = _t.getNextSibling();
					obj.AddToEvaluateList("AND");
					break;
				}
				case NOT:
				{
					AST tmp72_AST_in = (AST)_t;
					match(_t,NOT);
					_t = _t.getNextSibling();
					obj.AddToEvaluateList("NOT");
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				s=exprAST(_t,obj);
				_t = _retTree;
				break;
			}
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
			AST tmp73_AST_in = (AST)_t;
			match(_t,THEN);
			_t = _t.getNextSibling();
			obj.AddToEvaluateList("THEN");
			_t = __t419;
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
		AST thisstrlit = null;
		AST str1 = null;
		String a="",b="";
		
		try {      // for error handling
			a=exprStringAST(_t,obj, "CTX"/*key Do not use key- depends on context so CTX*/);
			_t = _retTree;
			obj.AddToEvaluateList("Logic_Assignment");
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STRING_LITERAL:
			{
				thisstrlit = (AST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				b += thisstrlit.getText();
				{
				_loop425:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==ACTION_OP)) {
						AST tmp74_AST_in = (AST)_t;
						match(_t,ACTION_OP);
						_t = _t.getNextSibling();
						str1 = (AST)_t;
						match(_t,STRING_LITERAL);
						_t = _t.getNextSibling();
						b += str1.getText();
					}
					else {
						break _loop425;
					}
					
				} while (true);
				}
				obj.SetUserVarVal(a, b, key);
				break;
			}
			case IF:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case CALL:
			case ENDBLOCK:
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
			AST __t466 = _t;
			AST tmp75_AST_in = (AST)_t;
			match(_t,CONCLUDE);
			_t = _t.getFirstChild();
			
						{
				    			a = "Conclude";
				    		//	key = a;
				    			obj.SetConceptVar(key);
				    			obj.AddConcept(key);
				    			if(key.startsWith("Conclude_")) { // Simply Conclude
				    				obj.AddToEvaluateList(key); 
				    			}
				    			else {  // Associate with the Else before
				    				obj.AddToEvaluateList(a); 
				    			}
				    			if(key.startsWith("Func_")) {obj.SetDBAccess(false,key);}
				    		}
						
					
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case FALSE:
			{
				AST tmp76_AST_in = (AST)_t;
				match(_t,FALSE);
				_t = _t.getNextSibling();
				System.err.println("***CONCLUDE FALSE " );
								obj.SetConcludeVal(false, key);
				break;
			}
			case TRUE:
			{
				AST tmp77_AST_in = (AST)_t;
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
			_t = __t466;
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
	
	public final String  logic_elseifAST(AST _t,
		MLMObject obj, Integer i
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
				AST __t471 = _t;
				AST tmp78_AST_in = (AST)_t;
				match(_t,ELSEIF);
				_t = _t.getFirstChild();
				obj.ResetConceptVar();
				a = "ELSEIF"; System.err.println("ELSEIF" ); 
									        	 obj.AddToEvaluateList(a);
									        	 obj.SetConceptVar(a);
						
				s=exprAST(_t,obj);
				_t = _retTree;
				AST tmp79_AST_in = (AST)_t;
				match(_t,THEN);
				_t = _t.getNextSibling();
				obj.AddToEvaluateList("THEN");
				_t = __t471;
				_t = _t.getNextSibling();
				break;
			}
			case ELSE:
			{
				AST __t472 = _t;
				AST tmp80_AST_in = (AST)_t;
				match(_t,ELSE);
				_t = _t.getFirstChild();
				obj.ResetConceptVar();
				a = "ELSE_"; s= a+ Integer.toString(i); System.err.println("ELSE" ); 
									        	 obj.AddToEvaluateList(s);
									        	 obj.SetConceptVar(s);
						
				_t = __t472;
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
	
	public final String  callAST(AST _t,
		MLMObject obj, String key
	) throws RecognitionException {
		String s="";
		
		AST callAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			{
			AST __t416 = _t;
			AST tmp81_AST_in = (AST)_t;
			match(_t,CALL);
			_t = _t.getFirstChild();
			b=exprStringAST(_t,obj, key);
			_t = _retTree;
			obj.SetConceptVar(b);
			a=exprStringAST(_t,obj, b);
			_t = _retTree;
			obj.setReadType("call"); obj.AddConcept(a);obj.SetDBAccess(false,b);
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
	
	public final String  exprStringAST(AST _t,
		MLMObject obj, String instr
	) throws RecognitionException {
		String s="";
		
		AST exprStringAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST ift = null;
		AST val = null;
		AST strlit = null;
		AST termlit = null;
		AST id = null;
		AST str = null;
		String a="",b="";
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ID:
			{
				AST __t443 = _t;
				ift = _t==ASTNULL ? null :(AST)_t;
				match(_t,ID);
				_t = _t.getFirstChild();
				a = ift.getText(); System.err.println("text = " + a); 
							        if(instr.equals("")) {
							        		obj.AddToEvaluateList(a); obj.SetConceptVar(a);
							        		s= a;
								      //  	obj.RetrieveConcept(a); 
							        }
							        else if(instr.equals("CTX")) {
							        	s=a;
							        	// do nothing for now
							        }
							        else if(instr.equals("notnull")) {
							        	obj.AddToEvaluateList(a);
							        	if(obj.GetMLMObjectElement(a) == null) {
							        		s="Func_1";  // Func like Exist..          
							        	}
							        	else {
							        		s=a;
							        	}
							        	
							        }
							        else { // if instr is not empty then we are evaluating RHS of an equation, it can be a non string literal
							        	obj.SetAnswer(a,instr);					
							        	s=a;
							        }
							        
							
				_t = __t443;
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
					AST __t445 = _t;
					AST tmp82_AST_in = (AST)_t;
					match(_t,TRUE);
					_t = _t.getFirstChild();
					obj.SetAnswer(true, instr);
					_t = __t445;
					_t = _t.getNextSibling();
					break;
				}
				case FALSE:
				{
					AST __t446 = _t;
					AST tmp83_AST_in = (AST)_t;
					match(_t,FALSE);
					_t = _t.getFirstChild();
					obj.SetAnswer(false, instr);
					_t = __t446;
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
				
							b = strlit.getText();
							obj.SetAnswer(b,instr);					
							
						
				}
				break;
			}
			case TERM_LITERAL:
			{
				{
				termlit = (AST)_t;
				match(_t,TERM_LITERAL);
				_t = _t.getNextSibling();
				
							b = termlit.getText();
							obj.SetAnswer(b,instr);					
							
						
				}
				break;
			}
			case ACTION_OP:
			{
				AST __t450 = _t;
				AST tmp84_AST_in = (AST)_t;
				match(_t,ACTION_OP);
				_t = _t.getFirstChild();
				id = (AST)_t;
				match(_t,ID);
				_t = _t.getNextSibling();
				a = id.getText();
				AST tmp85_AST_in = (AST)_t;
				match(_t,ACTION_OP);
				_t = _t.getNextSibling();
				str = (AST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				b = str.getText(); 
								obj.SetUserVarVal(a, b, instr);
				_t = __t450;
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
				AST __t452 = _t;
				AST tmp86_AST_in = (AST)_t;
				match(_t,EQUALS);
				_t = _t.getFirstChild();
				
								System.err.println("Found = ");
								 obj.SetCompOperator(EQUALS, key);
							
				_t = __t452;
				_t = _t.getNextSibling();
				break;
			}
			case GTE:
			{
				AST __t453 = _t;
				AST tmp87_AST_in = (AST)_t;
				match(_t,GTE);
				_t = _t.getFirstChild();
				
								System.err.println("Found >= ");
								 obj.SetCompOperator(GTE, key);
							
				_t = __t453;
				_t = _t.getNextSibling();
				break;
			}
			case GT:
			{
				AST __t454 = _t;
				AST tmp88_AST_in = (AST)_t;
				match(_t,GT);
				_t = _t.getFirstChild();
				
								System.err.println("Found > ");
								 obj.SetCompOperator(GT, key);
							
				_t = __t454;
				_t = _t.getNextSibling();
				break;
			}
			case LT:
			{
				AST __t455 = _t;
				AST tmp89_AST_in = (AST)_t;
				match(_t,LT);
				_t = _t.getFirstChild();
				
								System.err.println("Found < ");
								 obj.SetCompOperator(LT, key);
							
				_t = __t455;
				_t = _t.getNextSibling();
				break;
			}
			case LTE:
			{
				AST __t456 = _t;
				AST tmp90_AST_in = (AST)_t;
				match(_t,LTE);
				_t = _t.getFirstChild();
				
								System.err.println("Found <= ");
								 obj.SetCompOperator(LTE, key);
							
				_t = __t456;
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
	
	public final String  binary_comp_opAST(AST _t,
		MLMObject obj, String key
	) throws RecognitionException {
		String s="";
		
		AST binary_comp_opAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a,b;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUALS:
			{
				AST __t458 = _t;
				AST tmp91_AST_in = (AST)_t;
				match(_t,EQUALS);
				_t = _t.getFirstChild();
				
								System.err.println("Found = ");
								 obj.SetCompOperator(EQUALS, key);
							
				_t = __t458;
				_t = _t.getNextSibling();
				break;
			}
			case GREATER:
			{
				AST __t459 = _t;
				AST tmp92_AST_in = (AST)_t;
				match(_t,GREATER);
				_t = _t.getFirstChild();
				AST tmp93_AST_in = (AST)_t;
				match(_t,THAN);
				_t = _t.getNextSibling();
				
								System.err.println("Found > ");
								 obj.SetCompOperator(GT, key);
							
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case OR:
				{
					AST tmp94_AST_in = (AST)_t;
					match(_t,OR);
					_t = _t.getNextSibling();
					AST tmp95_AST_in = (AST)_t;
					match(_t,EQUAL);
					_t = _t.getNextSibling();
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				
							    System.err.println("Found >= ");
								 obj.SetCompOperator(GTE, key);
							
				_t = __t459;
				_t = _t.getNextSibling();
				break;
			}
			case IN:
			{
				AST __t463 = _t;
				AST tmp96_AST_in = (AST)_t;
				match(_t,IN);
				_t = _t.getFirstChild();
				
								System.err.println("Found IN ");
								 obj.SetCompOperator(IN, key);
							
				_t = __t463;
				_t = _t.getNextSibling();
				break;
			}
			default:
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LESS)) {
					AST __t461 = _t;
					AST tmp97_AST_in = (AST)_t;
					match(_t,LESS);
					_t = _t.getFirstChild();
					AST tmp98_AST_in = (AST)_t;
					match(_t,THAN);
					_t = _t.getNextSibling();
					
									System.err.println("Found < ");
									 obj.SetCompOperator(LT, key);
								
					_t = __t461;
					_t = _t.getNextSibling();
				}
				else if ((_t.getType()==LESS)) {
					AST __t462 = _t;
					AST tmp99_AST_in = (AST)_t;
					match(_t,LESS);
					_t = _t.getFirstChild();
					AST tmp100_AST_in = (AST)_t;
					match(_t,THAN);
					_t = _t.getNextSibling();
					AST tmp101_AST_in = (AST)_t;
					match(_t,OR);
					_t = _t.getNextSibling();
					AST tmp102_AST_in = (AST)_t;
					match(_t,EQUAL);
					_t = _t.getNextSibling();
					
									System.err.println("Found <= ");
									 obj.SetCompOperator(LTE, key);
								
					_t = __t462;
					_t = _t.getNextSibling();
				}
			else {
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
	
	public final String  expr_functionAST(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST expr_functionAST_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			if ((_tokenSet_7.member(_t.getType()))) {
				from_of_func_opAST(_t,obj);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case OF:
				{
					AST tmp103_AST_in = (AST)_t;
					match(_t,OF);
					_t = _t.getNextSibling();
					break;
				}
				case TRUE:
				case FALSE:
				case INTLIT:
				case ID:
				case ACTION_OP:
				case STRING_LITERAL:
				case TERM_LITERAL:
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
			else if ((_tokenSet_8.member(_t.getType()))) {
				of_read_func_opAST(_t,obj);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case OF:
				{
					AST tmp104_AST_in = (AST)_t;
					match(_t,OF);
					_t = _t.getNextSibling();
					break;
				}
				case TRUE:
				case FALSE:
				case INTLIT:
				case ID:
				case ACTION_OP:
				case STRING_LITERAL:
				case TERM_LITERAL:
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
			else if ((_tokenSet_9.member(_t.getType()))) {
				of_noread_func_opAST(_t,obj);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case OF:
				{
					AST tmp105_AST_in = (AST)_t;
					match(_t,OF);
					_t = _t.getNextSibling();
					break;
				}
				case TRUE:
				case FALSE:
				case INTLIT:
				case ID:
				case ACTION_OP:
				case STRING_LITERAL:
				case TERM_LITERAL:
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
			else {
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
			AST __t474 = _t;
			AST tmp106_AST_in = (AST)_t;
			match(_t,ACTION);
			_t = _t.getFirstChild();
			System.err.println("\n"); System.err.println("-------Starting Action--------");
			{
			_loop476:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==WRITE)) {
					System.err.println("-----------Starting Write -------");
					s=writeAST(_t,obj);
					_t = _retTree;
					System.err.println("\n");System.err.println("-----------End Write -------");
				}
				else {
					break _loop476;
				}
				
			} while (true);
			}
			{
			AST tmp107_AST_in = (AST)_t;
			match(_t,ENDBLOCK);
			_t = _t.getNextSibling();
			}
			System.err.println("\n");System.err.println("-----------End Action -------");
			_t = __t474;
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
			AST __t480 = _t;
			AST tmp108_AST_in = (AST)_t;
			match(_t,WRITE);
			_t = _t.getFirstChild();
			{
			_loop484:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ACTION_OP:
				{
					{
					AST tmp109_AST_in = (AST)_t;
					match(_t,ACTION_OP);
					_t = _t.getNextSibling();
					id = (AST)_t;
					match(_t,ID);
					_t = _t.getNextSibling();
					a = id.getText(); 
												//b= obj.getUserVarVal(a);
												b = "||" + a + "||";
												s += b;
					AST tmp110_AST_in = (AST)_t;
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
					break _loop484;
				}
				}
			} while (true);
			}
			_t = __t480;
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
	
	public final String  maintenance(AST _t,
		MLMObject obj
	) throws RecognitionException {
		String s="";
		
		AST maintenance_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		String a="",b = "";
		
		try {      // for error handling
			{
			AST __t487 = _t;
			AST tmp111_AST_in = (AST)_t;
			match(_t,MAINTENANCE);
			_t = _t.getFirstChild();
			{
			_loop492:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
					{
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==FILENAME)) {
						AST __t490 = _t;
						AST tmp112_AST_in = (AST)_t;
						match(_t,FILENAME);
						_t = _t.getFirstChild();
						AST tmp113_AST_in = (AST)_t;
						match(_t,COLON);
						_t = _t.getNextSibling();
						s += " Filename: ";
						b=textAST(_t,obj);
						_t = _retTree;
						obj.setClassName(b); s += b; s += "\n";
						_t = __t490;
						_t = _t.getNextSibling();
					}
					else if ((_t.getType()==MLMNAME)) {
						AST __t491 = _t;
						AST tmp114_AST_in = (AST)_t;
						match(_t,MLMNAME);
						_t = _t.getFirstChild();
						AST tmp115_AST_in = (AST)_t;
						match(_t,COLON);
						_t = _t.getNextSibling();
						s += " Filename: ";
						b=textAST(_t,obj);
						_t = _retTree;
						obj.setClassName(b); s += b; s += "\n";
						_t = __t491;
						_t = _t.getNextSibling();
					}
					else if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
						a=textAST(_t,obj);
						_t = _retTree;
						s += a;
						AST tmp116_AST_in = (AST)_t;
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
					break _loop492;
				}
				
			} while (true);
			}
			_t = __t487;
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
			_loop498:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_10.member(_t.getType()))) {
					{
					str = (AST)_t;
					match(_t,_tokenSet_10);
					_t = _t.getNextSibling();
					}
					a = " " + str.getText();s += a; /*System.err.println(s);*/
				}
				else {
					break _loop498;
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
			AST __t501 = _t;
			AST tmp117_AST_in = (AST)_t;
			match(_t,LIBRARY);
			_t = _t.getFirstChild();
			{
			_loop503:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= AND && _t.getType() <= TERM_LITERAL))) {
					a=textAST(_t,obj);
					_t = _retTree;
					s += a;
					AST tmp118_AST_in = (AST)_t;
					match(_t,ENDBLOCK);
					_t = _t.getNextSibling();
					s += "\n";
				}
				else {
					break _loop503;
				}
				
			} while (true);
			}
			_t = __t501;
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
		"\"greater\"",
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
		"\"call\"",
		"\"with\"",
		"\"to\"",
		"\"any\"",
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
		"EQUAL",
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
		"STRING_LITERAL",
		"TERM_LITERAL"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 7052694182476907520L, 137447342080L, 2L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 6917529027641081856L, 137447342080L, 845524441759744L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 0L, 0L, 91536490496L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 14336L, 0L, 268435456L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 7052694173886972928L, 137447346240L, 845524441759744L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 6917529027641081856L, 137447342144L, 845524441759744L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 2161727821272055808L, 131584L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 6917533408507723776L, 137447342144L, 845524441759744L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 7052689793020331008L, 137447342144L, 845524441759744L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 6917529027641081856L, 137447346240L, 845524441759744L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[8];
		data[0]=-16L;
		data[1]=-131073L;
		data[2]=1125899906842623L;
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	}
	
