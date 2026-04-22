CREATE TABLE IF NOT EXISTS flash_sale_activity
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    activity_name   VARCHAR(64)    NOT NULL COMMENT '活动名称',
    dish_id         BIGINT         NOT NULL COMMENT '菜品ID',
    sale_price      DECIMAL(10, 2) NOT NULL COMMENT '特卖价',
    stock           INT            NOT NULL COMMENT '总库存',
    available_stock INT            NOT NULL COMMENT '剩余库存',
    limit_per_user  INT            NOT NULL DEFAULT 1 COMMENT '单用户限购次数',
    start_time      DATETIME       NOT NULL COMMENT '活动开始时间',
    end_time        DATETIME       NOT NULL COMMENT '活动结束时间',
    status          TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0禁用 1启用',
    create_time     DATETIME       NULL COMMENT '创建时间',
    update_time     DATETIME       NULL COMMENT '更新时间',
    create_user     BIGINT         NULL COMMENT '创建人',
    update_user     BIGINT         NULL COMMENT '更新人',
    KEY idx_dish_id (dish_id),
    KEY idx_status_time (status, start_time, end_time)
) COMMENT='特卖活动表';

CREATE TABLE IF NOT EXISTS flash_sale_order
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    activity_id BIGINT       NOT NULL COMMENT '活动ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    order_id    BIGINT       NULL COMMENT '正式订单ID',
    request_id  VARCHAR(64)  NOT NULL COMMENT '抢购请求幂等ID',
    status      TINYINT      NOT NULL COMMENT '状态：1已创建待支付 2已支付 3已取消 4创建失败',
    fail_reason VARCHAR(255) NULL COMMENT '失败或取消原因',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    UNIQUE KEY uk_request_id (request_id),
    KEY idx_activity_user (activity_id, user_id),
    KEY idx_order_id (order_id)
) COMMENT='特卖抢购记录表';
