-- JDBC Git repository store
-- version 1

-- Create table for StoredConfig
CREATE TABLE stored_config (
     repo_name varchar(255) not null,
     section varchar(255) not null,
     sub_section varchar(255) not null,
     config_name varchar(255) not null,
     config_value varchar(255) not null, /* ? */
  /* entry_timestamp,
     version_no
     entry_userid */
   primary key (
    repo_name,
    section,
    sub_section,
    config_name
   )
);

-- Create table git objects
create table objects (
     /* repo_name varchar(255) not null, */
     id bytea not null,
     object_type int not null,
     object_size bigint not null,
     content bytea not null,
     base_id bytea null,
   primary key (
    /* repo_name, */
    id
   )
);