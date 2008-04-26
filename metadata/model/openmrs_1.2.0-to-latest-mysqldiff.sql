#--------------------------------------
# USE:
#  The diffs are ordered by datamodel version number.
#--------------------------------------

ALTER TABLE `obs` ADD INDEX `obs_group_id`(`obs_group_id`);

#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;
