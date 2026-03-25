package com.foodie.platform.service;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:51
 */
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.platform.CommissionConfigDTO;
import com.foodie.dto.platform.SettlementAuditDTO;
import com.foodie.dto.platform.SettlementGenerateDTO;
import com.foodie.entity.PlatformCommission;
import com.foodie.vo.platform.FinanceReportVO;
import com.foodie.vo.platform.SettlementVO;

/**
 * 财务服务接口
 */
public interface FinanceService {

    /**
     * 结算单分页查询
     */
    Page<SettlementVO> pageQuerySettlement(Integer page, Integer pageSize,
                                           Long merchantId, String settlementCycle, Integer status);

    /**
     * 生成结算单
     */
    void generateSettlement(SettlementGenerateDTO settlementGenerateDTO);

    /**
     * 结算审核
     */
    void auditSettlement(SettlementAuditDTO settlementAuditDTO);

    /**
     * 抽成配置分页查询
     */
    Page<PlatformCommission> pageQueryCommission(Integer page, Integer pageSize, Integer configType);

    /**
     * 新增抽成配置
     */
    void addCommission(CommissionConfigDTO commissionConfigDTO);

    /**
     * 修改抽成配置
     */
    void updateCommission(Long id, CommissionConfigDTO commissionConfigDTO);

    /**
     * 删除抽成配置
     */
    void deleteCommission(Long id);

    /**
     * 财务报表
     */
    FinanceReportVO getFinanceReport();
}
