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
// $ANTLR 2.7.6 (2005-12-22): "ArdenRecognizer.g" -> "ArdenBaseParser.java"$

package org.openmrs.arden;

import java.io.*;
import antlr.CommonAST;
import antlr.collections.AST;
import antlr.*;
import org.openmrs.arden.MLMObject;
import org.openmrs.arden.MLMObjectElement;
import java.lang.Integer;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class ArdenBaseParser extends antlr.LLkParser       implements ArdenBaseParserTokenTypes
 {

protected ArdenBaseParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public ArdenBaseParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected ArdenBaseParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public ArdenBaseParser(TokenStream lexer) {
  this(lexer,1);
}

public ArdenBaseParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void startRule() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST startRule_AST = null;
		
		try {      // for error handling
			maintenance_category();
			astFactory.addASTChild(currentAST, returnAST);
			library_category();
			astFactory.addASTChild(currentAST, returnAST);
			knowledge_category();
			astFactory.addASTChild(currentAST, returnAST);
			match(LITERAL_end);
			match(COLON);
			startRule_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = startRule_AST;
	}
	
	public final void maintenance_category() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST maintenance_category_AST = null;
		
		try {      // for error handling
			{
			AST tmp121_AST = null;
			tmp121_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp121_AST);
			match(MAINTENANCE);
			match(COLON);
			}
			maintenance_body();
			astFactory.addASTChild(currentAST, returnAST);
			maintenance_category_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		returnAST = maintenance_category_AST;
	}
	
	public final void library_category() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST library_category_AST = null;
		
		try {      // for error handling
			AST tmp123_AST = null;
			tmp123_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp123_AST);
			match(LIBRARY);
			match(COLON);
			library_body();
			astFactory.addASTChild(currentAST, returnAST);
			library_category_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = library_category_AST;
	}
	
	public final void knowledge_category() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST knowledge_category_AST = null;
		
		try {      // for error handling
			match(LITERAL_knowledge);
			match(COLON);
			knowledge_body();
			astFactory.addASTChild(currentAST, returnAST);
			knowledge_category_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		returnAST = knowledge_category_AST;
	}
	
	public final void maintenance_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST maintenance_body_AST = null;
		
		try {      // for error handling
			title_slot();
			astFactory.addASTChild(currentAST, returnAST);
			mlmname_slot();
			astFactory.addASTChild(currentAST, returnAST);
			arden_version_slot();
			astFactory.addASTChild(currentAST, returnAST);
			version_slot();
			astFactory.addASTChild(currentAST, returnAST);
			institution_slot();
			astFactory.addASTChild(currentAST, returnAST);
			author_slot();
			astFactory.addASTChild(currentAST, returnAST);
			specialist_slot();
			astFactory.addASTChild(currentAST, returnAST);
			date_slot();
			astFactory.addASTChild(currentAST, returnAST);
			validation_slot();
			astFactory.addASTChild(currentAST, returnAST);
			maintenance_body_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		returnAST = maintenance_body_AST;
	}
	
/********** Maintenance Slots **********************/
	public final void title_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST title_slot_AST = null;
		
		try {      // for error handling
			{
			AST tmp127_AST = null;
			tmp127_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp127_AST);
			match(LITERAL_title);
			AST tmp128_AST = null;
			tmp128_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp128_AST);
			match(COLON);
			{
			_loop12:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop12;
				}
				
			} while (true);
			}
			AST tmp129_AST = null;
			tmp129_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp129_AST);
			match(ENDBLOCK);
			}
			title_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_5);
			} else {
			  throw ex;
			}
		}
		returnAST = title_slot_AST;
	}
	
	public final void mlmname_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlmname_slot_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case MLMNAME:
			{
				AST tmp130_AST = null;
				tmp130_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp130_AST);
				match(MLMNAME);
				AST tmp131_AST = null;
				tmp131_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp131_AST);
				match(COLON);
				mlmname_text();
				astFactory.addASTChild(currentAST, returnAST);
				mlmname_slot_AST = (AST)currentAST.root;
				break;
			}
			case FILENAME:
			{
				AST tmp132_AST = null;
				tmp132_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp132_AST);
				match(FILENAME);
				AST tmp133_AST = null;
				tmp133_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp133_AST);
				match(COLON);
				mlmname_text();
				astFactory.addASTChild(currentAST, returnAST);
				mlmname_slot_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_6);
			} else {
			  throw ex;
			}
		}
		returnAST = mlmname_slot_AST;
	}
	
	public final void arden_version_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arden_version_slot_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_arden:
			{
				AST tmp134_AST = null;
				tmp134_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp134_AST);
				match(LITERAL_arden);
				AST tmp135_AST = null;
				tmp135_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp135_AST);
				match(COLON);
				{
				switch ( LA(1)) {
				case 86:
				{
					{
					AST tmp136_AST = null;
					tmp136_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp136_AST);
					match(86);
					AST tmp137_AST = null;
					tmp137_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp137_AST);
					match(INTLIT);
					AST tmp138_AST = null;
					tmp138_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp138_AST);
					match(MINUS);
					AST tmp139_AST = null;
					tmp139_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp139_AST);
					match(INTLIT);
					}
					break;
				}
				case LITERAL_version:
				{
					{
					AST tmp140_AST = null;
					tmp140_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp140_AST);
					match(LITERAL_version);
					version_num();
					astFactory.addASTChild(currentAST, returnAST);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				AST tmp141_AST = null;
				tmp141_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp141_AST);
				match(ENDBLOCK);
				arden_version_slot_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_version:
			{
				arden_version_slot_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_7);
			} else {
			  throw ex;
			}
		}
		returnAST = arden_version_slot_AST;
	}
	
	public final void version_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST version_slot_AST = null;
		
		try {      // for error handling
			AST tmp142_AST = null;
			tmp142_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp142_AST);
			match(LITERAL_version);
			AST tmp143_AST = null;
			tmp143_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp143_AST);
			match(COLON);
			AST tmp144_AST = null;
			tmp144_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp144_AST);
			match(INTLIT);
			AST tmp145_AST = null;
			tmp145_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp145_AST);
			match(DOT);
			AST tmp146_AST = null;
			tmp146_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp146_AST);
			match(INTLIT);
			AST tmp147_AST = null;
			tmp147_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp147_AST);
			match(ENDBLOCK);
			version_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_8);
			} else {
			  throw ex;
			}
		}
		returnAST = version_slot_AST;
	}
	
	public final void institution_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST institution_slot_AST = null;
		
		try {      // for error handling
			AST tmp148_AST = null;
			tmp148_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp148_AST);
			match(LITERAL_institution);
			AST tmp149_AST = null;
			tmp149_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp149_AST);
			match(COLON);
			{
			_loop32:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop32;
				}
				
			} while (true);
			}
			AST tmp150_AST = null;
			tmp150_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp150_AST);
			match(ENDBLOCK);
			institution_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		returnAST = institution_slot_AST;
	}
	
	public final void author_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST author_slot_AST = null;
		
		try {      // for error handling
			AST tmp151_AST = null;
			tmp151_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp151_AST);
			match(LITERAL_author);
			AST tmp152_AST = null;
			tmp152_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp152_AST);
			match(COLON);
			{
			_loop35:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop35;
				}
				
			} while (true);
			}
			{
			_loop39:
			do {
				if ((LA(1)==SEMI)) {
					AST tmp153_AST = null;
					tmp153_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp153_AST);
					match(SEMI);
					{
					_loop38:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop38;
						}
						
					} while (true);
					}
				}
				else {
					break _loop39;
				}
				
			} while (true);
			}
			AST tmp154_AST = null;
			tmp154_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp154_AST);
			match(ENDBLOCK);
			author_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_10);
			} else {
			  throw ex;
			}
		}
		returnAST = author_slot_AST;
	}
	
	public final void specialist_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST specialist_slot_AST = null;
		
		try {      // for error handling
			AST tmp155_AST = null;
			tmp155_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp155_AST);
			match(LITERAL_specialist);
			AST tmp156_AST = null;
			tmp156_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp156_AST);
			match(COLON);
			{
			_loop42:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop42;
				}
				
			} while (true);
			}
			AST tmp157_AST = null;
			tmp157_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp157_AST);
			match(ENDBLOCK);
			specialist_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_11);
			} else {
			  throw ex;
			}
		}
		returnAST = specialist_slot_AST;
	}
	
	public final void date_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST date_slot_AST = null;
		
		try {      // for error handling
			AST tmp158_AST = null;
			tmp158_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp158_AST);
			match(LITERAL_date);
			AST tmp159_AST = null;
			tmp159_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp159_AST);
			match(COLON);
			mlm_date();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp160_AST = null;
			tmp160_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp160_AST);
			match(ENDBLOCK);
			date_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_12);
			} else {
			  throw ex;
			}
		}
		returnAST = date_slot_AST;
	}
	
	public final void validation_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST validation_slot_AST = null;
		
		try {      // for error handling
			AST tmp161_AST = null;
			tmp161_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp161_AST);
			match(LITERAL_validation);
			AST tmp162_AST = null;
			tmp162_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp162_AST);
			match(COLON);
			validation_code();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp163_AST = null;
			tmp163_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp163_AST);
			match(ENDBLOCK);
			validation_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		returnAST = validation_slot_AST;
	}
	
	public final void library_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST library_body_AST = null;
		
		try {      // for error handling
			purpose_slot();
			astFactory.addASTChild(currentAST, returnAST);
			explanation_slot();
			astFactory.addASTChild(currentAST, returnAST);
			keywords_slot();
			astFactory.addASTChild(currentAST, returnAST);
			citations_slot();
			astFactory.addASTChild(currentAST, returnAST);
			links_slot();
			astFactory.addASTChild(currentAST, returnAST);
			library_body_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = library_body_AST;
	}
	
