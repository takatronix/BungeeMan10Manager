create table message_log
(
	id int auto_increment,
	uuid varchar(36) null,
	mcid varchar(16) null,
	message varchar(256) null,
	date datetime default now() null,
	constraint message_log_pk
		primary key (id)
);

create index message_log_uuid_message_index
	on message_log (uuid,message);

