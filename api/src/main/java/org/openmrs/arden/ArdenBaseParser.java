/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import org.openmrs.logic.*;
import org.openmrs.logic.op.*;
import java.util.GregorianCalendar;

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

public class ArdenBaseParser extends antlr.LLkParser implements ArdenBaseParserTokenTypes {
	
	protected ArdenBaseParser(TokenBuffer tokenBuf, int k) {
		super(tokenBuf, k);
		tokenNames = _tokenNames;
		buildTokenTypeASTClassMap();
		astFactory = new ASTFactory(getTokenTypeToASTClassMap());
	}
	
	public ArdenBaseParser(TokenBuffer tokenBuf) {
		this(tokenBuf, 1);
	}
	
	protected ArdenBaseParser(TokenStream lexer, int k) {
		super(lexer, k);
		tokenNames = _tokenNames;
		buildTokenTypeASTClassMap();
		astFactory = new ASTFactory(getTokenTypeToASTClassMap());
	}
	
	public ArdenBaseParser(TokenStream lexer) {
		this(lexer, 1);
	}
	
	public ArdenBaseParser(ParserSharedInputState state) {
		super(state, 1);
		tokenNames = _tokenNames;
		buildTokenTypeASTClassMap();
		astFactory = new ASTFactory(getTokenTypeToASTClassMap());
	}
	
	public final void startRule() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST startRule_AST = null;
		
