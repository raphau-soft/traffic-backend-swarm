USE `traffic_generator`;

CREATE TABLE IF NOT EXISTS `test`
(
    `id`        int         NOT NULL AUTO_INCREMENT,
    `name`      varchar(45) NOT NULL,
    `start_timestamp` bigint(45)  NOT NULL,
    `end_timestamp` bigint(45),
    `finished`  boolean     NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE INDEX TEST_INDEX ON test(id);

CREATE TABLE IF NOT EXISTS `test_parameters`
(
    `id`                    int NOT NULL AUTO_INCREMENT,
    `test_id`               int NOT NULL,
    `number_of_requests`    int NOT NULL,
    `test_time`             int NOT NULL,
    `time_between_requests` int NOT NULL,
    `first`                 int NOT NULL,
    `second`                int NOT NULL,
    `third`                 int NOT NULL,
    `request_limit`         bool NOT NULL,
    `time_limit`            bool NOT NULL,
    `break_between_trades`  int NOT NUll,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`test_id`) REFERENCES `test` (`id`)
);

CREATE INDEX TEST_PARAM_INDEX ON test_parameters(id);

CREATE TABLE IF NOT EXISTS `traffic_generator_cpu_data`
(
    `id`        int        NOT NULL AUTO_INCREMENT,
    `test_id`   int        NOT NULL,
    `timestamp` bigint(45) NOT NULL,
    `cpu_usage` double     NOT NULL,
    `memory`    double      NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`test_id`) REFERENCES `test` (`id`)
);

CREATE INDEX TGC_INDEX ON traffic_generator_cpu_data(id, test_id);

CREATE TABLE IF NOT EXISTS `traffic_generator_time_data`
(
    `id`               int        NOT NULL AUTO_INCREMENT,
    `test_id`          int        NOT NULL,
    `database_time`    bigint(45),
    `api_time`         bigint(45),
    `application_time` bigint(45),
    `timestamp`        bigint(45),
    `endpoint_url`     varchar(45),
    `queue_size_forward`       int,
    `queue_size_back`       int,
    `stock_id`         varchar(45),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`test_id`) REFERENCES `test` (`id`)
);

CREATE INDEX TGT_INDEX ON traffic_generator_time_data(id, test_id);

CREATE TABLE IF NOT EXISTS `stock_exchange_cpu_data`
(
    `id`        int         NOT NULL AUTO_INCREMENT,
    `test_id`   int         NOT NULL,
    `timestamp` bigint      NOT NULL,
    `cpu_usage` double      NOT NULL,
    `memory`    double      NOT NULL,
    `stock_id`  varchar(45),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`test_id`) REFERENCES `test` (`id`)
);

CREATE INDEX SEC_INDEX ON stock_exchange_cpu_data(id, test_id);

CREATE TABLE IF NOT EXISTS `stock_exchange_time_data`
(
    `id`                       int    NOT NULL AUTO_INCREMENT,
    `test_id`                  int    NOT NULL,
    `timestamp`                bigint NOT NULL,
    `application_time`         bigint NOT NULL,
    `database_time`            bigint NOT NULL,
    `number_of_sell_offers`    bigint NOT NULL,
    `number_of_buy_offers`     bigint NOT NULL,
    `stock_id`                 varchar(45),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`test_id`) REFERENCES `test` (`id`)
);

CREATE INDEX SET_INDEX ON stock_exchange_time_data(id, test_id);