// $ANTLR 2.7.6 (2005-12-22): "LogicQueryParser.g" -> "LogicQueryBaseParser.java"$

package org.openmrs.logic.queryparser;

import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.collections.impl.BitSet;

public class LogicQueryBaseParser extends antlr.LLkParser       implements LogicQueryBaseParserTokenTypes
 {

protected LogicQueryBaseParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public LogicQueryBaseParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected LogicQueryBaseParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public LogicQueryBaseParser(TokenStream lexer) {
  this(lexer,1);
}

public LogicQueryBaseParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void any_reserved_word() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST any_reserved_word_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case AND:
			{
				AST tmp1_AST = null;
				tmp1_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp1_AST);
				match(AND);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IS:
			{
				AST tmp2_AST = null;
				tmp2_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp2_AST);
				match(IS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ARE:
			{
				AST tmp3_AST = null;
				tmp3_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp3_AST);
				match(ARE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WAS:
			{
				AST tmp4_AST = null;
				tmp4_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp4_AST);
				match(WAS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WERE:
			{
				AST tmp5_AST = null;
				tmp5_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp5_AST);
				match(WERE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			{
				AST tmp6_AST = null;
				tmp6_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp6_AST);
				match(COUNT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IN:
			{
				AST tmp7_AST = null;
				tmp7_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp7_AST);
				match(IN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LESS:
			{
				AST tmp8_AST = null;
				tmp8_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp8_AST);
				match(LESS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THE:
			{
				AST tmp9_AST = null;
				tmp9_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp9_AST);
				match(THE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THAN:
			{
				AST tmp10_AST = null;
				tmp10_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp10_AST);
				match(THAN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FROM:
			{
				AST tmp11_AST = null;
				tmp11_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp11_AST);
				match(FROM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case BEFORE:
			{
				AST tmp12_AST = null;
				tmp12_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp12_AST);
				match(BEFORE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AFTER:
			{
				AST tmp13_AST = null;
				tmp13_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp13_AST);
				match(AFTER);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AGO:
			{
				AST tmp14_AST = null;
				tmp14_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp14_AST);
				match(AGO);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AT:
			{
				AST tmp15_AST = null;
				tmp15_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp15_AST);
				match(AT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case OF:
			{
				AST tmp16_AST = null;
				tmp16_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp16_AST);
				match(OF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WRITE:
			{
				AST tmp17_AST = null;
				tmp17_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp17_AST);
				match(WRITE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case BE:
			{
				AST tmp18_AST = null;
				tmp18_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp18_AST);
				match(BE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LET:
			{
				AST tmp19_AST = null;
				tmp19_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp19_AST);
				match(LET);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case YEAR:
			{
				AST tmp20_AST = null;
				tmp20_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp20_AST);
				match(YEAR);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case YEARS:
			{
				AST tmp21_AST = null;
				tmp21_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp21_AST);
				match(YEARS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IF:
			{
				AST tmp22_AST = null;
				tmp22_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp22_AST);
				match(IF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case IT:
			{
				AST tmp23_AST = null;
				tmp23_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp23_AST);
				match(IT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THEY:
			{
				AST tmp24_AST = null;
				tmp24_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp24_AST);
				match(THEY);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case NOT:
			{
				AST tmp25_AST = null;
				tmp25_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp25_AST);
				match(NOT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case OR:
			{
				AST tmp26_AST = null;
				tmp26_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp26_AST);
				match(OR);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case THEN:
			{
				AST tmp27_AST = null;
				tmp27_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp27_AST);
				match(THEN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MONTH:
			{
				AST tmp28_AST = null;
				tmp28_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp28_AST);
				match(MONTH);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MONTHS:
			{
				AST tmp29_AST = null;
				tmp29_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp29_AST);
				match(MONTHS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TIME:
			{
				AST tmp30_AST = null;
				tmp30_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp30_AST);
				match(TIME);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TIMES:
			{
				AST tmp31_AST = null;
				tmp31_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp31_AST);
				match(TIMES);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WITHIN:
			{
				AST tmp32_AST = null;
				tmp32_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp32_AST);
				match(WITHIN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case READ:
			{
				AST tmp33_AST = null;
				tmp33_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp33_AST);
				match(READ);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MINIMUM:
			{
				AST tmp34_AST = null;
				tmp34_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp34_AST);
				match(MINIMUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MIN:
			{
				AST tmp35_AST = null;
				tmp35_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp35_AST);
				match(MIN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MAXIMUM:
			{
				AST tmp36_AST = null;
				tmp36_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp36_AST);
				match(MAXIMUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MAX:
			{
				AST tmp37_AST = null;
				tmp37_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp37_AST);
				match(MAX);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LAST:
			{
				AST tmp38_AST = null;
				tmp38_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp38_AST);
				match(LAST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FIRST:
			{
				AST tmp39_AST = null;
				tmp39_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp39_AST);
				match(FIRST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EARLIEST:
			{
				AST tmp40_AST = null;
				tmp40_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp40_AST);
				match(EARLIEST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LATEST:
			{
				AST tmp41_AST = null;
				tmp41_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp41_AST);
				match(LATEST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EVENT:
			{
				AST tmp42_AST = null;
				tmp42_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp42_AST);
				match(EVENT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WHERE:
			{
				AST tmp43_AST = null;
				tmp43_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp43_AST);
				match(WHERE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EXIST:
			{
				AST tmp44_AST = null;
				tmp44_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp44_AST);
				match(EXIST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case EXISTS:
			{
				AST tmp45_AST = null;
				tmp45_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp45_AST);
				match(EXISTS);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case PAST:
			{
				AST tmp46_AST = null;
				tmp46_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp46_AST);
				match(PAST);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AVERAGE:
			{
				AST tmp47_AST = null;
				tmp47_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp47_AST);
				match(AVERAGE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case AVG:
			{
				AST tmp48_AST = null;
				tmp48_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp48_AST);
				match(AVG);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case SUM:
			{
				AST tmp49_AST = null;
				tmp49_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp49_AST);
				match(SUM);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case MEDIAN:
			{
				AST tmp50_AST = null;
				tmp50_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp50_AST);
				match(MEDIAN);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case CONCLUDE:
			{
				AST tmp51_AST = null;
				tmp51_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp51_AST);
				match(CONCLUDE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ELSE:
			{
				AST tmp52_AST = null;
				tmp52_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp52_AST);
				match(ELSE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ELSEIF:
			{
				AST tmp53_AST = null;
				tmp53_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp53_AST);
				match(ELSEIF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ENDIF:
			{
				AST tmp54_AST = null;
				tmp54_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp54_AST);
				match(ENDIF);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			{
				AST tmp55_AST = null;
				tmp55_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp55_AST);
				match(TRUE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				AST tmp56_AST = null;
				tmp56_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp56_AST);
				match(FALSE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case DATA:
			{
				AST tmp57_AST = null;
				tmp57_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp57_AST);
				match(DATA);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case LOGIC:
			{
				AST tmp58_AST = null;
				tmp58_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp58_AST);
				match(LOGIC);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ACTION:
			{
				AST tmp59_AST = null;
				tmp59_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp59_AST);
				match(ACTION);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case CALL:
			{
				AST tmp60_AST = null;
				tmp60_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp60_AST);
				match(CALL);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case WITH:
			{
				AST tmp61_AST = null;
				tmp61_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp61_AST);
				match(WITH);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case TO:
			{
				AST tmp62_AST = null;
				tmp62_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp62_AST);
				match(TO);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case ANY:
			{
				AST tmp63_AST = null;
				tmp63_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp63_AST);
				match(ANY);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case RESEARCH:
			{
				AST tmp64_AST = null;
				tmp64_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp64_AST);
				match(RESEARCH);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case DAY:
			{
				AST tmp65_AST = null;
				tmp65_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp65_AST);
				match(DAY);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case SECOND:
			{
				AST tmp66_AST = null;
				tmp66_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp66_AST);
				match(SECOND);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case OCCUR:
			{
				AST tmp67_AST = null;
				tmp67_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp67_AST);
				match(OCCUR);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case PURPOSE:
			{
				AST tmp68_AST = null;
				tmp68_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp68_AST);
				match(PURPOSE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case PRESENT:
			{
				AST tmp69_AST = null;
				tmp69_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp69_AST);
				match(PRESENT);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case NUMBER:
			{
				AST tmp70_AST = null;
				tmp70_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp70_AST);
				match(NUMBER);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case KNOWLEDGE:
			{
				AST tmp71_AST = null;
				tmp71_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp71_AST);
				match(KNOWLEDGE);
				any_reserved_word_AST = (AST)currentAST.root;
				break;
			}
			case PRIORITY:
			{
				AST tmp72_AST = null;
				tmp72_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp72_AST);
				match(PRIORITY);
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
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		returnAST = any_reserved_word_AST;
	}
	
	public final void text() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST text_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				AST tmp73_AST = null;
				tmp73_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp73_AST);
				match(ID);
				text_AST = (AST)currentAST.root;
				break;
			}
			case INTLIT:
			{
				AST tmp74_AST = null;
				tmp74_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp74_AST);
				match(INTLIT);
				text_AST = (AST)currentAST.root;
				break;
			}
			case MINUS:
			{
				AST tmp75_AST = null;
				tmp75_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp75_AST);
				match(MINUS);
				text_AST = (AST)currentAST.root;
				break;
			}
			case COMMA:
			{
				AST tmp76_AST = null;
				tmp76_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp76_AST);
				match(COMMA);
				text_AST = (AST)currentAST.root;
				break;
			}
			case DOT:
			{
				AST tmp77_AST = null;
				tmp77_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp77_AST);
				match(DOT);
				text_AST = (AST)currentAST.root;
				break;
			}
			case DIV:
			{
				AST tmp78_AST = null;
				tmp78_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp78_AST);
				match(DIV);
				text_AST = (AST)currentAST.root;
				break;
			}
			case UNDERSCORE:
			{
				AST tmp79_AST = null;
				tmp79_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp79_AST);
				match(UNDERSCORE);
				text_AST = (AST)currentAST.root;
				break;
			}
			case STRING_LITERAL:
			{
				AST tmp80_AST = null;
				tmp80_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp80_AST);
				match(STRING_LITERAL);
				text_AST = (AST)currentAST.root;
				break;
			}
			case LPAREN:
			{
				{
				AST tmp81_AST = null;
				tmp81_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp81_AST);
				match(LPAREN);
				{
				_loop7:
				do {
					switch ( LA(1)) {
					case ID:
					{
						AST tmp82_AST = null;
						tmp82_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp82_AST);
						match(ID);
						break;
					}
					case INTLIT:
					{
						AST tmp83_AST = null;
						tmp83_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp83_AST);
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
						break _loop7;
					}
					}
				} while (true);
				}
				AST tmp84_AST = null;
				tmp84_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp84_AST);
				match(RPAREN);
				}
				text_AST = (AST)currentAST.root;
				break;
			}
			default:
				if ((_tokenSet_1.member(LA(1)))) {
					{
					any_reserved_word();
					astFactory.addASTChild(currentAST, returnAST);
					}
					text_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==AT)) {
					AST tmp85_AST = null;
					tmp85_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp85_AST);
					match(AT);
					text_AST = (AST)currentAST.root;
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = text_AST;
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
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		returnAST = iso_date_time_AST;
	}
	
	public final void datepart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST datepart_AST = null;
		
		try {      // for error handling
			{
			AST tmp86_AST = null;
			tmp86_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp86_AST);
			match(INTLIT);
			{
			int _cnt12=0;
			_loop12:
			do {
				if ((LA(1)==MINUS)) {
					AST tmp87_AST = null;
					tmp87_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp87_AST);
					match(MINUS);
					AST tmp88_AST = null;
					tmp88_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp88_AST);
					match(INTLIT);
				}
				else {
					if ( _cnt12>=1 ) { break _loop12; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt12++;
			} while (true);
			}
			}
			timepart_opt();
			astFactory.addASTChild(currentAST, returnAST);
			datepart_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		returnAST = datepart_AST;
	}
	
	public final void timepart_opt() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST timepart_opt_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case SEMI:
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
				AST tmp89_AST = null;
				tmp89_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp89_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_3);
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
			AST tmp90_AST = null;
			tmp90_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp90_AST);
			match(INTLIT);
			AST tmp91_AST = null;
			tmp91_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp91_AST);
			match(112);
			AST tmp92_AST = null;
			tmp92_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp92_AST);
			match(INTLIT);
			AST tmp93_AST = null;
			tmp93_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp93_AST);
			match(112);
			AST tmp94_AST = null;
			tmp94_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp94_AST);
			match(INTLIT);
			fractional_seconds();
			astFactory.addASTChild(currentAST, returnAST);
			time_zone();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp95_AST = null;
			tmp95_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp95_AST);
			match(ENDBLOCK);
			timepart_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
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
				AST tmp96_AST = null;
				tmp96_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp96_AST);
				match(LITERAL_T);
				time_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_t:
			{
				AST tmp97_AST = null;
				tmp97_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp97_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
		returnAST = time_AST;
	}
	
	public final void fractional_seconds() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST fractional_seconds_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case 115:
			{
				AST tmp98_AST = null;
				tmp98_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp98_AST);
				match(115);
				AST tmp99_AST = null;
				tmp99_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp99_AST);
				match(DIGIT);
				fractional_seconds_AST = (AST)currentAST.root;
				break;
			}
			case ENDBLOCK:
			case 117:
			case 118:
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
			reportError(ex);
			recover(ex,_tokenSet_6);
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
			case 117:
			{
				AST tmp100_AST = null;
				tmp100_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp100_AST);
				match(117);
				AST tmp101_AST = null;
				tmp101_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp101_AST);
				match(DIGIT);
				AST tmp102_AST = null;
				tmp102_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp102_AST);
				match(DIGIT);
				AST tmp103_AST = null;
				tmp103_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp103_AST);
				match(112);
				AST tmp104_AST = null;
				tmp104_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp104_AST);
				match(DIGIT);
				AST tmp105_AST = null;
				tmp105_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp105_AST);
				match(DIGIT);
				time_zone_AST = (AST)currentAST.root;
				break;
			}
			case 118:
			{
				AST tmp106_AST = null;
				tmp106_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp106_AST);
				match(118);
				AST tmp107_AST = null;
				tmp107_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp107_AST);
				match(DIGIT);
				AST tmp108_AST = null;
				tmp108_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp108_AST);
				match(DIGIT);
				AST tmp109_AST = null;
				tmp109_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp109_AST);
				match(112);
				AST tmp110_AST = null;
				tmp110_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp110_AST);
				match(DIGIT);
				AST tmp111_AST = null;
				tmp111_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp111_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_4);
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
				AST tmp112_AST = null;
				tmp112_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp112_AST);
				match(LITERAL_Z);
				zulu_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_z:
			{
				AST tmp113_AST = null;
				tmp113_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp113_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		returnAST = zulu_AST;
	}
	
