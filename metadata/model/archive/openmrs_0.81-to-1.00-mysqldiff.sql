/* Create Table... */
create table concept_proposal(concept_proposal_id integer not null auto_increment primary key,
concept_id integer,
encounter_id integer,
original_text varchar(255) not null default '',
final_text varchar(255),
obs_id integer,
obs_concept_id integer,
state varchar(32) not null default 'unmapped',
comments varchar(255),
creator integer not null default '0',
date_created datetime not null default '0000-00-00 00:00:00',
changed_by integer,
date_changed datetime,
key concept_for_proposal(concept_id),
key encounter_for_proposal(encounter_id),
key user_who_created_proposal(creator),
key user_who_changed_proposal(changed_by))
engine=innodb
default charset=utf8;

create table global_property(property varchar(255),
property_value varchar(255))
engine=innodb
default charset=utf8;

insert into `global_property` values ('database_version', '1.0.0');

create table role_role(parent_role varchar(50) not null default '',
child_role varchar(255) not null default '',
key inherited_role(child_role))
engine=innodb
default charset=utf8;

alter table field add default_value text;

alter table form add build integer;

alter table form add published tinyint not null default '0';

alter table form add encounter_type integer;

alter table form add infopath_solution_version varchar(50);

alter table form add uri varchar(255);

alter table form add xslt mediumtext;

alter table note add parent integer;

alter table obs add comments varchar(255);

alter table patient_identifier add preferred tinyint not null default '0';

alter table patient_identifier_type add check_digit tinyint not null default '0';

alter table relationship drop foreign key relationship_type;

alter table relationship_type change relationship_id relationship_type_id integer;

alter table user_property add property varchar(100) not null default '';

alter table user_property add property_value varchar(255) not null default '';

alter table users add system_id varchar(50) not null default '';

alter table note modify column parent integer after priority;

alter table user_property modify column property varchar(100) not null default '' after user_id;

alter table users modify column username varchar(50);

alter table users modify column first_name varchar(50);

alter table users modify column middle_name varchar(50);

alter table users modify column last_name varchar(50);

alter table users modify column password varchar(50);

alter table users modify column salt varchar(50);

alter table users modify column secret_question varchar(255);

alter table users modify column secret_answer varchar(255);

alter table users modify column void_reason varchar(255);

alter table obs modify column comments varchar(255) after date_stopped;

alter table user_property modify column property_value varchar(255) not null default '' after property;


/* Drop table-fields... */
alter table form drop definition;

alter table note drop weight;

alter table user_property drop `key`;

alter table obs drop comment;

alter table user_property drop value;


/* Create Primary Key... */
alter table patient_identifier drop foreign key defines_identifier_type;

alter table patient_identifier drop foreign key identifier_creator;

alter table patient_identifier drop foreign key identifier_voider;

alter table patient_identifier drop foreign key identifies_patient;

alter table patient_identifier drop foreign key patient_identifier_ibfk_2;

alter table patient_identifier drop primary key, add primary key (patient_id, identifier, identifier_type);

alter table user_property drop foreign key user_property;

alter table user_property drop primary key, add primary key (user_id, property);


/* Create Foreign Key... */
alter table concept_proposal add constraint concept_for_proposal foreign key (concept_id) references concept (concept_id);

alter table concept_proposal add constraint encounter_for_proposal foreign key (encounter_id) references encounter (encounter_id);

alter table concept_proposal add constraint user_who_changed_proposal foreign key (changed_by) references users (user_id);

alter table concept_proposal add constraint user_who_created_proposal foreign key (creator) references users (user_id);

alter table concept_proposal add constraint proposal_obs_concept_id foreign key (obs_concept_id) references concept (concept_id);

alter table concept_proposal add constraint proposal_obs_id foreign key (obs_id) references obs (obs_id);

alter table form add constraint form_encounter_type foreign key (encounter_type) references encounter_type (encounter_type_id);

alter table note add constraint note_hierarchy foreign key (parent) references note (note_id);

alter table patient_identifier add constraint defines_identifier_type foreign key (identifier_type) references patient_identifier_type (patient_identifier_type_id);

alter table patient_identifier add constraint identifier_creator foreign key (creator) references users (user_id);

alter table patient_identifier add constraint identifier_voider foreign key (voided_by) references users (user_id);

alter table patient_identifier add constraint identifies_patient foreign key (patient_id) references patient (patient_id);

alter table patient_identifier add constraint identifer_location foreign key (location_id) references location (location_id);

alter table relationship add constraint relationship_type_id foreign key (relationship) references relationship_type (relationship_type_id);

alter table user_property add constraint user_property foreign key (user_id) references users (user_id);

alter table role_role add primary key (parent_role, child_role);

/* Won't work!

alter table role_role add constraint inherited_role foreign key (child_role) references role (role);

alter table role_role add constraint parent_role foreign key (parent_role) references role (role);
*/

/* Testing script lines
alter table role add primary key (role);

alter table role_privilege add constraint role_privilege foreign key (role) references role (role);

alter table user_role add constraint role_definitions foreign key (role) references role (role);

alter table role_privilege drop foreign key role_privilege;

alter table user_role drop foreign key role_definitions;

alter table role drop primary key; */