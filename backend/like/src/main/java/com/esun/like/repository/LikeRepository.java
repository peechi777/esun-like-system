package com.esun.like.repository;

import com.esun.like.model.dto.AddOrUpdateLikeReq;
import com.esun.like.model.dto.UpdateBySnReq;
import com.esun.like.model.dto.ListLikesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikeRepository {

    private final DataSource dataSource;

    public void addOrMerge(AddOrUpdateLikeReq req) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_add_or_merge_like_item");
        call.execute(Map.of(
                "p_user_id", req.getUserId(),
                "p_product_no", req.getProductNo(),
                "p_account", req.getAccount(),
                "p_quantity", req.getOrderName()
        ));
    }

    public void updateBySn(UpdateBySnReq req) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_update_like_item");
        call.execute(Map.of(
                "p_sn", req.getSn(),
                "p_account", req.getAccount(),
                "p_quantity", req.getOrderName()
        ));
    }

    public void deleteBySn(Long sn) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_delete_like_item");
        call.execute(Map.of("p_sn", sn));
    }

    public ListLikesResponse listByUser(String userId) {
        ListLikesResponse resp = new ListLikesResponse();
        List<ListLikesResponse.Item> items = new ArrayList<>();
        try (var con = dataSource.getConnection();
             var cs = con.prepareCall("{call sp_list_user_likes(?)}")) {
            cs.setString(1, userId);

            boolean hasResult = cs.execute();
            if (hasResult) {
                try (ResultSet rs = cs.getResultSet()) {
                    while (rs.next()) {
                        var it = new ListLikesResponse.Item();
                        it.setSn(rs.getLong("sn"));
                        it.setUserId(rs.getString("user_id"));
                        it.setUserName(rs.getString("user_name"));
                        it.setEmail(rs.getString("email"));
                        it.setAccount(rs.getString("account"));
                        it.setProductNo(rs.getLong("product_no"));
                        it.setProductName(rs.getString("product_name"));
                        it.setPrice(rs.getBigDecimal("price"));
                        it.setFeeRate(rs.getBigDecimal("fee_rate"));
                        it.setOrderName(rs.getInt("quantity")); 
                        it.setTotalFee(rs.getBigDecimal("total_fee"));
                        it.setTotalAmount(rs.getBigDecimal("total_amount"));
                        items.add(it);
                    }
                }
            }
            if (cs.getMoreResults()) {
                try (ResultSet rs2 = cs.getResultSet()) {
                    if (rs2.next()) {
                        resp.setSumTotalAmount(rs2.getBigDecimal("sum_total_amount"));
                        resp.setSumTotalFee(rs2.getBigDecimal("sum_total_fee"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list user likes", e);
        }
        resp.setItems(items);
        return resp;
    }
}
