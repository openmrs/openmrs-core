<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:openmrs="http://schema.yoursite.org/FormEntry/1" xmlns:my="http://schemas.microsoft.com/office/infopath/2003/myXSD/2006-07-25T11:22:21" xmlns:xd="http://schemas.microsoft.com/office/infopath/2003" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:msxsl="urn:schemas-microsoft-com:xslt" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns:xdExtension="http://schemas.microsoft.com/office/infopath/2003/xslt/extension" xmlns:xdXDocument="http://schemas.microsoft.com/office/infopath/2003/xslt/xDocument" xmlns:xdSolution="http://schemas.microsoft.com/office/infopath/2003/xslt/solution" xmlns:xdFormatting="http://schemas.microsoft.com/office/infopath/2003/xslt/formatting" xmlns:xdImage="http://schemas.microsoft.com/office/infopath/2003/xslt/xImage" xmlns:xdUtil="http://schemas.microsoft.com/office/infopath/2003/xslt/Util" xmlns:xdMath="http://schemas.microsoft.com/office/infopath/2003/xslt/Math" xmlns:xdDate="http://schemas.microsoft.com/office/infopath/2003/xslt/Date" xmlns:sig="http://www.w3.org/2000/09/xmldsig#" xmlns:xdSignatureProperties="http://schemas.microsoft.com/office/infopath/2003/SignatureProperties">
	<xsl:output method="html" indent="no"/>
	<xsl:template match="form">
		<html xmlns:ns1="http://schema.iukenya.org/2006/AMRS/FormEntry/15" xmlns:openmrs="http://schema.iukenya.org/2006/AMRS/FormEntry/15" xmlns:my="http://schemas.microsoft.com/office/infopath/2003/myXSD/2005-08-07T13:39:21" xmlns:d="http://schemas.microsoft.com/office/infopath/2003/ado/dataFields" xmlns:dfs="http://schemas.microsoft.com/office/infopath/2003/dataFormSolution">
			<head>
				<meta http-equiv="Content-Type" content="text/html"></meta>
				<style controlStyle="controlStyle">@media screen 			{ 			BODY{margin-left:21px;background-position:21px 0px;} 			} 		BODY{color:windowtext;background-color:window;layout-grid:none;} 		.xdListItem {display:inline-block;width:100%;vertical-align:text-top;} 		.xdListBox,.xdComboBox{margin:1px;} 		.xdInlinePicture{margin:1px; BEHAVIOR: url(#default#urn::xdPicture) } 		.xdLinkedPicture{margin:1px; BEHAVIOR: url(#default#urn::xdPicture) url(#default#urn::controls/Binder) } 		.xdSection{border:1pt solid #FFFFFF;margin:6px 0px 6px 0px;padding:1px 1px 1px 5px;} 		.xdRepeatingSection{border:1pt solid #FFFFFF;margin:6px 0px 6px 0px;padding:1px 1px 1px 5px;} 		.xdBehavior_Formatting {BEHAVIOR: url(#default#urn::controls/Binder) url(#default#Formatting);} 	 .xdBehavior_FormattingNoBUI{BEHAVIOR: url(#default#CalPopup) url(#default#urn::controls/Binder) url(#default#Formatting);} 	.xdExpressionBox{margin: 1px;padding:1px;word-wrap: break-word;text-overflow: ellipsis;overflow-x:hidden;}.xdBehavior_GhostedText,.xdBehavior_GhostedTextNoBUI{BEHAVIOR: url(#default#urn::controls/Binder) url(#default#TextField) url(#default#GhostedText);}	.xdBehavior_GTFormatting{BEHAVIOR: url(#default#urn::controls/Binder) url(#default#Formatting) url(#default#GhostedText);}	.xdBehavior_GTFormattingNoBUI{BEHAVIOR: url(#default#CalPopup) url(#default#urn::controls/Binder) url(#default#Formatting) url(#default#GhostedText);}	.xdBehavior_Boolean{BEHAVIOR: url(#default#urn::controls/Binder) url(#default#BooleanHelper);}	.xdBehavior_Select{BEHAVIOR: url(#default#urn::controls/Binder) url(#default#SelectHelper);}	.xdRepeatingTable{BORDER-TOP-STYLE: none; BORDER-RIGHT-STYLE: none; BORDER-LEFT-STYLE: none; BORDER-BOTTOM-STYLE: none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word;}.xdScrollableRegion{BEHAVIOR: url(#default#ScrollableRegion);} 		.xdMaster{BEHAVIOR: url(#default#MasterHelper);} 		.xdActiveX{margin:1px; BEHAVIOR: url(#default#ActiveX);} 		.xdFileAttachment{display:inline-block;margin:1px;BEHAVIOR:url(#default#urn::xdFileAttachment);} 		.xdPageBreak{display: none;}BODY{margin-right:21px;} 		.xdTextBoxRTL{display:inline-block;white-space:nowrap;text-overflow:ellipsis;;padding:1px;margin:1px;border: 1pt solid #dcdcdc;color:windowtext;background-color:window;overflow:hidden;text-align:right;} 		.xdRichTextBoxRTL{display:inline-block;;padding:1px;margin:1px;border: 1pt solid #dcdcdc;color:windowtext;background-color:window;overflow-x:hidden;word-wrap:break-word;text-overflow:ellipsis;text-align:right;font-weight:normal;font-style:normal;text-decoration:none;vertical-align:baseline;} 		.xdDTTextRTL{height:100%;width:100%;margin-left:22px;overflow:hidden;padding:0px;white-space:nowrap;} 		.xdDTButtonRTL{margin-right:-21px;height:18px;width:20px;behavior: url(#default#DTPicker);}.xdTextBox{display:inline-block;white-space:nowrap;text-overflow:ellipsis;;padding:1px;margin:1px;border: 1pt solid #dcdcdc;color:windowtext;background-color:window;overflow:hidden;text-align:left;} 		.xdRichTextBox{display:inline-block;;padding:1px;margin:1px;border: 1pt solid #dcdcdc;color:windowtext;background-color:window;overflow-x:hidden;word-wrap:break-word;text-overflow:ellipsis;text-align:left;font-weight:normal;font-style:normal;text-decoration:none;vertical-align:baseline;} 		.xdDTPicker{;display:inline;margin:1px;margin-bottom: 2px;border: 1pt solid #dcdcdc;color:windowtext;background-color:window;overflow:hidden;} 		.xdDTText{height:100%;width:100%;margin-right:22px;overflow:hidden;padding:0px;white-space:nowrap;} 		.xdDTButton{margin-left:-21px;height:18px;width:20px;behavior: url(#default#DTPicker);} 		.xdRepeatingTable TD {VERTICAL-ALIGN: top;}</style>
				<style tableEditor="TableStyleRulesID">TABLE.xdLayout TD {
	BORDER-RIGHT: medium none; BORDER-TOP: medium none; BORDER-LEFT: medium none; BORDER-BOTTOM: medium none
}
TABLE.msoUcTable TD {
	BORDER-RIGHT: 1pt solid; BORDER-TOP: 1pt solid; BORDER-LEFT: 1pt solid; BORDER-BOTTOM: 1pt solid
}
TABLE {
	BEHAVIOR: url (#default#urn::tables/NDTable)
}
</style>
				<style languageStyle="languageStyle">BODY {
	FONT-SIZE: 10pt; FONT-FAMILY: Verdana
}
TABLE {
	FONT-SIZE: 10pt; FONT-FAMILY: Verdana
}
SELECT {
	FONT-SIZE: 10pt; FONT-FAMILY: Verdana
}
.optionalPlaceholder {
	PADDING-LEFT: 20px; FONT-WEIGHT: normal; FONT-SIZE: xx-small; BEHAVIOR: url(#default#xOptional); COLOR: #333333; FONT-STYLE: normal; FONT-FAMILY: Verdana; TEXT-DECORATION: none
}
.langFont {
	FONT-FAMILY: Verdana
}
.defaultInDocUI {
	FONT-SIZE: xx-small; FONT-FAMILY: Verdana
}
.optionalPlaceholder {
	PADDING-RIGHT: 20px
}
</style>
			</head>
			<body>
				<div align="left"></div>
				<div align="left">
					<table class="xdLayout" style="BORDER-RIGHT: medium none; TABLE-LAYOUT: fixed; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 755px; BORDER-BOTTOM: medium none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word" borderColor="buttontext" border="1">
						<colgroup>
							<col style="WIDTH: 369px"></col>
							<col style="WIDTH: 243px"></col>
							<col style="WIDTH: 143px"></col>
						</colgroup>
						<tbody vAlign="top">
							<tr style="MIN-HEIGHT: 36px">
								<td colSpan="2" style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #000000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #000000 1pt solid; BACKGROUND-COLOR: #c0c0c0">
									<div align="center">
										<font face="Arial" size="3">
											<strong>Starter Form</strong>
										</font>
									</div>
								</td>
								<td style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #000000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #000000 1pt solid">
									<div>
										<font face="Verdana" size="2">
											<font face="Arial"> <strong>Date:</strong>
											</font>
											<div class="xdDTPicker" title="Every form must have a date assigned to it!  Please enter one by clicking on the calendar to the right." style="FONT-SIZE: small; WIDTH: 97px; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; TEXT-ALIGN: center" noWrap="1" xd:xctname="DTPicker" xd:CtrlId="CTRL2"><span class="xdDTText xdBehavior_GTFormattingNoBUI" hideFocus="1" contentEditable="true" xd:xctname="DTPicker_DTText" tabIndex="0" xd:binding="encounter/encounter.encounter_datetime" xd:datafmt="&quot;datetime&quot;,&quot;dateFormat:dd MMMM, yyyy;timeFormat:none;&quot;" xd:boundProp="xd:num" xd:innerCtrl="_DTText">
													<xsl:attribute name="xd:num">
														<xsl:value-of select="encounter/encounter.encounter_datetime"/>
													</xsl:attribute>
													<xsl:choose>
														<xsl:when test="not(string(encounter/encounter.encounter_datetime))">
															<xsl:attribute name="xd:ghosted">true</xsl:attribute>Click -&gt;</xsl:when>
														<xsl:when test="function-available('xdFormatting:formatString')">
															<xsl:value-of select="xdFormatting:formatString(encounter/encounter.encounter_datetime,&quot;datetime&quot;,&quot;dateFormat:dd MMMM, yyyy;timeFormat:none;&quot;)"/>
														</xsl:when>
														<xsl:otherwise>
															<xsl:value-of select="encounter/encounter.encounter_datetime"/>
														</xsl:otherwise>
													</xsl:choose>
												</span>
												<button class="xdDTButton" xd:xctname="DTPicker_DTButton" xd:innerCtrl="_DTButton" tabIndex="-1">
													<img src="res://infopath.exe/calendar.gif"/>
												</button>
											</div>
										</font>
									</div>
								</td>
							</tr>
							<tr style="MIN-HEIGHT: 105px">
								<td style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #000000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #000000 1pt solid">
									<div align="center">
										<table class="xdLayout" style="BORDER-RIGHT: medium none; TABLE-LAYOUT: fixed; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 340px; BORDER-BOTTOM: medium none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word" borderColor="buttontext" border="1">
											<colgroup>
												<col style="WIDTH: 98px"></col>
												<col style="WIDTH: 242px"></col>
											</colgroup>
											<tbody vAlign="top">
												<tr style="MIN-HEIGHT: 23px">
													<td style="PADDING-RIGHT: 1px; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px">
														<div align="right">
															<font face="Arial" size="2">
																<strong>Last Name:</strong>
															</font>
														</div>
													</td>
													<td style="PADDING-RIGHT: 1px; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px">
														<div>
															<font face="Arial" size="2"><span class="xdTextBox" hideFocus="1" title="" xd:xctname="PlainText" xd:CtrlId="CTRL4" tabIndex="-1" xd:binding="patient/patient.family_name" xd:disableEditing="yes" style="FONT-WEIGHT: bold; FONT-SIZE: small; WIDTH: 100%; COLOR: #000000; FONT-STYLE: normal; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; TEXT-DECORATION: none; WORD-WRAP: normal">
																	<xsl:value-of select="patient/patient.family_name"/>
																</span>
															</font>
														</div>
													</td>
												</tr>
												<tr style="MIN-HEIGHT: 23px">
													<td style="PADDING-RIGHT: 1px; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px">
														<div align="right">
															<font face="Arial" size="2">
																<strong>First Name:</strong>
															</font>
														</div>
													</td>
													<td style="PADDING-RIGHT: 1px; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px">
														<div><span class="xdTextBox" hideFocus="1" title="" xd:xctname="PlainText" xd:CtrlId="CTRL5" tabIndex="-1" xd:binding="patient/patient.given_name" xd:disableEditing="yes" style="FONT-WEIGHT: bold; FONT-SIZE: small; WIDTH: 100%; COLOR: #000000; FONT-STYLE: normal; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; TEXT-DECORATION: none; WORD-WRAP: normal">
																<xsl:value-of select="patient/patient.given_name"/>
															</span>
														</div>
													</td>
												</tr>
												<tr style="MIN-HEIGHT: 27px">
													<td style="PADDING-RIGHT: 1px; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px">
														<div align="right">
															<font face="Arial" size="2">
																<strong>Middle Name:</strong>
															</font>
														</div>
													</td>
													<td style="PADDING-RIGHT: 1px; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px">
														<div>
															<font face="Arial" size="2"><span class="xdTextBox" hideFocus="1" title="" xd:xctname="PlainText" xd:CtrlId="CTRL6" tabIndex="-1" xd:binding="patient/patient.middle_name" xd:disableEditing="yes" style="FONT-WEIGHT: bold; FONT-SIZE: small; WIDTH: 100%; COLOR: #000000; FONT-STYLE: normal; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; TEXT-DECORATION: none; WORD-WRAP: normal">
																	<xsl:value-of select="patient/patient.middle_name"/>
																</span>
															</font>
														</div>
													</td>
												</tr>
											</tbody>
										</table>
									</div>
								</td>
								<td colSpan="2" style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 5px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 5px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #000000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #000000 1pt solid">
									<div align="center">
										<font face="Arial" size="2">
											<strong>Patient Identifier:</strong>
										</font>
									</div>
									<div align="center"><span class="xdTextBox " hideFocus="1" title="" xd:xctname="PlainText" xd:CtrlId="CTRL7" tabIndex="-1" xd:binding="patient/patient.medical_record_number" xd:disableEditing="yes" style="FONT-WEIGHT: bold; FONT-SIZE: medium; WIDTH: 161px; COLOR: #000000; FONT-STYLE: normal; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; TEXT-ALIGN: center; TEXT-DECORATION: none; WORD-WRAP: normal">
											<xsl:value-of select="patient/patient.medical_record_number"/>
										</span>
									</div>
									<font face="Arial"></font>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div align="left">
					<table class="xdLayout" style="BORDER-RIGHT: medium none; TABLE-LAYOUT: fixed; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 753px; BORDER-BOTTOM: medium none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word" borderColor="buttontext" border="1">
						<colgroup>
							<col style="WIDTH: 244px"></col>
							<col style="WIDTH: 65px"></col>
							<col style="WIDTH: 222px"></col>
							<col style="WIDTH: 222px"></col>
						</colgroup>
						<tbody vAlign="top">
							<tr style="MIN-HEIGHT: 57px">
								<td style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 5px; BORDER-TOP: #000000 1pt; PADDING-LEFT: 5px; PADDING-BOTTOM: 5px; BORDER-LEFT: #000000 1pt solid; PADDING-TOP: 5px; BORDER-BOTTOM: #000000 1pt solid">
									<div>
										<font size="2">
											<font face="Arial">Date of Birth: </font>
										</font>
									</div>
									<div>
										<div class="xdDTPicker" title="" style="FONT-WEIGHT: bold; FONT-SIZE: small; WIDTH: 150px; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; HEIGHT: 25px; TEXT-ALIGN: center" noWrap="1" xd:xctname="DTPicker" xd:CtrlId="CTRL10"><span class="xdDTText xdBehavior_FormattingNoBUI" hideFocus="1" contentEditable="true" xd:xctname="DTPicker_DTText" tabIndex="0" xd:binding="patient/patient.birthdate" xd:datafmt="&quot;datetime&quot;,&quot;dateFormat:dd-MMM-yy;timeFormat:none;&quot;" xd:boundProp="xd:num" xd:innerCtrl="_DTText">
												<xsl:choose>
													<xsl:when test="patient/patient.birthdate != &quot;&quot;">
														<xsl:attribute name="contentEditable">false</xsl:attribute>
													</xsl:when>
												</xsl:choose>
												<xsl:attribute name="xd:num">
													<xsl:value-of select="patient/patient.birthdate"/>
												</xsl:attribute>
												<xsl:choose>
													<xsl:when test="function-available('xdFormatting:formatString')">
														<xsl:value-of select="xdFormatting:formatString(patient/patient.birthdate,&quot;datetime&quot;,&quot;dateFormat:dd-MMM-yy;timeFormat:none;&quot;)"/>
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="patient/patient.birthdate"/>
													</xsl:otherwise>
												</xsl:choose>
											</span>
											<button class="xdDTButton" xd:xctname="DTPicker_DTButton" xd:innerCtrl="_DTButton" tabIndex="-1">
												<img src="res://infopath.exe/calendar.gif"/>
											</button>
										</div>
									</div>
								</td>
								<td rowSpan="2" style="BORDER-RIGHT: #000000 1pt; PADDING-RIGHT: 5px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 5px; PADDING-BOTTOM: 5px; BORDER-LEFT: #000000 1pt solid; PADDING-TOP: 5px; BORDER-BOTTOM: #000000 1pt solid">
									<div>
										<font size="2">
											<font face="Arial">Address: </font>
										</font>
									</div>
								</td>
								<td rowSpan="2" style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 5px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 5px; PADDING-BOTTOM: 5px; BORDER-LEFT: #000000 1pt; PADDING-TOP: 5px; BORDER-BOTTOM: #000000 1pt solid">
									<font size="2">
										<font face="Arial">
											<div><span class="xdTextBox" hideFocus="1" title="" xd:xctname="PlainText" xd:CtrlId="CTRL46" tabIndex="-1" xd:binding="patient/patient_address.address1" xd:disableEditing="yes" style="FONT-WEIGHT: bold; FONT-SIZE: small; WIDTH: 179px; COLOR: #000000; FONT-STYLE: normal; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; TEXT-ALIGN: center; TEXT-DECORATION: none; WORD-WRAP: normal">
													<xsl:value-of select="patient/patient_address.address1"/>
												</span>
											</div>
											<div><span class="xdTextBox" hideFocus="1" title="" xd:xctname="PlainText" xd:CtrlId="CTRL47" tabIndex="-1" xd:binding="patient/patient_address.address2" xd:disableEditing="yes" style="FONT-WEIGHT: bold; FONT-SIZE: small; WIDTH: 179px; COLOR: #000000; FONT-STYLE: normal; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; TEXT-ALIGN: center; TEXT-DECORATION: none; WORD-WRAP: normal">
													<xsl:value-of select="patient/patient_address.address2"/>
												</span>
											</div>
										</font>
									</font>
								</td>
								<td rowSpan="2" style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 5px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 5px; PADDING-BOTTOM: 5px; BORDER-LEFT: #000000 1pt; PADDING-TOP: 5px; BORDER-BOTTOM: #000000 1pt solid">
									<font size="2">
										<font face="Arial">
											<div>Location: </div>
											<div>
												<div><input class="xdBehavior_Boolean" title="" type="radio" name="{generate-id(encounter/encounter.location_id)}" xd:xctname="OptionButton" xd:CtrlId="CTRL17" tabIndex="0" xd:binding="encounter/encounter.location_id" xd:boundProp="xd:value" xd:onValue="1^Unknown">
														<xsl:attribute name="xd:value">
															<xsl:value-of select="encounter/encounter.location_id"/>
														</xsl:attribute>
														<xsl:if test="encounter/encounter.location_id=&quot;1^Unknown&quot;">
															<xsl:attribute name="CHECKED">CHECKED</xsl:attribute>
														</xsl:if>
													</input>Unknown</div>
											</div>
										</font>
									</font>
								</td>
							</tr>
							<tr style="MIN-HEIGHT: 36px">
								<td style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 5px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 5px; PADDING-BOTTOM: 5px; VERTICAL-ALIGN: middle; BORDER-LEFT: #000000 1pt solid; PADDING-TOP: 5px; BORDER-BOTTOM: #000000 1pt solid">
									<div>
										<font face="Arial" size="2">Gender:  <span class="xdExpressionBox xdDataBindingUI" title="" xd:xctname="ExpressionBox" xd:CtrlId="CTRL19" tabIndex="-1" xd:disableEditing="yes" style="FONT-WEIGHT: bold; WIDTH: 43px">
												<xsl:value-of select="patient/patient.sex"/>
											</span>
										</font>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div align="left"> </div>
				<div align="left">
					<table class="xdLayout" style="BORDER-RIGHT: medium none; TABLE-LAYOUT: fixed; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 752px; BORDER-BOTTOM: medium none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word" borderColor="buttontext" border="1">
						<colgroup>
							<col style="WIDTH: 375px"></col>
							<col style="WIDTH: 377px"></col>
						</colgroup>
						<tbody vAlign="top">
							<tr>
								<td>
									<div align="center">
										<em>
											<font face="Arial"><input class="langFont" title="" style="FONT-FAMILY: Arial" type="button" value="Add New Problem" xd:xctname="Button" xd:CtrlId="AddNewProblem"/>
											</font>
										</em>
									</div>
									<em>
										<font face="Arial"></font>
									</em>
									<div align="center">
										<table class="xdRepeatingTable msoUcTable" title="" style="TABLE-LAYOUT: fixed; WIDTH: 367px; BORDER-TOP-STYLE: none; BORDER-RIGHT-STYLE: none; BORDER-LEFT-STYLE: none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word; BORDER-BOTTOM-STYLE: none" border="1" xd:CtrlId="CTRL37">
											<colgroup>
												<col style="WIDTH: 367px"></col>
											</colgroup>
											<tbody class="xdTableHeader">
												<tr>
													<td>
														<div>
															<strong>
																<font face="Arial" size="3">Problems Added:</font>
															</strong>
														</div>
													</td>
												</tr>
											</tbody><tbody xd:xctname="RepeatingTable">
												<xsl:for-each select="problem_list/problem_added">
													<tr>
														<td><span class="xdExpressionBox xdDataBindingUI" title="" xd:xctname="ExpressionBox" xd:CtrlId="CTRL44" xd:binding="concat(position(), &quot;.&quot;)" style="FONT-WEIGHT: bold; FONT-SIZE: small; WIDTH: 24px; FONT-FAMILY: Arial; WHITE-SPACE: normal; TEXT-ALIGN: center">
																<xsl:value-of select="concat(position(), &quot;.&quot;)"/>
															</span><span class="xdExpressionBox xdDataBindingUI" title="" xd:xctname="ExpressionBox" xd:CtrlId="CTRL45" xd:binding="substring-before(substring-after(., &quot;^&quot;), &quot;^&quot;)" style="OVERFLOW-Y: hidden; FONT-SIZE: small; WIDTH: 286px; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; HEIGHT: 24px; WORD-WRAP: normal">
																<xsl:value-of select="substring-before(substring-after(., &quot;^&quot;), &quot;^&quot;)"/>
															</span><input class="langFont" title="" style="FONT-SIZE: xx-small; FONT-FAMILY: Arial" type="button" value="Delete" xd:xctname="Button" xd:CtrlId="DeleteNewProblem"/>
														</td>
													</tr>
												</xsl:for-each>
											</tbody>
										</table>
									</div>
								</td>
								<td>
									<div align="center">
										<em>
											<font face="Arial" size="2"><input class="langFont" title="" style="FONT-FAMILY: Arial" type="button" value="Remove Problem From List" xd:xctname="Button" xd:CtrlId="AddResolvedProblem"/>
											</font>
										</em>
									</div>
									<div align="center">
										<table class="xdRepeatingTable msoUcTable" title="" style="TABLE-LAYOUT: fixed; WIDTH: 361px; BORDER-TOP-STYLE: none; BORDER-RIGHT-STYLE: none; BORDER-LEFT-STYLE: none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word; BORDER-BOTTOM-STYLE: none" border="1" xd:CtrlId="CTRL49">
											<colgroup>
												<col style="WIDTH: 361px"></col>
											</colgroup>
											<tbody class="xdTableHeader">
												<tr>
													<td>
														<div>
															<strong>
																<font face="Arial" size="3">Problems Removed:</font>
															</strong>
														</div>
													</td>
												</tr>
											</tbody><tbody xd:xctname="RepeatingTable">
												<xsl:for-each select="problem_list/problem_resolved">
													<tr>
														<td><span class="xdExpressionBox xdDataBindingUI" title="" xd:xctname="ExpressionBox" xd:CtrlId="CTRL56" xd:binding="concat(position(), &quot;.&quot;)" style="FONT-WEIGHT: bold; FONT-SIZE: small; WIDTH: 24px; FONT-FAMILY: Arial; WHITE-SPACE: normal; TEXT-ALIGN: center">
																<xsl:value-of select="concat(position(), &quot;.&quot;)"/>
															</span><span class="xdExpressionBox xdDataBindingUI" title="" xd:xctname="ExpressionBox" xd:CtrlId="CTRL57" xd:binding="substring-before(substring-after(., &quot;^&quot;), &quot;^&quot;)" style="OVERFLOW-Y: hidden; FONT-SIZE: small; WIDTH: 276px; FONT-FAMILY: Arial; WHITE-SPACE: nowrap; HEIGHT: 22px; WORD-WRAP: normal">
																<xsl:value-of select="substring-before(substring-after(., &quot;^&quot;), &quot;^&quot;)"/>
															</span><input class="langFont" title="" style="FONT-SIZE: xx-small; FONT-FAMILY: Arial" type="button" value="Delete" xd:xctname="Button" xd:CtrlId="DeleteResolvedProblem"/>
														</td>
													</tr>
												</xsl:for-each>
											</tbody>
										</table>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div align="left"> </div>
				<div align="left">
					<table class="xdLayout" style="BORDER-RIGHT: medium none; TABLE-LAYOUT: fixed; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 756px; BORDER-BOTTOM: medium none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word" borderColor="buttontext" width="undefined" border="1">
						<colgroup>
							<col style="WIDTH: 756px"></col>
						</colgroup>
						<tbody vAlign="top">
							<tr>
								<td style="BORDER-RIGHT: #000000 1pt solid; PADDING-RIGHT: 5px; BORDER-TOP: #000000 1pt solid; PADDING-LEFT: 5px; PADDING-BOTTOM: 5px; VERTICAL-ALIGN: middle; BORDER-LEFT: #000000 1pt solid; PADDING-TOP: 5px; BORDER-BOTTOM: #000000 1pt solid">
									<div>
										<font face="Arial" size="2">Form completed today by: <span class="xdExpressionBox xdDataBindingUI" title="" xd:xctname="ExpressionBox" xd:CtrlId="CTRL120" tabIndex="-1" xd:binding="substring-after(encounter/encounter.provider_id, &quot;^&quot;)" xd:disableEditing="yes" style="FONT-WEIGHT: bold; FONT-SIZE: medium; WIDTH: 369px; FONT-FAMILY: Bradley Hand ITC">
												<xsl:value-of select="substring-after(encounter/encounter.provider_id, &quot;^&quot;)"/>
											</span>  <span class="xdExpressionBox xdDataBindingUI" title="" xd:xctname="ExpressionBox" xd:CtrlId="CTRL121" tabIndex="-1" xd:disableEditing="yes">
												<xsl:attribute name="style">FONT-WEIGHT: bold; FONT-SIZE: medium; WIDTH: 10px; COLOR: #ffffff;<xsl:choose>
														<xsl:when test="encounter/encounter.provider_id = &quot;&quot;">COLOR: #ff0000</xsl:when>
													</xsl:choose>
												</xsl:attribute>
												<xsl:value-of select="&quot;*&quot;"/>
											</span><input class="langFont" title="" type="button" value="&lt;- Select Provider" xd:xctname="Button" xd:CtrlId="SelectProvider" tabIndex="0"/>
										</font>
										<font face="Arial" size="2"></font>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div align="left"> </div>
				<div align="right"><input class="langFont" title="" style="FONT-SIZE: medium; WIDTH: 129px; FONT-FAMILY: Arial" type="button" size="1" value="Submit" xd:xctname="Button" xd:CtrlId="SubmitButton" tabIndex="0"/>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
