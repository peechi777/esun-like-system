package com.esun.like.repository;

import com.esun.like.model.dto.AddOrUpdateLikeReq;
import com.esun.like.model.dto.UpdateBySnReq;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;

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
                // 你要的名稱是 orderName，這裡對應到 SP 的 p_quantity
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
}
