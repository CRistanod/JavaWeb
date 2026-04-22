package com.sky.controller.user;

import com.sky.dto.FlashSalePurchaseDTO;
import com.sky.result.Result;
import com.sky.service.FlashSaleService;
import com.sky.vo.FlashSaleActivityVO;
import com.sky.vo.FlashSalePurchaseVO;
import com.sky.vo.FlashSaleResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userFlashSaleController")
@RequestMapping("/user/flashsale")
@Api(tags = "用户端-特卖活动接口")
@Slf4j
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @GetMapping("/list")
    @ApiOperation("查询当前可抢购的特卖活动列表")
    public Result<List<FlashSaleActivityVO>> list() {
        return Result.success(flashSaleService.listCurrentActivities());
    }

    @GetMapping("/{id}")
    @ApiOperation("查询特卖活动详情")
    @ApiImplicitParam(name = "id", value = "活动ID", required = true, dataTypeClass = Long.class, paramType = "path")
    public Result<FlashSaleActivityVO> getById(@PathVariable Long id) {
        return Result.success(flashSaleService.getDetail(id));
    }

    @PostMapping("/purchase")
    @ApiOperation("发起特卖抢购")
    public Result<FlashSalePurchaseVO> purchase(@RequestBody FlashSalePurchaseDTO purchaseDTO) {
        log.info("用户发起特卖抢购：{}", purchaseDTO);
        return Result.success(flashSaleService.purchase(purchaseDTO));
    }

    @GetMapping("/result/{requestId}")
    @ApiOperation("查询特卖抢购结果")
    @ApiImplicitParam(name = "requestId", value = "抢购请求幂等ID", required = true, dataTypeClass = String.class, paramType = "path")
    public Result<FlashSaleResultVO> result(@PathVariable String requestId) {
        return Result.success(flashSaleService.getPurchaseResult(requestId));
    }
}
