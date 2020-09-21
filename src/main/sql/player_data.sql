create table player_data
(
	id int auto_increment,
	uuid varchar(36) not null,
	mcid varchar(16) null,
	freeze_until datetime null,
	mute_until datetime null,
	jail_until datetime null,
	ban_until datetime null,
	score int default 0,
	constraint player_data_pk
		primary key (id)
)
comment 'プレイヤーデータ';

create index player_data_uuid_mcid_index
	on player_data (uuid, mcid);
