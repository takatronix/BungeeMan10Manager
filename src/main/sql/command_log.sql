create table command_log
(
	id int auto_increment,
	uuid varchar(36) not null,
	mcid varchar(16) null,
	command varchar(256) null,
	date datetime default now() null,
	constraint command_log_pk
		primary key (id)
);

create index command_log_uuid_command_index
	on command_log (uuid,command);

