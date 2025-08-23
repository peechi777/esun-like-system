package com.esun.like.repository;

import com.esun.like.model.dto.SaveUserReq;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DataSource dataSource;

    public void upsert(SaveUserReq req) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_upsert_user")
                .declareParameters(
                        new SqlParameter("p_user_id", Types.VARCHAR),
                        new SqlParameter("p_user_name", Types.VARCHAR),
                        new SqlParameter("p_email", Types.VARCHAR),
                        new SqlParameter("p_account", Types.VARCHAR),
                        new SqlOutParameter("p_upserted", Types.INTEGER)
                );
        call.execute(Map.of(
                "p_user_id", req.getUserId(),
                "p_user_name", req.getUserName(),
                "p_email", req.getEmail(),
                "p_account", req.getAccount()
        ));
    }
}
