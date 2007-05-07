#--------------------------------------
# USE:
#  The diffs are ordered by datamodel version number.
#--------------------------------------

DROP PROCEDURE IF EXISTS update_user_password;
DROP PROCEDURE IF EXISTS insert_patient_stub;
DROP PROCEDURE IF EXISTS insert_user_stub;




#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;