CREATE TABLE `bookModel`
(
    `id`     varchar(36)    NOT NULL,
    `author` varchar(64)    NOT NULL,
    `title`  varchar(128)   NOT NULL,
    `price`  decimal(10, 2) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;