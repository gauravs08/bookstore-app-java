CREATE TABLE `inventory`
(
    `id`     varchar(36)    NOT NULL,
    `copies` integer    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;