The liquibase 1.9.4 patch contains these modifications:

1) Added GroupedChange so that exporting of xml files is smaller in size (only used when dumping databases into xml changelog files)

2) Added attribute to generateChangeLog ant task to allow for passing in of diffTypes argument

3) Modified indexExists and columnExists to speed up preconditions - http://dev.openmrs.org/ticket/1719 
