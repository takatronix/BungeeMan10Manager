create table connection_log
(
    id                 int auto_increment
        primary key,
    mcid               varchar(16)  null,
    uuid               varchar(36)  not null,
    server             varchar(16)  not null comment '接続サーバー名',
    connected_time     datetime     not null comment '接続時刻',
    disconnected_time  datetime     null comment '切断時刻',
    connection_seconds int          null comment '接続していた秒数: Disconnect時に保存',
    ip                 varchar(256) null         '接続元IPアドレス'
    port               int    null comment '接続元ポート'
);

create index connection_log_uuid_index
    on connection_log (uuid);
