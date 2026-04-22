package com.sky.service;

import com.sky.dto.FlashSaleActivityDTO;
import com.sky.dto.FlashSaleActivityPageQueryDTO;
import com.sky.dto.FlashSalePurchaseDTO;
import com.sky.result.PageResult;
import com.sky.vo.FlashSaleActivityVO;
import com.sky.vo.FlashSalePurchaseVO;
import com.sky.vo.FlashSaleResultVO;

import java.util.List;

public interface FlashSaleService {

    void saveActivity(FlashSaleActivityDTO flashSaleActivityDTO);

    void updateActivity(FlashSaleActivityDTO flashSaleActivityDTO);

    PageResult pageQuery(FlashSaleActivityPageQueryDTO queryDTO);

    FlashSaleActivityVO getDetail(Long id);

    void enable(Long id);

    void disable(Long id);

    void preheat(Long id);

    List<FlashSaleActivityVO> listCurrentActivities();

    FlashSalePurchaseVO purchase(FlashSalePurchaseDTO purchaseDTO);

    FlashSaleResultVO getPurchaseResult(String requestId);
}
