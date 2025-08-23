USE esun_like;
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_upsert_user $$
CREATE PROCEDURE sp_upsert_user(
  IN  p_user_id    VARCHAR(20),
  IN  p_user_name  VARCHAR(100),
  IN  p_email      VARCHAR(200),
  IN  p_account    VARCHAR(30),
  OUT p_upserted   INT        
)
BEGIN
  START TRANSACTION;
    INSERT INTO users(user_id, user_name, email, account)
    VALUES (p_user_id, p_user_name, p_email, p_account)
    ON DUPLICATE KEY UPDATE
      user_name = VALUES(user_name),
      email     = VALUES(email),
      account   = VALUES(account);
    SET p_upserted = 1;
  COMMIT;
END $$
DELIMITER ;
