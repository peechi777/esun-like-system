USE esun_like;
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_delete_product $$
CREATE PROCEDURE sp_delete_product(
  IN  p_product_no BIGINT,
  OUT p_deleted    INT,   
  OUT p_in_use     INT    
)
BEGIN
  DECLARE v_cnt INT DEFAULT 0;

  SET p_deleted = 0;
  SET p_in_use  = 0;

  SELECT COUNT(*) INTO v_cnt
  FROM like_list
  WHERE product_no = p_product_no;

  IF v_cnt > 0 THEN
    SET p_in_use = 1;         
  ELSE
    START TRANSACTION;
      DELETE FROM products WHERE no = p_product_no;
      SET p_deleted = ROW_COUNT();  
    COMMIT;
  END IF;
END $$
DELIMITER ;
