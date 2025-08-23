package com.esun.like.repository;

import com.esun.like.model.dto.AddProductReq;
import com.esun.like.model.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    public enum DeleteStatus { DELETED, IN_USE, NOT_FOUND }

    private final DataSource dataSource;

    public long create(AddProductReq req) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_add_product")
                .declareParameters(
                        new SqlParameter("p_product_name", Types.VARCHAR),
                        new SqlParameter("p_price", Types.DECIMAL),
                        new SqlParameter("p_fee_rate", Types.DECIMAL),
                        new SqlOutParameter("p_product_no", Types.BIGINT)
                );

        Map<String, Object> out = call.execute(Map.of(
                "p_product_name", req.getProductName(),
                "p_price", req.getPrice(),
                "p_fee_rate", req.getFeeRate()
        ));
        Number no = (Number) out.get("p_product_no");
        return no.longValue();
    }

    @SuppressWarnings("unchecked")
    public List<ProductDto> list() {
        RowMapper<ProductDto> mapper = (rs, rowNum) -> {
            ProductDto d = new ProductDto();
            d.setProductNo(rs.getLong("product_no"));
            d.setProductName(rs.getString("product_name"));
            d.setPrice(rs.getBigDecimal("price"));
            d.setFeeRate(rs.getBigDecimal("fee_rate"));
            return d;
        };

        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_list_products")
                .returningResultSet("products", mapper);

        Map<String, Object> out = call.execute(Collections.emptyMap());
        return (List<ProductDto>) out.getOrDefault("products", List.of());
    }

    public DeleteStatus delete(long productNo) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_delete_product")
                .declareParameters(
                        new SqlParameter("p_product_no", Types.BIGINT),
                        new SqlOutParameter("p_deleted", Types.INTEGER),
                        new SqlOutParameter("p_in_use", Types.INTEGER)
                );

        Map<String, Object> out = call.execute(Map.of("p_product_no", productNo));
        int inUse = ((Number) out.getOrDefault("p_in_use", 0)).intValue();
        int deleted = ((Number) out.getOrDefault("p_deleted", 0)).intValue();

        if (inUse == 1) return DeleteStatus.IN_USE;
        if (deleted == 1) return DeleteStatus.DELETED;
        return DeleteStatus.NOT_FOUND;
    }
}
