package com.sky.mapper;

import com.sky.entity.FlashSaleOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FlashSaleOrderMapper {

    void insert(FlashSaleOrder flashSaleOrder);

    void update(FlashSaleOrder flashSaleOrder);

    @Select("select * from flash_sale_order where id = #{id}")
    FlashSaleOrder getById(Long id);

    @Select("select * from flash_sale_order where request_id = #{requestId} limit 1")
    FlashSaleOrder getByRequestId(String requestId);

    @Select("select * from flash_sale_order where order_id = #{orderId} limit 1")
    FlashSaleOrder getByOrderId(Long orderId);

    @Select("select * from flash_sale_order where activity_id = #{activityId} and user_id = #{userId} limit 1")
    FlashSaleOrder getByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);

    List<Map<String, Object>> countActiveUserPurchases(Long activityId);
}
