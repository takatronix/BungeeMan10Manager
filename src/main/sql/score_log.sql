create table score_log
(
	id int auto_increment,
	mcid varchar(16) null,
	uuid varchar(36) null,
	score int null,
	note varchar(256) null,
	issuer varchar(16) null,
	date datetime null,
	constraint score_log_pk
		primary key (id)
);

create index score_log_mcid_uuid_index
	on score_log (mcid, uuid);

