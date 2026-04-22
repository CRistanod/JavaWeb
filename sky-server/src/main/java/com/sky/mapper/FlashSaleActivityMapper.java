package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.FlashSaleActivityPageQueryDTO;
import com.sky.entity.FlashSaleActivity;
import com.sky.enumeration.OperationType;
import com.sky.vo.FlashSaleActivityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FlashSaleActivityMapper {

    @AutoFill(OperationType.INSERT)
    void insert(FlashSaleActivity flashSaleActivity);

    @AutoFill(OperationType.UPDATE)
    void update(FlashSaleActivity flashSaleActivity);

    @Select("select * from flash_sale_activity where id = #{id}")
    FlashSaleActivity getById(Long id);

    Page<FlashSaleActivityVO> pageQuery(FlashSaleActivityPageQueryDTO queryDTO);

    FlashSaleActivityVO getDetailVOById(Long id);

    List<FlashSaleActivityVO> listCurrentActivities(@Param("now") LocalDateTime now);

    int decrementAvailableStock(@Param("id") Long id, @Param("amount") Integer amount);

    int incrementAvailableStock(@Param("id") Long id, @Param("amount") Integer amount);
}
