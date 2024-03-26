CREATE TABLE `inventory`
(
    `id`     VARCHAR(36) NOT NULL,
    --`isbn`   VARCHAR(36) NOT NULL,
    `copies` INTEGER     NOT NULL default 0,
    PRIMARY KEY (`id`)
);