		try { // for error handling
			maintenance_category();
			astFactory.addASTChild(currentAST, returnAST);
			library_category();
			astFactory.addASTChild(currentAST, returnAST);
			knowledge_category();
			astFactory.addASTChild(currentAST, returnAST);
			match(LITERAL_end);
			match(COLON);
			startRule_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			{
				AST tmp180_AST = null;
				tmp180_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp180_AST);
				match(MAINTENANCE);
				match(COLON);
			}
			maintenance_body();
			astFactory.addASTChild(currentAST, returnAST);
			maintenance_category_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_1);
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
		
		try { // for error handling
			AST tmp182_AST = null;
			tmp182_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp182_AST);
			match(LIBRARY);
			match(COLON);
			library_body();
			astFactory.addASTChild(currentAST, returnAST);
			library_category_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_2);
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
		
		try { // for error handling
			{
				AST tmp184_AST = null;
				tmp184_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp184_AST);
				match(KNOWLEDGE);
				match(COLON);
			}
			knowledge_body();
			astFactory.addASTChild(currentAST, returnAST);
			knowledge_category_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_3);
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
		
		try { // for error handling
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
			maintenance_body_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_1);
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
		
		try { // for error handling
			{
				AST tmp186_AST = null;
				tmp186_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp186_AST);
				match(TITLE);
				AST tmp187_AST = null;
				tmp187_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp187_AST);
				match(COLON);
				{
					_loop13: do {
						if ((_tokenSet_4.member(LA(1)))) {
							text();
							astFactory.addASTChild(currentAST, returnAST);
						} else {
							break _loop13;
						}
						
					} while (true);
				}
				AST tmp188_AST = null;
				tmp188_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp188_AST);
				match(ENDBLOCK);
			}
			title_slot_AST = (AST) currentAST.root;
		}
		catch (MismatchedTokenException mv) {
			if (inputState.guessing == 0) {
				
				reportError(mv);
				System.err.println("***Rule Priority NOT SET***");
				consumeUntil(ENDBLOCK); // throw away all until ';;'
				consume();
				
			} else {
				throw mv;
			}
		}
		returnAST = title_slot_AST;
	}
	
	public final void mlmname_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST mlmname_slot_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case MLMNAME: {
					AST tmp189_AST = null;
					tmp189_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp189_AST);
					match(MLMNAME);
					AST tmp190_AST = null;
					tmp190_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp190_AST);
					match(COLON);
					mlmname_text();
					astFactory.addASTChild(currentAST, returnAST);
					mlmname_slot_AST = (AST) currentAST.root;
					break;
				}
				case FILENAME: {
					AST tmp191_AST = null;
					tmp191_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp191_AST);
					match(FILENAME);
					AST tmp192_AST = null;
					tmp192_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp192_AST);
					match(COLON);
					mlmname_text();
					astFactory.addASTChild(currentAST, returnAST);
					mlmname_slot_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_5);
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
		
		try { // for error handling
			switch (LA(1)) {
				case LITERAL_arden: {
					AST tmp193_AST = null;
					tmp193_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp193_AST);
					match(LITERAL_arden);
					AST tmp194_AST = null;
					tmp194_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp194_AST);
					match(COLON);
					{
						switch (LA(1)) {
							case 107: {
								{
									AST tmp195_AST = null;
									tmp195_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp195_AST);
									match(107);
									AST tmp196_AST = null;
									tmp196_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp196_AST);
									match(INTLIT);
									AST tmp197_AST = null;
									tmp197_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp197_AST);
									match(MINUS);
									AST tmp198_AST = null;
									tmp198_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp198_AST);
									match(INTLIT);
								}
								break;
							}
							case VERSION: {
								{
									AST tmp199_AST = null;
									tmp199_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp199_AST);
									match(VERSION);
									version_num();
									astFactory.addASTChild(currentAST, returnAST);
								}
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					AST tmp200_AST = null;
					tmp200_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp200_AST);
					match(ENDBLOCK);
					arden_version_slot_AST = (AST) currentAST.root;
					break;
				}
				case VERSION: {
					arden_version_slot_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_6);
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
		
		try { // for error handling
			AST tmp201_AST = null;
			tmp201_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp201_AST);
			match(VERSION);
			AST tmp202_AST = null;
			tmp202_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp202_AST);
			match(COLON);
			AST tmp203_AST = null;
			tmp203_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp203_AST);
			match(INTLIT);
			AST tmp204_AST = null;
			tmp204_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp204_AST);
			match(DOT);
			AST tmp205_AST = null;
			tmp205_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp205_AST);
			match(INTLIT);
			AST tmp206_AST = null;
			tmp206_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp206_AST);
			match(ENDBLOCK);
			version_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_7);
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
		
		try { // for error handling
			AST tmp207_AST = null;
			tmp207_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp207_AST);
			match(INSTITUTION);
			AST tmp208_AST = null;
			tmp208_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp208_AST);
			match(COLON);
			{
				_loop33: do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop33;
					}
					
				} while (true);
			}
			AST tmp209_AST = null;
			tmp209_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp209_AST);
			match(ENDBLOCK);
			institution_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_8);
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
		
		try { // for error handling
			AST tmp210_AST = null;
			tmp210_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp210_AST);
			match(AUTHOR);
			AST tmp211_AST = null;
			tmp211_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp211_AST);
			match(COLON);
			{
				_loop36: do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop36;
					}
					
				} while (true);
			}
			{
				_loop40: do {
					if ((LA(1) == SEMI)) {
						AST tmp212_AST = null;
						tmp212_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp212_AST);
						match(SEMI);
						{
							_loop39: do {
								if ((_tokenSet_4.member(LA(1)))) {
									text();
									astFactory.addASTChild(currentAST, returnAST);
								} else {
									break _loop39;
								}
								
							} while (true);
						}
					} else {
						break _loop40;
					}
					
				} while (true);
			}
			AST tmp213_AST = null;
			tmp213_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp213_AST);
			match(ENDBLOCK);
			author_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_9);
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
		
		try { // for error handling
			AST tmp214_AST = null;
			tmp214_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp214_AST);
			match(SPECIALIST);
			AST tmp215_AST = null;
			tmp215_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp215_AST);
			match(COLON);
			{
				_loop43: do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop43;
					}
					
				} while (true);
			}
			AST tmp216_AST = null;
			tmp216_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp216_AST);
			match(ENDBLOCK);
			specialist_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_10);
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
		
		try { // for error handling
			AST tmp217_AST = null;
			tmp217_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp217_AST);
			match(DATE);
			AST tmp218_AST = null;
			tmp218_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp218_AST);
			match(COLON);
			mlm_date();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp219_AST = null;
			tmp219_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp219_AST);
			match(ENDBLOCK);
			date_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_11);
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
		
		try { // for error handling
			AST tmp220_AST = null;
			tmp220_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp220_AST);
			match(LITERAL_validation);
			AST tmp221_AST = null;
			tmp221_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp221_AST);
			match(COLON);
			validation_code();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp222_AST = null;
			tmp222_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp222_AST);
			match(ENDBLOCK);
			validation_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_1);
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
		
		try { // for error handling
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
			library_body_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_2);
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
		
		try { // for error handling
			AST tmp223_AST = null;
			tmp223_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp223_AST);
			match(PURPOSE);
			AST tmp224_AST = null;
			tmp224_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp224_AST);
			match(COLON);
			{
				_loop72: do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop72;
					}
					
				} while (true);
			}
			AST tmp225_AST = null;
			tmp225_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp225_AST);
			match(ENDBLOCK);
			purpose_slot_AST = (AST) currentAST.root;
		}
		catch (MismatchedTokenException mv) {
			if (inputState.guessing == 0) {
				
				reportError(mv);
				consumeUntil(ENDBLOCK); // throw away all until ';;'
				consume();
				
			} else {
				throw mv;
			}
		}
		returnAST = purpose_slot_AST;
	}
	
	public final void explanation_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST explanation_slot_AST = null;
		
		try { // for error handling
			AST tmp226_AST = null;
			tmp226_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp226_AST);
			match(EXPLANATION);
			AST tmp227_AST = null;
			tmp227_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp227_AST);
			match(COLON);
			{
				_loop75: do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					} else if ((LA(1) == INTLIT)) {
						AST tmp228_AST = null;
						tmp228_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp228_AST);
						match(INTLIT);
					} else {
						break _loop75;
					}
					
				} while (true);
			}
			AST tmp229_AST = null;
			tmp229_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp229_AST);
			match(ENDBLOCK);
			explanation_slot_AST = (AST) currentAST.root;
		}
		catch (MismatchedTokenException mv) {
			if (inputState.guessing == 0) {
				
				reportError(mv);
				consumeUntil(ENDBLOCK); // throw away all until ';;'
				consume();
				
			} else {
				throw mv;
			}
		}
		returnAST = explanation_slot_AST;
	}
	
	public final void keywords_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST keywords_slot_AST = null;
		
		try { // for error handling
			AST tmp230_AST = null;
			tmp230_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp230_AST);
			match(KEYWORDS);
			AST tmp231_AST = null;
			tmp231_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp231_AST);
			match(COLON);
			{
				keyword_text();
				astFactory.addASTChild(currentAST, returnAST);
			}
			keywords_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_12);
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
		
		try { // for error handling
			if ((LA(1) == KNOWLEDGE || LA(1) == LINKS)) {
				citations_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == KNOWLEDGE || LA(1) == LINKS)) {
				citations_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == CITATIONS)) {
				AST tmp232_AST = null;
				tmp232_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp232_AST);
				match(CITATIONS);
				AST tmp233_AST = null;
				tmp233_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp233_AST);
				match(COLON);
				{
					citations_list();
					astFactory.addASTChild(currentAST, returnAST);
				}
				AST tmp234_AST = null;
				tmp234_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp234_AST);
				match(ENDBLOCK);
				citations_slot_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_13);
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
		
		try { // for error handling
			if ((LA(1) == KNOWLEDGE)) {
				links_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == KNOWLEDGE)) {
				links_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == LINKS)) {
				AST tmp235_AST = null;
				tmp235_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp235_AST);
				match(LINKS);
				AST tmp236_AST = null;
				tmp236_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp236_AST);
				match(COLON);
				{
					if ((LA(1) == ENDBLOCK)) {} else if ((LA(1) == ENDBLOCK)) {} else if ((LA(1) == HTTP)) {
						AST tmp237_AST = null;
						tmp237_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp237_AST);
						match(HTTP);
						AST tmp238_AST = null;
						tmp238_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp238_AST);
						match(NOT_COMMENT);
						{
							_loop108: do {
								if ((_tokenSet_4.member(LA(1)))) {
									text();
									astFactory.addASTChild(currentAST, returnAST);
								} else {
									break _loop108;
								}
								
							} while (true);
						}
					} else if ((LA(1) == ENDBLOCK || LA(1) == SINGLE_QUOTE)) {
						{
							_loop113: do {
								if ((LA(1) == SINGLE_QUOTE)) {
									AST tmp239_AST = null;
									tmp239_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp239_AST);
									match(SINGLE_QUOTE);
									{
										_loop111: do {
											if ((_tokenSet_4.member(LA(1)))) {
												text();
												astFactory.addASTChild(currentAST, returnAST);
											} else if ((LA(1) == DOT)) {
												AST tmp240_AST = null;
												tmp240_AST = astFactory.create(LT(1));
												astFactory.addASTChild(currentAST, tmp240_AST);
												match(DOT);
											} else if ((LA(1) == INTLIT)) {
												AST tmp241_AST = null;
												tmp241_AST = astFactory.create(LT(1));
												astFactory.addASTChild(currentAST, tmp241_AST);
												match(INTLIT);
											} else {
												break _loop111;
											}
											
										} while (true);
									}
									AST tmp242_AST = null;
									tmp242_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp242_AST);
									match(SINGLE_QUOTE);
									{
										switch (LA(1)) {
											case SEMI: {
												AST tmp243_AST = null;
												tmp243_AST = astFactory.create(LT(1));
												astFactory.addASTChild(currentAST, tmp243_AST);
												match(SEMI);
												break;
											}
											case ENDBLOCK:
											case SINGLE_QUOTE: {
												break;
											}
											default: {
												throw new NoViableAltException(LT(1), getFilename());
											}
										}
									}
								} else {
									break _loop113;
								}
								
							} while (true);
						}
					} else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
				}
				AST tmp244_AST = null;
				tmp244_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp244_AST);
				match(ENDBLOCK);
				links_slot_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (NoViableAltException nv) {
			if (inputState.guessing == 0) {
				
				reportError(nv);
				consumeUntil(ENDBLOCK); // throw away all until ';;'
				consume();
				
			} else {
				throw nv;
			}
		}
		returnAST = links_slot_AST;
	}
	
	public final void knowledge_body() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST knowledge_body_AST = null;
		
		try { // for error handling
			type_slot();
			data_slot();
			astFactory.addASTChild(currentAST, returnAST);
			priority_slot();
			astFactory.addASTChild(currentAST, returnAST);
			evoke_slot();
			logic_slot();
			astFactory.addASTChild(currentAST, returnAST);
			action_slot();
			astFactory.addASTChild(currentAST, returnAST);
			urgency_slot();
			age_min_slot();
			astFactory.addASTChild(currentAST, returnAST);
			age_max_slot();
			astFactory.addASTChild(currentAST, returnAST);
			knowledge_body_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_3);
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
		
		try { // for error handling
			AST tmp245_AST = null;
			tmp245_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp245_AST);
			match(TYPE);
			AST tmp246_AST = null;
			tmp246_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp246_AST);
			match(COLON);
			type_code();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp247_AST = null;
			tmp247_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp247_AST);
			match(ENDBLOCK);
			type_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_14);
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
		
		try { // for error handling
			AST tmp248_AST = null;
			tmp248_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp248_AST);
			match(DATA);
			AST tmp249_AST = null;
			tmp249_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp249_AST);
			match(COLON);
			{
				_loop118: do {
					if ((_tokenSet_15.member(LA(1)))) {
						data_statement();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop118;
					}
					
				} while (true);
			}
			AST tmp250_AST = null;
			tmp250_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp250_AST);
			match(ENDBLOCK);
			data_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_16);
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
		
		try { // for error handling
			if ((LA(1) == LITERAL_evoke)) {
				priority_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == LITERAL_evoke)) {
				priority_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == PRIORITY)) {
				AST tmp251_AST = null;
				tmp251_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp251_AST);
				match(PRIORITY);
				AST tmp252_AST = null;
				tmp252_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp252_AST);
				match(COLON);
				AST tmp253_AST = null;
				tmp253_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp253_AST);
				match(INTLIT);
				AST tmp254_AST = null;
				tmp254_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp254_AST);
				match(ENDBLOCK);
				priority_slot_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (MismatchedTokenException mv) {
			if (inputState.guessing == 0) {
				
				reportError(mv);
				System.err.println("***Rule Priority NOT SET***");
				consumeUntil(ENDBLOCK); // throw away all until ';;'
				consume();
				
			} else {
				throw mv;
			}
		}
		returnAST = priority_slot_AST;
	}
	
	public final void evoke_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST evoke_slot_AST = null;
		
		try { // for error handling
			AST tmp255_AST = null;
			tmp255_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp255_AST);
			match(LITERAL_evoke);
			AST tmp256_AST = null;
			tmp256_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp256_AST);
			match(COLON);
			{
				evoke_statement();
				astFactory.addASTChild(currentAST, returnAST);
			}
			AST tmp257_AST = null;
			tmp257_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp257_AST);
			match(ENDBLOCK);
			evoke_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_17);
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
		
		try { // for error handling
			AST tmp258_AST = null;
			tmp258_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp258_AST);
			match(LOGIC);
			AST tmp259_AST = null;
			tmp259_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp259_AST);
			match(COLON);
			{
				_loop219: do {
					switch (LA(1)) {
						case CONCLUDE:
						case CALL:
						case SEMI:
						case ID:
						case ACTION_OP: {
							logic_statement();
							astFactory.addASTChild(currentAST, returnAST);
							match(SEMI);
							break;
						}
						case IF: {
							if_statement();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						case ELSE:
						case ELSEIF:
						case ENDIF: {
							logic_elseif();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						default: {
							break _loop219;
						}
					}
				} while (true);
			}
			AST tmp261_AST = null;
			tmp261_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp261_AST);
			match(ENDBLOCK);
			logic_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_18);
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
		
		try { // for error handling
			AST tmp262_AST = null;
			tmp262_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp262_AST);
			match(ACTION);
			AST tmp263_AST = null;
			tmp263_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp263_AST);
			match(COLON);
			{
				_loop254: do {
					switch (LA(1)) {
						case WRITE:
						case LET:
						case NOW:
						case CALL:
						case SEMI:
						case ID: {
							action_statement();
							astFactory.addASTChild(currentAST, returnAST);
							match(SEMI);
							break;
						}
						case IF: {
							if_statement();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						case ELSE:
						case ELSEIF:
						case ENDIF: {
							logic_elseif();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						default: {
							break _loop254;
						}
					}
				} while (true);
			}
			AST tmp265_AST = null;
			tmp265_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp265_AST);
			match(ENDBLOCK);
			action_slot_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_19);
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
		
		try { // for error handling
			switch (LA(1)) {
				case AGE_MIN:
				case AGE_MAX:
				case LITERAL_end: {
					urgency_slot_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_urgency: {
					AST tmp266_AST = null;
					tmp266_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp266_AST);
					match(LITERAL_urgency);
					AST tmp267_AST = null;
					tmp267_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp267_AST);
					match(COLON);
					urgency_val();
					astFactory.addASTChild(currentAST, returnAST);
					AST tmp268_AST = null;
					tmp268_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp268_AST);
					match(ENDBLOCK);
					urgency_slot_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_20);
			} else {
				throw ex;
			}
		}
		returnAST = urgency_slot_AST;
	}
	
	public final void age_min_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST age_min_slot_AST = null;
		
		try { // for error handling
			if ((LA(1) == AGE_MAX || LA(1) == LITERAL_end)) {
				age_min_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == AGE_MAX || LA(1) == LITERAL_end)) {
				age_min_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == AGE_MIN)) {
				AST tmp269_AST = null;
				tmp269_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp269_AST);
				match(AGE_MIN);
				AST tmp270_AST = null;
				tmp270_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp270_AST);
				match(COLON);
				AST tmp271_AST = null;
				tmp271_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp271_AST);
				match(INTLIT);
				age_code();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp272_AST = null;
				tmp272_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp272_AST);
				match(ENDBLOCK);
				age_min_slot_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_21);
			} else {
				throw ex;
			}
		}
		returnAST = age_min_slot_AST;
	}
	
	public final void age_max_slot() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST age_max_slot_AST = null;
		
		try { // for error handling
			if ((LA(1) == LITERAL_end)) {
				age_max_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == LITERAL_end)) {
				age_max_slot_AST = (AST) currentAST.root;
			} else if ((LA(1) == AGE_MAX)) {
				AST tmp273_AST = null;
				tmp273_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp273_AST);
				match(AGE_MAX);
				AST tmp274_AST = null;
				tmp274_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp274_AST);
				match(COLON);
				AST tmp275_AST = null;
				tmp275_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp275_AST);
				match(INTLIT);
				age_code();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp276_AST = null;
				tmp276_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp276_AST);
				match(ENDBLOCK);
				age_max_slot_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_3);
			} else {
				throw ex;
			}
		}
		returnAST = age_max_slot_AST;
	}
	
	public final void text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST text_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case ID: {
					AST tmp277_AST = null;
					tmp277_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp277_AST);
					match(ID);
					{
						switch (LA(1)) {
							case APOSTROPHE: {
								AST tmp278_AST = null;
								tmp278_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp278_AST);
								match(APOSTROPHE);
								break;
							}
							case AMPERSAND: {
								AST tmp279_AST = null;
								tmp279_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp279_AST);
								match(AMPERSAND);
								break;
							}
							case PERCENT: {
								AST tmp280_AST = null;
								tmp280_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp280_AST);
								match(PERCENT);
								break;
							}
							case GT: {
								AST tmp281_AST = null;
								tmp281_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp281_AST);
								match(GT);
								break;
							}
							case GTE: {
								AST tmp282_AST = null;
								tmp282_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp282_AST);
								match(GTE);
								break;
							}
							case LT: {
								AST tmp283_AST = null;
								tmp283_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp283_AST);
								match(LT);
								break;
							}
							case LTE: {
								AST tmp284_AST = null;
								tmp284_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp284_AST);
								match(LTE);
								break;
							}
							case POUND: {
								AST tmp285_AST = null;
								tmp285_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp285_AST);
								match(POUND);
								break;
							}
							case EOF:
							case AND:
							case IS:
							case ARE:
							case WAS:
							case WERE:
							case COUNT:
							case IN:
							case LESS:
							case GREATER:
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
							case DAY:
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
							case MAINTENANCE:
							case KNOWLEDGE:
							case PRIORITY:
							case VERSION:
							case PURPOSE:
							case OF:
							case TIME:
							case WITHIN:
							case CALL:
							case WITH:
							case TO:
							case ANY:
							case RESEARCH:
							case SECOND:
							case OCCUR:
							case PRESENT:
							case NUMBER:
							case COLON:
							case ENDBLOCK:
							case DOT:
							case MINUS:
							case UNDERSCORE:
							case LITERAL_arden:
							case INTLIT:
							case SEMI:
							case TIMES:
							case ID:
							case COMMA:
							case DIV:
							case STRING_LITERAL:
							case LPAREN:
							case SINGLE_QUOTE: {
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					text_AST = (AST) currentAST.root;
					break;
				}
				case INTLIT: {
					AST tmp286_AST = null;
					tmp286_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp286_AST);
					match(INTLIT);
					text_AST = (AST) currentAST.root;
					break;
				}
				case MINUS: {
					AST tmp287_AST = null;
					tmp287_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp287_AST);
					match(MINUS);
					text_AST = (AST) currentAST.root;
					break;
				}
				case COMMA: {
					AST tmp288_AST = null;
					tmp288_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp288_AST);
					match(COMMA);
					text_AST = (AST) currentAST.root;
					break;
				}
				case DOT: {
					AST tmp289_AST = null;
					tmp289_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp289_AST);
					match(DOT);
					text_AST = (AST) currentAST.root;
					break;
				}
				case DIV: {
					AST tmp290_AST = null;
					tmp290_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp290_AST);
					match(DIV);
					text_AST = (AST) currentAST.root;
					break;
				}
				case UNDERSCORE: {
					AST tmp291_AST = null;
					tmp291_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp291_AST);
					match(UNDERSCORE);
					text_AST = (AST) currentAST.root;
					break;
				}
				case STRING_LITERAL: {
					AST tmp292_AST = null;
					tmp292_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp292_AST);
					match(STRING_LITERAL);
					text_AST = (AST) currentAST.root;
					break;
				}
				case LPAREN: {
					{
						AST tmp293_AST = null;
						tmp293_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp293_AST);
						match(LPAREN);
						{
							_loop56: do {
								switch (LA(1)) {
									case ID: {
										AST tmp294_AST = null;
										tmp294_AST = astFactory.create(LT(1));
										astFactory.addASTChild(currentAST, tmp294_AST);
										match(ID);
										{
											switch (LA(1)) {
												case APOSTROPHE: {
													AST tmp295_AST = null;
													tmp295_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp295_AST);
													match(APOSTROPHE);
													break;
												}
												case AMPERSAND: {
													AST tmp296_AST = null;
													tmp296_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp296_AST);
													match(AMPERSAND);
													break;
												}
												case PERCENT: {
													AST tmp297_AST = null;
													tmp297_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp297_AST);
													match(PERCENT);
													break;
												}
												case GT: {
													AST tmp298_AST = null;
													tmp298_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp298_AST);
													match(GT);
													break;
												}
												case GTE: {
													AST tmp299_AST = null;
													tmp299_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp299_AST);
													match(GTE);
													break;
												}
												case LT: {
													AST tmp300_AST = null;
													tmp300_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp300_AST);
													match(LT);
													break;
												}
												case LTE: {
													AST tmp301_AST = null;
													tmp301_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp301_AST);
													match(LTE);
													break;
												}
												case POUND: {
													AST tmp302_AST = null;
													tmp302_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp302_AST);
													match(POUND);
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
												case GREATER:
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
												case DAY:
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
												case MAINTENANCE:
												case KNOWLEDGE:
												case PRIORITY:
												case PURPOSE:
												case OF:
												case TIME:
												case WITHIN:
												case CALL:
												case WITH:
												case TO:
												case ANY:
												case RESEARCH:
												case SECOND:
												case OCCUR:
												case PRESENT:
												case NUMBER:
												case INTLIT:
												case TIMES:
												case ID:
												case RPAREN: {
													break;
												}
												default: {
													throw new NoViableAltException(LT(1), getFilename());
												}
											}
										}
										break;
									}
									case INTLIT: {
										AST tmp303_AST = null;
										tmp303_AST = astFactory.create(LT(1));
										astFactory.addASTChild(currentAST, tmp303_AST);
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
									case GREATER:
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
									case DAY:
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
									case MAINTENANCE:
									case KNOWLEDGE:
									case PRIORITY:
									case PURPOSE:
									case OF:
									case TIME:
									case WITHIN:
									case CALL:
									case WITH:
									case TO:
									case ANY:
									case RESEARCH:
									case SECOND:
									case OCCUR:
									case PRESENT:
									case NUMBER:
									case TIMES: {
										{
											any_reserved_word();
											astFactory.addASTChild(currentAST, returnAST);
										}
										break;
									}
									default: {
										break _loop56;
									}
								}
							} while (true);
						}
						AST tmp304_AST = null;
						tmp304_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp304_AST);
						match(RPAREN);
					}
					text_AST = (AST) currentAST.root;
					break;
				}
				default:
					if ((_tokenSet_22.member(LA(1)))) {
						{
							any_reserved_word();
							astFactory.addASTChild(currentAST, returnAST);
						}
						text_AST = (AST) currentAST.root;
					} else if ((LA(1) == AT)) {
						AST tmp305_AST = null;
						tmp305_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp305_AST);
						match(AT);
						text_AST = (AST) currentAST.root;
					} else {
						throw new NoViableAltException(LT(1), getFilename());
					}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_23);
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
		
		try { // for error handling
			text();
			astFactory.addASTChild(currentAST, returnAST);
			{
				mlmname_text_rest();
				astFactory.addASTChild(currentAST, returnAST);
			}
			mlmname_text_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_5);
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
		
		try { // for error handling
			switch (LA(1)) {
				case VERSION:
				case LITERAL_arden: {
					mlmname_text_rest_AST = (AST) currentAST.root;
					break;
				}
				case DOT: {
					AST tmp306_AST = null;
					tmp306_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp306_AST);
					match(DOT);
					{
						_loop19: do {
							if ((_tokenSet_4.member(LA(1)))) {
								text();
								astFactory.addASTChild(currentAST, returnAST);
							} else {
								break _loop19;
							}
							
						} while (true);
					}
					AST tmp307_AST = null;
					tmp307_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp307_AST);
					match(ENDBLOCK);
					mlmname_text_rest_AST = (AST) currentAST.root;
					break;
				}
				case MINUS: {
					AST tmp308_AST = null;
					tmp308_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp308_AST);
					match(MINUS);
					{
						_loop21: do {
							if ((_tokenSet_4.member(LA(1)))) {
								text();
								astFactory.addASTChild(currentAST, returnAST);
							} else {
								break _loop21;
							}
							
						} while (true);
					}
					AST tmp309_AST = null;
					tmp309_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp309_AST);
					match(ENDBLOCK);
					mlmname_text_rest_AST = (AST) currentAST.root;
					break;
				}
				case UNDERSCORE: {
					AST tmp310_AST = null;
					tmp310_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp310_AST);
					match(UNDERSCORE);
					{
						_loop23: do {
							if ((_tokenSet_4.member(LA(1)))) {
								text();
								astFactory.addASTChild(currentAST, returnAST);
							} else {
								break _loop23;
							}
							
						} while (true);
					}
					AST tmp311_AST = null;
					tmp311_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp311_AST);
					match(ENDBLOCK);
					mlmname_text_rest_AST = (AST) currentAST.root;
					break;
				}
				case ENDBLOCK: {
					AST tmp312_AST = null;
					tmp312_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp312_AST);
					match(ENDBLOCK);
					mlmname_text_rest_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_5);
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
		
		try { // for error handling
			switch (LA(1)) {
				case INTLIT: {
					AST tmp313_AST = null;
					tmp313_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp313_AST);
					match(INTLIT);
					version_num_AST = (AST) currentAST.root;
					break;
				}
				case DIGIT: {
					AST tmp314_AST = null;
					tmp314_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp314_AST);
					match(DIGIT);
					AST tmp315_AST = null;
					tmp315_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp315_AST);
					match(DOT);
					AST tmp316_AST = null;
					tmp316_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp316_AST);
					match(DIGIT);
					version_num_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_24);
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
		
		try { // for error handling
			text();
			astFactory.addASTChild(currentAST, returnAST);
			mlm_version_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			iso_date_time();
			astFactory.addASTChild(currentAST, returnAST);
			mlm_date_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_24);
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
		
		try { // for error handling
			datepart();
			astFactory.addASTChild(currentAST, returnAST);
			iso_date_time_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_25);
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
		
		try { // for error handling
			switch (LA(1)) {
				case LITERAL_production: {
					AST tmp317_AST = null;
					tmp317_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp317_AST);
					match(LITERAL_production);
					validation_code_AST = (AST) currentAST.root;
					break;
				}
				case RESEARCH: {
					AST tmp318_AST = null;
					tmp318_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp318_AST);
					match(RESEARCH);
					validation_code_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_testing: {
					AST tmp319_AST = null;
					tmp319_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp319_AST);
					match(LITERAL_testing);
					validation_code_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_expired: {
					AST tmp320_AST = null;
					tmp320_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp320_AST);
					match(LITERAL_expired);
					validation_code_AST = (AST) currentAST.root;
					break;
				}
				case ENDBLOCK: {
					validation_code_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_24);
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
		
		try { // for error handling
			switch (LA(1)) {
				case AND: {
					AST tmp321_AST = null;
					tmp321_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp321_AST);
					match(AND);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case IS: {
					AST tmp322_AST = null;
					tmp322_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp322_AST);
					match(IS);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case ARE: {
					AST tmp323_AST = null;
					tmp323_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp323_AST);
					match(ARE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case WAS: {
					AST tmp324_AST = null;
					tmp324_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp324_AST);
					match(WAS);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case WERE: {
					AST tmp325_AST = null;
					tmp325_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp325_AST);
					match(WERE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case COUNT: {
					AST tmp326_AST = null;
					tmp326_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp326_AST);
					match(COUNT);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case IN: {
					AST tmp327_AST = null;
					tmp327_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp327_AST);
					match(IN);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case THE: {
					AST tmp328_AST = null;
					tmp328_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp328_AST);
					match(THE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case THAN: {
					AST tmp329_AST = null;
					tmp329_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp329_AST);
					match(THAN);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case FROM: {
					AST tmp330_AST = null;
					tmp330_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp330_AST);
					match(FROM);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case BEFORE: {
					AST tmp331_AST = null;
					tmp331_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp331_AST);
					match(BEFORE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case AFTER: {
					AST tmp332_AST = null;
					tmp332_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp332_AST);
					match(AFTER);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case AGO: {
					AST tmp333_AST = null;
					tmp333_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp333_AST);
					match(AGO);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case AT: {
					AST tmp334_AST = null;
					tmp334_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp334_AST);
					match(AT);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case OF: {
					AST tmp335_AST = null;
					tmp335_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp335_AST);
					match(OF);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case WRITE: {
					AST tmp336_AST = null;
					tmp336_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp336_AST);
					match(WRITE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case BE: {
					AST tmp337_AST = null;
					tmp337_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp337_AST);
					match(BE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case LET: {
					AST tmp338_AST = null;
					tmp338_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp338_AST);
					match(LET);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case YEAR: {
					AST tmp339_AST = null;
					tmp339_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp339_AST);
					match(YEAR);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case YEARS: {
					AST tmp340_AST = null;
					tmp340_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp340_AST);
					match(YEARS);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case IF: {
					AST tmp341_AST = null;
					tmp341_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp341_AST);
					match(IF);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case IT: {
					AST tmp342_AST = null;
					tmp342_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp342_AST);
					match(IT);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case THEY: {
					AST tmp343_AST = null;
					tmp343_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp343_AST);
					match(THEY);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case NOT: {
					AST tmp344_AST = null;
					tmp344_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp344_AST);
					match(NOT);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case OR: {
					AST tmp345_AST = null;
					tmp345_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp345_AST);
					match(OR);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case THEN: {
					AST tmp346_AST = null;
					tmp346_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp346_AST);
					match(THEN);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case MONTH: {
					AST tmp347_AST = null;
					tmp347_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp347_AST);
					match(MONTH);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case MONTHS: {
					AST tmp348_AST = null;
					tmp348_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp348_AST);
					match(MONTHS);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case TIME: {
					AST tmp349_AST = null;
					tmp349_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp349_AST);
					match(TIME);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case TIMES: {
					AST tmp350_AST = null;
					tmp350_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp350_AST);
					match(TIMES);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case WITHIN: {
					AST tmp351_AST = null;
					tmp351_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp351_AST);
					match(WITHIN);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case READ: {
					AST tmp352_AST = null;
					tmp352_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp352_AST);
					match(READ);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case MINIMUM: {
					AST tmp353_AST = null;
					tmp353_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp353_AST);
					match(MINIMUM);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case MIN: {
					AST tmp354_AST = null;
					tmp354_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp354_AST);
					match(MIN);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case MAXIMUM: {
					AST tmp355_AST = null;
					tmp355_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp355_AST);
					match(MAXIMUM);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case MAX: {
					AST tmp356_AST = null;
					tmp356_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp356_AST);
					match(MAX);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case LAST: {
					AST tmp357_AST = null;
					tmp357_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp357_AST);
					match(LAST);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case FIRST: {
					AST tmp358_AST = null;
					tmp358_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp358_AST);
					match(FIRST);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case EARLIEST: {
					AST tmp359_AST = null;
					tmp359_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp359_AST);
					match(EARLIEST);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case LATEST: {
					AST tmp360_AST = null;
					tmp360_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp360_AST);
					match(LATEST);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case EVENT: {
					AST tmp361_AST = null;
					tmp361_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp361_AST);
					match(EVENT);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case WHERE: {
					AST tmp362_AST = null;
					tmp362_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp362_AST);
					match(WHERE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case EXIST: {
					AST tmp363_AST = null;
					tmp363_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp363_AST);
					match(EXIST);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case EXISTS: {
					AST tmp364_AST = null;
					tmp364_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp364_AST);
					match(EXISTS);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case PAST: {
					AST tmp365_AST = null;
					tmp365_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp365_AST);
					match(PAST);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case AVERAGE: {
					AST tmp366_AST = null;
					tmp366_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp366_AST);
					match(AVERAGE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case AVG: {
					AST tmp367_AST = null;
					tmp367_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp367_AST);
					match(AVG);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case SUM: {
					AST tmp368_AST = null;
					tmp368_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp368_AST);
					match(SUM);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case MEDIAN: {
					AST tmp369_AST = null;
					tmp369_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp369_AST);
					match(MEDIAN);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case CONCLUDE: {
					AST tmp370_AST = null;
					tmp370_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp370_AST);
					match(CONCLUDE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case ELSE: {
					AST tmp371_AST = null;
					tmp371_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp371_AST);
					match(ELSE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case ELSEIF: {
					AST tmp372_AST = null;
					tmp372_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp372_AST);
					match(ELSEIF);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case ENDIF: {
					AST tmp373_AST = null;
					tmp373_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp373_AST);
					match(ENDIF);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case TRUE: {
					AST tmp374_AST = null;
					tmp374_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp374_AST);
					match(TRUE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case FALSE: {
					AST tmp375_AST = null;
					tmp375_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp375_AST);
					match(FALSE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case DATA: {
					AST tmp376_AST = null;
					tmp376_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp376_AST);
					match(DATA);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case LOGIC: {
					AST tmp377_AST = null;
					tmp377_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp377_AST);
					match(LOGIC);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case ACTION: {
					AST tmp378_AST = null;
					tmp378_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp378_AST);
					match(ACTION);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case CALL: {
					AST tmp379_AST = null;
					tmp379_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp379_AST);
					match(CALL);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case WITH: {
					AST tmp380_AST = null;
					tmp380_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp380_AST);
					match(WITH);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case TO: {
					AST tmp381_AST = null;
					tmp381_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp381_AST);
					match(TO);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case ANY: {
					AST tmp382_AST = null;
					tmp382_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp382_AST);
					match(ANY);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case RESEARCH: {
					AST tmp383_AST = null;
					tmp383_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp383_AST);
					match(RESEARCH);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case DAY: {
					AST tmp384_AST = null;
					tmp384_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp384_AST);
					match(DAY);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case SECOND: {
					AST tmp385_AST = null;
					tmp385_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp385_AST);
					match(SECOND);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case OCCUR: {
					AST tmp386_AST = null;
					tmp386_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp386_AST);
					match(OCCUR);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case PURPOSE: {
					AST tmp387_AST = null;
					tmp387_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp387_AST);
					match(PURPOSE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case PRESENT: {
					AST tmp388_AST = null;
					tmp388_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp388_AST);
					match(PRESENT);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case NUMBER: {
					AST tmp389_AST = null;
					tmp389_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp389_AST);
					match(NUMBER);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case KNOWLEDGE: {
					AST tmp390_AST = null;
					tmp390_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp390_AST);
					match(KNOWLEDGE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case PRIORITY: {
					AST tmp391_AST = null;
					tmp391_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp391_AST);
					match(PRIORITY);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case GREATER: {
					AST tmp392_AST = null;
					tmp392_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp392_AST);
					match(GREATER);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				case MAINTENANCE: {
					AST tmp393_AST = null;
					tmp393_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp393_AST);
					match(MAINTENANCE);
					any_reserved_word_AST = (AST) currentAST.root;
					break;
				}
				default:
					if ((LA(1) == LESS)) {
						AST tmp394_AST = null;
						tmp394_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp394_AST);
						match(LESS);
						any_reserved_word_AST = (AST) currentAST.root;
					} else if ((LA(1) == LESS)) {
						AST tmp395_AST = null;
						tmp395_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp395_AST);
						match(LESS);
						any_reserved_word_AST = (AST) currentAST.root;
					} else {
						throw new NoViableAltException(LT(1), getFilename());
					}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_26);
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
		
		try { // for error handling
			{
				AST tmp396_AST = null;
				tmp396_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp396_AST);
				match(INTLIT);
				AST tmp397_AST = null;
				tmp397_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp397_AST);
				match(MINUS);
				AST tmp398_AST = null;
				tmp398_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp398_AST);
				match(INTLIT);
				AST tmp399_AST = null;
				tmp399_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp399_AST);
				match(MINUS);
				AST tmp400_AST = null;
				tmp400_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp400_AST);
				match(INTLIT);
			}
			{
				switch (LA(1)) {
					case ID: {
						{
							AST tmp401_AST = null;
							tmp401_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp401_AST);
							match(ID);
						}
						{
							AST tmp402_AST = null;
							tmp402_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp402_AST);
							match(COLON);
							AST tmp403_AST = null;
							tmp403_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp403_AST);
							match(INTLIT);
							AST tmp404_AST = null;
							tmp404_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp404_AST);
							match(COLON);
							AST tmp405_AST = null;
							tmp405_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp405_AST);
							match(INTLIT);
						}
						{
							switch (LA(1)) {
								case DOT: {
									AST tmp406_AST = null;
									tmp406_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp406_AST);
									match(DOT);
									AST tmp407_AST = null;
									tmp407_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp407_AST);
									match(DIGIT);
									break;
								}
								case EOF:
								case ENDBLOCK:
								case MINUS:
								case ID:
								case PLUS: {
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
						time_zone();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case EOF:
					case ENDBLOCK: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			datepart_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_25);
			} else {
				throw ex;
			}
		}
		returnAST = datepart_AST;
	}
	
	public final void time_zone() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST time_zone_AST = null;
		
		try { // for error handling
			{
				switch (LA(1)) {
					case EOF:
					case ENDBLOCK: {
						break;
					}
					case ID: {
						AST tmp408_AST = null;
						tmp408_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp408_AST);
						match(ID);
						break;
					}
					case PLUS: {
						AST tmp409_AST = null;
						tmp409_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp409_AST);
						match(PLUS);
						AST tmp410_AST = null;
						tmp410_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp410_AST);
						match(INTLIT);
						{
							_loop67: do {
								if ((LA(1) == COLON)) {
									AST tmp411_AST = null;
									tmp411_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp411_AST);
									match(COLON);
									AST tmp412_AST = null;
									tmp412_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp412_AST);
									match(INTLIT);
								} else {
									break _loop67;
								}
								
							} while (true);
						}
						break;
					}
					case MINUS: {
						AST tmp413_AST = null;
						tmp413_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp413_AST);
						match(MINUS);
						AST tmp414_AST = null;
						tmp414_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp414_AST);
						match(INTLIT);
						{
							_loop69: do {
								if ((LA(1) == COLON)) {
									AST tmp415_AST = null;
									tmp415_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp415_AST);
									match(COLON);
									AST tmp416_AST = null;
									tmp416_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp416_AST);
									match(INTLIT);
								} else {
									break _loop69;
								}
								
							} while (true);
						}
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			time_zone_AST = (AST) currentAST.root;
		}
		catch (NoViableAltException nv) {
			if (inputState.guessing == 0) {
				
				reportError(nv);
				consumeUntil(ENDBLOCK); // throw away all until ';;'
				
			} else {
				throw nv;
			}
		}
		returnAST = time_zone_AST;
	}
	
	public final void keyword_text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST keyword_text_AST = null;
		
		try { // for error handling
			{
				_loop80: do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop80;
					}
					
				} while (true);
			}
			{
				_loop84: do {
					if ((LA(1) == SEMI)) {
						AST tmp417_AST = null;
						tmp417_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp417_AST);
						match(SEMI);
						{
							_loop83: do {
								if ((_tokenSet_4.member(LA(1)))) {
									text();
									astFactory.addASTChild(currentAST, returnAST);
								} else {
									break _loop83;
								}
								
							} while (true);
						}
					} else {
						break _loop84;
					}
					
				} while (true);
			}
			AST tmp418_AST = null;
			tmp418_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp418_AST);
			match(ENDBLOCK);
			keyword_text_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_12);
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
		
		try { // for error handling
			if ((LA(1) == ENDBLOCK)) {
				citations_list_AST = (AST) currentAST.root;
			} else if ((LA(1) == ENDBLOCK)) {
				citations_list_AST = (AST) currentAST.root;
			} else if ((_tokenSet_27.member(LA(1)))) {
				single_citation();
				astFactory.addASTChild(currentAST, returnAST);
				{
					_loop89: do {
						if ((LA(1) == SEMI)) {
							AST tmp419_AST = null;
							tmp419_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp419_AST);
							match(SEMI);
							single_citation();
							astFactory.addASTChild(currentAST, returnAST);
						} else {
							break _loop89;
						}
						
					} while (true);
				}
				citations_list_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_24);
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
		
		try { // for error handling
			{
				_loop92: do {
					if ((LA(1) == INTLIT)) {
						AST tmp420_AST = null;
						tmp420_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp420_AST);
						match(INTLIT);
						AST tmp421_AST = null;
						tmp421_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp421_AST);
						match(DOT);
						citation_type();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop92;
					}
					
				} while (true);
			}
			citation_text();
			astFactory.addASTChild(currentAST, returnAST);
			single_citation_AST = (AST) currentAST.root;
		}
		catch (NoViableAltException nv) {
			if (inputState.guessing == 0) {
				
				reportError(nv);
				consumeUntil(ENDBLOCK); // throw away all until ';;'
				
			} else {
				throw nv;
			}
		}
		returnAST = single_citation_AST;
	}
	
	public final void citation_type() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST citation_type_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case LITERAL_SUPPORT: {
					AST tmp422_AST = null;
					tmp422_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp422_AST);
					match(LITERAL_SUPPORT);
					citation_type_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_REFUTE: {
					AST tmp423_AST = null;
					tmp423_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp423_AST);
					match(LITERAL_REFUTE);
					citation_type_AST = (AST) currentAST.root;
					break;
				}
				default:
					if ((_tokenSet_27.member(LA(1)))) {
						citation_type_AST = (AST) currentAST.root;
					} else if ((_tokenSet_27.member(LA(1)))) {
						citation_type_AST = (AST) currentAST.root;
					} else {
						throw new NoViableAltException(LT(1), getFilename());
					}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_27);
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
		
		try { // for error handling
			{
				_loop95: do {
					if ((_tokenSet_4.member(LA(1)))) {
						text();
						astFactory.addASTChild(currentAST, returnAST);
					} else if ((LA(1) == INTLIT)) {
						AST tmp424_AST = null;
						tmp424_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp424_AST);
						match(INTLIT);
					} else {
						break _loop95;
					}
					
				} while (true);
			}
			{
				_loop103: do {
					if ((LA(1) == COLON)) {
						AST tmp425_AST = null;
						tmp425_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp425_AST);
						match(COLON);
						{
							_loop98: do {
								if ((_tokenSet_4.member(LA(1)))) {
									text();
									astFactory.addASTChild(currentAST, returnAST);
								} else {
									break _loop98;
								}
								
							} while (true);
						}
						{
							if ((LA(1) == MINUS)) {
								AST tmp426_AST = null;
								tmp426_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp426_AST);
								match(MINUS);
								AST tmp427_AST = null;
								tmp427_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp427_AST);
								match(INTLIT);
							} else if ((_tokenSet_27.member(LA(1)))) {} else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							
						}
						{
							_loop101: do {
								if ((_tokenSet_4.member(LA(1)))) {
									text();
									astFactory.addASTChild(currentAST, returnAST);
								} else {
									break _loop101;
								}
								
							} while (true);
						}
						{
							switch (LA(1)) {
								case DOT: {
									AST tmp428_AST = null;
									tmp428_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp428_AST);
									match(DOT);
									break;
								}
								case COLON:
								case ENDBLOCK:
								case SEMI: {
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
					} else {
						break _loop103;
					}
					
				} while (true);
			}
			citation_text_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_28);
			} else {
				throw ex;
			}
		}
		returnAST = citation_text_AST;
	}
	
	public final void type_code() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST type_code_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case 135: {
					AST tmp429_AST = null;
					tmp429_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp429_AST);
					match(135);
					type_code_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_data_driven: {
					AST tmp430_AST = null;
					tmp430_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp430_AST);
					match(LITERAL_data_driven);
					type_code_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_24);
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
		
		try { // for error handling
			{
				switch (LA(1)) {
					case IF: {
						{
							data_if_statement();
							astFactory.addASTChild(currentAST, returnAST);
						}
						break;
					}
					case LET:
					case NOW:
					case SEMI:
					case ID: {
						{
							data_assignment();
							astFactory.addASTChild(currentAST, returnAST);
							match(SEMI);
						}
						break;
					}
					case ELSE:
					case ELSEIF:
					case ENDIF: {
						data_elseif();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			data_statement_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_29);
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
		
		try { // for error handling
			AST tmp432_AST = null;
			tmp432_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp432_AST);
			match(IF);
			data_if_then_else2();
			astFactory.addASTChild(currentAST, returnAST);
			data_if_statement_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_29);
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
		
		try { // for error handling
			switch (LA(1)) {
				case LET:
				case NOW:
				case ID: {
					identifier_becomes();
					astFactory.addASTChild(currentAST, returnAST);
					{
						switch (LA(1)) {
							case READ: {
								{
									AST tmp433_AST = null;
									tmp433_AST = astFactory.create(LT(1));
									astFactory.makeASTRoot(currentAST, tmp433_AST);
									match(READ);
								}
								{
									{
										if ((_tokenSet_30.member(LA(1)))) {
											{
												of_read_func_op();
												astFactory.addASTChild(currentAST, returnAST);
											}
										} else if ((_tokenSet_31.member(LA(1)))) {
											{
												from_of_func_op();
												astFactory.addASTChild(currentAST, returnAST);
												{
													switch (LA(1)) {
														case INTLIT: {
															AST tmp434_AST = null;
															tmp434_AST = astFactory.create(LT(1));
															astFactory.addASTChild(currentAST, tmp434_AST);
															match(INTLIT);
															break;
														}
														case LPAREN:
														case ARDEN_CURLY_BRACKETS: {
															break;
														}
														default: {
															throw new NoViableAltException(LT(1), getFilename());
														}
													}
												}
											}
										} else if ((LA(1) == LPAREN || LA(1) == ARDEN_CURLY_BRACKETS)) {} else {
											throw new NoViableAltException(LT(1), getFilename());
										}
										
									}
									{
										switch (LA(1)) {
											case ARDEN_CURLY_BRACKETS: {
												mapping_factor();
												astFactory.addASTChild(currentAST, returnAST);
												{
													switch (LA(1)) {
														case BEFORE:
														case AFTER:
														case NOT:
														case WHERE:
														case WITHIN: {
															{
																switch (LA(1)) {
																	case WHERE: {
																		where();
																		astFactory.addASTChild(currentAST, returnAST);
																		it();
																		occur();
																		break;
																	}
																	case BEFORE:
																	case AFTER:
																	case NOT:
																	case WITHIN: {
																		break;
																	}
																	default: {
																		throw new NoViableAltException(LT(1), getFilename());
																	}
																}
															}
															{
																switch (LA(1)) {
																	case BEFORE:
																	case AFTER:
																	case WITHIN: {
																		temporal_comp_op();
																		astFactory.addASTChild(currentAST, returnAST);
																		break;
																	}
																	case NOT: {
																		AST tmp435_AST = null;
																		tmp435_AST = astFactory.create(LT(1));
																		astFactory.addASTChild(currentAST, tmp435_AST);
																		match(NOT);
																		temporal_comp_op();
																		astFactory.addASTChild(currentAST, returnAST);
																		break;
																	}
																	default: {
																		throw new NoViableAltException(LT(1), getFilename());
																	}
																}
															}
															break;
														}
														case SEMI: {
															break;
														}
														default: {
															throw new NoViableAltException(LT(1), getFilename());
														}
													}
												}
												break;
											}
											case LPAREN: {
												match(LPAREN);
												{
													mapping_factor();
													astFactory.addASTChild(currentAST, returnAST);
													{
														switch (LA(1)) {
															case WHERE: {
																where();
																astFactory.addASTChild(currentAST, returnAST);
																it();
																astFactory.addASTChild(currentAST, returnAST);
																occur();
																astFactory.addASTChild(currentAST, returnAST);
																{
																	switch (LA(1)) {
																		case BEFORE:
																		case AFTER:
																		case WITHIN: {
																			temporal_comp_op();
																			astFactory.addASTChild(currentAST, returnAST);
																			break;
																		}
																		case NOT: {
																			AST tmp437_AST = null;
																			tmp437_AST = astFactory.create(LT(1));
																			astFactory.addASTChild(currentAST, tmp437_AST);
																			match(NOT);
																			temporal_comp_op();
																			astFactory.addASTChild(currentAST, returnAST);
																			break;
																		}
																		default: {
																			throw new NoViableAltException(LT(1),
																			        getFilename());
																		}
																	}
																}
																break;
															}
															case RPAREN: {
																break;
															}
															default: {
																throw new NoViableAltException(LT(1), getFilename());
															}
														}
													}
												}
												match(RPAREN);
												break;
											}
											default: {
												throw new NoViableAltException(LT(1), getFilename());
											}
										}
									}
								}
								break;
							}
							case EVENT: {
								{
									AST tmp439_AST = null;
									tmp439_AST = astFactory.create(LT(1));
									astFactory.makeASTRoot(currentAST, tmp439_AST);
									match(EVENT);
								}
								mapping_factor();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							case CALL: {
								{
									AST tmp440_AST = null;
									tmp440_AST = astFactory.create(LT(1));
									astFactory.makeASTRoot(currentAST, tmp440_AST);
									match(CALL);
								}
								call_phrase();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							case COUNT:
							case THE:
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
							case TIME:
							case ANY:
							case NULL:
							case INTLIT:
							case SEMI:
							case ID:
							case COMMA:
							case STRING_LITERAL:
							case LPAREN:
							case LITERAL_MERGE:
							case LITERAL_SORT:
							case TERM_LITERAL: {
								expr();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					data_assignment_AST = (AST) currentAST.root;
					break;
				}
				case SEMI: {
					data_assignment_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_32);
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
		
		try { // for error handling
			switch (LA(1)) {
				case ELSE: {
					AST tmp441_AST = null;
					tmp441_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp441_AST);
					match(ELSE);
					data_elseif_AST = (AST) currentAST.root;
					break;
				}
				case ELSEIF: {
					AST tmp442_AST = null;
					tmp442_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp442_AST);
					match(ELSEIF);
					data_if_then_else2();
					astFactory.addASTChild(currentAST, returnAST);
					data_elseif_AST = (AST) currentAST.root;
					break;
				}
				case ENDIF: {
					{
						AST tmp443_AST = null;
						tmp443_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp443_AST);
						match(ENDIF);
					}
					data_elseif_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_29);
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
		
		try { // for error handling
			{
				if ((_tokenSet_33.member(LA(1)))) {
					expr();
					astFactory.addASTChild(currentAST, returnAST);
				} else if ((LA(1) == LPAREN)) {
					{
						match(LPAREN);
					}
					expr();
					astFactory.addASTChild(currentAST, returnAST);
					{
						match(RPAREN);
					}
				} else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
			}
			AST tmp446_AST = null;
			tmp446_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp446_AST);
			match(THEN);
			data_statement();
			astFactory.addASTChild(currentAST, returnAST);
			data_if_then_else2_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_29);
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
		
		try { // for error handling
			expr_sort();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop277: do {
					if ((LA(1) == COMMA)) {
						match(COMMA);
						expr_sort();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop277;
					}
					
				} while (true);
			}
			expr_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_34);
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
		
		try { // for error handling
			switch (LA(1)) {
				case COMMENT: {
					{
						AST tmp448_AST = null;
						tmp448_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp448_AST);
						match(COMMENT);
					}
					data_comment_AST = (AST) currentAST.root;
					break;
				}
				case ML_COMMENT: {
					{
						AST tmp449_AST = null;
						tmp449_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp449_AST);
						match(ML_COMMENT);
					}
					data_comment_AST = (AST) currentAST.root;
					break;
				}
				case EOF: {
					data_comment_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			switch (LA(1)) {
				case ID: {
					identifier_or_object_ref();
					astFactory.addASTChild(currentAST, returnAST);
					match(BECOMES);
					identifier_becomes_AST = (AST) currentAST.root;
					break;
				}
				case LET: {
					match(LET);
					{
						switch (LA(1)) {
							case LPAREN: {
								match(LPAREN);
								break;
							}
							case ID: {
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					AST tmp453_AST = null;
					tmp453_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp453_AST);
					match(ID);
					{
						_loop161: do {
							if ((LA(1) == COMMA)) {
								AST tmp454_AST = null;
								tmp454_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp454_AST);
								match(COMMA);
								AST tmp455_AST = null;
								tmp455_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp455_AST);
								match(ID);
							} else {
								break _loop161;
							}
							
						} while (true);
					}
					{
						switch (LA(1)) {
							case RPAREN: {
								match(RPAREN);
								break;
							}
							case BE: {
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					match(BE);
					identifier_becomes_AST = (AST) currentAST.root;
					break;
				}
				case NOW: {
					AST tmp458_AST = null;
					tmp458_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp458_AST);
					match(NOW);
					AST tmp459_AST = null;
					tmp459_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp459_AST);
					match(BECOMES);
					identifier_becomes_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_35);
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
		
		try { // for error handling
			switch (LA(1)) {
				case AVERAGE: {
					AST tmp460_AST = null;
					tmp460_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp460_AST);
					match(AVERAGE);
					of_read_func_op_AST = (AST) currentAST.root;
					break;
				}
				case AVG: {
					AST tmp461_AST = null;
					tmp461_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp461_AST);
					match(AVG);
					of_read_func_op_AST = (AST) currentAST.root;
					break;
				}
				case COUNT: {
					AST tmp462_AST = null;
					tmp462_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp462_AST);
					match(COUNT);
					of_read_func_op_AST = (AST) currentAST.root;
					break;
				}
				case EXIST: {
					{
						AST tmp463_AST = null;
						tmp463_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp463_AST);
						match(EXIST);
					}
					of_read_func_op_AST = (AST) currentAST.root;
					break;
				}
				case EXISTS: {
					AST tmp464_AST = null;
					tmp464_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp464_AST);
					match(EXISTS);
					of_read_func_op_AST = (AST) currentAST.root;
					break;
				}
				case SUM: {
					AST tmp465_AST = null;
					tmp465_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp465_AST);
					match(SUM);
					of_read_func_op_AST = (AST) currentAST.root;
					break;
				}
				case MEDIAN: {
					AST tmp466_AST = null;
					tmp466_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp466_AST);
					match(MEDIAN);
					of_read_func_op_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_36);
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
		
		try { // for error handling
			switch (LA(1)) {
				case MINIMUM:
				case MIN: {
					{
						switch (LA(1)) {
							case MINIMUM: {
								AST tmp467_AST = null;
								tmp467_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp467_AST);
								match(MINIMUM);
								break;
							}
							case MIN: {
								AST tmp468_AST = null;
								tmp468_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp468_AST);
								match(MIN);
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					from_of_func_op_AST = (AST) currentAST.root;
					break;
				}
				case MAXIMUM:
				case MAX: {
					{
						switch (LA(1)) {
							case MAXIMUM: {
								AST tmp469_AST = null;
								tmp469_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp469_AST);
								match(MAXIMUM);
								break;
							}
							case MAX: {
								AST tmp470_AST = null;
								tmp470_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp470_AST);
								match(MAX);
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					from_of_func_op_AST = (AST) currentAST.root;
					break;
				}
				case LAST: {
					{
						AST tmp471_AST = null;
						tmp471_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp471_AST);
						match(LAST);
					}
					from_of_func_op_AST = (AST) currentAST.root;
					break;
				}
				case FIRST: {
					{
						AST tmp472_AST = null;
						tmp472_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp472_AST);
						match(FIRST);
					}
					from_of_func_op_AST = (AST) currentAST.root;
					break;
				}
				case EARLIEST: {
					{
						AST tmp473_AST = null;
						tmp473_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp473_AST);
						match(EARLIEST);
					}
					from_of_func_op_AST = (AST) currentAST.root;
					break;
				}
				case LATEST: {
					{
						AST tmp474_AST = null;
						tmp474_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp474_AST);
						match(LATEST);
					}
					from_of_func_op_AST = (AST) currentAST.root;
					break;
				}
				case TRUE:
				case FALSE:
				case OF:
				case NULL:
				case INTLIT:
				case ID:
				case STRING_LITERAL:
				case LPAREN:
				case ARDEN_CURLY_BRACKETS:
				case TERM_LITERAL: {
					from_of_func_op_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_36);
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
		
		try { // for error handling
			AST tmp475_AST = null;
			tmp475_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp475_AST);
			match(ARDEN_CURLY_BRACKETS);
			mapping_factor_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_37);
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
		
		try { // for error handling
			{
				AST tmp476_AST = null;
				tmp476_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp476_AST);
				match(WHERE);
			}
			where_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_38);
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
		
		try { // for error handling
			{
				switch (LA(1)) {
					case IT: {
						match(IT);
						break;
					}
					case THEY: {
						match(THEY);
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			it_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_39);
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
		
		try { // for error handling
			switch (LA(1)) {
				case OCCUR:
				case LITERAL_OCCUR:
				case LITERAL_Occur: {
					{
						switch (LA(1)) {
							case LITERAL_OCCUR: {
								match(LITERAL_OCCUR);
								break;
							}
							case LITERAL_Occur: {
								match(LITERAL_Occur);
								break;
							}
							case OCCUR: {
								match(OCCUR);
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					occur_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_OCCURS:
				case LITERAL_Occurs:
				case LITERAL_occurs: {
					{
						switch (LA(1)) {
							case LITERAL_OCCURS: {
								match(LITERAL_OCCURS);
								break;
							}
							case LITERAL_Occurs: {
								match(LITERAL_Occurs);
								break;
							}
							case LITERAL_occurs: {
								match(LITERAL_occurs);
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					occur_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_OCCURRED:
				case LITERAL_Occurred: {
					{
						if ((LA(1) == LITERAL_OCCURRED)) {
							match(LITERAL_OCCURRED);
						} else if ((LA(1) == LITERAL_Occurred)) {
							match(LITERAL_Occurred);
						} else if ((LA(1) == LITERAL_Occurred)) {
							match(LITERAL_Occurred);
						} else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
					}
					occur_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_40);
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
		
		try { // for error handling
			switch (LA(1)) {
				case WITHIN: {
					AST tmp488_AST = null;
					tmp488_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp488_AST);
					match(WITHIN);
					{
						switch (LA(1)) {
							case THE: {
								the();
								break;
							}
							case PAST: {
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					AST tmp489_AST = null;
					tmp489_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp489_AST);
					match(PAST);
					expr_string();
					astFactory.addASTChild(currentAST, returnAST);
					temporal_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case AFTER: {
					AST tmp490_AST = null;
					tmp490_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp490_AST);
					match(AFTER);
					expr_string();
					astFactory.addASTChild(currentAST, returnAST);
					temporal_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case BEFORE: {
					AST tmp491_AST = null;
					tmp491_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp491_AST);
					match(BEFORE);
					expr_string();
					astFactory.addASTChild(currentAST, returnAST);
					temporal_comp_op_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_41);
			} else {
				throw ex;
			}
		}
		returnAST = temporal_comp_op_AST;
	}
	
	public final void call_phrase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST call_phrase_AST = null;
		
		try { // for error handling
			AST tmp492_AST = null;
			tmp492_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp492_AST);
			match(ID);
			{
				switch (LA(1)) {
					case WITH: {
						AST tmp493_AST = null;
						tmp493_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp493_AST);
						match(WITH);
						expr();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case CONCLUDE:
					case CALL:
					case SEMI:
					case ID:
					case ACTION_OP: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			call_phrase_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_42);
			} else {
				throw ex;
			}
		}
		returnAST = call_phrase_AST;
	}
	
	public final void endassignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST endassignment_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case SEMI: {
					AST tmp494_AST = null;
					tmp494_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp494_AST);
					match(SEMI);
					endassignment_AST = (AST) currentAST.root;
					break;
				}
				case EOF: {
					{}
					endassignment_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			switch (LA(1)) {
				case ENDBLOCK: {
					AST tmp495_AST = null;
					tmp495_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp495_AST);
					match(ENDBLOCK);
					endblock_AST = (AST) currentAST.root;
					break;
				}
				case EOF: {
					{}
					endblock_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			AST tmp496_AST = null;
			tmp496_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp496_AST);
			match(ID);
			{
				_loop165: do {
					if ((LA(1) == DOT)) {
						AST tmp497_AST = null;
						tmp497_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp497_AST);
						match(DOT);
						AST tmp498_AST = null;
						tmp498_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp498_AST);
						match(ID);
					} else {
						break _loop165;
					}
					
				} while (true);
			}
			identifier_or_object_ref_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_43);
			} else {
				throw ex;
			}
		}
		returnAST = identifier_or_object_ref_AST;
	}
	
	public final void data_var_list() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST data_var_list_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case EOF: {
					data_var_list_AST = (AST) currentAST.root;
					break;
				}
				case ID: {
					AST tmp499_AST = null;
					tmp499_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp499_AST);
					match(ID);
					{
						_loop168: do {
							if ((LA(1) == COMMA)) {
								AST tmp500_AST = null;
								tmp500_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp500_AST);
								match(COMMA);
								AST tmp501_AST = null;
								tmp501_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp501_AST);
								match(ID);
							} else {
								break _loop168;
							}
							
						} while (true);
					}
					data_var_list_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
			} else {
				throw ex;
			}
		}
		returnAST = data_var_list_AST;
	}
	
	public final void read_phrase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST read_phrase_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case LPAREN:
				case ARDEN_CURLY_BRACKETS: {
					read_where();
					astFactory.addASTChild(currentAST, returnAST);
					read_phrase_AST = (AST) currentAST.root;
					break;
				}
				case COUNT:
				case EXIST:
				case EXISTS:
				case AVG:
				case AVERAGE:
				case SUM:
				case MEDIAN: {
					of_read_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					read_where();
					astFactory.addASTChild(currentAST, returnAST);
					from_of_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					read_where();
					astFactory.addASTChild(currentAST, returnAST);
					read_phrase_AST = (AST) currentAST.root;
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
				case INTLIT: {
					from_of_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					AST tmp502_AST = null;
					tmp502_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp502_AST);
					match(INTLIT);
					{
						AST tmp503_AST = null;
						tmp503_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp503_AST);
						match(FROM);
					}
					read_where();
					astFactory.addASTChild(currentAST, returnAST);
					read_phrase_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
			} else {
				throw ex;
			}
		}
		returnAST = read_phrase_AST;
	}
	
	public final void read_where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST read_where_AST = null;
		
		try { // for error handling
			{
				switch (LA(1)) {
					case ARDEN_CURLY_BRACKETS: {
						mapping_factor();
						astFactory.addASTChild(currentAST, returnAST);
						{
							switch (LA(1)) {
								case BEFORE:
								case AFTER:
								case NOT:
								case WHERE:
								case WITHIN: {
									{
										switch (LA(1)) {
											case WHERE: {
												where();
												astFactory.addASTChild(currentAST, returnAST);
												it();
												astFactory.addASTChild(currentAST, returnAST);
												occur();
												astFactory.addASTChild(currentAST, returnAST);
												break;
											}
											case BEFORE:
											case AFTER:
											case NOT:
											case WITHIN: {
												break;
											}
											default: {
												throw new NoViableAltException(LT(1), getFilename());
											}
										}
									}
									{
										switch (LA(1)) {
											case BEFORE:
											case AFTER:
											case WITHIN: {
												temporal_comp_op();
												astFactory.addASTChild(currentAST, returnAST);
												break;
											}
											case NOT: {
												AST tmp504_AST = null;
												tmp504_AST = astFactory.create(LT(1));
												astFactory.addASTChild(currentAST, tmp504_AST);
												match(NOT);
												temporal_comp_op();
												astFactory.addASTChild(currentAST, returnAST);
												break;
											}
											default: {
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
								case ARDEN_CURLY_BRACKETS: {
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
						break;
					}
					case LPAREN: {
						match(LPAREN);
						{
							mapping_factor();
							astFactory.addASTChild(currentAST, returnAST);
							{
								switch (LA(1)) {
									case WHERE: {
										where();
										astFactory.addASTChild(currentAST, returnAST);
										it();
										astFactory.addASTChild(currentAST, returnAST);
										occur();
										astFactory.addASTChild(currentAST, returnAST);
										{
											switch (LA(1)) {
												case BEFORE:
												case AFTER:
												case WITHIN: {
													temporal_comp_op();
													astFactory.addASTChild(currentAST, returnAST);
													break;
												}
												case NOT: {
													AST tmp506_AST = null;
													tmp506_AST = astFactory.create(LT(1));
													astFactory.addASTChild(currentAST, tmp506_AST);
													match(NOT);
													temporal_comp_op();
													astFactory.addASTChild(currentAST, returnAST);
													break;
												}
												default: {
													throw new NoViableAltException(LT(1), getFilename());
												}
											}
										}
										break;
									}
									case RPAREN: {
										break;
									}
									default: {
										throw new NoViableAltException(LT(1), getFilename());
									}
								}
							}
						}
						match(RPAREN);
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			read_where_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_44);
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
		
		try { // for error handling
			switch (LA(1)) {
				case NOW: {
					AST tmp508_AST = null;
					tmp508_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp508_AST);
					match(NOW);
					time_value_AST = (AST) currentAST.root;
					break;
				}
				case INTLIT: {
					iso_date_time();
					astFactory.addASTChild(currentAST, returnAST);
					time_value_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			AST tmp509_AST = null;
			tmp509_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp509_AST);
			match(IN);
			in_comp_op_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			switch (LA(1)) {
				case LITERAL_PRESENT: {
					AST tmp510_AST = null;
					tmp510_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp510_AST);
					match(LITERAL_PRESENT);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_NULL: {
					AST tmp511_AST = null;
					tmp511_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp511_AST);
					match(LITERAL_NULL);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_BOOLEAN: {
					AST tmp512_AST = null;
					tmp512_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp512_AST);
					match(LITERAL_BOOLEAN);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_NUMBER: {
					AST tmp513_AST = null;
					tmp513_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp513_AST);
					match(LITERAL_NUMBER);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case TIME: {
					AST tmp514_AST = null;
					tmp514_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp514_AST);
					match(TIME);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_DURATION: {
					AST tmp515_AST = null;
					tmp515_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp515_AST);
					match(LITERAL_DURATION);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_STRING: {
					AST tmp516_AST = null;
					tmp516_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp516_AST);
					match(LITERAL_STRING);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_LIST: {
					AST tmp517_AST = null;
					tmp517_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp517_AST);
					match(LITERAL_LIST);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_OBJECT: {
					AST tmp518_AST = null;
					tmp518_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp518_AST);
					match(LITERAL_OBJECT);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case ID: {
					AST tmp519_AST = null;
					tmp519_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp519_AST);
					match(ID);
					unary_comp_op_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			if ((LA(1) == LESS)) {
				AST tmp520_AST = null;
				tmp520_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp520_AST);
				match(LESS);
				AST tmp521_AST = null;
				tmp521_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp521_AST);
				match(THAN);
				binary_comp_op_AST = (AST) currentAST.root;
			} else if ((LA(1) == GREATER)) {
				AST tmp522_AST = null;
				tmp522_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp522_AST);
				match(GREATER);
				AST tmp523_AST = null;
				tmp523_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp523_AST);
				match(THAN);
				binary_comp_op_AST = (AST) currentAST.root;
			} else if ((LA(1) == GREATER)) {
				AST tmp524_AST = null;
				tmp524_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp524_AST);
				match(GREATER);
				AST tmp525_AST = null;
				tmp525_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp525_AST);
				match(THAN);
				AST tmp526_AST = null;
				tmp526_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp526_AST);
				match(OR);
				AST tmp527_AST = null;
				tmp527_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp527_AST);
				match(EQUAL);
				binary_comp_op_AST = (AST) currentAST.root;
			} else if ((LA(1) == LESS)) {
				AST tmp528_AST = null;
				tmp528_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp528_AST);
				match(LESS);
				AST tmp529_AST = null;
				tmp529_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp529_AST);
				match(THAN);
				AST tmp530_AST = null;
				tmp530_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp530_AST);
				match(OR);
				AST tmp531_AST = null;
				tmp531_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp531_AST);
				match(EQUAL);
				binary_comp_op_AST = (AST) currentAST.root;
			} else if ((LA(1) == IN)) {
				AST tmp532_AST = null;
				tmp532_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp532_AST);
				match(IN);
				binary_comp_op_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_45);
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
		
		try { // for error handling
			switch (LA(1)) {
				case YEAR: {
					AST tmp533_AST = null;
					tmp533_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp533_AST);
					match(YEAR);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case YEARS: {
					AST tmp534_AST = null;
					tmp534_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp534_AST);
					match(YEARS);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case MONTH: {
					AST tmp535_AST = null;
					tmp535_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp535_AST);
					match(MONTH);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case MONTHS: {
					AST tmp536_AST = null;
					tmp536_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp536_AST);
					match(MONTHS);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case WEEK: {
					AST tmp537_AST = null;
					tmp537_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp537_AST);
					match(WEEK);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case WEEKS: {
					AST tmp538_AST = null;
					tmp538_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp538_AST);
					match(WEEKS);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case DAY: {
					AST tmp539_AST = null;
					tmp539_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp539_AST);
					match(DAY);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case DAYS: {
					AST tmp540_AST = null;
					tmp540_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp540_AST);
					match(DAYS);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_hour: {
					AST tmp541_AST = null;
					tmp541_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp541_AST);
					match(LITERAL_hour);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_hours: {
					AST tmp542_AST = null;
					tmp542_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp542_AST);
					match(LITERAL_hours);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_minute: {
					AST tmp543_AST = null;
					tmp543_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp543_AST);
					match(LITERAL_minute);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_minutes: {
					AST tmp544_AST = null;
					tmp544_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp544_AST);
					match(LITERAL_minutes);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case SECOND: {
					AST tmp545_AST = null;
					tmp545_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp545_AST);
					match(SECOND);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_seconds: {
					AST tmp546_AST = null;
					tmp546_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp546_AST);
					match(LITERAL_seconds);
					duration_op_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_46);
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
		
		try { // for error handling
			AST tmp547_AST = null;
			tmp547_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp547_AST);
			match(THE);
			the_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_47);
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
		
		try { // for error handling
			expr_plus();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop304: do {
					if ((LA(1) == ACTION_OP)) {
						AST tmp548_AST = null;
						tmp548_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp548_AST);
						match(ACTION_OP);
						expr_plus();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop304;
					}
					
				} while (true);
			}
			expr_string_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_48);
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
		
		try { // for error handling
			switch (LA(1)) {
				case IS: {
					AST tmp549_AST = null;
					tmp549_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp549_AST);
					match(IS);
					is_AST = (AST) currentAST.root;
					break;
				}
				case ARE: {
					AST tmp550_AST = null;
					tmp550_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp550_AST);
					match(ARE);
					is_AST = (AST) currentAST.root;
					break;
				}
				case WERE: {
					AST tmp551_AST = null;
					tmp551_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp551_AST);
					match(WERE);
					is_AST = (AST) currentAST.root;
					break;
				}
				case WAS: {
					AST tmp552_AST = null;
					tmp552_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp552_AST);
					match(WAS);
					is_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_49);
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
		
		try { // for error handling
			switch (LA(1)) {
				case ANY:
				case ID:
				case LPAREN: {
					event_or();
					astFactory.addASTChild(currentAST, returnAST);
					evoke_statement_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_CALL: {
					AST tmp553_AST = null;
					tmp553_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp553_AST);
					match(LITERAL_CALL);
					evoke_statement_AST = (AST) currentAST.root;
					break;
				}
				default:
					if ((LA(1) == ENDBLOCK)) {
						evoke_statement_AST = (AST) currentAST.root;
					} else if ((LA(1) == ENDBLOCK)) {
						evoke_statement_AST = (AST) currentAST.root;
					} else {
						throw new NoViableAltException(LT(1), getFilename());
					}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_24);
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
		
		try { // for error handling
			event_any();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop212: do {
					if ((LA(1) == OR)) {
						AST tmp554_AST = null;
						tmp554_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp554_AST);
						match(OR);
						event_any();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop212;
					}
					
				} while (true);
			}
			event_or_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_50);
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
		
		try { // for error handling
			switch (LA(1)) {
				case ANY: {
					AST tmp555_AST = null;
					tmp555_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp555_AST);
					match(ANY);
					AST tmp556_AST = null;
					tmp556_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp556_AST);
					match(LPAREN);
					event_list();
					astFactory.addASTChild(currentAST, returnAST);
					AST tmp557_AST = null;
					tmp557_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp557_AST);
					match(RPAREN);
					event_any_AST = (AST) currentAST.root;
					break;
				}
				case ID:
				case LPAREN: {
					event_factor();
					astFactory.addASTChild(currentAST, returnAST);
					event_any_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_51);
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
		
		try { // for error handling
			event_or();
			astFactory.addASTChild(currentAST, returnAST);
			{
				AST tmp558_AST = null;
				tmp558_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp558_AST);
				match(COMMA);
				event_or();
				astFactory.addASTChild(currentAST, returnAST);
			}
			event_list_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_52);
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
		
		try { // for error handling
			switch (LA(1)) {
				case LPAREN: {
					AST tmp559_AST = null;
					tmp559_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp559_AST);
					match(LPAREN);
					event_or();
					astFactory.addASTChild(currentAST, returnAST);
					AST tmp560_AST = null;
					tmp560_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp560_AST);
					match(RPAREN);
					event_factor_AST = (AST) currentAST.root;
					break;
				}
				case ID: {
					AST tmp561_AST = null;
					tmp561_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp561_AST);
					match(ID);
					event_factor_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_51);
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
		
		try { // for error handling
			{
				_loop222: do {
					switch (LA(1)) {
						case CONCLUDE: {
							conclude_statement();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						case ID:
						case ACTION_OP: {
							logic_assignment();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						case CALL: {
							AST tmp562_AST = null;
							tmp562_AST = astFactory.create(LT(1));
							astFactory.makeASTRoot(currentAST, tmp562_AST);
							match(CALL);
							call_phrase();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						default: {
							break _loop222;
						}
					}
				} while (true);
			}
			logic_statement_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_32);
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
		
		try { // for error handling
			AST tmp563_AST = null;
			tmp563_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp563_AST);
			match(IF);
			logic_if_then_else2();
			astFactory.addASTChild(currentAST, returnAST);
			if_statement_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_53);
			} else {
				throw ex;
			}
		}
		returnAST = if_statement_AST;
	}
	
	public final void logic_elseif() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_elseif_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case ELSE: {
					AST tmp564_AST = null;
					tmp564_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp564_AST);
					match(ELSE);
					logic_elseif_AST = (AST) currentAST.root;
					break;
				}
				case ELSEIF: {
					AST tmp565_AST = null;
					tmp565_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp565_AST);
					match(ELSEIF);
					logic_if_then_else2();
					astFactory.addASTChild(currentAST, returnAST);
					logic_elseif_AST = (AST) currentAST.root;
					break;
				}
				case ENDIF: {
					{
						AST tmp566_AST = null;
						tmp566_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp566_AST);
						match(ENDIF);
					}
					logic_elseif_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_53);
			} else {
				throw ex;
			}
		}
		returnAST = logic_elseif_AST;
	}
	
	public final void conclude_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST conclude_statement_AST = null;
		
		try { // for error handling
			{
				AST tmp567_AST = null;
				tmp567_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp567_AST);
				match(CONCLUDE);
			}
			boolean_value();
			astFactory.addASTChild(currentAST, returnAST);
			conclude_statement_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_42);
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
		
		try { // for error handling
			{
				switch (LA(1)) {
					case ACTION_OP: {
						AST tmp568_AST = null;
						tmp568_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp568_AST);
						match(ACTION_OP);
						break;
					}
					case ID: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			identifier_or_object_ref();
			astFactory.addASTChild(currentAST, returnAST);
			{
				switch (LA(1)) {
					case ACTION_OP: {
						AST tmp569_AST = null;
						tmp569_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp569_AST);
						match(ACTION_OP);
						break;
					}
					case BECOMES:
					case EQUALS: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			{
				switch (LA(1)) {
					case BECOMES: {
						match(BECOMES);
						break;
					}
					case EQUALS: {
						match(EQUALS);
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			{
				if ((_tokenSet_54.member(LA(1)))) {
					expr();
					astFactory.addASTChild(currentAST, returnAST);
				} else if ((LA(1) == CALL)) {
					{
						AST tmp572_AST = null;
						tmp572_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp572_AST);
						match(CALL);
					}
					call_phrase();
					astFactory.addASTChild(currentAST, returnAST);
				} else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
			}
			logic_assignment_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_42);
			} else {
				throw ex;
			}
		}
		returnAST = logic_assignment_AST;
	}
	
	public final void logic_expr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logic_expr_AST = null;
		
		try { // for error handling
			{
				switch (LA(1)) {
					case THE: {
						the();
						astFactory.addASTChild(currentAST, returnAST);
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
					case TRUE:
					case FALSE:
					case NULL:
					case INTLIT:
					case ID:
					case STRING_LITERAL:
					case LPAREN:
					case TERM_LITERAL: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			from_of_func_op();
			astFactory.addASTChild(currentAST, returnAST);
			expr_factor();
			astFactory.addASTChild(currentAST, returnAST);
			{
				switch (LA(1)) {
					case IS:
					case ARE:
					case WAS:
					case WERE: {
						is();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case IN:
					case LESS:
					case GREATER: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			binary_comp_op();
			astFactory.addASTChild(currentAST, returnAST);
			{
				switch (LA(1)) {
					case THE: {
						the();
						astFactory.addASTChild(currentAST, returnAST);
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
					case TRUE:
					case FALSE:
					case NULL:
					case INTLIT:
					case ID:
					case STRING_LITERAL:
					case LPAREN:
					case TERM_LITERAL: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			{
				if ((_tokenSet_55.member(LA(1)))) {
					expr_factor();
					astFactory.addASTChild(currentAST, returnAST);
				} else if ((_tokenSet_56.member(LA(1)))) {
					from_of_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					expr_factor();
					astFactory.addASTChild(currentAST, returnAST);
				} else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
			}
			logic_expr_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			expr_factor_atom();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop327: do {
					if ((LA(1) == DOT)) {
						AST tmp573_AST = null;
						tmp573_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp573_AST);
						match(DOT);
						expr_factor_atom();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop327;
					}
					
				} while (true);
			}
			expr_factor_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_57);
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
		
		try { // for error handling
			{
				switch (LA(1)) {
					case AND: {
						{
							AST tmp574_AST = null;
							tmp574_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp574_AST);
							match(AND);
						}
						break;
					}
					case OR: {
						{
							AST tmp575_AST = null;
							tmp575_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp575_AST);
							match(OR);
						}
						break;
					}
					case NOT: {
						{
							AST tmp576_AST = null;
							tmp576_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp576_AST);
							match(NOT);
						}
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			logic_condition_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			switch (LA(1)) {
				case EQUALS: {
					{
						AST tmp577_AST = null;
						tmp577_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp577_AST);
						match(EQUALS);
					}
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_EQ: {
					{
						AST tmp578_AST = null;
						tmp578_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp578_AST);
						match(LITERAL_EQ);
					}
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LT: {
					AST tmp579_AST = null;
					tmp579_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp579_AST);
					match(LT);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_LT: {
					AST tmp580_AST = null;
					tmp580_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp580_AST);
					match(LITERAL_LT);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case GT: {
					AST tmp581_AST = null;
					tmp581_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp581_AST);
					match(GT);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_GT: {
					AST tmp582_AST = null;
					tmp582_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp582_AST);
					match(LITERAL_GT);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LTE: {
					AST tmp583_AST = null;
					tmp583_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp583_AST);
					match(LTE);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_LE: {
					AST tmp584_AST = null;
					tmp584_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp584_AST);
					match(LITERAL_LE);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case GTE: {
					AST tmp585_AST = null;
					tmp585_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp585_AST);
					match(GTE);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_GE: {
					AST tmp586_AST = null;
					tmp586_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp586_AST);
					match(LITERAL_GE);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case NE: {
					AST tmp587_AST = null;
					tmp587_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp587_AST);
					match(NE);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_NE: {
					AST tmp588_AST = null;
					tmp588_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp588_AST);
					match(LITERAL_NE);
					simple_comp_op_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_45);
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
		
		try { // for error handling
			binary_comp_op();
			astFactory.addASTChild(currentAST, returnAST);
			expr_string();
			astFactory.addASTChild(currentAST, returnAST);
			main_comp_op_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_58);
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
		
		try { // for error handling
			{
				expr();
				astFactory.addASTChild(currentAST, returnAST);
			}
			AST tmp589_AST = null;
			tmp589_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp589_AST);
			match(THEN);
			logic_if_then_else2_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_53);
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
		
		try { // for error handling
			switch (LA(1)) {
				case TRUE: {
					{
						AST tmp590_AST = null;
						tmp590_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp590_AST);
						match(TRUE);
					}
					boolean_value_AST = (AST) currentAST.root;
					break;
				}
				case FALSE: {
					{
						AST tmp591_AST = null;
						tmp591_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp591_AST);
						match(FALSE);
					}
					boolean_value_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_59);
			} else {
				throw ex;
			}
		}
		returnAST = boolean_value_AST;
	}
	
	public final void action_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST action_statement_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case WRITE: {
					{
						AST tmp592_AST = null;
						tmp592_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp592_AST);
						match(WRITE);
					}
					{
						{
							if ((LA(1) == LPAREN)) {
								match(LPAREN);
							} else if ((_tokenSet_60.member(LA(1)))) {} else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							
						}
						{
							if ((_tokenSet_61.member(LA(1)))) {
								{
									_loop261: do {
										if ((LA(1) == ACTION_OP)) {
											AST tmp594_AST = null;
											tmp594_AST = astFactory.create(LT(1));
											astFactory.addASTChild(currentAST, tmp594_AST);
											match(ACTION_OP);
											expr_factor();
											astFactory.addASTChild(currentAST, returnAST);
										} else {
											break _loop261;
										}
										
									} while (true);
								}
							} else if ((_tokenSet_62.member(LA(1)))) {
								expr();
								astFactory.addASTChild(currentAST, returnAST);
							} else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							
						}
						{
							switch (LA(1)) {
								case RPAREN: {
									match(RPAREN);
									break;
								}
								case AT:
								case SEMI: {
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
					}
					{
						switch (LA(1)) {
							case AT: {
								{
									AST tmp596_AST = null;
									tmp596_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp596_AST);
									match(AT);
								}
								AST tmp597_AST = null;
								tmp597_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp597_AST);
								match(ID);
								break;
							}
							case SEMI: {
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					action_statement_AST = (AST) currentAST.root;
					break;
				}
				case SEMI: {
					action_statement_AST = (AST) currentAST.root;
					break;
				}
				case LET:
				case NOW:
				case CALL:
				case ID: {
					{
						switch (LA(1)) {
							case LET:
							case NOW:
							case ID: {
								identifier_becomes();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							case CALL: {
								break;
							}
							default: {
								throw new NoViableAltException(LT(1), getFilename());
							}
						}
					}
					{
						{
							AST tmp598_AST = null;
							tmp598_AST = astFactory.create(LT(1));
							astFactory.makeASTRoot(currentAST, tmp598_AST);
							match(CALL);
						}
						call_phrase();
						astFactory.addASTChild(currentAST, returnAST);
					}
					action_statement_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_32);
			} else {
				throw ex;
			}
		}
		returnAST = action_statement_AST;
	}
	
	public final void action_if_then_else2() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST action_if_then_else2_AST = null;
		
		try { // for error handling
			{
				expr();
				astFactory.addASTChild(currentAST, returnAST);
			}
			AST tmp599_AST = null;
			tmp599_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp599_AST);
			match(THEN);
			action_if_then_else2_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
			} else {
				throw ex;
			}
		}
		returnAST = action_if_then_else2_AST;
	}
	
	public final void urgency_val() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST urgency_val_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case INTLIT: {
					AST tmp600_AST = null;
					tmp600_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp600_AST);
					match(INTLIT);
					urgency_val_AST = (AST) currentAST.root;
					break;
				}
				case ENDBLOCK: {
					urgency_val_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_24);
			} else {
				throw ex;
			}
		}
		returnAST = urgency_val_AST;
	}
	
	public final void age_code() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST age_code_AST = null;
		
		try { // for error handling
			switch (LA(1)) {
				case DAYS: {
					AST tmp601_AST = null;
					tmp601_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp601_AST);
					match(DAYS);
					age_code_AST = (AST) currentAST.root;
					break;
				}
				case WEEKS: {
					AST tmp602_AST = null;
					tmp602_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp602_AST);
					match(WEEKS);
					age_code_AST = (AST) currentAST.root;
					break;
				}
				case MONTHS: {
					AST tmp603_AST = null;
					tmp603_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp603_AST);
					match(MONTHS);
					age_code_AST = (AST) currentAST.root;
					break;
				}
				case YEARS: {
					AST tmp604_AST = null;
					tmp604_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp604_AST);
					match(YEARS);
					age_code_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_24);
			} else {
				throw ex;
			}
		}
		returnAST = age_code_AST;
	}
	
	public final void expr_sort() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_sort_AST = null;
		
		try { // for error handling
			expr_where();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop283: do {
					if ((LA(1) == LITERAL_MERGE || LA(1) == LITERAL_SORT)) {
						{
							switch (LA(1)) {
								case LITERAL_MERGE: {
									AST tmp605_AST = null;
									tmp605_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp605_AST);
									match(LITERAL_MERGE);
									break;
								}
								case LITERAL_SORT: {
									{
										AST tmp606_AST = null;
										tmp606_AST = astFactory.create(LT(1));
										astFactory.addASTChild(currentAST, tmp606_AST);
										match(LITERAL_SORT);
										{
											if ((LA(1) == TIME || LA(1) == LITERAL_DATA)) {
												sort_option();
												astFactory.addASTChild(currentAST, returnAST);
											} else if ((_tokenSet_63.member(LA(1)))) {} else {
												throw new NoViableAltException(LT(1), getFilename());
											}
											
										}
									}
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
						expr_where();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop283;
					}
					
				} while (true);
			}
			expr_sort_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_64);
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
		
		try { // for error handling
			if ((_tokenSet_65.member(LA(1)))) {
				expr_range();
				astFactory.addASTChild(currentAST, returnAST);
				{
					_loop287: do {
						if ((LA(1) == WHERE)) {
							AST tmp607_AST = null;
							tmp607_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp607_AST);
							match(WHERE);
							expr_range();
							astFactory.addASTChild(currentAST, returnAST);
						} else {
							break _loop287;
						}
						
					} while (true);
				}
				expr_where_AST = (AST) currentAST.root;
			} else if ((_tokenSet_66.member(LA(1)))) {
				expr_where_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_66);
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
		
		try { // for error handling
			switch (LA(1)) {
				case TIME: {
					AST tmp608_AST = null;
					tmp608_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp608_AST);
					match(TIME);
					sort_option_AST = (AST) currentAST.root;
					break;
				}
				case LITERAL_DATA: {
					AST tmp609_AST = null;
					tmp609_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp609_AST);
					match(LITERAL_DATA);
					sort_option_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_63);
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
		
		try { // for error handling
			expr_or();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop290: do {
					if ((LA(1) == LITERAL_SEQTO)) {
						AST tmp610_AST = null;
						tmp610_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp610_AST);
						match(LITERAL_SEQTO);
						expr_or();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop290;
					}
					
				} while (true);
			}
			expr_range_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_67);
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
		
		try { // for error handling
			expr_and();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop293: do {
					if ((LA(1) == OR)) {
						AST tmp611_AST = null;
						tmp611_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp611_AST);
						match(OR);
						expr_and();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop293;
					}
					
				} while (true);
			}
			expr_or_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_68);
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
		
		try { // for error handling
			expr_not();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop296: do {
					if ((LA(1) == AND)) {
						AST tmp612_AST = null;
						tmp612_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp612_AST);
						match(AND);
						expr_not();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop296;
					}
					
				} while (true);
			}
			expr_and_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_69);
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
		
		try { // for error handling
			switch (LA(1)) {
				case COUNT:
				case THE:
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
				case NULL:
				case INTLIT:
				case ID:
				case STRING_LITERAL:
				case LPAREN:
				case TERM_LITERAL: {
					expr_comparison();
					astFactory.addASTChild(currentAST, returnAST);
					expr_not_AST = (AST) currentAST.root;
					break;
				}
				case NOT: {
					AST tmp613_AST = null;
					tmp613_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp613_AST);
					match(NOT);
					expr_comparison();
					astFactory.addASTChild(currentAST, returnAST);
					expr_not_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_58);
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
		
		try { // for error handling
			expr_string();
			astFactory.addASTChild(currentAST, returnAST);
			{
				switch (LA(1)) {
					case GT:
					case GTE:
					case LT:
					case LTE:
					case EQUALS:
					case LITERAL_EQ:
					case LITERAL_LT:
					case LITERAL_GT:
					case LITERAL_LE:
					case LITERAL_GE:
					case NE:
					case LITERAL_NE: {
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
					case WERE: {
						{
							is();
							main_comp_op();
							astFactory.addASTChild(currentAST, returnAST);
						}
						break;
					}
					case AND:
					case AT:
					case OR:
					case THEN:
					case WHERE:
					case CONCLUDE:
					case CALL:
					case SEMI:
					case ID:
					case COMMA:
					case RPAREN:
					case ACTION_OP:
					case LITERAL_MERGE:
					case LITERAL_SORT:
					case LITERAL_SEQTO: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			expr_comparison_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_58);
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
		
		try { // for error handling
			expr_times();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop308: do {
					if ((LA(1) == 178 || LA(1) == 179)) {
						{
							switch (LA(1)) {
								case 178: {
									AST tmp614_AST = null;
									tmp614_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp614_AST);
									match(178);
									break;
								}
								case 179: {
									AST tmp615_AST = null;
									tmp615_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp615_AST);
									match(179);
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
						expr_times();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop308;
					}
					
				} while (true);
			}
			expr_plus_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_48);
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
		
		try { // for error handling
			expr_power();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop312: do {
					if ((LA(1) == 180 || LA(1) == 181)) {
						{
							switch (LA(1)) {
								case 180: {
									AST tmp616_AST = null;
									tmp616_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp616_AST);
									match(180);
									break;
								}
								case 181: {
									AST tmp617_AST = null;
									tmp617_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp617_AST);
									match(181);
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
						expr_times();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop312;
					}
					
				} while (true);
			}
			expr_times_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_70);
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
		
		try { // for error handling
			expr_duration();
			astFactory.addASTChild(currentAST, returnAST);
			{
				_loop316: do {
					if (((LA(1) >= FROM && LA(1) <= AFTER))) {
						{
							switch (LA(1)) {
								case BEFORE: {
									AST tmp618_AST = null;
									tmp618_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp618_AST);
									match(BEFORE);
									break;
								}
								case AFTER: {
									AST tmp619_AST = null;
									tmp619_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp619_AST);
									match(AFTER);
									break;
								}
								case FROM: {
									AST tmp620_AST = null;
									tmp620_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp620_AST);
									match(FROM);
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
						expr_duration();
						astFactory.addASTChild(currentAST, returnAST);
					} else {
						break _loop316;
					}
					
				} while (true);
			}
			{
				switch (LA(1)) {
					case AGO: {
						AST tmp621_AST = null;
						tmp621_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp621_AST);
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
					case CALL:
					case SEMI:
					case ID:
					case GT:
					case GTE:
					case LT:
					case LTE:
					case COMMA:
					case LPAREN:
					case RPAREN:
					case ARDEN_CURLY_BRACKETS:
					case EQUALS:
					case LITERAL_EQ:
					case LITERAL_LT:
					case LITERAL_GT:
					case LITERAL_LE:
					case LITERAL_GE:
					case NE:
					case LITERAL_NE:
					case ACTION_OP:
					case LITERAL_MERGE:
					case LITERAL_SORT:
					case LITERAL_SEQTO:
					case 178:
					case 179:
					case 180:
					case 181: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			expr_power_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_70);
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
		
		try { // for error handling
			expr_function();
			astFactory.addASTChild(currentAST, returnAST);
			{
				switch (LA(1)) {
					case YEAR:
					case YEARS:
					case DAYS:
					case DAY:
					case MONTH:
					case MONTHS:
					case WEEK:
					case WEEKS:
					case SECOND:
					case LITERAL_hour:
					case LITERAL_hours:
					case LITERAL_minute:
					case LITERAL_minutes:
					case LITERAL_seconds: {
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
					case CALL:
					case SEMI:
					case ID:
					case GT:
					case GTE:
					case LT:
					case LTE:
					case COMMA:
					case LPAREN:
					case RPAREN:
					case ARDEN_CURLY_BRACKETS:
					case EQUALS:
					case LITERAL_EQ:
					case LITERAL_LT:
					case LITERAL_GT:
					case LITERAL_LE:
					case LITERAL_GE:
					case NE:
					case LITERAL_NE:
					case ACTION_OP:
					case LITERAL_MERGE:
					case LITERAL_SORT:
					case LITERAL_SEQTO:
					case 178:
					case 179:
					case 180:
					case 181: {
						break;
					}
					default: {
						throw new NoViableAltException(LT(1), getFilename());
					}
				}
			}
			expr_duration_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_46);
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
		
		try { // for error handling
			if ((_tokenSet_55.member(LA(1)))) {
				expr_factor();
				astFactory.addASTChild(currentAST, returnAST);
				expr_function_AST = (AST) currentAST.root;
			} else if ((_tokenSet_45.member(LA(1)))) {
				{
					switch (LA(1)) {
						case THE: {
							the();
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
						case TIME:
						case ANY:
						case NULL:
						case INTLIT:
						case ID:
						case STRING_LITERAL:
						case LPAREN:
						case TERM_LITERAL: {
							break;
						}
						default: {
							throw new NoViableAltException(LT(1), getFilename());
						}
					}
				}
				{
					switch (LA(1)) {
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
						case NULL:
						case INTLIT:
						case ID:
						case STRING_LITERAL:
						case LPAREN:
						case TERM_LITERAL: {
							from_of_func_op();
							astFactory.addASTChild(currentAST, returnAST);
							{
								switch (LA(1)) {
									case OF: {
										match(OF);
										break;
									}
									case TRUE:
									case FALSE:
									case NULL:
									case INTLIT:
									case ID:
									case STRING_LITERAL:
									case LPAREN:
									case TERM_LITERAL: {
										break;
									}
									default: {
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
						case ANY: {
							of_func_op();
							astFactory.addASTChild(currentAST, returnAST);
							{
								switch (LA(1)) {
									case OF: {
										match(OF);
										break;
									}
									case TRUE:
									case FALSE:
									case NULL:
									case INTLIT:
									case ID:
									case STRING_LITERAL:
									case LPAREN:
									case TERM_LITERAL: {
										break;
									}
									default: {
										throw new NoViableAltException(LT(1), getFilename());
									}
								}
							}
							expr_factor();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						default: {
							throw new NoViableAltException(LT(1), getFilename());
						}
					}
				}
				expr_function_AST = (AST) currentAST.root;
			} else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_71);
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
		
		try { // for error handling
			switch (LA(1)) {
				case COUNT:
				case EXIST:
				case EXISTS:
				case AVG:
				case AVERAGE:
				case SUM:
				case MEDIAN: {
					of_read_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					of_func_op_AST = (AST) currentAST.root;
					break;
				}
				case TIME:
				case ANY: {
					of_noread_func_op();
					astFactory.addASTChild(currentAST, returnAST);
					of_func_op_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_72);
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
		
		try { // for error handling
			switch (LA(1)) {
				case ID: {
					AST tmp624_AST = null;
					tmp624_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp624_AST);
					match(ID);
					expr_factor_atom_AST = (AST) currentAST.root;
					break;
				}
				case LPAREN: {
					match(LPAREN);
					expr();
					astFactory.addASTChild(currentAST, returnAST);
					match(RPAREN);
					expr_factor_atom_AST = (AST) currentAST.root;
					break;
				}
				case INTLIT: {
					{
						AST tmp627_AST = null;
						tmp627_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp627_AST);
						match(INTLIT);
						{
							switch (LA(1)) {
								case MINUS: {
									{
										int _cnt332 = 0;
										_loop332: do {
											if ((LA(1) == MINUS)) {
												AST tmp628_AST = null;
												tmp628_AST = astFactory.create(LT(1));
												astFactory.makeASTRoot(currentAST, tmp628_AST);
												match(MINUS);
												AST tmp629_AST = null;
												tmp629_AST = astFactory.create(LT(1));
												astFactory.addASTChild(currentAST, tmp629_AST);
												match(INTLIT);
											} else {
												if (_cnt332 >= 1) {
													break _loop332;
												} else {
													throw new NoViableAltException(LT(1), getFilename());
												}
											}
											
											_cnt332++;
										} while (true);
									}
									break;
								}
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
								case CALL:
								case SECOND:
								case DOT:
								case SEMI:
								case ID:
								case GT:
								case GTE:
								case LT:
								case LTE:
								case COMMA:
								case LPAREN:
								case RPAREN:
								case ARDEN_CURLY_BRACKETS:
								case LITERAL_hour:
								case LITERAL_hours:
								case LITERAL_minute:
								case LITERAL_minutes:
								case LITERAL_seconds:
								case EQUALS:
								case LITERAL_EQ:
								case LITERAL_LT:
								case LITERAL_GT:
								case LITERAL_LE:
								case LITERAL_GE:
								case NE:
								case LITERAL_NE:
								case ACTION_OP:
								case LITERAL_MERGE:
								case LITERAL_SORT:
								case LITERAL_SEQTO:
								case 178:
								case 179:
								case 180:
								case 181: {
									break;
								}
								default: {
									throw new NoViableAltException(LT(1), getFilename());
								}
							}
						}
					}
					expr_factor_atom_AST = (AST) currentAST.root;
					break;
				}
				case TRUE:
				case FALSE: {
					boolean_value();
					astFactory.addASTChild(currentAST, returnAST);
					expr_factor_atom_AST = (AST) currentAST.root;
					break;
				}
				case STRING_LITERAL: {
					AST tmp630_AST = null;
					tmp630_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp630_AST);
					match(STRING_LITERAL);
					expr_factor_atom_AST = (AST) currentAST.root;
					break;
				}
				case TERM_LITERAL: {
					AST tmp631_AST = null;
					tmp631_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp631_AST);
					match(TERM_LITERAL);
					expr_factor_atom_AST = (AST) currentAST.root;
					break;
				}
				case NULL: {
					AST tmp632_AST = null;
					tmp632_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp632_AST);
					match(NULL);
					expr_factor_atom_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_59);
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
		
		try { // for error handling
			AST tmp633_AST = null;
			tmp633_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp633_AST);
			match(LITERAL_NUMBER);
			as_func_op_AST = (AST) currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_0);
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
		
		try { // for error handling
			switch (LA(1)) {
				case TIME: {
					AST tmp634_AST = null;
					tmp634_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp634_AST);
					match(TIME);
					of_noread_func_op_AST = (AST) currentAST.root;
					break;
				}
				case ANY: {
					AST tmp635_AST = null;
					tmp635_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp635_AST);
					match(ANY);
					of_noread_func_op_AST = (AST) currentAST.root;
					break;
				}
				default: {
					throw new NoViableAltException(LT(1), getFilename());
				}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing == 0) {
				reportError(ex);
				recover(ex, _tokenSet_72);
			} else {
				throw ex;
			}
		}
		returnAST = of_noread_func_op_AST;
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
	        "TERM_LITERAL", "WS", "LBRACKET", "RBRACKET", "DOTDOT", "NOT_EQUALS", "QUESTION", "LCURLY", "RCURLY" };
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap = null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	private static final long[] mk_tokenSet_1() {
		long[] data = { 0L, 16L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	private static final long[] mk_tokenSet_2() {
		long[] data = { 0L, 8L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	private static final long[] mk_tokenSet_3() {
		long[] data = { 0L, 68719476736L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	
	private static final long[] mk_tokenSet_4() {
		long[] data = { -6896136937799728L, -2299066152120212465L, 1L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	
	private static final long[] mk_tokenSet_5() {
		long[] data = { 0L, 4398046513152L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	
	private static final long[] mk_tokenSet_6() {
		long[] data = { 0L, 2048L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	
	private static final long[] mk_tokenSet_7() {
		long[] data = { 0L, 256L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	
	private static final long[] mk_tokenSet_8() {
		long[] data = { 0L, 512L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	
	private static final long[] mk_tokenSet_9() {
		long[] data = { 0L, 4096L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	
	private static final long[] mk_tokenSet_10() {
		long[] data = { 0L, 524288L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	
	private static final long[] mk_tokenSet_11() {
		long[] data = { 0L, 140737488355328L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	
	private static final long[] mk_tokenSet_12() {
		long[] data = { 0L, 196616L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	
	private static final long[] mk_tokenSet_13() {
		long[] data = { 0L, 131080L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	
	private static final long[] mk_tokenSet_14() {
		long[] data = { -9223372036854775808L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	
	private static final long[] mk_tokenSet_15() {
		long[] data = { 2017612633208782848L, 4573968371548160L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	
	private static final long[] mk_tokenSet_16() {
		long[] data = { 0L, 1024L, 17179869184L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	
	private static final long[] mk_tokenSet_17() {
		long[] data = { 0L, 1L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	
	private static final long[] mk_tokenSet_18() {
		long[] data = { 0L, 2L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	
	private static final long[] mk_tokenSet_19() {
		long[] data = { 0L, 68722622464L, 35184372088832L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	
	private static final long[] mk_tokenSet_20() {
		long[] data = { 0L, 68722622464L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	
	private static final long[] mk_tokenSet_21() {
		long[] data = { 0L, 68721573888L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	
	private static final long[] mk_tokenSet_22() {
		long[] data = { -6896136937799728L, 2251816989369359L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	
	private static final long[] mk_tokenSet_23() {
		long[] data = { -6896136937799726L, -2298990973012661233L, 65L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	
	private static final long[] mk_tokenSet_24() {
		long[] data = { 0L, 274877906944L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	
	private static final long[] mk_tokenSet_25() {
		long[] data = { 2L, 274877906944L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	
	private static final long[] mk_tokenSet_26() {
		long[] data = { -6896136937799726L, -2298990973012661233L, 67L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	
	private static final long[] mk_tokenSet_27() {
		long[] data = { -6896136937799728L, -2298995371059174385L, 1L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	
	private static final long[] mk_tokenSet_28() {
		long[] data = { 0L, 70643622084608L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	
	private static final long[] mk_tokenSet_29() {
		long[] data = { 2017612633208782848L, 4574243249455104L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	
	private static final long[] mk_tokenSet_30() {
		long[] data = { 135160765379249152L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	
	private static final long[] mk_tokenSet_31() {
		long[] data = { 4380866641920L, 17592186044416L, 4097L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	
	private static final long[] mk_tokenSet_32() {
		long[] data = { 0L, 70368744177664L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	
	private static final long[] mk_tokenSet_33() {
		long[] data = { 7052694179255698432L, -6913007801186910208L, 18225504742014977L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	
	private static final long[] mk_tokenSet_34() {
		long[] data = { 144115192372920320L, 4573968405102592L, 17592186044418L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	
	private static final long[] mk_tokenSet_35() {
		long[] data = { 7052698581597176832L, -6912937432409178112L, 18225504742014977L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	
	private static final long[] mk_tokenSet_36() {
		long[] data = { 6917529027641081856L, -9218850810677428224L, 18014398509486081L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	
	private static final long[] mk_tokenSet_37() {
		long[] data = { 13178033799170L, 70368760954880L, 4099L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	
	private static final long[] mk_tokenSet_38() {
		long[] data = { 805306368L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	
	private static final long[] mk_tokenSet_39() {
		long[] data = { 0L, 2147483648L, 17045651456L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	
	private static final long[] mk_tokenSet_40() {
		long[] data = { 1074135040L, 16777216L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	
	private static final long[] mk_tokenSet_41() {
		long[] data = { 4380866641922L, 70368744177664L, 4099L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	
	private static final long[] mk_tokenSet_42() {
		long[] data = { 144115188075855872L, 4573968405102592L, 17592186044416L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	
	private static final long[] mk_tokenSet_43() {
		long[] data = { 0L, 0L, 17660905523200L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	
	private static final long[] mk_tokenSet_44() {
		long[] data = { 4380866641922L, 0L, 4097L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	
	private static final long[] mk_tokenSet_45() {
		long[] data = { 7052694173886989312L, -9218850810400604160L, 18014398509481985L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	
	private static final long[] mk_tokenSet_46() {
		long[] data = { 144128371481052114L, 3391280888187715584L, 17697670441209859L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	
	private static final long[] mk_tokenSet_47() {
		long[] data = { 7052764542631150592L, -9218850810400604160L, 18014398509481985L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	
	private static final long[] mk_tokenSet_48() {
		long[] data = { 144128371480069074L, 3391280888187715584L, 809171838570499L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	
	private static final long[] mk_tokenSet_49() {
		long[] data = { 14336L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	
	private static final long[] mk_tokenSet_50() {
		long[] data = { 0L, 2305843284091600896L, 2L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	
	private static final long[] mk_tokenSet_51() {
		long[] data = { 2147483648L, 2305843284091600896L, 2L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	
	private static final long[] mk_tokenSet_52() {
		long[] data = { 0L, 0L, 2L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	
	private static final long[] mk_tokenSet_53() {
		long[] data = { 2161727821285687296L, 4574243283009536L, 17592186044416L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	
	private static final long[] mk_tokenSet_54() {
		long[] data = { 7196809363036587008L, -6912937432409178112L, 18243096928059393L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	
	private static final long[] mk_tokenSet_55() {
		long[] data = { 6917529027641081856L, -9218850810681622528L, 18014398509481985L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	
	private static final long[] mk_tokenSet_56() {
		long[] data = { 6917533408507723776L, -9218850810681622528L, 18014398509481985L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	
	private static final long[] mk_tokenSet_57() {
		long[] data = { 152994833348115410L, 3391280889261457408L, 17697670571233283L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	
	private static final long[] mk_tokenSet_58() {
		long[] data = { 144123990613426192L, 2310416977618796544L, 791648371998722L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	
	private static final long[] mk_tokenSet_59() {
		long[] data = { 152994833348115410L, 3391281439017271296L, 17697670571233283L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	
	private static final long[] mk_tokenSet_60() {
		long[] data = { 7052694174962828288L, -6912937432442732544L, 18243096928059395L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	
	private static final long[] mk_tokenSet_61() {
		long[] data = { 2097152L, 70368744177664L, 17592186044418L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	
	private static final long[] mk_tokenSet_62() {
		long[] data = { 7052694174962828288L, -6912937432442732544L, 18225504742014979L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	
	private static final long[] mk_tokenSet_63() {
		long[] data = { 7196809367333651456L, -6912937432409178112L, 18243096928059395L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	
	private static final long[] mk_tokenSet_64() {
		long[] data = { 144115192372920320L, 2310416977618796544L, 17592186044418L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	
	private static final long[] mk_tokenSet_65() {
		long[] data = { 7052694174960731136L, -9218850810400604160L, 18014398509481985L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	
	private static final long[] mk_tokenSet_66() {
		long[] data = { 144115192372920320L, 2310416977618796544L, 228698418577410L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	
	private static final long[] mk_tokenSet_67() {
		long[] data = { 144123988465942528L, 2310416977618796544L, 228698418577410L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	
	private static final long[] mk_tokenSet_68() {
		long[] data = { 144123988465942528L, 2310416977618796544L, 791648371998722L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	
	private static final long[] mk_tokenSet_69() {
		long[] data = { 144123990613426176L, 2310416977618796544L, 791648371998722L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	
	private static final long[] mk_tokenSet_70() {
		long[] data = { 144128371480069074L, 3391280888187715584L, 17697670441209859L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	
	private static final long[] mk_tokenSet_71() {
		long[] data = { 152994833348101074L, 3391280889261457408L, 17697670571233283L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	
	private static final long[] mk_tokenSet_72() {
		long[] data = { 6917529027641081856L, -9218850810677428224L, 18014398509481985L, 0L, 0L, 0L };
		return data;
	}
	
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	
}
