package com.sky.constant;

public final class FlashSaleRedisConstant {

    private FlashSaleRedisConstant() {
    }

    public static final String ACTIVITY_INFO_KEY = "flashsale:activity:%d:info";
    public static final String ACTIVITY_STOCK_KEY = "flashsale:activity:%d:stock";
    public static final String ACTIVITY_USER_COUNT_KEY = "flashsale:activity:%d:usercount";
    public static final String REQUEST_RESULT_KEY = "flashsale:request:%s";

    public static final String REQUEST_PENDING = "PENDING";
    public static final String REQUEST_SUCCESS_PREFIX = "SUCCESS:";
    public static final String REQUEST_FAIL_PREFIX = "FAIL:";
}
