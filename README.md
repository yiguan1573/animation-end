# animation-end
视频弹幕项目的后端，使用auth2、redis、minio、kafka、nacos、gateway
animation-consumer8500模块没有使用，因为本项目链路很短，没有使用消费者模块的必要
## 表格
![Snipaste_2022-11-25_23-24-58.png](http://tva1.sinaimg.cn/large/006QQPIfly1h8hsg852dtj307i06wmy7.jpg)
```sql
-- auto-generated definition
create table animation_bullet
(
    id           int auto_increment,
    animation_id int            null comment '动画id',
    user_id      int            null comment '用户id',
    episode_no   int            null comment '当前集数',
    content      varchar(300)   null comment '弹幕内容',
    type         int            null comment '弹幕方向 0代表default 1代表top',
    color        varchar(30)    null comment '弹幕颜色',
    send_time    decimal(10, 3) null comment '弹幕发送时间单位s',
    constraint animation_bullet_id_uindex
        unique (id)
);

alter table animation_bullet
    add primary key (id);
    
-- auto-generated definition
create table animation_collect
(
    id           int auto_increment,
    animation_id int              null comment '动画id',
    user_id      int              null comment '用户id',
    deleted      int(3) default 0 null comment '该动画是否被删除的标志位，1代表被删除，0代表没有被删除',
    constraint animation_collect_id_uindex
        unique (id)
)
    comment '收藏表';

alter table animation_collect
    add primary key (id);
    
-- auto-generated definition
create table animation_file
(
    id           int auto_increment,
    animation_id int          null comment '关联animation_info表的id',
    episode_no   int          null comment '第几集',
    file_size    double       null comment '单位mb',
    file_name    varchar(500) null comment '文件名',
    constraint animation_file_id_uindex
        unique (id)
)
    comment 'animation文件信息';

alter table animation_file
    add primary key (id);
    
-- auto-generated definition
create table animation_history
(
    id            int auto_increment,
    animation_id  int           null comment '动画id',
    user_id       int           null comment '用户id',
    episode_no    int           null comment '当前集数',
    specific_time int           null comment '最后观看位于动画的具体时间单位s',
    deleted       int default 0 null comment '1代表该动画被删除0代表未被删除',
    update_time   bigint        null comment '更新时间',
    constraint animation_history_id_uindex
        unique (id)
);

alter table animation_history
    add primary key (id);
    
-- auto-generated definition
create table animation_history
(
    id            int auto_increment,
    animation_id  int           null comment '动画id',
    user_id       int           null comment '用户id',
    episode_no    int           null comment '当前集数',
    specific_time int           null comment '最后观看位于动画的具体时间单位s',
    deleted       int default 0 null comment '1代表该动画被删除0代表未被删除',
    update_time   bigint        null comment '更新时间',
    constraint animation_history_id_uindex
        unique (id)
);

alter table animation_history
    add primary key (id);
    
-- auto-generated definition
create table animation_message
(
    id          int auto_increment,
    title       varchar(200)  null,
    content     varchar(2000) null,
    update_time varchar(30)   null,
    constraint animation_message_id_uindex
        unique (id)
);

alter table animation_message
    add primary key (id);

-- auto-generated definition
create table authorization_info
(
    id                 int auto_increment,
    authorization_name varchar(200)  null comment '授权名',
    authorization_url  varchar(2000) null comment '授权的url',
    constraint authorization_info_id_uindex
        unique (id)
);

alter table authorization_info
    add primary key (id);

-- auto-generated definition
create table oauth_client_details
(
    client_id               varchar(255) not null comment '客户端ID',
    resource_ids            varchar(255) null comment '资源ID集合,多个资源时用逗号(,)分隔',
    client_secret           varchar(255) null comment '客户端密匙',
    scope                   varchar(255) null comment '客户端申请的权限范围',
    authorized_grant_types  varchar(255) null comment '客户端支持的grant_type',
    web_server_redirect_uri varchar(255) null comment '重定向URI',
    authorities             varchar(255) null comment '客户端所拥有的Spring Security的权限值，多个用逗号(,)分隔',
    access_token_validity   int          null comment '访问令牌有效时间值(单位:秒)',
    refresh_token_validity  int          null comment '更新令牌有效时间值(单位:秒)',
    additional_information  varchar(255) null comment '预留字段',
    autoapprove             varchar(255) null comment '用户是否自动Approval操作'
);

INSERT INTO animation.oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) VALUES ('animation', null, '{noop}123456', 'all', 'password,refresh_token,authorization_code', null, null, 604800, 2592000, null, 'true')

-- auto-generated definition
create table role_info
(
    id                 int auto_increment,
    role_name          varchar(100)  null comment '角色名',
    authorization_list varchar(2000) null comment '权限列表',
    constraint role_info_id_uindex
        unique (id)
);

alter table role_info
    add primary key (id);

-- auto-generated definition
create table upload_task
(
    id          bigint auto_increment,
    upload_id   varchar(255) not null comment '分片上传的uploadId',
    md5         varchar(500) not null comment '文件唯一标识（md5）',
    file_name   varchar(500) not null comment '文件名',
    bucket_name varchar(255) not null comment '所属桶名',
    total_size  bigint       not null comment '文件大小（byte）',
    chunk_size  bigint       not null comment '每个分片大小（byte）',
    chunk_num   int          not null comment '分片数量',
    status      int          null comment '1表示完成，0表示正在上传',
    constraint upload_task_id_uindex
        unique (id)
)
    comment '分片上传-分片任务记录';

alter table upload_task
    add primary key (id);

-- auto-generated definition
create table user_info
(
    id        int auto_increment,
    user_name varchar(64)   null comment '用户名',
    password  varchar(2000) null comment '加密的密码',
    role_list varchar(2000) null comment '角色集合',
    image     varchar(200)  null comment '用户头像名',
    constraint user_info_id_uindex
        unique (id),
    constraint user_info_user_name_uindex
        unique (user_name)
);

alter table user_info
    add primary key (id);


```

## 服务部署情况
101 nginx:3737 	nacos	mysql kafka zookeeper  
102 nacos	sentinel:8080	minio:9010  kafka zookeeper  
103 nacos	seata:8091   redis:6379 kafka zookeeper