/*****************Library slots*********************************/
	public final void purpose_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST purpose_slot_AST = null;
		
		try {      // for error handling
			AST tmp164_AST = null;
			tmp164_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp164_AST);
			match(LITERAL_purpose);
			AST tmp165_AST = null;
			tmp165_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp165_AST);
			match(COLON);
			{
			_loop68:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop68;
				}
				
			} while (true);
			}
			AST tmp166_AST = null;
			tmp166_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp166_AST);
			match(ENDBLOCK);
			purpose_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
		returnAST = purpose_slot_AST;
	}
	
	public final void explanation_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST explanation_slot_AST = null;
		
		try {      // for error handling
			AST tmp167_AST = null;
			tmp167_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp167_AST);
			match(LITERAL_explanation);
			AST tmp168_AST = null;
			tmp168_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp168_AST);
			match(COLON);
			{
			_loop71:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((LA(1)==INTLIT)) {
					AST tmp169_AST = null;
					tmp169_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp169_AST);
					match(INTLIT);
				}
				else {
					break _loop71;
				}
				
			} while (true);
			}
			AST tmp170_AST = null;
			tmp170_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp170_AST);
			match(ENDBLOCK);
			explanation_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_14);
			} else {
			  throw ex;
			}
		}
		returnAST = explanation_slot_AST;
	}
	
	public final void keywords_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST keywords_slot_AST = null;
		
		try {      // for error handling
			AST tmp171_AST = null;
			tmp171_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp171_AST);
			match(LITERAL_keywords);
			AST tmp172_AST = null;
			tmp172_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp172_AST);
			match(COLON);
			{
			keyword_text();
			astFactory.addASTChild(currentAST, returnAST);
			}
			keywords_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		returnAST = keywords_slot_AST;
	}
	
	public final void citations_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citations_slot_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LITERAL_knowledge||LA(1)==LITERAL_links)) {
				citations_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_knowledge||LA(1)==LITERAL_links)) {
				citations_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_citations)) {
				AST tmp173_AST = null;
				tmp173_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp173_AST);
				match(LITERAL_citations);
				AST tmp174_AST = null;
				tmp174_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp174_AST);
				match(COLON);
				{
				citations_list();
				astFactory.addASTChild(currentAST, returnAST);
				}
				AST tmp175_AST = null;
				tmp175_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp175_AST);
				match(ENDBLOCK);
				citations_slot_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_16);
			} else {
			  throw ex;
			}
		}
		returnAST = citations_slot_AST;
	}
	
	public final void links_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST links_slot_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LITERAL_knowledge)) {
				links_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_knowledge)) {
				links_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_links)) {
				AST tmp176_AST = null;
				tmp176_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp176_AST);
				match(LITERAL_links);
				AST tmp177_AST = null;
				tmp177_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp177_AST);
				match(COLON);
				link_body();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp178_AST = null;
				tmp178_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp178_AST);
				match(ENDBLOCK);
				links_slot_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = links_slot_AST;
	}
	
	public final void knowledge_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST knowledge_body_AST = null;
		
		try {      // for error handling
			type_slot();
			data_slot();
			astFactory.addASTChild(currentAST, returnAST);
			priority_slot();
			evoke_slot();
			logic_slot();
			astFactory.addASTChild(currentAST, returnAST);
			action_slot();
			astFactory.addASTChild(currentAST, returnAST);
			urgency_slot();
			knowledge_body_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		returnAST = knowledge_body_AST;
	}
	
	public final void type_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST type_slot_AST = null;
		
		try {      // for error handling
			AST tmp179_AST = null;
			tmp179_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp179_AST);
			match(LITERAL_type);
			AST tmp180_AST = null;
			tmp180_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp180_AST);
			match(COLON);
			type_code();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp181_AST = null;
			tmp181_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp181_AST);
			match(ENDBLOCK);
			type_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_17);
			} else {
			  throw ex;
			}
		}
		returnAST = type_slot_AST;
	}
	
	public final void data_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_slot_AST = null;
		
		try {      // for error handling
			AST tmp182_AST = null;
			tmp182_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp182_AST);
			match(DATA);
			match(COLON);
			{
			_loop112:
			do {
				if ((_tokenSet_18.member(LA(1)))) {
					data_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMI);
				}
				else {
					break _loop112;
				}
				
			} while (true);
			}
			AST tmp185_AST = null;
			tmp185_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp185_AST);
			match(ENDBLOCK);
			data_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_19);
			} else {
			  throw ex;
			}
		}
		returnAST = data_slot_AST;
	}
	
	public final void priority_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST priority_slot_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LITERAL_evoke)) {
				priority_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_evoke)) {
				priority_slot_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LITERAL_priority)) {
				AST tmp186_AST = null;
				tmp186_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp186_AST);
				match(LITERAL_priority);
				AST tmp187_AST = null;
				tmp187_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp187_AST);
				match(COLON);
				AST tmp188_AST = null;
				tmp188_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp188_AST);
				match(INTLIT);
				AST tmp189_AST = null;
				tmp189_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp189_AST);
				match(ENDBLOCK);
				priority_slot_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_20);
			} else {
			  throw ex;
			}
		}
		returnAST = priority_slot_AST;
	}
	
	public final void evoke_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST evoke_slot_AST = null;
		
		try {      // for error handling
			AST tmp190_AST = null;
			tmp190_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp190_AST);
			match(LITERAL_evoke);
			AST tmp191_AST = null;
			tmp191_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp191_AST);
			match(COLON);
			{
			evoke_statement();
			astFactory.addASTChild(currentAST, returnAST);
			}
			AST tmp192_AST = null;
			tmp192_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp192_AST);
			match(ENDBLOCK);
			evoke_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
		returnAST = evoke_slot_AST;
	}
	
	public final void logic_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_slot_AST = null;
		
		try {      // for error handling
			AST tmp193_AST = null;
			tmp193_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp193_AST);
			match(LOGIC);
			match(COLON);
			{
			_loop214:
			do {
				if ((_tokenSet_22.member(LA(1)))) {
					logic_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMI);
				}
				else {
					break _loop214;
				}
				
			} while (true);
			}
			AST tmp196_AST = null;
			tmp196_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp196_AST);
			match(ENDBLOCK);
			logic_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_23);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_slot_AST;
	}
	
	public final void action_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST action_slot_AST = null;
		
		try {      // for error handling
			AST tmp197_AST = null;
			tmp197_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp197_AST);
			match(ACTION);
			match(COLON);
			{
			_loop249:
			do {
				if ((LA(1)==WRITE)) {
					action_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMI);
				}
				else {
					break _loop249;
				}
				
			} while (true);
			}
			AST tmp200_AST = null;
			tmp200_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp200_AST);
			match(ENDBLOCK);
			action_slot_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		returnAST = action_slot_AST;
	}
	
	public final void urgency_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST urgency_slot_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_end:
			{
				urgency_slot_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_urgency:
			{
				AST tmp201_AST = null;
				tmp201_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp201_AST);
				match(LITERAL_urgency);
				AST tmp202_AST = null;
				tmp202_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp202_AST);
				match(COLON);
				urgency_val();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp203_AST = null;
				tmp203_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp203_AST);
				match(ENDBLOCK);
				urgency_slot_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		returnAST = urgency_slot_AST;
	}
	
	public final void text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST text_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				AST tmp204_AST = null;
				tmp204_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp204_AST);
				match(ID);
				text_AST = (AST)currentAST.root;
				break;
			}
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case COUNT:
			case IN:
			case LESS:
			case THE:
			case THAN:
			case FROM:
			case BEFORE:
			case AFTER:
			case AGO:
			case WRITE:
			case AT:
			case LET:
			case BE:
			case YEAR:
			case YEARS:
			case IF:
			case IT:
			case THEY:
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
			case WHERE:
			case EXIST:
			case EXISTS:
			case PAST:
			case MONTH:
			case MONTHS:
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
			case DATA:
			case LOGIC:
			case ACTION:
			case OF:
			case TIME:
			case WITHIN:
			case CALL:
			case WITH:
			case TO:
			case ANY:
			case TIMES:
			{
				{
				any_reserved_word();
				astFactory.addASTChild(currentAST, returnAST);
				}
				text_AST = (AST)currentAST.root;
				break;
			}
			case INTLIT:
			{
				AST tmp205_AST = null;
				tmp205_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp205_AST);
				match(INTLIT);
				text_AST = (AST)currentAST.root;
				break;
			}
			case LPAREN:
			{
				{
				AST tmp206_AST = null;
				tmp206_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp206_AST);
				match(LPAREN);
				{
				_loop53:
				do {
					switch ( LA(1)) {
					case ID:
					{
						AST tmp207_AST = null;
						tmp207_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp207_AST);
						match(ID);
						break;
					}
					case INTLIT:
					{
						AST tmp208_AST = null;
						tmp208_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp208_AST);
						match(INTLIT);
						break;
					}
					case AND:
					case IS:
					case ARE:
					case WAS:
					case WERE:
					case COUNT:
					case IN:
					case LESS:
					case THE:
					case THAN:
					case FROM:
					case BEFORE:
					case AFTER:
					case AGO:
					case WRITE:
					case AT:
					case LET:
					case BE:
					case YEAR:
					case YEARS:
					case IF:
					case IT:
					case THEY:
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
					case WHERE:
					case EXIST:
					case EXISTS:
					case PAST:
					case MONTH:
					case MONTHS:
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
					case DATA:
					case LOGIC:
					case ACTION:
					case OF:
					case TIME:
					case WITHIN:
					case CALL:
					case WITH:
					case TO:
					case ANY:
					case TIMES:
					{
						{
						any_reserved_word();
						astFactory.addASTChild(currentAST, returnAST);
						}
						break;
					}
					default:
					{
						break _loop53;
					}
					}
				} while (true);
				}
				AST tmp209_AST = null;
				tmp209_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp209_AST);
				match(RPAREN);
				}
				text_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
		returnAST = text_AST;
	}
	
	public final void mlmname_text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlmname_text_AST = null;
		
		try {      // for error handling
			text();
			astFactory.addASTChild(currentAST, returnAST);
			{
			mlmname_text_rest();
			astFactory.addASTChild(currentAST, returnAST);
			}
			mlmname_text_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_6);
			} else {
			  throw ex;
			}
		}
		returnAST = mlmname_text_AST;
	}
	
	public final void mlmname_text_rest() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlmname_text_rest_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_arden:
			case LITERAL_version:
			{
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			case DOT:
			{
				AST tmp210_AST = null;
				tmp210_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp210_AST);
				match(DOT);
				{
				_loop18:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop18;
					}
					
				} while (true);
				}
				AST tmp211_AST = null;
				tmp211_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp211_AST);
				match(ENDBLOCK);
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			case MINUS:
			{
				AST tmp212_AST = null;
				tmp212_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp212_AST);
				match(MINUS);
				{
				_loop20:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop20;
					}
					
				} while (true);
				}
				AST tmp213_AST = null;
				tmp213_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp213_AST);
				match(ENDBLOCK);
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			case UNDERSCORE:
			{
				AST tmp214_AST = null;
				tmp214_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp214_AST);
				match(UNDERSCORE);
				{
				_loop22:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop22;
					}
					
				} while (true);
				}
				AST tmp215_AST = null;
				tmp215_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp215_AST);
				match(ENDBLOCK);
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			{
				AST tmp216_AST = null;
				tmp216_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp216_AST);
				match(ENDBLOCK);
				mlmname_text_rest_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_6);
			} else {
			  throw ex;
			}
		}
		returnAST = mlmname_text_rest_AST;
	}
	
	public final void version_num() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST version_num_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INTLIT:
			{
				AST tmp217_AST = null;
				tmp217_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp217_AST);
				match(INTLIT);
				version_num_AST = (AST)currentAST.root;
				break;
			}
			case DIGIT:
			{
				AST tmp218_AST = null;
				tmp218_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp218_AST);
				match(DIGIT);
				AST tmp219_AST = null;
				tmp219_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp219_AST);
				match(DOT);
				AST tmp220_AST = null;
				tmp220_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp220_AST);
				match(DIGIT);
				version_num_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = version_num_AST;
	}
	
	public final void mlm_version() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlm_version_AST = null;
		
		try {      // for error handling
			text();
			astFactory.addASTChild(currentAST, returnAST);
			mlm_version_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = mlm_version_AST;
	}
	
	public final void mlm_date() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlm_date_AST = null;
		
		try {      // for error handling
			iso_date_time();
			astFactory.addASTChild(currentAST, returnAST);
			mlm_date_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = mlm_date_AST;
	}
	
	public final void iso_date_time() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST iso_date_time_AST = null;
		
		try {      // for error handling
			datepart();
			astFactory.addASTChild(currentAST, returnAST);
			iso_date_time_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_27);
			} else {
			  throw ex;
			}
		}
		returnAST = iso_date_time_AST;
	}
	
	public final void validation_code() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST validation_code_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_production:
			{
				AST tmp221_AST = null;
				tmp221_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp221_AST);
				match(LITERAL_production);
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_research:
			{
				AST tmp222_AST = null;
				tmp222_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp222_AST);
				match(LITERAL_research);
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_testing:
			{
				AST tmp223_AST = null;
				tmp223_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp223_AST);
				match(LITERAL_testing);
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_expired:
			{
				AST tmp224_AST = null;
				tmp224_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp224_AST);
				match(LITERAL_expired);
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			{
				validation_code_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = validation_code_AST;
	}
	
	public final void any_reserved_word() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST any_reserved_word_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case AND:
			{
				AST tmp225_AST = null;
				tmp225_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp225_AST);
				match(AND);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IS:
			{
				AST tmp226_AST = null;
				tmp226_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp226_AST);
				match(IS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ARE:
			{
				AST tmp227_AST = null;
				tmp227_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp227_AST);
				match(ARE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WAS:
			{
				AST tmp228_AST = null;
				tmp228_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp228_AST);
				match(WAS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WERE:
			{
				AST tmp229_AST = null;
				tmp229_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp229_AST);
				match(WERE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			{
				AST tmp230_AST = null;
				tmp230_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp230_AST);
				match(COUNT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IN:
			{
				AST tmp231_AST = null;
				tmp231_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp231_AST);
				match(IN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LESS:
			{
				AST tmp232_AST = null;
				tmp232_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp232_AST);
				match(LESS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THE:
			{
				AST tmp233_AST = null;
				tmp233_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp233_AST);
				match(THE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THAN:
			{
				AST tmp234_AST = null;
				tmp234_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp234_AST);
				match(THAN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FROM:
			{
				AST tmp235_AST = null;
				tmp235_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp235_AST);
				match(FROM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case BEFORE:
			{
				AST tmp236_AST = null;
				tmp236_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp236_AST);
				match(BEFORE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AFTER:
			{
				AST tmp237_AST = null;
				tmp237_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp237_AST);
				match(AFTER);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AGO:
			{
				AST tmp238_AST = null;
				tmp238_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp238_AST);
				match(AGO);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AT:
			{
				AST tmp239_AST = null;
				tmp239_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp239_AST);
				match(AT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case OF:
			{
				AST tmp240_AST = null;
				tmp240_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp240_AST);
				match(OF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WRITE:
			{
				AST tmp241_AST = null;
				tmp241_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp241_AST);
				match(WRITE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case BE:
			{
				AST tmp242_AST = null;
				tmp242_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp242_AST);
				match(BE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LET:
			{
				AST tmp243_AST = null;
				tmp243_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp243_AST);
				match(LET);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case YEAR:
			{
				AST tmp244_AST = null;
				tmp244_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp244_AST);
				match(YEAR);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case YEARS:
			{
				AST tmp245_AST = null;
				tmp245_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp245_AST);
				match(YEARS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IF:
			{
				AST tmp246_AST = null;
				tmp246_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp246_AST);
				match(IF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IT:
			{
				AST tmp247_AST = null;
				tmp247_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp247_AST);
				match(IT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THEY:
			{
				AST tmp248_AST = null;
				tmp248_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp248_AST);
				match(THEY);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case NOT:
			{
				AST tmp249_AST = null;
				tmp249_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp249_AST);
				match(NOT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case OR:
			{
				AST tmp250_AST = null;
				tmp250_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp250_AST);
				match(OR);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THEN:
			{
				AST tmp251_AST = null;
				tmp251_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp251_AST);
				match(THEN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MONTH:
			{
				AST tmp252_AST = null;
				tmp252_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp252_AST);
				match(MONTH);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MONTHS:
			{
				AST tmp253_AST = null;
				tmp253_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp253_AST);
				match(MONTHS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TIME:
			{
				AST tmp254_AST = null;
				tmp254_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp254_AST);
				match(TIME);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TIMES:
			{
				AST tmp255_AST = null;
				tmp255_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp255_AST);
				match(TIMES);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WITHIN:
			{
				AST tmp256_AST = null;
				tmp256_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp256_AST);
				match(WITHIN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case READ:
			{
				AST tmp257_AST = null;
				tmp257_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp257_AST);
				match(READ);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MINIMUM:
			{
				AST tmp258_AST = null;
				tmp258_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp258_AST);
				match(MINIMUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MIN:
			{
				AST tmp259_AST = null;
				tmp259_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp259_AST);
				match(MIN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MAXIMUM:
			{
				AST tmp260_AST = null;
				tmp260_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp260_AST);
				match(MAXIMUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MAX:
			{
				AST tmp261_AST = null;
				tmp261_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp261_AST);
				match(MAX);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LAST:
			{
				AST tmp262_AST = null;
				tmp262_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp262_AST);
				match(LAST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FIRST:
			{
				AST tmp263_AST = null;
				tmp263_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp263_AST);
				match(FIRST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EARLIEST:
			{
				AST tmp264_AST = null;
				tmp264_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp264_AST);
				match(EARLIEST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LATEST:
			{
				AST tmp265_AST = null;
				tmp265_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp265_AST);
				match(LATEST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EVENT:
			{
				AST tmp266_AST = null;
				tmp266_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp266_AST);
				match(EVENT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WHERE:
			{
				AST tmp267_AST = null;
				tmp267_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp267_AST);
				match(WHERE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EXIST:
			{
				AST tmp268_AST = null;
				tmp268_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp268_AST);
				match(EXIST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EXISTS:
			{
				AST tmp269_AST = null;
				tmp269_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp269_AST);
				match(EXISTS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case PAST:
			{
				AST tmp270_AST = null;
				tmp270_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp270_AST);
				match(PAST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AVERAGE:
			{
				AST tmp271_AST = null;
				tmp271_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp271_AST);
				match(AVERAGE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AVG:
			{
				AST tmp272_AST = null;
				tmp272_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp272_AST);
				match(AVG);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case SUM:
			{
				AST tmp273_AST = null;
				tmp273_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp273_AST);
				match(SUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MEDIAN:
			{
				AST tmp274_AST = null;
				tmp274_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp274_AST);
				match(MEDIAN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case CONCLUDE:
			{
				AST tmp275_AST = null;
				tmp275_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp275_AST);
				match(CONCLUDE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ELSE:
			{
				AST tmp276_AST = null;
				tmp276_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp276_AST);
				match(ELSE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ELSEIF:
			{
				AST tmp277_AST = null;
				tmp277_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp277_AST);
				match(ELSEIF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ENDIF:
			{
				AST tmp278_AST = null;
				tmp278_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp278_AST);
				match(ENDIF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			{
				AST tmp279_AST = null;
				tmp279_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp279_AST);
				match(TRUE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				AST tmp280_AST = null;
				tmp280_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp280_AST);
				match(FALSE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case DATA:
			{
				AST tmp281_AST = null;
				tmp281_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp281_AST);
				match(DATA);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LOGIC:
			{
				AST tmp282_AST = null;
				tmp282_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp282_AST);
				match(LOGIC);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ACTION:
			{
				AST tmp283_AST = null;
				tmp283_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp283_AST);
				match(ACTION);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case CALL:
			{
				AST tmp284_AST = null;
				tmp284_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp284_AST);
				match(CALL);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WITH:
			{
				AST tmp285_AST = null;
				tmp285_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp285_AST);
				match(WITH);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TO:
			{
				AST tmp286_AST = null;
				tmp286_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp286_AST);
				match(TO);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ANY:
			{
				AST tmp287_AST = null;
				tmp287_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp287_AST);
				match(ANY);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_28);
			} else {
			  throw ex;
			}
		}
		returnAST = any_reserved_word_AST;
	}
	
	public final void datepart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST datepart_AST = null;
		
		try {      // for error handling
			{
			AST tmp288_AST = null;
			tmp288_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp288_AST);
			match(INTLIT);
			{
			int _cnt58=0;
			_loop58:
			do {
				if ((LA(1)==MINUS)) {
					AST tmp289_AST = null;
					tmp289_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp289_AST);
					match(MINUS);
					AST tmp290_AST = null;
					tmp290_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp290_AST);
					match(INTLIT);
				}
				else {
					if ( _cnt58>=1 ) { break _loop58; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt58++;
			} while (true);
			}
			}
			timepart_opt();
			astFactory.addASTChild(currentAST, returnAST);
			datepart_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_27);
			} else {
			  throw ex;
			}
		}
		returnAST = datepart_AST;
	}
	
	public final void timepart_opt() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST timepart_opt_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case EOF:
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case IN:
			case LESS:
			case GREATER:
			case FROM:
			case BEFORE:
			case AFTER:
			case AGO:
			case AT:
			case YEAR:
			case YEARS:
			case IF:
			case NOT:
			case OR:
			case THEN:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case WHERE:
			case DAYS:
			case DAY:
			case MONTH:
			case MONTHS:
			case WEEK:
			case WEEKS:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case ENDBLOCK:
			case DOT:
			case SEMI:
			case ID:
			case LPAREN:
			case RPAREN:
			case 108:
			case 109:
			case COMMA:
			case ARDEN_CURLY_BRACKETS:
			case LITERAL_hour:
			case LITERAL_hours:
			case LITERAL_minute:
			case LITERAL_minutes:
			case LITERAL_second:
			case LITERAL_seconds:
			case EQUALS:
			case LITERAL_EQ:
			case LT:
			case LITERAL_LT:
			case GT:
			case LITERAL_GT:
			case LTE:
			case LITERAL_LE:
			case GTE:
			case LITERAL_GE:
			case NE:
			case LITERAL_NE:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			case 174:
			case 175:
			{
				timepart_opt_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_T:
			case LITERAL_t:
			{
				{
				timepart();
				astFactory.addASTChild(currentAST, returnAST);
				}
				AST tmp291_AST = null;
				tmp291_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp291_AST);
				match(ENDBLOCK);
				timepart_opt_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_27);
			} else {
			  throw ex;
			}
		}
		returnAST = timepart_opt_AST;
	}
	
	public final void timepart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST timepart_AST = null;
		
		try {      // for error handling
			time();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp292_AST = null;
			tmp292_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp292_AST);
			match(INTLIT);
			AST tmp293_AST = null;
			tmp293_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp293_AST);
			match(104);
			AST tmp294_AST = null;
			tmp294_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp294_AST);
			match(INTLIT);
			AST tmp295_AST = null;
			tmp295_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp295_AST);
			match(104);
			AST tmp296_AST = null;
			tmp296_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp296_AST);
			match(INTLIT);
			fractional_seconds();
			astFactory.addASTChild(currentAST, returnAST);
			time_zone();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp297_AST = null;
			tmp297_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp297_AST);
			match(ENDBLOCK);
			timepart_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = timepart_AST;
	}
	
	public final void time() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST time_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_T:
			{
				AST tmp298_AST = null;
				tmp298_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp298_AST);
				match(LITERAL_T);
				time_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_t:
			{
				AST tmp299_AST = null;
				tmp299_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp299_AST);
				match(LITERAL_t);
				time_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_29);
			} else {
			  throw ex;
			}
		}
		returnAST = time_AST;
	}
	
	public final void fractional_seconds() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST fractional_seconds_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case 107:
			{
				AST tmp300_AST = null;
				tmp300_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp300_AST);
				match(107);
				AST tmp301_AST = null;
				tmp301_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp301_AST);
				match(DIGIT);
				fractional_seconds_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			case 108:
			case 109:
			case LITERAL_Z:
			case LITERAL_z:
			{
				fractional_seconds_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_30);
			} else {
			  throw ex;
			}
		}
		returnAST = fractional_seconds_AST;
	}
	
	public final void time_zone() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST time_zone_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ENDBLOCK:
			{
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_Z:
			case LITERAL_z:
			{
				zulu();
				astFactory.addASTChild(currentAST, returnAST);
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			case 108:
			{
				AST tmp302_AST = null;
				tmp302_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp302_AST);
				match(108);
				AST tmp303_AST = null;
				tmp303_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp303_AST);
				match(DIGIT);
				AST tmp304_AST = null;
				tmp304_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp304_AST);
				match(DIGIT);
				AST tmp305_AST = null;
				tmp305_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp305_AST);
				match(104);
				AST tmp306_AST = null;
				tmp306_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp306_AST);
				match(DIGIT);
				AST tmp307_AST = null;
				tmp307_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp307_AST);
				match(DIGIT);
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			case 109:
			{
				AST tmp308_AST = null;
				tmp308_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp308_AST);
				match(109);
				AST tmp309_AST = null;
				tmp309_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp309_AST);
				match(DIGIT);
				AST tmp310_AST = null;
				tmp310_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp310_AST);
				match(DIGIT);
				AST tmp311_AST = null;
				tmp311_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp311_AST);
				match(104);
				AST tmp312_AST = null;
				tmp312_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp312_AST);
				match(DIGIT);
				AST tmp313_AST = null;
				tmp313_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp313_AST);
				match(DIGIT);
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = time_zone_AST;
	}
	
	public final void zulu() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST zulu_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_Z:
			{
				AST tmp314_AST = null;
				tmp314_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp314_AST);
				match(LITERAL_Z);
				zulu_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_z:
			{
				AST tmp315_AST = null;
				tmp315_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp315_AST);
				match(LITERAL_z);
				zulu_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = zulu_AST;
	}
	
	public final void keyword_text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST keyword_text_AST = null;
		
		try {      // for error handling
			{
			_loop76:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop76;
				}
				
			} while (true);
			}
			{
			_loop80:
			do {
				if ((LA(1)==SEMI)) {
					AST tmp316_AST = null;
					tmp316_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp316_AST);
					match(SEMI);
					{
					_loop79:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop79;
						}
						
					} while (true);
					}
				}
				else {
					break _loop80;
				}
				
			} while (true);
			}
			AST tmp317_AST = null;
			tmp317_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp317_AST);
			match(ENDBLOCK);
			keyword_text_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		returnAST = keyword_text_AST;
	}
	
	public final void citations_list() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citations_list_AST = null;
		
		try {      // for error handling
			if ((LA(1)==ENDBLOCK)) {
				citations_list_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==ENDBLOCK)) {
				citations_list_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_31.member(LA(1)))) {
				single_citation();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop85:
				do {
					if ((LA(1)==SEMI)) {
						AST tmp318_AST = null;
						tmp318_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp318_AST);
						match(SEMI);
						single_citation();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop85;
					}
					
				} while (true);
				}
				citations_list_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = citations_list_AST;
	}
	
	public final void single_citation() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST single_citation_AST = null;
		
		try {      // for error handling
			{
			_loop88:
			do {
				if ((LA(1)==INTLIT)) {
					AST tmp319_AST = null;
					tmp319_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp319_AST);
					match(INTLIT);
					AST tmp320_AST = null;
					tmp320_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp320_AST);
					match(DOT);
					citation_type();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop88;
				}
				
			} while (true);
			}
			citation_text();
			astFactory.addASTChild(currentAST, returnAST);
			single_citation_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = single_citation_AST;
	}
	
	public final void citation_type() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citation_type_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_SUPPORT:
			{
				AST tmp321_AST = null;
				tmp321_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp321_AST);
				match(LITERAL_SUPPORT);
				citation_type_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_REFUTE:
			{
				AST tmp322_AST = null;
				tmp322_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp322_AST);
				match(LITERAL_REFUTE);
				citation_type_AST = (AST)currentAST.root;
				break;
			}
			default:
				if ((_tokenSet_31.member(LA(1)))) {
					citation_type_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_31.member(LA(1)))) {
					citation_type_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		returnAST = citation_type_AST;
	}
	
	public final void citation_text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citation_text_AST = null;
		
		try {      // for error handling
			{
			_loop91:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					text();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((LA(1)==INTLIT)) {
					AST tmp323_AST = null;
					tmp323_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp323_AST);
					match(INTLIT);
				}
				else {
					break _loop91;
				}
				
			} while (true);
			}
			{
			_loop99:
			do {
				if ((LA(1)==COLON)) {
					AST tmp324_AST = null;
					tmp324_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp324_AST);
					match(COLON);
					{
					_loop94:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop94;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case MINUS:
					{
						AST tmp325_AST = null;
						tmp325_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp325_AST);
						match(MINUS);
						AST tmp326_AST = null;
						tmp326_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp326_AST);
						match(INTLIT);
						break;
					}
					case AND:
					case IS:
					case ARE:
					case WAS:
					case WERE:
					case COUNT:
					case IN:
					case LESS:
					case THE:
					case THAN:
					case FROM:
					case BEFORE:
					case AFTER:
					case AGO:
					case WRITE:
					case AT:
					case LET:
					case BE:
					case YEAR:
					case YEARS:
					case IF:
					case IT:
					case THEY:
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
					case WHERE:
					case EXIST:
					case EXISTS:
					case PAST:
					case MONTH:
					case MONTHS:
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
					case DATA:
					case LOGIC:
					case ACTION:
					case OF:
					case TIME:
					case WITHIN:
					case CALL:
					case WITH:
					case TO:
					case ANY:
					case COLON:
					case ENDBLOCK:
					case DOT:
					case INTLIT:
					case SEMI:
					case TIMES:
					case ID:
					case LPAREN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					_loop97:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop97;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case DOT:
					{
						AST tmp327_AST = null;
						tmp327_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp327_AST);
						match(DOT);
						break;
					}
					case COLON:
					case ENDBLOCK:
					case SEMI:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop99;
				}
				
			} while (true);
			}
			citation_text_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		returnAST = citation_text_AST;
	}
	
	public final void link_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST link_body_AST = null;
		
		try {      // for error handling
			{
			_loop107:
			do {
				if ((LA(1)==SINGLE_QUOTE)) {
					AST tmp328_AST = null;
					tmp328_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp328_AST);
					match(SINGLE_QUOTE);
					{
					_loop105:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((LA(1)==DOT)) {
							AST tmp329_AST = null;
							tmp329_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp329_AST);
							match(DOT);
						}
						else if ((LA(1)==INTLIT)) {
							AST tmp330_AST = null;
							tmp330_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp330_AST);
							match(INTLIT);
						}
						else {
							break _loop105;
						}
						
					} while (true);
					}
					AST tmp331_AST = null;
					tmp331_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp331_AST);
					match(SINGLE_QUOTE);
					{
					switch ( LA(1)) {
					case SEMI:
					{
						AST tmp332_AST = null;
						tmp332_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp332_AST);
						match(SEMI);
						break;
					}
					case ENDBLOCK:
					case SINGLE_QUOTE:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop107;
				}
				
			} while (true);
			}
			link_body_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = link_body_AST;
	}
	
	public final void type_code() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST type_code_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case 121:
			{
				AST tmp333_AST = null;
				tmp333_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp333_AST);
				match(121);
				type_code_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_data_driven:
			{
				AST tmp334_AST = null;
				tmp334_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp334_AST);
				match(LITERAL_data_driven);
				type_code_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = type_code_AST;
	}
	
	public final void data_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_statement_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case IF:
			{
				{
				data_if_statement();
				astFactory.addASTChild(currentAST, returnAST);
				}
				break;
			}
			case LET:
			case NOW:
			case SEMI:
			case ID:
			{
				data_assignment();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case ELSE:
			case ELSEIF:
			case ENDIF:
			{
				data_elseif();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			data_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		returnAST = data_statement_AST;
	}
	
	public final void data_if_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_if_statement_AST = null;
		
		try {      // for error handling
			AST tmp335_AST = null;
			tmp335_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp335_AST);
			match(IF);
			data_if_then_else2();
			astFactory.addASTChild(currentAST, returnAST);
			data_if_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		returnAST = data_if_statement_AST;
	}
	
	public final void data_assignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_assignment_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LET:
			case NOW:
			case ID:
			{
				identifier_becomes();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case READ:
				{
					{
					AST tmp336_AST = null;
					tmp336_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp336_AST);
					match(READ);
					}
					{
					{
					if ((_tokenSet_34.member(LA(1)))) {
						{
						of_read_func_op();
						astFactory.addASTChild(currentAST, returnAST);
						}
					}
					else if ((_tokenSet_35.member(LA(1)))) {
						{
						from_of_func_op();
						astFactory.addASTChild(currentAST, returnAST);
						{
						switch ( LA(1)) {
						case INTLIT:
						{
							AST tmp337_AST = null;
							tmp337_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp337_AST);
							match(INTLIT);
							break;
						}
						case LPAREN:
						case ARDEN_CURLY_BRACKETS:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						}
					}
					else if ((LA(1)==LPAREN||LA(1)==ARDEN_CURLY_BRACKETS)) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					{
					switch ( LA(1)) {
					case ARDEN_CURLY_BRACKETS:
					{
						mapping_factor();
						astFactory.addASTChild(currentAST, returnAST);
						{
						switch ( LA(1)) {
						case AFTER:
						case NOT:
						case WHERE:
						case WITHIN:
						{
							{
							switch ( LA(1)) {
							case WHERE:
							{
								where();
								astFactory.addASTChild(currentAST, returnAST);
								it();
								occur();
								break;
							}
							case AFTER:
							case NOT:
							case WITHIN:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							{
							switch ( LA(1)) {
							case AFTER:
							case WITHIN:
							{
								temporal_comp_op();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							case NOT:
							{
								AST tmp338_AST = null;
								tmp338_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp338_AST);
								match(NOT);
								temporal_comp_op();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							break;
						}
						case SEMI:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						break;
					}
					case LPAREN:
					{
						match(LPAREN);
						{
						mapping_factor();
						astFactory.addASTChild(currentAST, returnAST);
						{
						switch ( LA(1)) {
						case WHERE:
						{
							where();
							astFactory.addASTChild(currentAST, returnAST);
							it();
							astFactory.addASTChild(currentAST, returnAST);
							occur();
							astFactory.addASTChild(currentAST, returnAST);
							{
							switch ( LA(1)) {
							case AFTER:
							case WITHIN:
							{
								temporal_comp_op();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							case NOT:
							{
								AST tmp340_AST = null;
								tmp340_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp340_AST);
								match(NOT);
								temporal_comp_op();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							break;
						}
						case RPAREN:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						}
						match(RPAREN);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					}
					break;
				}
				case EVENT:
				{
					{
					AST tmp342_AST = null;
					tmp342_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp342_AST);
					match(EVENT);
					}
					mapping_factor();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case COUNT:
				case THE:
				case NOW:
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
				case TIME:
				case ANY:
				case INTLIT:
				case SEMI:
				case ID:
				case LPAREN:
				case COMMA:
				case LITERAL_MERGE:
				case LITERAL_SORT:
				case STRING_LITERAL:
				case TERM_LITERAL:
				{
					expr();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				data_assignment_AST = (AST)currentAST.root;
				break;
			}
			case SEMI:
			{
				data_assignment_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		returnAST = data_assignment_AST;
	}
	
	public final void data_elseif() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_elseif_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ELSE:
			{
				AST tmp343_AST = null;
				tmp343_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp343_AST);
				match(ELSE);
				data_elseif_AST = (AST)currentAST.root;
				break;
			}
			case ELSEIF:
			{
				AST tmp344_AST = null;
				tmp344_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp344_AST);
				match(ELSEIF);
				data_if_then_else2();
				astFactory.addASTChild(currentAST, returnAST);
				data_elseif_AST = (AST)currentAST.root;
				break;
			}
			case ENDIF:
			{
				{
				AST tmp345_AST = null;
				tmp345_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp345_AST);
				match(ENDIF);
				}
				data_elseif_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		returnAST = data_elseif_AST;
	}
	
	public final void data_if_then_else2() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_if_then_else2_AST = null;
		
		try {      // for error handling
			{
			if ((_tokenSet_36.member(LA(1)))) {
				expr();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((LA(1)==LPAREN)) {
				{
				match(LPAREN);
				}
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				{
				match(RPAREN);
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			AST tmp348_AST = null;
			tmp348_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp348_AST);
			match(THEN);
			data_statement();
			astFactory.addASTChild(currentAST, returnAST);
			data_if_then_else2_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		returnAST = data_if_then_else2_AST;
	}
	
/****** expressions ******/
	public final void expr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_AST = null;
		
		try {      // for error handling
			expr_sort();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop264:
			do {
				if ((LA(1)==COMMA)) {
					AST tmp349_AST = null;
					tmp349_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp349_AST);
					match(COMMA);
					expr_sort();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop264;
				}
				
			} while (true);
			}
			expr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_37);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_AST;
	}
	
	public final void data_comment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_comment_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case COMMENT:
			{
				{
				AST tmp350_AST = null;
				tmp350_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp350_AST);
				match(COMMENT);
				}
				data_comment_AST = (AST)currentAST.root;
				break;
			}
			case ML_COMMENT:
			{
				{
				AST tmp351_AST = null;
				tmp351_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp351_AST);
				match(ML_COMMENT);
				}
				data_comment_AST = (AST)currentAST.root;
				break;
			}
			case EOF:
			{
				data_comment_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = data_comment_AST;
	}
	
	public final void identifier_becomes() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifier_becomes_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				identifier_or_object_ref();
				astFactory.addASTChild(currentAST, returnAST);
				match(BECOMES);
				identifier_becomes_AST = (AST)currentAST.root;
				break;
			}
			case LET:
			{
				match(LET);
				{
				switch ( LA(1)) {
				case LPAREN:
				{
					match(LPAREN);
					break;
				}
				case ID:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				AST tmp355_AST = null;
				tmp355_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp355_AST);
				match(ID);
				{
				_loop153:
				do {
					if ((LA(1)==COMMA)) {
						AST tmp356_AST = null;
						tmp356_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp356_AST);
						match(COMMA);
						AST tmp357_AST = null;
						tmp357_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp357_AST);
						match(ID);
					}
					else {
						break _loop153;
					}
					
				} while (true);
				}
				{
				switch ( LA(1)) {
				case RPAREN:
				{
					match(RPAREN);
					break;
				}
				case BE:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(BE);
				identifier_becomes_AST = (AST)currentAST.root;
				break;
			}
			case NOW:
			{
				AST tmp360_AST = null;
				tmp360_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp360_AST);
				match(NOW);
				AST tmp361_AST = null;
				tmp361_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp361_AST);
				match(BECOMES);
				identifier_becomes_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_38);
			} else {
			  throw ex;
			}
		}
		returnAST = identifier_becomes_AST;
	}
	
	public final void of_read_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST of_read_func_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case AVERAGE:
			{
				AST tmp362_AST = null;
				tmp362_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp362_AST);
				match(AVERAGE);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case AVG:
			{
				AST tmp363_AST = null;
				tmp363_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp363_AST);
				match(AVG);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			{
				AST tmp364_AST = null;
				tmp364_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp364_AST);
				match(COUNT);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case EXIST:
			{
				{
				AST tmp365_AST = null;
				tmp365_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp365_AST);
				match(EXIST);
				}
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case EXISTS:
			{
				AST tmp366_AST = null;
				tmp366_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp366_AST);
				match(EXISTS);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case SUM:
			{
				AST tmp367_AST = null;
				tmp367_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp367_AST);
				match(SUM);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case MEDIAN:
			{
				AST tmp368_AST = null;
				tmp368_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp368_AST);
				match(MEDIAN);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		returnAST = of_read_func_op_AST;
	}
	
	public final void from_of_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST from_of_func_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case MINIMUM:
			case MIN:
			{
				{
				switch ( LA(1)) {
				case MINIMUM:
				{
					AST tmp369_AST = null;
					tmp369_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp369_AST);
					match(MINIMUM);
					break;
				}
				case MIN:
				{
					AST tmp370_AST = null;
					tmp370_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp370_AST);
					match(MIN);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case MAXIMUM:
			case MAX:
			{
				{
				switch ( LA(1)) {
				case MAXIMUM:
				{
					AST tmp371_AST = null;
					tmp371_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp371_AST);
					match(MAXIMUM);
					break;
				}
				case MAX:
				{
					AST tmp372_AST = null;
					tmp372_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp372_AST);
					match(MAX);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case LAST:
			{
				{
				AST tmp373_AST = null;
				tmp373_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp373_AST);
				match(LAST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case FIRST:
			{
				{
				AST tmp374_AST = null;
				tmp374_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp374_AST);
				match(FIRST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case EARLIEST:
			{
				{
				AST tmp375_AST = null;
				tmp375_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp375_AST);
				match(EARLIEST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case LATEST:
			{
				{
				AST tmp376_AST = null;
				tmp376_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp376_AST);
				match(LATEST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case NOW:
			case TRUE:
			case FALSE:
			case OF:
			case INTLIT:
			case ID:
			case LPAREN:
			case ARDEN_CURLY_BRACKETS:
			case STRING_LITERAL:
			case TERM_LITERAL:
			{
				from_of_func_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		returnAST = from_of_func_op_AST;
	}
	
	public final void mapping_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mapping_factor_AST = null;
		
		try {      // for error handling
			AST tmp377_AST = null;
			tmp377_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp377_AST);
			match(ARDEN_CURLY_BRACKETS);
			mapping_factor_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_40);
			} else {
			  throw ex;
			}
		}
		returnAST = mapping_factor_AST;
	}
	
/************************************************************************************************/
	public final void where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST where_AST = null;
		
		try {      // for error handling
			{
			AST tmp378_AST = null;
			tmp378_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp378_AST);
			match(WHERE);
			}
			where_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_41);
			} else {
			  throw ex;
			}
		}
		returnAST = where_AST;
	}
	
	public final void it() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST it_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case IT:
			{
				match(IT);
				break;
			}
			case THEY:
			{
				match(THEY);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			it_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_42);
			} else {
			  throw ex;
			}
		}
		returnAST = it_AST;
	}
	
	public final void occur() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST occur_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_OCCUR:
			case LITERAL_Occur:
			case LITERAL_occur:
			{
				{
				switch ( LA(1)) {
				case LITERAL_OCCUR:
				{
					match(LITERAL_OCCUR);
					break;
				}
				case LITERAL_Occur:
				{
					match(LITERAL_Occur);
					break;
				}
				case LITERAL_occur:
				{
					match(LITERAL_occur);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				occur_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_OCCURS:
			case LITERAL_Occurs:
			case LITERAL_occurs:
			{
				{
				switch ( LA(1)) {
				case LITERAL_OCCURS:
				{
					match(LITERAL_OCCURS);
					break;
				}
				case LITERAL_Occurs:
				{
					match(LITERAL_Occurs);
					break;
				}
				case LITERAL_occurs:
				{
					match(LITERAL_occurs);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				occur_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_OCCURRED:
			case LITERAL_Occurred:
			{
				{
				if ((LA(1)==LITERAL_OCCURRED)) {
					match(LITERAL_OCCURRED);
				}
				else if ((LA(1)==LITERAL_Occurred)) {
					match(LITERAL_Occurred);
				}
				else if ((LA(1)==LITERAL_Occurred)) {
					match(LITERAL_Occurred);
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				occur_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_43);
			} else {
			  throw ex;
			}
		}
		returnAST = occur_AST;
	}
	
	public final void temporal_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST temporal_comp_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case WITHIN:
			{
				AST tmp390_AST = null;
				tmp390_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp390_AST);
				match(WITHIN);
				{
				switch ( LA(1)) {
				case THE:
				{
					the();
					break;
				}
				case PAST:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				AST tmp391_AST = null;
				tmp391_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp391_AST);
				match(PAST);
				expr_string();
				astFactory.addASTChild(currentAST, returnAST);
				temporal_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case AFTER:
			{
				AST tmp392_AST = null;
				tmp392_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp392_AST);
				match(AFTER);
				expr_string();
				astFactory.addASTChild(currentAST, returnAST);
				temporal_comp_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_44);
			} else {
			  throw ex;
			}
		}
		returnAST = temporal_comp_op_AST;
	}
	
	public final void endassignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST endassignment_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case SEMI:
			{
				AST tmp393_AST = null;
				tmp393_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp393_AST);
				match(SEMI);
				endassignment_AST = (AST)currentAST.root;
				break;
			}
			case EOF:
			{
				{
				}
				endassignment_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = endassignment_AST;
	}
	
	public final void endblock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST endblock_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ENDBLOCK:
			{
				AST tmp394_AST = null;
				tmp394_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp394_AST);
				match(ENDBLOCK);
				endblock_AST = (AST)currentAST.root;
				break;
			}
			case EOF:
			{
				{
				}
				endblock_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = endblock_AST;
	}
	
	public final void identifier_or_object_ref() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifier_or_object_ref_AST = null;
		
		try {      // for error handling
			AST tmp395_AST = null;
			tmp395_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp395_AST);
			match(ID);
			{
			_loop157:
			do {
				if ((LA(1)==DOT)) {
					AST tmp396_AST = null;
					tmp396_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp396_AST);
					match(DOT);
					AST tmp397_AST = null;
					tmp397_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp397_AST);
					match(ID);
				}
				else {
					break _loop157;
				}
				
			} while (true);
			}
			identifier_or_object_ref_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_45);
			} else {
			  throw ex;
			}
		}
		returnAST = identifier_or_object_ref_AST;
	}
	
	public final void data_assign_phrase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_assign_phrase_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case READ:
			{
				AST tmp398_AST = null;
				tmp398_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp398_AST);
				match(READ);
				read_phrase();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EVENT:
			case LITERAL_EVENT:
			case LITERAL_Event:
			{
				{
				switch ( LA(1)) {
				case LITERAL_EVENT:
				{
					AST tmp399_AST = null;
					tmp399_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp399_AST);
					match(LITERAL_EVENT);
					break;
				}
				case LITERAL_Event:
				{
					AST tmp400_AST = null;
					tmp400_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp400_AST);
					match(LITERAL_Event);
					break;
				}
				case EVENT:
				{
					AST tmp401_AST = null;
					tmp401_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp401_AST);
					match(EVENT);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				mapping_factor();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			data_assign_phrase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = data_assign_phrase_AST;
	}
	
	public final void read_phrase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST read_phrase_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			case ARDEN_CURLY_BRACKETS:
			{
				read_where();
				astFactory.addASTChild(currentAST, returnAST);
				read_phrase_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			case EXIST:
			case EXISTS:
			case AVG:
			case AVERAGE:
			case SUM:
			case MEDIAN:
			{
				of_read_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				read_where();
				astFactory.addASTChild(currentAST, returnAST);
				from_of_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				read_where();
				astFactory.addASTChild(currentAST, returnAST);
				read_phrase_AST = (AST)currentAST.root;
				break;
			}
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case INTLIT:
			{
				from_of_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp402_AST = null;
				tmp402_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp402_AST);
				match(INTLIT);
				{
				AST tmp403_AST = null;
				tmp403_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp403_AST);
				match(FROM);
				}
				read_where();
				astFactory.addASTChild(currentAST, returnAST);
				read_phrase_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = read_phrase_AST;
	}
	
	public final void data_var_list() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_var_list_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case EOF:
			{
				data_var_list_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			{
				AST tmp404_AST = null;
				tmp404_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp404_AST);
				match(ID);
				{
				_loop163:
				do {
					if ((LA(1)==COMMA)) {
						AST tmp405_AST = null;
						tmp405_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp405_AST);
						match(COMMA);
						AST tmp406_AST = null;
						tmp406_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp406_AST);
						match(ID);
					}
					else {
						break _loop163;
					}
					
				} while (true);
				}
				data_var_list_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = data_var_list_AST;
	}
	
	public final void read_where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST read_where_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case ARDEN_CURLY_BRACKETS:
			{
				mapping_factor();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case AFTER:
				case NOT:
				case WHERE:
				case WITHIN:
				{
					{
					switch ( LA(1)) {
					case WHERE:
					{
						where();
						astFactory.addASTChild(currentAST, returnAST);
						it();
						astFactory.addASTChild(currentAST, returnAST);
						occur();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case AFTER:
					case NOT:
					case WITHIN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					switch ( LA(1)) {
					case AFTER:
					case WITHIN:
					{
						temporal_comp_op();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case NOT:
					{
						AST tmp407_AST = null;
						tmp407_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp407_AST);
						match(NOT);
						temporal_comp_op();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					break;
				}
				case EOF:
				case MINIMUM:
				case MIN:
				case MAXIMUM:
				case MAX:
				case LAST:
				case FIRST:
				case EARLIEST:
				case LATEST:
				case LPAREN:
				case ARDEN_CURLY_BRACKETS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				{
				mapping_factor();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case WHERE:
				{
					where();
					astFactory.addASTChild(currentAST, returnAST);
					it();
					astFactory.addASTChild(currentAST, returnAST);
					occur();
					astFactory.addASTChild(currentAST, returnAST);
					{
					switch ( LA(1)) {
					case AFTER:
					case WITHIN:
					{
						temporal_comp_op();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case NOT:
					{
						AST tmp409_AST = null;
						tmp409_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp409_AST);
						match(NOT);
						temporal_comp_op();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					break;
				}
				case RPAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				match(RPAREN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			read_where_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_46);
			} else {
			  throw ex;
			}
		}
		returnAST = read_where_AST;
	}
	
	public final void time_value() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST time_value_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case NOW:
			{
				AST tmp411_AST = null;
				tmp411_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp411_AST);
				match(NOW);
				time_value_AST = (AST)currentAST.root;
				break;
			}
			case INTLIT:
			{
				iso_date_time();
				astFactory.addASTChild(currentAST, returnAST);
				time_value_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_47);
			} else {
			  throw ex;
			}
		}
		returnAST = time_value_AST;
	}
	
/*********************************OPERATORS***************************************************************/
	public final void in_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST in_comp_op_AST = null;
		
		try {      // for error handling
			AST tmp412_AST = null;
			tmp412_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp412_AST);
			match(IN);
			in_comp_op_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = in_comp_op_AST;
	}
	
	public final void unary_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unary_comp_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_PRESENT:
			{
				AST tmp413_AST = null;
				tmp413_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp413_AST);
				match(LITERAL_PRESENT);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NULL:
			{
				AST tmp414_AST = null;
				tmp414_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp414_AST);
				match(LITERAL_NULL);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_BOOLEAN:
			{
				AST tmp415_AST = null;
				tmp415_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp415_AST);
				match(LITERAL_BOOLEAN);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NUMBER:
			{
				AST tmp416_AST = null;
				tmp416_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp416_AST);
				match(LITERAL_NUMBER);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case TIME:
			{
				AST tmp417_AST = null;
				tmp417_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp417_AST);
				match(TIME);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_DURATION:
			{
				AST tmp418_AST = null;
				tmp418_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp418_AST);
				match(LITERAL_DURATION);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_STRING:
			{
				AST tmp419_AST = null;
				tmp419_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp419_AST);
				match(LITERAL_STRING);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LIST:
			{
				AST tmp420_AST = null;
				tmp420_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp420_AST);
				match(LITERAL_LIST);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_OBJECT:
			{
				AST tmp421_AST = null;
				tmp421_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp421_AST);
				match(LITERAL_OBJECT);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			{
				AST tmp422_AST = null;
				tmp422_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp422_AST);
				match(ID);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = unary_comp_op_AST;
	}
	
	public final void binary_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST binary_comp_op_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LESS)) {
				AST tmp423_AST = null;
				tmp423_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp423_AST);
				match(LESS);
				AST tmp424_AST = null;
				tmp424_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp424_AST);
				match(THAN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==GREATER)) {
				AST tmp425_AST = null;
				tmp425_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp425_AST);
				match(GREATER);
				AST tmp426_AST = null;
				tmp426_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp426_AST);
				match(THAN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==GREATER)) {
				AST tmp427_AST = null;
				tmp427_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp427_AST);
				match(GREATER);
				AST tmp428_AST = null;
				tmp428_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp428_AST);
				match(THAN);
				AST tmp429_AST = null;
				tmp429_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp429_AST);
				match(OR);
				AST tmp430_AST = null;
				tmp430_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp430_AST);
				match(EQUAL);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LESS)) {
				AST tmp431_AST = null;
				tmp431_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp431_AST);
				match(LESS);
				AST tmp432_AST = null;
				tmp432_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp432_AST);
				match(THAN);
				AST tmp433_AST = null;
				tmp433_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp433_AST);
				match(OR);
				AST tmp434_AST = null;
				tmp434_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp434_AST);
				match(EQUAL);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==IN)) {
				AST tmp435_AST = null;
				tmp435_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp435_AST);
				match(IN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_48);
			} else {
			  throw ex;
			}
		}
		returnAST = binary_comp_op_AST;
	}
	
	public final void duration_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST duration_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case YEAR:
			{
				AST tmp436_AST = null;
				tmp436_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp436_AST);
				match(YEAR);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case YEARS:
			{
				AST tmp437_AST = null;
				tmp437_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp437_AST);
				match(YEARS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case MONTH:
			{
				AST tmp438_AST = null;
				tmp438_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp438_AST);
				match(MONTH);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case MONTHS:
			{
				AST tmp439_AST = null;
				tmp439_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp439_AST);
				match(MONTHS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case WEEK:
			{
				AST tmp440_AST = null;
				tmp440_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp440_AST);
				match(WEEK);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case WEEKS:
			{
				AST tmp441_AST = null;
				tmp441_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp441_AST);
				match(WEEKS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case DAY:
			{
				AST tmp442_AST = null;
				tmp442_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp442_AST);
				match(DAY);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case DAYS:
			{
				AST tmp443_AST = null;
				tmp443_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp443_AST);
				match(DAYS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_hour:
			{
				AST tmp444_AST = null;
				tmp444_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp444_AST);
				match(LITERAL_hour);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_hours:
			{
				AST tmp445_AST = null;
				tmp445_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp445_AST);
				match(LITERAL_hours);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_minute:
			{
				AST tmp446_AST = null;
				tmp446_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp446_AST);
				match(LITERAL_minute);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_minutes:
			{
				AST tmp447_AST = null;
				tmp447_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp447_AST);
				match(LITERAL_minutes);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_second:
			{
				AST tmp448_AST = null;
				tmp448_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp448_AST);
				match(LITERAL_second);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_seconds:
			{
				AST tmp449_AST = null;
				tmp449_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp449_AST);
				match(LITERAL_seconds);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_49);
			} else {
			  throw ex;
			}
		}
		returnAST = duration_op_AST;
	}
	
	public final void the() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST the_AST = null;
		
		try {      // for error handling
			AST tmp450_AST = null;
			tmp450_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp450_AST);
			match(THE);
			the_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_50);
			} else {
			  throw ex;
			}
		}
		returnAST = the_AST;
	}
	
