package com.sky.controller.admin;

import com.sky.dto.FlashSaleActivityDTO;
import com.sky.dto.FlashSaleActivityPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.FlashSaleService;
import com.sky.vo.FlashSaleActivityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/flashsale")
@Api(tags = "管理端-特卖活动接口")
@Slf4j
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @PostMapping
    @ApiOperation("新增特卖活动")
    public Result save(@RequestBody FlashSaleActivityDTO flashSaleActivityDTO) {
        log.info("新增特卖活动：{}", flashSaleActivityDTO);
        flashSaleService.saveActivity(flashSaleActivityDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改特卖活动")
    public Result update(@RequestBody FlashSaleActivityDTO flashSaleActivityDTO) {
        log.info("修改特卖活动：{}", flashSaleActivityDTO);
        flashSaleService.updateActivity(flashSaleActivityDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询特卖活动")
    public Result<PageResult> page(FlashSaleActivityPageQueryDTO queryDTO) {
        log.info("分页查询特卖活动：{}", queryDTO);
        return Result.success(flashSaleService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation("查询特卖活动详情")
    @ApiImplicitParam(name = "id", value = "活动ID", required = true, dataTypeClass = Long.class, paramType = "path")
    public Result<FlashSaleActivityVO> getById(@PathVariable Long id) {
        return Result.success(flashSaleService.getDetail(id));
    }

    @PostMapping("/{id}/enable")
    @ApiOperation("启用特卖活动")
    @ApiImplicitParam(name = "id", value = "活动ID", required = true, dataTypeClass = Long.class, paramType = "path")
    public Result enable(@PathVariable Long id) {
        flashSaleService.enable(id);
        return Result.success();
    }

    @PostMapping("/{id}/disable")
    @ApiOperation("停用特卖活动")
    @ApiImplicitParam(name = "id", value = "活动ID", required = true, dataTypeClass = Long.class, paramType = "path")
    public Result disable(@PathVariable Long id) {
        flashSaleService.disable(id);
        return Result.success();
    }

    @PostMapping("/{id}/preheat")
    @ApiOperation("预热活动到Redis")
    @ApiImplicitParam(name = "id", value = "活动ID", required = true, dataTypeClass = Long.class, paramType = "path")
    public Result preheat(@PathVariable Long id) {
        flashSaleService.preheat(id);
        return Result.success();
    }
}
