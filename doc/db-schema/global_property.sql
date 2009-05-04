CREATE TABLE `global_property` (
	`property` VARCHAR(255) DEFAULT '' NOT NULL,
	`property_value` MEDIUMTEXT,
	`description` TEXT,
	`property_type` INTEGER UNSIGNED DEFAULT 0 NOT NULL,
	`default_value` MEDIUMTEXT DEFAULT '' NOT NULL,
	PRIMARY KEY (`property`)
);

CREATE INDEX `type_of_global_property` ON `global_property` (`property_type` ASC);

ALTER TABLE `global_property` ADD CONSTRAINT `type_of_global_property` FOREIGN KEY (`property_type`)
	REFERENCES `global_property_type` (`global_property_type_id`)
	ON UPDATE CASCADE;