/**********************************************************************************/
	public final void expr_string() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_string_AST = null;
		
		try {      // for error handling
			expr_plus();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop293:
			do {
				if ((LA(1)==ACTION_OP)) {
					AST tmp451_AST = null;
					tmp451_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp451_AST);
					match(ACTION_OP);
					expr_plus();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop293;
				}
				
			} while (true);
			}
			expr_string_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_51);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_string_AST;
	}
	
/****** comparison synonyms ******/
	public final void is() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST is_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case IS:
			{
				AST tmp452_AST = null;
				tmp452_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp452_AST);
				match(IS);
				is_AST = (AST)currentAST.root;
				break;
			}
			case ARE:
			{
				AST tmp453_AST = null;
				tmp453_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp453_AST);
				match(ARE);
				is_AST = (AST)currentAST.root;
				break;
			}
			case WERE:
			{
				AST tmp454_AST = null;
				tmp454_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp454_AST);
				match(WERE);
				is_AST = (AST)currentAST.root;
				break;
			}
			case WAS:
			{
				AST tmp455_AST = null;
				tmp455_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp455_AST);
				match(WAS);
				is_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_52);
			} else {
			  throw ex;
			}
		}
		returnAST = is_AST;
	}
	
	public final void evoke_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST evoke_statement_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ANY:
			case ID:
			case LPAREN:
			{
				event_or();
				astFactory.addASTChild(currentAST, returnAST);
				evoke_statement_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_CALL:
			{
				AST tmp456_AST = null;
				tmp456_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp456_AST);
				match(LITERAL_CALL);
				evoke_statement_AST = (AST)currentAST.root;
				break;
			}
			default:
				if ((LA(1)==ENDBLOCK)) {
					evoke_statement_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==ENDBLOCK)) {
					evoke_statement_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = evoke_statement_AST;
	}
	
	public final void event_or() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST event_or_AST = null;
		
		try {      // for error handling
			event_any();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop207:
			do {
				if ((LA(1)==OR)) {
					AST tmp457_AST = null;
					tmp457_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp457_AST);
					match(OR);
					event_any();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop207;
				}
				
			} while (true);
			}
			event_or_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_53);
			} else {
			  throw ex;
			}
		}
		returnAST = event_or_AST;
	}
	
	public final void event_any() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST event_any_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ANY:
			{
				AST tmp458_AST = null;
				tmp458_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp458_AST);
				match(ANY);
				AST tmp459_AST = null;
				tmp459_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp459_AST);
				match(LPAREN);
				event_list();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp460_AST = null;
				tmp460_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp460_AST);
				match(RPAREN);
				event_any_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			case LPAREN:
			{
				event_factor();
				astFactory.addASTChild(currentAST, returnAST);
				event_any_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		returnAST = event_any_AST;
	}
	
	public final void event_list() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST event_list_AST = null;
		
		try {      // for error handling
			event_or();
			astFactory.addASTChild(currentAST, returnAST);
			{
			AST tmp461_AST = null;
			tmp461_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp461_AST);
			match(COMMA);
			event_or();
			astFactory.addASTChild(currentAST, returnAST);
			}
			event_list_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_55);
			} else {
			  throw ex;
			}
		}
		returnAST = event_list_AST;
	}
	
	public final void event_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST event_factor_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			{
				AST tmp462_AST = null;
				tmp462_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp462_AST);
				match(LPAREN);
				event_or();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp463_AST = null;
				tmp463_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp463_AST);
				match(RPAREN);
				event_factor_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			{
				AST tmp464_AST = null;
				tmp464_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp464_AST);
				match(ID);
				event_factor_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		returnAST = event_factor_AST;
	}
	
	public final void logic_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_statement_AST = null;
		
		try {      // for error handling
			{
			_loop217:
			do {
				switch ( LA(1)) {
				case IF:
				{
					if_statement();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case CONCLUDE:
				{
					conclude_statement();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case ID:
				case ACTION_OP:
				{
					logic_assignment();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case ELSE:
				case ELSEIF:
				case ENDIF:
				{
					logic_elseif();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					break _loop217;
				}
				}
			} while (true);
			}
			logic_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_statement_AST;
	}
	
	public final void if_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST if_statement_AST = null;
		
		try {      // for error handling
			AST tmp465_AST = null;
			tmp465_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp465_AST);
			match(IF);
			logic_if_then_else2();
			astFactory.addASTChild(currentAST, returnAST);
			if_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = if_statement_AST;
	}
	
	public final void conclude_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST conclude_statement_AST = null;
		
		try {      // for error handling
			{
			AST tmp466_AST = null;
			tmp466_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp466_AST);
			match(CONCLUDE);
			}
			boolean_value();
			astFactory.addASTChild(currentAST, returnAST);
			conclude_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = conclude_statement_AST;
	}
	
	public final void logic_assignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_assignment_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case ACTION_OP:
			{
				AST tmp467_AST = null;
				tmp467_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp467_AST);
				match(ACTION_OP);
				break;
			}
			case ID:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			identifier_or_object_ref();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case ACTION_OP:
			{
				AST tmp468_AST = null;
				tmp468_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp468_AST);
				match(ACTION_OP);
				break;
			}
			case BECOMES:
			case EQUALS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case BECOMES:
			{
				match(BECOMES);
				break;
			}
			case EQUALS:
			{
				match(EQUALS);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case COUNT:
			case THE:
			case NOW:
			case IF:
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
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case TRUE:
			case FALSE:
			case OF:
			case TIME:
			case ANY:
			case INTLIT:
			case SEMI:
			case ID:
			case LPAREN:
			case COMMA:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case STRING_LITERAL:
			case TERM_LITERAL:
			{
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case CALL:
			{
				{
				AST tmp471_AST = null;
				tmp471_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp471_AST);
				match(CALL);
				}
				call_phrase();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			logic_assignment_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_assignment_AST;
	}
	
	public final void logic_elseif() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_elseif_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ELSE:
			{
				AST tmp472_AST = null;
				tmp472_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp472_AST);
				match(ELSE);
				logic_elseif_AST = (AST)currentAST.root;
				break;
			}
			case ELSEIF:
			{
				AST tmp473_AST = null;
				tmp473_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp473_AST);
				match(ELSEIF);
				logic_if_then_else2();
				astFactory.addASTChild(currentAST, returnAST);
				logic_elseif_AST = (AST)currentAST.root;
				break;
			}
			case ENDIF:
			{
				{
				AST tmp474_AST = null;
				tmp474_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp474_AST);
				match(ENDIF);
				}
				logic_elseif_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_elseif_AST;
	}
	
	public final void logic_expr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_expr_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case THE:
			{
				the();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NOW:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case TRUE:
			case FALSE:
			case INTLIT:
			case ID:
			case LPAREN:
			case STRING_LITERAL:
			case TERM_LITERAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			from_of_func_op();
			astFactory.addASTChild(currentAST, returnAST);
			expr_factor();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case IS:
			case ARE:
			case WAS:
			case WERE:
			{
				is();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IN:
			case LESS:
			case GREATER:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			binary_comp_op();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case THE:
			{
				the();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NOW:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case TRUE:
			case FALSE:
			case INTLIT:
			case ID:
			case LPAREN:
			case STRING_LITERAL:
			case TERM_LITERAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			if ((_tokenSet_56.member(LA(1)))) {
				expr_factor();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_57.member(LA(1)))) {
				from_of_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				expr_factor();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			logic_expr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_expr_AST;
	}
	
	public final void expr_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_factor_AST = null;
		
		try {      // for error handling
			expr_factor_atom();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop316:
			do {
				if ((LA(1)==DOT)) {
					AST tmp475_AST = null;
					tmp475_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp475_AST);
					match(DOT);
					expr_factor_atom();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop316;
				}
				
			} while (true);
			}
			expr_factor_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_58);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_factor_AST;
	}
	
	public final void logic_condition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_condition_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case AND:
			{
				{
				AST tmp476_AST = null;
				tmp476_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp476_AST);
				match(AND);
				}
				break;
			}
			case OR:
			{
				{
				AST tmp477_AST = null;
				tmp477_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp477_AST);
				match(OR);
				}
				break;
			}
			case NOT:
			{
				{
				AST tmp478_AST = null;
				tmp478_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp478_AST);
				match(NOT);
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			logic_condition_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_condition_AST;
	}
	
	public final void simple_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST simple_comp_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case EQUALS:
			{
				{
				AST tmp479_AST = null;
				tmp479_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp479_AST);
				match(EQUALS);
				}
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_EQ:
			{
				{
				AST tmp480_AST = null;
				tmp480_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp480_AST);
				match(LITERAL_EQ);
				}
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LT:
			{
				AST tmp481_AST = null;
				tmp481_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp481_AST);
				match(LT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LT:
			{
				AST tmp482_AST = null;
				tmp482_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp482_AST);
				match(LITERAL_LT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case GT:
			{
				AST tmp483_AST = null;
				tmp483_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp483_AST);
				match(GT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_GT:
			{
				AST tmp484_AST = null;
				tmp484_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp484_AST);
				match(LITERAL_GT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LTE:
			{
				AST tmp485_AST = null;
				tmp485_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp485_AST);
				match(LTE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LE:
			{
				AST tmp486_AST = null;
				tmp486_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp486_AST);
				match(LITERAL_LE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case GTE:
			{
				AST tmp487_AST = null;
				tmp487_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp487_AST);
				match(GTE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_GE:
			{
				AST tmp488_AST = null;
				tmp488_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp488_AST);
				match(LITERAL_GE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case NE:
			{
				AST tmp489_AST = null;
				tmp489_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp489_AST);
				match(NE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NE:
			{
				AST tmp490_AST = null;
				tmp490_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp490_AST);
				match(LITERAL_NE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_48);
			} else {
			  throw ex;
			}
		}
		returnAST = simple_comp_op_AST;
	}
	
	public final void main_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST main_comp_op_AST = null;
		
		try {      // for error handling
			binary_comp_op();
			astFactory.addASTChild(currentAST, returnAST);
			expr_string();
			astFactory.addASTChild(currentAST, returnAST);
			main_comp_op_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_59);
			} else {
			  throw ex;
			}
		}
		returnAST = main_comp_op_AST;
	}
	
	public final void logic_if_then_else2() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_if_then_else2_AST = null;
		
		try {      // for error handling
			{
			expr();
			astFactory.addASTChild(currentAST, returnAST);
			}
			AST tmp491_AST = null;
			tmp491_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp491_AST);
			match(THEN);
			logic_if_then_else2_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = logic_if_then_else2_AST;
	}
	
	public final void boolean_value() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolean_value_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case TRUE:
			{
				{
				AST tmp492_AST = null;
				tmp492_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp492_AST);
				match(TRUE);
				}
				boolean_value_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				{
				AST tmp493_AST = null;
				tmp493_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp493_AST);
				match(FALSE);
				}
				boolean_value_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_47);
			} else {
			  throw ex;
			}
		}
		returnAST = boolean_value_AST;
	}
	
	public final void call_phrase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST call_phrase_AST = null;
		
		try {      // for error handling
			AST tmp494_AST = null;
			tmp494_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp494_AST);
			match(ID);
			{
			switch ( LA(1)) {
			case WITH:
			{
				AST tmp495_AST = null;
				tmp495_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp495_AST);
				match(WITH);
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case SEMI:
			case ID:
			case ACTION_OP:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			call_phrase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		returnAST = call_phrase_AST;
	}
	
	public final void action_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST action_statement_AST = null;
		
		try {      // for error handling
			{
			AST tmp496_AST = null;
			tmp496_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp496_AST);
			match(WRITE);
			}
			{
			{
			if ((LA(1)==LPAREN)) {
				match(LPAREN);
			}
			else if ((_tokenSet_60.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((_tokenSet_61.member(LA(1)))) {
				{
				_loop256:
				do {
					if ((LA(1)==ACTION_OP)) {
						AST tmp498_AST = null;
						tmp498_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp498_AST);
						match(ACTION_OP);
						expr_factor();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop256;
					}
					
				} while (true);
				}
			}
			else if ((_tokenSet_62.member(LA(1)))) {
				expr();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case RPAREN:
			{
				match(RPAREN);
				break;
			}
			case AT:
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			}
			{
			switch ( LA(1)) {
			case AT:
			{
				{
				AST tmp500_AST = null;
				tmp500_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp500_AST);
				match(AT);
				}
				AST tmp501_AST = null;
				tmp501_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp501_AST);
				match(ID);
				break;
			}
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			action_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		returnAST = action_statement_AST;
	}
	
	public final void urgency_val() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST urgency_val_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INTLIT:
			{
				AST tmp502_AST = null;
				tmp502_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp502_AST);
				match(INTLIT);
				urgency_val_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			{
				urgency_val_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		returnAST = urgency_val_AST;
	}
	
	public final void expr_sort() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_sort_AST = null;
		
		try {      // for error handling
			expr_where();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop270:
			do {
				if ((LA(1)==LITERAL_MERGE||LA(1)==LITERAL_SORT)) {
					{
					switch ( LA(1)) {
					case LITERAL_MERGE:
					{
						AST tmp503_AST = null;
						tmp503_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp503_AST);
						match(LITERAL_MERGE);
						break;
					}
					case LITERAL_SORT:
					{
						{
						AST tmp504_AST = null;
						tmp504_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp504_AST);
						match(LITERAL_SORT);
						{
						if ((LA(1)==TIME||LA(1)==LITERAL_DATA)) {
							sort_option();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((_tokenSet_63.member(LA(1)))) {
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_where();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop270;
				}
				
			} while (true);
			}
			expr_sort_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_64);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_sort_AST;
	}
	
	public final void expr_where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_where_AST = null;
		
		try {      // for error handling
			if ((_tokenSet_48.member(LA(1)))) {
				expr_range();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop274:
				do {
					if ((LA(1)==WHERE)) {
						AST tmp505_AST = null;
						tmp505_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp505_AST);
						match(WHERE);
						expr_range();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop274;
					}
					
				} while (true);
				}
				expr_where_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_65.member(LA(1)))) {
				expr_where_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_65);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_where_AST;
	}
	
	public final void sort_option() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST sort_option_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case TIME:
			{
				AST tmp506_AST = null;
				tmp506_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp506_AST);
				match(TIME);
				sort_option_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_DATA:
			{
				AST tmp507_AST = null;
				tmp507_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp507_AST);
				match(LITERAL_DATA);
				sort_option_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_63);
			} else {
			  throw ex;
			}
		}
		returnAST = sort_option_AST;
	}
	
	public final void expr_range() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_range_AST = null;
		
		try {      // for error handling
			expr_or();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop277:
			do {
				if ((LA(1)==LITERAL_SEQTO)) {
					AST tmp508_AST = null;
					tmp508_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp508_AST);
					match(LITERAL_SEQTO);
					expr_or();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop277;
				}
				
			} while (true);
			}
			expr_range_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_66);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_range_AST;
	}
	
	public final void expr_or() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_or_AST = null;
		
		try {      // for error handling
			expr_and();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop280:
			do {
				if ((LA(1)==OR)) {
					AST tmp509_AST = null;
					tmp509_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp509_AST);
					match(OR);
					expr_and();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop280;
				}
				
			} while (true);
			}
			expr_or_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_67);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_or_AST;
	}
	
	public final void expr_and() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_and_AST = null;
		
		try {      // for error handling
			expr_not();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop283:
			do {
				if ((LA(1)==AND)) {
					AST tmp510_AST = null;
					tmp510_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp510_AST);
					match(AND);
					expr_not();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop283;
				}
				
			} while (true);
			}
			expr_and_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_68);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_and_AST;
	}
	
	public final void expr_not() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_not_AST = null;
		
		try {      // for error handling
			expr_comparison();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop286:
			do {
				if ((LA(1)==NOT)) {
					AST tmp511_AST = null;
					tmp511_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp511_AST);
					match(NOT);
					expr_comparison();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop286;
				}
				
			} while (true);
			}
			expr_not_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_69);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_not_AST;
	}
	
	public final void expr_comparison() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_comparison_AST = null;
		
		try {      // for error handling
			expr_string();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case EQUALS:
			case LITERAL_EQ:
			case LT:
			case LITERAL_LT:
			case GT:
			case LITERAL_GT:
			case LTE:
			case LITERAL_LE:
			case GTE:
			case LITERAL_GE:
			case NE:
			case LITERAL_NE:
			{
				{
				simple_comp_op();
				astFactory.addASTChild(currentAST, returnAST);
				expr_string();
				astFactory.addASTChild(currentAST, returnAST);
				}
				break;
			}
			case IS:
			case ARE:
			case WAS:
			case WERE:
			{
				{
				is();
				main_comp_op();
				astFactory.addASTChild(currentAST, returnAST);
				}
				break;
			}
			case AND:
			case AT:
			case IF:
			case NOT:
			case OR:
			case THEN:
			case WHERE:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case SEMI:
			case ID:
			case RPAREN:
			case COMMA:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			expr_comparison_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_59);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_comparison_AST;
	}
	
	public final void expr_plus() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_plus_AST = null;
		
		try {      // for error handling
			expr_times();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop297:
			do {
				if ((LA(1)==108||LA(1)==109)) {
					{
					switch ( LA(1)) {
					case 108:
					{
						AST tmp512_AST = null;
						tmp512_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp512_AST);
						match(108);
						break;
					}
					case 109:
					{
						AST tmp513_AST = null;
						tmp513_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp513_AST);
						match(109);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_times();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop297;
				}
				
			} while (true);
			}
			expr_plus_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_51);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_plus_AST;
	}
	
	public final void expr_times() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_times_AST = null;
		
		try {      // for error handling
			expr_power();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop301:
			do {
				if ((LA(1)==174||LA(1)==175)) {
					{
					switch ( LA(1)) {
					case 174:
					{
						AST tmp514_AST = null;
						tmp514_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp514_AST);
						match(174);
						break;
					}
					case 175:
					{
						AST tmp515_AST = null;
						tmp515_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp515_AST);
						match(175);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_times();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop301;
				}
				
			} while (true);
			}
			expr_times_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_times_AST;
	}
	
	public final void expr_power() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_power_AST = null;
		
		try {      // for error handling
			expr_duration();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop305:
			do {
				if (((LA(1) >= FROM && LA(1) <= AFTER))) {
					{
					switch ( LA(1)) {
					case BEFORE:
					{
						AST tmp516_AST = null;
						tmp516_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp516_AST);
						match(BEFORE);
						break;
					}
					case AFTER:
					{
						AST tmp517_AST = null;
						tmp517_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp517_AST);
						match(AFTER);
						break;
					}
					case FROM:
					{
						AST tmp518_AST = null;
						tmp518_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp518_AST);
						match(FROM);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_duration();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop305;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case AGO:
			{
				AST tmp519_AST = null;
				tmp519_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp519_AST);
				match(AGO);
				break;
			}
			case EOF:
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case AT:
			case IF:
			case NOT:
			case OR:
			case THEN:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case WHERE:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case SEMI:
			case ID:
			case LPAREN:
			case RPAREN:
			case 108:
			case 109:
			case COMMA:
			case ARDEN_CURLY_BRACKETS:
			case EQUALS:
			case LITERAL_EQ:
			case LT:
			case LITERAL_LT:
			case GT:
			case LITERAL_GT:
			case LTE:
			case LITERAL_LE:
			case GTE:
			case LITERAL_GE:
			case NE:
			case LITERAL_NE:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			case 174:
			case 175:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			expr_power_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_power_AST;
	}
	
	public final void expr_duration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_duration_AST = null;
		
		try {      // for error handling
			expr_function();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case YEAR:
			case YEARS:
			case DAYS:
			case DAY:
			case MONTH:
			case MONTHS:
			case WEEK:
			case WEEKS:
			case LITERAL_hour:
			case LITERAL_hours:
			case LITERAL_minute:
			case LITERAL_minutes:
			case LITERAL_second:
			case LITERAL_seconds:
			{
				duration_op();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case FROM:
			case BEFORE:
			case AFTER:
			case AGO:
			case AT:
			case IF:
			case NOT:
			case OR:
			case THEN:
			case MINIMUM:
			case MIN:
			case MAXIMUM:
			case MAX:
			case LAST:
			case FIRST:
			case EARLIEST:
			case LATEST:
			case WHERE:
			case CONCLUDE:
			case ELSE:
			case ELSEIF:
			case ENDIF:
			case SEMI:
			case ID:
			case LPAREN:
			case RPAREN:
			case 108:
			case 109:
			case COMMA:
			case ARDEN_CURLY_BRACKETS:
			case EQUALS:
			case LITERAL_EQ:
			case LT:
			case LITERAL_LT:
			case GT:
			case LITERAL_GT:
			case LTE:
			case LITERAL_LE:
			case GTE:
			case LITERAL_GE:
			case NE:
			case LITERAL_NE:
			case ACTION_OP:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			case 174:
			case 175:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			expr_duration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_49);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_duration_AST;
	}
	
	public final void expr_function() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_function_AST = null;
		
		try {      // for error handling
			if ((_tokenSet_56.member(LA(1)))) {
				expr_factor();
				astFactory.addASTChild(currentAST, returnAST);
				expr_function_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_48.member(LA(1)))) {
				{
				switch ( LA(1)) {
				case THE:
				{
					the();
					break;
				}
				case COUNT:
				case NOW:
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
				case TIME:
				case ANY:
				case INTLIT:
				case ID:
				case LPAREN:
				case STRING_LITERAL:
				case TERM_LITERAL:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case NOW:
				case MINIMUM:
				case MIN:
				case MAXIMUM:
				case MAX:
				case LAST:
				case FIRST:
				case EARLIEST:
				case LATEST:
				case TRUE:
				case FALSE:
				case OF:
				case INTLIT:
				case ID:
				case LPAREN:
				case STRING_LITERAL:
				case TERM_LITERAL:
				{
					from_of_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					{
					switch ( LA(1)) {
					case OF:
					{
						match(OF);
						break;
					}
					case NOW:
					case TRUE:
					case FALSE:
					case INTLIT:
					case ID:
					case LPAREN:
					case STRING_LITERAL:
					case TERM_LITERAL:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_factor();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case COUNT:
				case EXIST:
				case EXISTS:
				case AVG:
				case AVERAGE:
				case SUM:
				case MEDIAN:
				case TIME:
				case ANY:
				{
					of_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					{
					switch ( LA(1)) {
					case OF:
					{
						match(OF);
						break;
					}
					case NOW:
					case TRUE:
					case FALSE:
					case INTLIT:
					case ID:
					case LPAREN:
					case STRING_LITERAL:
					case TERM_LITERAL:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr_factor();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				expr_function_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_71);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_function_AST;
	}
	
	public final void of_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST of_func_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case COUNT:
			case EXIST:
			case EXISTS:
			case AVG:
			case AVERAGE:
			case SUM:
			case MEDIAN:
			{
				of_read_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				of_func_op_AST = (AST)currentAST.root;
				break;
			}
			case TIME:
			case ANY:
			{
				of_noread_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				of_func_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_72);
			} else {
			  throw ex;
			}
		}
		returnAST = of_func_op_AST;
	}
	
	public final void expr_factor_atom() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_factor_atom_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				AST tmp522_AST = null;
				tmp522_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp522_AST);
				match(ID);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				expr();
				astFactory.addASTChild(currentAST, returnAST);
				match(RPAREN);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			case FALSE:
			{
				boolean_value();
				astFactory.addASTChild(currentAST, returnAST);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case STRING_LITERAL:
			{
				AST tmp525_AST = null;
				tmp525_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp525_AST);
				match(STRING_LITERAL);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case TERM_LITERAL:
			{
				AST tmp526_AST = null;
				tmp526_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp526_AST);
				match(TERM_LITERAL);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			default:
				if ((LA(1)==INTLIT)) {
					AST tmp527_AST = null;
					tmp527_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp527_AST);
					match(INTLIT);
					expr_factor_atom_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==NOW||LA(1)==INTLIT)) {
					time_value();
					astFactory.addASTChild(currentAST, returnAST);
					expr_factor_atom_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_47);
			} else {
			  throw ex;
			}
		}
		returnAST = expr_factor_atom_AST;
	}
	
	public final void as_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST as_func_op_AST = null;
		
		try {      // for error handling
			AST tmp528_AST = null;
			tmp528_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp528_AST);
			match(LITERAL_NUMBER);
			as_func_op_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = as_func_op_AST;
	}
	
	public final void of_noread_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST of_noread_func_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case TIME:
			{
				AST tmp529_AST = null;
				tmp529_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp529_AST);
				match(TIME);
				of_noread_func_op_AST = (AST)currentAST.root;
				break;
			}
			case ANY:
			{
				AST tmp530_AST = null;
				tmp530_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp530_AST);
				match(ANY);
				of_noread_func_op_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_72);
			} else {
			  throw ex;
			}
		}
		returnAST = of_noread_func_op_AST;
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
		"TERM_LITERAL",
		"WS",
		"LBRACKET",
		"RBRACKET",
		"DOTDOT",
		"NOT_EQUALS",
		"PLUS",
		"DIV",
		"LCURLY",
		"RCURLY"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 0L, 8L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 0L, 32768L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 0L, 8192L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { -7177611914518576L, 481044733891L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 0L, 48L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 0L, 18874368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 0L, 16777216L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 0L, 67108864L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 0L, 134217728L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 0L, 536870912L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 0L, 1073741824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 0L, 2147483648L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 0L, 562949953421312L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 0L, 1125899906842624L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 0L, 20266198323200000L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 0L, 18014398509514752L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { -9223372036854775808L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 2017612633208782848L, 137707388928L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 0L, 0L, 100663296L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 0L, 0L, 67108864L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 0L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 2161727821272055808L, 137707388928L, 1099511627776L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 0L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 0L, 8192L, 2199023255552L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { -7177611914518574L, 36029278352990147L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 0L, 131072L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { 2170607467618057170L, 4611739757327024128L, 261683499104258L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { -7177611914518574L, 36029828108804035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { 0L, 8388608L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { 0L, 263882790797312L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { -7177611914518576L, 481313316803L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 0L, 268566528L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { 0L, 268435456L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { 135160765379249152L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { 4380866641920L, 274886295552L, 2L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { 7052694178190345216L, 4611686430752641216L, 857619069665280L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { 2161727825569120256L, 687463202816L, 1099511627776L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { 7052698580531823616L, 4611686431021076672L, 857619069665280L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { 6917529027649470464L, 412325249088L, 844424930131970L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { 13178033668098L, 824902156544L, 2L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { 805306368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { 0L, 0L, 33423360L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { 1074003968L, 256L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { 4380866641922L, 824902156288L, 2L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { 0L, 2305843009213693952L, 1099780063232L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { 4380866641922L, 274877906944L, 2L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { 2170607467618057170L, 4611739757326893056L, 261683499104258L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { 7052694173895377920L, 412325253312L, 844424930131968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = { 2161741005750993874L, 4611739757326630912L, 261683498975234L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 7052764542639539200L, 412325253312L, 844424930131968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { 2161741005750010834L, 4611686980768497664L, 50577266442242L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { 14336L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = { 0L, 4611686568183332864L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = { 2147483648L, 4611686568183332864L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { 0L, 549755813888L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = { 6917529027649470464L, 412325249024L, 844424930131968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = { 6917533408516112384L, 412325249024L, 844424930131968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = { 2170607467618057170L, 4611739757326630912L, 261683499104258L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { 2161736624883367952L, 4611686705890590720L, 49478023249920L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = { 7052694173897475072L, 4611686980776890560L, 858718581293056L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = { 2097152L, 550024249344L, 1099511627776L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { 7052694173897475072L, 4611686980776890560L, 857619069665280L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = { 9214421999464498176L, 4611686980776890560L, 858718581293056L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { 2161727825569120256L, 4611686705890590720L, 1099511627776L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = { 2161727825569120256L, 4611686705890590720L, 14293651161088L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = { 2161736621662142464L, 4611686705890590720L, 14293651161088L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = { 2161736621662142464L, 4611686705890590720L, 49478023249920L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = { 2161736623809626112L, 4611686705890590720L, 49478023249920L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = { 2161736623809626128L, 4611686705890590720L, 49478023249920L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = { 2161741005750010834L, 4611739757326630912L, 261683498975234L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = { 2170607467618042834L, 4611739757326630912L, 261683499104258L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	private static final long[] mk_tokenSet_72() {
		long[] data = { 6917529027649470464L, 412325249088L, 844424930131968L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	
	}
