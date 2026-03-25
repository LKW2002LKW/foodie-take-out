package com.foodie.merchant.service;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:30
 */
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.merchant.IncomeQueryDTO;
import com.foodie.dto.merchant.SettlementQueryDTO;
import com.foodie.vo.merchant.IncomeDetailVO;
import com.foodie.vo.merchant.SettlementRecordVO;


/**
 * 商家财务服务接口
 */
public interface MerchantFinanceService {

    /**
     * 结算记录分页查询
     */
    Page<SettlementRecordVO> pageQuerySettlement(SettlementQueryDTO settlementQueryDTO);

    /**
     * 收入明细分页查询
     */
    Page<IncomeDetailVO> pageQueryIncome(IncomeQueryDTO incomeQueryDTO);

    /**
     * 提交提现申请
     */
    //void applyWithdrawal(WithdrawalApplicationDTO withdrawalApplicationDTO);

    /**
     * 提现记录分页查询
     */
    //Page<WithdrawalRecordVO> pageQueryWithdrawal(Integer page, Integer pageSize, Integer status);

    /**
     * 可提现余额
     */
    //java.math.BigDecimal getAvailableBalance();
}
