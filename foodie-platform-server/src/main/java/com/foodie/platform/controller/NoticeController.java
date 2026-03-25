package com.foodie.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.platform.NoticeDTO;
import com.foodie.entity.SystemNotice;
import com.foodie.platform.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/platform/notice")
@Api(tags = "平台端-公告管理接口")
@Slf4j
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 公告分页查询
     */
    @GetMapping("/page")
    @ApiOperation("公告分页查询")
    public Result<Page<SystemNotice>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        log.info("公告分页查询");
        Page<SystemNotice> result = noticeService.pageQuery(page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 发布公告
     */
    @PostMapping("/publish")
    @ApiOperation("发布公告")
    public Result<Void> publishNotice(@RequestBody @Validated NoticeDTO noticeDTO) {
        log.info("发布公告：{}", noticeDTO);
        noticeService.publishNotice(noticeDTO);
        return Result.success();
    }

    /**
     * 保存草稿
     */
    @PostMapping("/draft")
    @ApiOperation("保存草稿")
    public Result<Void> saveDraft(@RequestBody @Validated NoticeDTO noticeDTO) {
        log.info("保存公告草稿：{}", noticeDTO);
        noticeService.saveDraft(noticeDTO);
        return Result.success();
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除公告")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        log.info("删除公告：{}", id);
        noticeService.deleteNotice(id);
        return Result.success();
    }
}