/*********************************OPERATORS***************************************************************/
	public final void in_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST in_comp_op_AST = null;
		
		try {      // for error handling
			AST tmp114_AST = null;
			tmp114_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp114_AST);
			match(IN);
			in_comp_op_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = in_comp_op_AST;
	}
	
	public final void of_read_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST of_read_func_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case AVERAGE:
			{
				AST tmp115_AST = null;
				tmp115_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp115_AST);
				match(AVERAGE);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case AVG:
			{
				AST tmp116_AST = null;
				tmp116_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp116_AST);
				match(AVG);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case COUNT:
			{
				AST tmp117_AST = null;
				tmp117_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp117_AST);
				match(COUNT);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case EXIST:
			{
				{
				AST tmp118_AST = null;
				tmp118_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp118_AST);
				match(EXIST);
				}
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case EXISTS:
			{
				AST tmp119_AST = null;
				tmp119_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp119_AST);
				match(EXISTS);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case SUM:
			{
				AST tmp120_AST = null;
				tmp120_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp120_AST);
				match(SUM);
				of_read_func_op_AST = (AST)currentAST.root;
				break;
			}
			case MEDIAN:
			{
				AST tmp121_AST = null;
				tmp121_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp121_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_7);
		}
		returnAST = of_read_func_op_AST;
	}
	
	public final void from_of_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST from_of_func_op_AST = null;
		
		try {      // for error handling
			if ((LA(1)==MINIMUM||LA(1)==MIN)) {
				{
				switch ( LA(1)) {
				case MINIMUM:
				{
					AST tmp122_AST = null;
					tmp122_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp122_AST);
					match(MINIMUM);
					break;
				}
				case MIN:
				{
					AST tmp123_AST = null;
					tmp123_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp123_AST);
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
			}
			else if ((LA(1)==MAXIMUM||LA(1)==MAX)) {
				{
				switch ( LA(1)) {
				case MAXIMUM:
				{
					AST tmp124_AST = null;
					tmp124_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp124_AST);
					match(MAXIMUM);
					break;
				}
				case MAX:
				{
					AST tmp125_AST = null;
					tmp125_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp125_AST);
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
			}
			else if ((LA(1)==LAST)) {
				{
				AST tmp126_AST = null;
				tmp126_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp126_AST);
				match(LAST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==FIRST)) {
				{
				AST tmp127_AST = null;
				tmp127_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp127_AST);
				match(FIRST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==EARLIEST)) {
				{
				AST tmp128_AST = null;
				tmp128_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp128_AST);
				match(EARLIEST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LATEST)) {
				{
				AST tmp129_AST = null;
				tmp129_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp129_AST);
				match(LATEST);
				}
				from_of_func_op_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_7.member(LA(1)))) {
				from_of_func_op_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_7);
		}
		returnAST = from_of_func_op_AST;
	}
	
	public final void unary_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unary_comp_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_PRESENT:
			{
				AST tmp130_AST = null;
				tmp130_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp130_AST);
				match(LITERAL_PRESENT);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NULL:
			{
				AST tmp131_AST = null;
				tmp131_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp131_AST);
				match(LITERAL_NULL);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_BOOLEAN:
			{
				AST tmp132_AST = null;
				tmp132_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp132_AST);
				match(LITERAL_BOOLEAN);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NUMBER:
			{
				AST tmp133_AST = null;
				tmp133_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp133_AST);
				match(LITERAL_NUMBER);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case TIME:
			{
				AST tmp134_AST = null;
				tmp134_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp134_AST);
				match(TIME);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_DURATION:
			{
				AST tmp135_AST = null;
				tmp135_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp135_AST);
				match(LITERAL_DURATION);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_STRING:
			{
				AST tmp136_AST = null;
				tmp136_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp136_AST);
				match(LITERAL_STRING);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LIST:
			{
				AST tmp137_AST = null;
				tmp137_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp137_AST);
				match(LITERAL_LIST);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_OBJECT:
			{
				AST tmp138_AST = null;
				tmp138_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp138_AST);
				match(LITERAL_OBJECT);
				unary_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case ID:
			{
				AST tmp139_AST = null;
				tmp139_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp139_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = unary_comp_op_AST;
	}
	
	public final void binary_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST binary_comp_op_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LESS)) {
				AST tmp140_AST = null;
				tmp140_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp140_AST);
				match(LESS);
				AST tmp141_AST = null;
				tmp141_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp141_AST);
				match(THAN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==GREATER)) {
				AST tmp142_AST = null;
				tmp142_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp142_AST);
				match(GREATER);
				AST tmp143_AST = null;
				tmp143_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp143_AST);
				match(THAN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==GREATER)) {
				AST tmp144_AST = null;
				tmp144_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp144_AST);
				match(GREATER);
				AST tmp145_AST = null;
				tmp145_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp145_AST);
				match(THAN);
				AST tmp146_AST = null;
				tmp146_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp146_AST);
				match(OR);
				AST tmp147_AST = null;
				tmp147_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp147_AST);
				match(EQUAL);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LESS)) {
				AST tmp148_AST = null;
				tmp148_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp148_AST);
				match(LESS);
				AST tmp149_AST = null;
				tmp149_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp149_AST);
				match(THAN);
				AST tmp150_AST = null;
				tmp150_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp150_AST);
				match(OR);
				AST tmp151_AST = null;
				tmp151_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp151_AST);
				match(EQUAL);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==IN)) {
				AST tmp152_AST = null;
				tmp152_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp152_AST);
				match(IN);
				binary_comp_op_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
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
				AST tmp153_AST = null;
				tmp153_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp153_AST);
				match(YEAR);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case YEARS:
			{
				AST tmp154_AST = null;
				tmp154_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp154_AST);
				match(YEARS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case MONTH:
			{
				AST tmp155_AST = null;
				tmp155_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp155_AST);
				match(MONTH);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case MONTHS:
			{
				AST tmp156_AST = null;
				tmp156_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp156_AST);
				match(MONTHS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case WEEK:
			{
				AST tmp157_AST = null;
				tmp157_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp157_AST);
				match(WEEK);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case WEEKS:
			{
				AST tmp158_AST = null;
				tmp158_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp158_AST);
				match(WEEKS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case DAY:
			{
				AST tmp159_AST = null;
				tmp159_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp159_AST);
				match(DAY);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case DAYS:
			{
				AST tmp160_AST = null;
				tmp160_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp160_AST);
				match(DAYS);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_hour:
			{
				AST tmp161_AST = null;
				tmp161_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp161_AST);
				match(LITERAL_hour);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_hours:
			{
				AST tmp162_AST = null;
				tmp162_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp162_AST);
				match(LITERAL_hours);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_minute:
			{
				AST tmp163_AST = null;
				tmp163_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp163_AST);
				match(LITERAL_minute);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_minutes:
			{
				AST tmp164_AST = null;
				tmp164_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp164_AST);
				match(LITERAL_minutes);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case SECOND:
			{
				AST tmp165_AST = null;
				tmp165_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp165_AST);
				match(SECOND);
				duration_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_seconds:
			{
				AST tmp166_AST = null;
				tmp166_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp166_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_9);
		}
		returnAST = duration_op_AST;
	}
	
	public final void temporal_comp_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST temporal_comp_op_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case WITHIN:
			{
				AST tmp167_AST = null;
				tmp167_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp167_AST);
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
				AST tmp168_AST = null;
				tmp168_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp168_AST);
				match(PAST);
				expr_string();
				astFactory.addASTChild(currentAST, returnAST);
				temporal_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case AFTER:
			{
				AST tmp169_AST = null;
				tmp169_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp169_AST);
				match(AFTER);
				expr_string();
				astFactory.addASTChild(currentAST, returnAST);
				temporal_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case BEFORE:
			{
				AST tmp170_AST = null;
				tmp170_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp170_AST);
				match(BEFORE);
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
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
		returnAST = temporal_comp_op_AST;
	}
	
	public final void the() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST the_AST = null;
		
		try {      // for error handling
			AST tmp171_AST = null;
			tmp171_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp171_AST);
			match(THE);
			the_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
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
			_loop78:
			do {
				if ((LA(1)==ACTION_OP)) {
					AST tmp172_AST = null;
					tmp172_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp172_AST);
					match(ACTION_OP);
					expr_plus();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop78;
				}
				
			} while (true);
			}
			expr_string_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_11);
		}
		returnAST = expr_string_AST;
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
				AST tmp173_AST = null;
				tmp173_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp173_AST);
				match(EQUALS);
				}
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_EQ:
			{
				{
				AST tmp174_AST = null;
				tmp174_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp174_AST);
				match(LITERAL_EQ);
				}
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LT:
			{
				AST tmp175_AST = null;
				tmp175_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp175_AST);
				match(LT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LT:
			{
				AST tmp176_AST = null;
				tmp176_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp176_AST);
				match(LITERAL_LT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case GT:
			{
				AST tmp177_AST = null;
				tmp177_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp177_AST);
				match(GT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_GT:
			{
				AST tmp178_AST = null;
				tmp178_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp178_AST);
				match(LITERAL_GT);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LTE:
			{
				AST tmp179_AST = null;
				tmp179_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp179_AST);
				match(LTE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_LE:
			{
				AST tmp180_AST = null;
				tmp180_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp180_AST);
				match(LITERAL_LE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case GTE:
			{
				AST tmp181_AST = null;
				tmp181_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp181_AST);
				match(GTE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_GE:
			{
				AST tmp182_AST = null;
				tmp182_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp182_AST);
				match(LITERAL_GE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case NE:
			{
				AST tmp183_AST = null;
				tmp183_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp183_AST);
				match(NE);
				simple_comp_op_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_NE:
			{
				AST tmp184_AST = null;
				tmp184_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp184_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_8);
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
			reportError(ex);
			recover(ex,_tokenSet_12);
		}
		returnAST = main_comp_op_AST;
	}
	
/************************************************************************************************/
	public final void where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST where_AST = null;
		
		try {      // for error handling
			{
			AST tmp185_AST = null;
			tmp185_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp185_AST);
			match(WHERE);
			}
			where_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
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
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = it_AST;
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
				AST tmp188_AST = null;
				tmp188_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp188_AST);
				match(IS);
				is_AST = (AST)currentAST.root;
				break;
			}
			case ARE:
			{
				AST tmp189_AST = null;
				tmp189_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp189_AST);
				match(ARE);
				is_AST = (AST)currentAST.root;
				break;
			}
			case WERE:
			{
				AST tmp190_AST = null;
				tmp190_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp190_AST);
				match(WERE);
				is_AST = (AST)currentAST.root;
				break;
			}
			case WAS:
			{
				AST tmp191_AST = null;
				tmp191_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp191_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
		returnAST = is_AST;
	}
	
	public final void occur() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST occur_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case OCCUR:
			case LITERAL_OCCUR:
			case LITERAL_Occur:
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
				case OCCUR:
				{
					match(OCCUR);
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
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = occur_AST;
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
			_loop51:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					expr_sort();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop51;
				}
				
			} while (true);
			}
			expr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_14);
		}
		returnAST = expr_AST;
	}
	
	public final void expr_sort() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_sort_AST = null;
		
		try {      // for error handling
			expr_where();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop57:
			do {
				if ((LA(1)==LITERAL_MERGE||LA(1)==LITERAL_SORT)) {
					{
					switch ( LA(1)) {
					case LITERAL_MERGE:
					{
						AST tmp202_AST = null;
						tmp202_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp202_AST);
						match(LITERAL_MERGE);
						break;
					}
					case LITERAL_SORT:
					{
						{
						AST tmp203_AST = null;
						tmp203_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp203_AST);
						match(LITERAL_SORT);
						{
						if ((LA(1)==TIME||LA(1)==LITERAL_DATA)) {
							sort_option();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((_tokenSet_15.member(LA(1)))) {
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
					break _loop57;
				}
				
			} while (true);
			}
			expr_sort_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_16);
		}
		returnAST = expr_sort_AST;
	}
	
	public final void expr_where() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_where_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
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
			case ID:
			case INTLIT:
			case STRING_LITERAL:
			case LPAREN:
			case TERM_LITERAL:
			{
				expr_range();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop61:
				do {
					if ((LA(1)==WHERE)) {
						AST tmp204_AST = null;
						tmp204_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp204_AST);
						match(WHERE);
						expr_range();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop61;
					}
					
				} while (true);
				}
				expr_where_AST = (AST)currentAST.root;
				break;
			}
			case BEFORE:
			case AFTER:
			case WITHIN:
			case COMMA:
			case RPAREN:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case SEMI:
			{
				expr_where_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_17);
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
				AST tmp205_AST = null;
				tmp205_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp205_AST);
				match(TIME);
				sort_option_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_DATA:
			{
				AST tmp206_AST = null;
				tmp206_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp206_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_15);
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
			_loop64:
			do {
				if ((LA(1)==LITERAL_SEQTO)) {
					AST tmp207_AST = null;
					tmp207_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp207_AST);
					match(LITERAL_SEQTO);
					expr_or();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop64;
				}
				
			} while (true);
			}
			expr_range_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_18);
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
			_loop67:
			do {
				if ((LA(1)==OR)) {
					AST tmp208_AST = null;
					tmp208_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp208_AST);
					match(OR);
					expr_and();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop67;
				}
				
			} while (true);
			}
			expr_or_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_19);
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
			_loop70:
			do {
				if ((LA(1)==AND)) {
					AST tmp209_AST = null;
					tmp209_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp209_AST);
					match(AND);
					expr_not();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop70;
				}
				
			} while (true);
			}
			expr_and_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_20);
		}
		returnAST = expr_and_AST;
	}
	
	public final void expr_not() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_not_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
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
			case ID:
			case INTLIT:
			case STRING_LITERAL:
			case LPAREN:
			case TERM_LITERAL:
			{
				expr_comparison();
				astFactory.addASTChild(currentAST, returnAST);
				expr_not_AST = (AST)currentAST.root;
				break;
			}
			case NOT:
			{
				AST tmp210_AST = null;
				tmp210_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp210_AST);
				match(NOT);
				expr_comparison();
				astFactory.addASTChild(currentAST, returnAST);
				expr_not_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_12);
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
			case BEFORE:
			case AFTER:
			case OR:
			case WHERE:
			case WITHIN:
			case COMMA:
			case RPAREN:
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
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
			expr_comparison_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_12);
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
			_loop82:
			do {
				if ((LA(1)==117||LA(1)==118)) {
					{
					switch ( LA(1)) {
					case 117:
					{
						AST tmp211_AST = null;
						tmp211_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp211_AST);
						match(117);
						break;
					}
					case 118:
					{
						AST tmp212_AST = null;
						tmp212_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp212_AST);
						match(118);
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
					break _loop82;
				}
				
			} while (true);
			}
			expr_plus_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_21);
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
			_loop86:
			do {
				if ((LA(1)==159||LA(1)==160)) {
					{
					switch ( LA(1)) {
					case 159:
					{
						AST tmp213_AST = null;
						tmp213_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp213_AST);
						match(159);
						break;
					}
					case 160:
					{
						AST tmp214_AST = null;
						tmp214_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp214_AST);
						match(160);
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
					break _loop86;
				}
				
			} while (true);
			}
			expr_times_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
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
			_loop90:
			do {
				if (((LA(1) >= FROM && LA(1) <= AFTER))) {
					{
					switch ( LA(1)) {
					case BEFORE:
					{
						AST tmp215_AST = null;
						tmp215_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp215_AST);
						match(BEFORE);
						break;
					}
					case AFTER:
					{
						AST tmp216_AST = null;
						tmp216_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp216_AST);
						match(AFTER);
						break;
					}
					case FROM:
					{
						AST tmp217_AST = null;
						tmp217_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp217_AST);
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
					break _loop90;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case AGO:
			{
				AST tmp218_AST = null;
				tmp218_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp218_AST);
				match(AGO);
				break;
			}
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case BEFORE:
			case AFTER:
			case OR:
			case WHERE:
			case WITHIN:
			case INTLIT:
			case COMMA:
			case RPAREN:
			case 117:
			case 118:
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
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			case ACTION_OP:
			case 159:
			case 160:
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
			expr_power_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
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
			case SECOND:
			case LITERAL_hour:
			case LITERAL_hours:
			case LITERAL_minute:
			case LITERAL_minutes:
			case LITERAL_seconds:
			{
				duration_op();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case AND:
			case IS:
			case ARE:
			case WAS:
			case WERE:
			case FROM:
			case BEFORE:
			case AFTER:
			case AGO:
			case OR:
			case WHERE:
			case WITHIN:
			case INTLIT:
			case COMMA:
			case RPAREN:
			case 117:
			case 118:
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
			case LITERAL_MERGE:
			case LITERAL_SORT:
			case LITERAL_SEQTO:
			case ACTION_OP:
			case 159:
			case 160:
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
			expr_duration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_9);
		}
		returnAST = expr_duration_AST;
	}
	
	public final void expr_function() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_function_AST = null;
		
		try {      // for error handling
			if ((_tokenSet_23.member(LA(1)))) {
				expr_factor();
				astFactory.addASTChild(currentAST, returnAST);
				expr_function_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_8.member(LA(1)))) {
				{
				switch ( LA(1)) {
				case THE:
				{
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
				case ID:
				case INTLIT:
				case STRING_LITERAL:
				case LPAREN:
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
				case ID:
				case INTLIT:
				case STRING_LITERAL:
				case LPAREN:
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
					case TRUE:
					case FALSE:
					case NULL:
					case ID:
					case INTLIT:
					case STRING_LITERAL:
					case LPAREN:
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
					case TRUE:
					case FALSE:
					case NULL:
					case ID:
					case INTLIT:
					case STRING_LITERAL:
					case LPAREN:
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
			reportError(ex);
			recover(ex,_tokenSet_24);
		}
		returnAST = expr_function_AST;
	}
	
	public final void expr_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_factor_AST = null;
		
		try {      // for error handling
			expr_factor_atom();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop101:
			do {
				if ((LA(1)==DOT)) {
					AST tmp221_AST = null;
					tmp221_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp221_AST);
					match(DOT);
					expr_factor_atom();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop101;
				}
				
			} while (true);
			}
			expr_factor_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_24);
		}
		returnAST = expr_factor_AST;
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
			reportError(ex);
			recover(ex,_tokenSet_25);
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
				AST tmp222_AST = null;
				tmp222_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp222_AST);
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
			case INTLIT:
			{
				{
				AST tmp225_AST = null;
				tmp225_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp225_AST);
				match(INTLIT);
				{
				if ((LA(1)==MINUS)) {
					{
					int _cnt106=0;
					_loop106:
					do {
						if ((LA(1)==MINUS)) {
							AST tmp226_AST = null;
							tmp226_AST = astFactory.create(LT(1));
							astFactory.makeASTRoot(currentAST, tmp226_AST);
							match(MINUS);
							AST tmp227_AST = null;
							tmp227_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp227_AST);
							match(INTLIT);
						}
						else {
							if ( _cnt106>=1 ) { break _loop106; } else {throw new NoViableAltException(LT(1), getFilename());}
						}
						
						_cnt106++;
					} while (true);
					}
				}
				else if ((LA(1)==DOT)) {
					{
					AST tmp228_AST = null;
					tmp228_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp228_AST);
					match(DOT);
					AST tmp229_AST = null;
					tmp229_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp229_AST);
					match(INTLIT);
					}
				}
				else if ((_tokenSet_26.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
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
				AST tmp230_AST = null;
				tmp230_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp230_AST);
				match(STRING_LITERAL);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case TERM_LITERAL:
			{
				AST tmp231_AST = null;
				tmp231_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp231_AST);
				match(TERM_LITERAL);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			case NULL:
			{
				AST tmp232_AST = null;
				tmp232_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp232_AST);
				match(NULL);
				expr_factor_atom_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_26);
		}
		returnAST = expr_factor_atom_AST;
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
				AST tmp233_AST = null;
				tmp233_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp233_AST);
				match(TRUE);
				}
				boolean_value_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				{
				AST tmp234_AST = null;
				tmp234_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp234_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_26);
		}
		returnAST = boolean_value_AST;
	}
	
	public final void as_func_op() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST as_func_op_AST = null;
		
		try {      // for error handling
			AST tmp235_AST = null;
			tmp235_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp235_AST);
			match(LITERAL_NUMBER);
			as_func_op_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
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
				AST tmp236_AST = null;
				tmp236_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp236_AST);
				match(TIME);
				of_noread_func_op_AST = (AST)currentAST.root;
				break;
			}
			case ANY:
			{
				AST tmp237_AST = null;
				tmp237_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp237_AST);
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
			reportError(ex);
			recover(ex,_tokenSet_25);
		}
		returnAST = of_noread_func_op_AST;
	}
	
	public final void query_parse() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST query_parse_AST = null;
		
		try {      // for error handling
			{
			if ((_tokenSet_27.member(LA(1)))) {
				{
				of_read_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				}
			}
			else if ((_tokenSet_7.member(LA(1)))) {
				{
				from_of_func_op();
				astFactory.addASTChild(currentAST, returnAST);
				{
				if ((LA(1)==INTLIT)) {
					AST tmp238_AST = null;
					tmp238_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp238_AST);
					match(INTLIT);
				}
				else if ((_tokenSet_7.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			expr();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case BEFORE:
			case AFTER:
			case WITHIN:
			{
				temporal_comp_op();
				astFactory.addASTChild(currentAST, returnAST);
				iso_date_time();
				astFactory.addASTChild(currentAST, returnAST);
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
			match(SEMI);
			query_parse_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		returnAST = query_parse_AST;
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
		"\"knowledge\"",
		"\"library\"",
		"\"filename\"",
		"\"mlmname\"",
		"\"title\"",
		"\"institution\"",
		"\"author\"",
		"\"priority\"",
		"\"version\"",
		"\"specialist\"",
		"\"purpose\"",
		"\"explanation\"",
		"\"keywords\"",
		"\"citations\"",
		"\"links\"",
		"\"type\"",
		"\"date\"",
		"\"age_min\"",
		"\"age_max\"",
		"\"of\"",
		"\"time\"",
		"\"within\"",
		"\"call\"",
		"\"with\"",
		"\"to\"",
		"\"any\"",
		"\"research\"",
		"\"second\"",
		"\"occur\"",
		"\"present\"",
		"\"number\"",
		"\"http\"",
		"\"null\"",
		"TIMES",
		"an identifier",
		"INTLIT",
		"MINUS",
		"COMMA",
		"DOT",
		"DIV",
		"UNDERSCORE",
		"STRING_LITERAL",
		"LPAREN",
		"RPAREN",
		";;",
		"\":\"",
		"\"T\"",
		"\"t\"",
		"\".\"",
		"DIGIT",
		"\"+\"",
		"\"-\"",
		"\"Z\"",
		"\"z\"",
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
		"\"seconds\"",
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
		"\"OCCUR\"",
		"\"Occur\"",
		"\"OCCURS\"",
		"\"Occurs\"",
		"\"occurs\"",
		"\"OCCURRED\"",
		"\"Occurred\"",
		"\"MERGE\"",
		"\"SORT\"",
		"\"DATA\"",
		"\"SEQTO\"",
		"ACTION_OP",
		"\"*\"",
		"\"/\"",
		"TERM_LITERAL",
		"SEMI",
		"ARDEN_CURLY_BRACKETS",
		"NOT_COMMENT",
		"COMMENT",
		"ML_COMMENT",
		"WS",
		"BECOMES",
		"COLON",
		"LBRACKET",
		"RBRACKET",
		"DOTDOT",
		"NOT_EQUALS",
		"PLUS",
		"SINGLE_QUOTE",
		"LCURLY",
		"RCURLY"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { -6896136937807918L, 70866956198923L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { -6896136937807920L, 85895160843L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 0L, 0L, 17179869184L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 0L, 140737488355328L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 0L, 274877906944L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 0L, 135248726309470208L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 7052694174961124352L, 54323044155392L, 25971130368L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 7052694173886989312L, 53223515750400L, 8589934592L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 8798241489872L, 27093340914712576L, 25434783616L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 7052764542631150592L, 53223515750400L, 8589934592L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 8798240900048L, 71743150489600L, 17918590848L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 8798240899088L, 71468272582656L, 17918066688L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 14336L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 393216L, 70368760954880L, 17179869184L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 7052694174961124352L, 124691788333056L, 25971130368L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 393216L, 71468272582656L, 17179869184L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 393216L, 71468272582656L, 17381195776L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 8796093415424L, 71468272582656L, 17381195776L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 8796093415424L, 71468272582656L, 17918066688L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 8798240899072L, 71468272582656L, 17918066688L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 8798240900048L, 71743150489600L, 18992332672L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 8798240900048L, 27093340914712576L, 25434783616L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 6917529027641081856L, 53223234732032L, 8589934592L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 8875260108538832L, 27093341988454400L, 25434783740L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 6917529027641081856L, 53223238926336L, 8589934592L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 8875260108538832L, 27095541011709952L, 25434783740L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { 135160765379249152L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	
	}
