-- changeset frost:1
create table users
(
    id bigserial primary key,
    username varchar(64) not null unique,
    password varchar(128) not null
);