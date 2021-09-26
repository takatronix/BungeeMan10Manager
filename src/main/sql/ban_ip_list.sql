create table ban_ip_list
(
	id int auto_increment,
	ip_address varchar(24) not null,
	date datetime default now() null,
	reason varchar(64) null,
	constraint ban_ip_list_pk
		primary key (id)
);

