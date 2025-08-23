package com.esun.like.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ListLikesResponse {
    private List<Item> items;
    private BigDecimal sumTotalAmount;
    private BigDecimal sumTotalFee;

    @Data
    public static class Item {
        private Long sn;
        private String userId;
        private String userName;
        private String email;
        private String account;
        private Long productNo;
        private String productName;
        private BigDecimal price;
        private BigDecimal feeRate;
        private Integer orderName;       
        private BigDecimal totalFee;
        private BigDecimal totalAmount;
    }
}
