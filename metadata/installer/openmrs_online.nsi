!include LogicLib.nsh
!include nsDialogs.nsh
!include installer_setup.nsh

!define TOMCAT_VERSION 6.0
!define MYSQL_VERSION 5.1
!define PRODUCT_NAME "OpenMRS"
!define JAVA_VERSION

;Parameters used by the downloading window and progress bar. 
LangString DESC_REMAINING ${LANG_ENGLISH} " (%d %s%s remaining)"
LangString DESC_PROGRESS ${LANG_ENGLISH} "%d.%01dkB" ;"%dkB (%d%%) of %dkB @ %d.%01dkB"
LangString DESC_PLURAL ${LANG_ENGLISH} "s"
LangString DESC_HOUR ${LANG_ENGLISH} "hour"
LangString DESC_MINUTE ${LANG_ENGLISH} "minute"
LangString DESC_SECOND ${LANG_ENGLISH} "second"
LangString DESC_DOWNLOADING_JAVA ${LANG_ENGLISH} "Downloading Java"
LangString DESC_DOWNLOADING_TOMCAT ${LANG_ENGLISH} "Downloading Tomcat"
LangString DESC_DOWNLOADING_MYSQL ${LANG_ENGLISH} "Downloading MYSQL"
LangString DESC_DOWNLOADING_OPENMRS_WAR ${LANG_ENGLISH} "Downloading Openmrs.war"


Var OpenmrsWarFile

Function DownloadOpenmrsWar
    StrCpy $OpenmrsWarFile "$OpenmrsWar\openmrs.war"
	nsisdl::download /TRANSLATE "$(DESC_DOWNLOADING_OPENMRS_WAR)" "$(DESC_CONNECTING)" \
       "$(DESC_SECOND)" "$(DESC_MINUTE)" "$(DESC_HOUR)" "$(DESC_PLURAL)" \
       "$(DESC_PROGRESS)" "$(DESC_REMAINING)" \
	   /TIMEOUT=30000 ${OPENMRS_WAR_DOWNLOAD_URL} $OpenmrsWarFile
FunctionEnd

Function VerifyOpenmrsWarStatus
	Pop $R0 ;Get the return value
	StrCmp $R0 "success" +4
		MessageBox MB_OK "Download failed (or) cancelled: $R0"
		Quit
FunctionEnd

Function DownloadJava
	nsisdl::download /TRANSLATE "$(DESC_DOWNLOADING_JAVA)" "$(DESC_CONNECTING)" \
       "$(DESC_SECOND)" "$(DESC_MINUTE)" "$(DESC_HOUR)" "$(DESC_PLURAL)" \
       "$(DESC_PROGRESS)" "$(DESC_REMAINING)" \
	   /TIMEOUT=30000 ${JAVA_6_DOWNLOAD_URL} $JavaSetup
FunctionEnd

Function VerifyJavaDownloadStatus
	Pop $R0 ;Get the return value
	StrCmp $R0 "success" +4
		StrCpy $JavaDownload false
		MessageBox MB_OK "Download failed: $R0"
		Quit
	StrCpy $JavaDownload true
FunctionEnd

Function DownloadTomcat
	nsisdl::download /TRANSLATE "$(DESC_DOWNLOADING_TOMCAT)" "$(DESC_CONNECTING)" \
       "$(DESC_SECOND)" "$(DESC_MINUTE)" "$(DESC_HOUR)" "$(DESC_PLURAL)" \
       "$(DESC_PROGRESS)" "$(DESC_REMAINING)" \
	   /TIMEOUT=30000 ${TOMCAT_DOWNLOAD_URL} $TomcatSetup
FunctionEnd

Function VerifyTomcatDownloadStatus
	Pop $R0 ;Get the return value
	StrCmp $R0 "success" +4
		StrCpy $TomcatDownload false
		MessageBox MB_OK "Download failed: $R0"
		Quit
	StrCpy $TomcatDownload true
FunctionEnd

Function DownloadMysql
	nsisdl::download /TRANSLATE "$(DESC_DOWNLOADING_MYSQL)" "$(DESC_CONNECTING)" \
       "$(DESC_SECOND)" "$(DESC_MINUTE)" "$(DESC_HOUR)" "$(DESC_PLURAL)" \
       "$(DESC_PROGRESS)" "$(DESC_REMAINING)" \
	   /TIMEOUT=30000 ${MYSQL_DOWNLOAD_URL} $MysqlSetup
FunctionEnd

Function VerifyMysqlDownloadStatus
	Pop $R0 ;Get the return value
	StrCmp $R0 "success" +4
		StrCpy $MysqlDownload false
		MessageBox MB_OK "Download failed: $R0"
		Quit
	StrCpy $MysqlDownload true
FunctionEnd
