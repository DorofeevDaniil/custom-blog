create table if not exists posts(
    id bigserial primary key,
    title varchar(256) not null,
    text varchar(1000000000) not null,
    image_path varchar(256) not null,
    likes_count integer not null,
    tags varchar(500) not null
);

create table if not exists comments(
    id bigserial primary key,
    post_id bigint not null,
    text varchar(1000000000) not null,
    foreign key (post_id) references posts(id)
);