package com.foodie.user.controller;

import com.foodie.common.result.Result;
import com.foodie.user.service.SystemNoticeService;
import com.foodie.pojo.dto.NoticeQueryDTO;
import com.foodie.pojo.vo.SystemNoticeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 系统公告控制器（用户端）
 */
@Slf4j
@RestController
@RequestMapping("/user/notice")
@Api(tags = "用户端-系统公告")
public class SystemNoticeController {

    @Resource
    private SystemNoticeService systemNoticeService;

    /**
     * 分页查询公告列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询公告列表")
    public Result<Map<String, Object>> getNoticePage(
            @ApiParam("公告类型(1-系统公告 2-活动公告)") @RequestParam(required = false) Integer type,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("查询公告列表: type={}, page={}, pageSize={}", type, page, pageSize);

        // 构建查询参数
        NoticeQueryDTO query = new NoticeQueryDTO();
        query.setType(type);
        query.setPage(page);
        query.setPageSize(pageSize);

        // 查询数据
        Map<String, Object> result = systemNoticeService.getNoticePage(query);

        return Result.success(result);
    }

    /**
     * 查询公告详情
     */
    @GetMapping("/{noticeId}")
    @ApiOperation("查询公告详情")
    public Result<SystemNoticeVO> getNoticeDetail(
            @ApiParam("公告ID") @PathVariable Long noticeId) {

        log.info("查询公告详情: noticeId={}", noticeId);

        SystemNoticeVO notice = systemNoticeService.getNoticeDetail(noticeId);

        return Result.success(notice);
    }
}