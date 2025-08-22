USE esun_like;
DELIMITER $$

-- 1) 新增或合併
CREATE PROCEDURE sp_add_or_merge_like_item (
  IN p_user_id    VARCHAR(20),
  IN p_product_no BIGINT,
  IN p_account    VARCHAR(32),
  IN p_quantity   INT
)
BEGIN
  DECLARE v_price DECIMAL(18,2);
  DECLARE v_rate  DECIMAL(7,6);
  DECLARE v_fee   DECIMAL(18,2);
  DECLARE v_total DECIMAL(18,2);
  DECLARE v_sn    BIGINT;

  IF p_quantity <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Quantity must be > 0';
  END IF;

  SELECT price, fee_rate INTO v_price, v_rate
  FROM products WHERE no = p_product_no;
  IF v_price IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Product not found';
  END IF;

  SET v_fee   = ROUND(v_price * p_quantity * v_rate, 2);
  SET v_total = ROUND(v_price * p_quantity + v_fee, 2);

  START TRANSACTION;
    SELECT sn INTO v_sn
    FROM like_list
    WHERE user_id = p_user_id AND product_no = p_product_no AND account = p_account
    LIMIT 1 FOR UPDATE;

    IF v_sn IS NULL THEN
      INSERT INTO like_list(user_id, product_no, quantity, account, total_fee, total_amount)
      VALUES (p_user_id, p_product_no, p_quantity, p_account, v_fee, v_total);
    ELSE
      UPDATE like_list
      SET quantity = p_quantity,
          total_fee = v_fee,
          total_amount = v_total
      WHERE sn = v_sn;
    END IF;
  COMMIT;
END$$

-- 2) 依 SN 更新
CREATE PROCEDURE sp_update_like_item (
  IN p_sn       BIGINT,
  IN p_account  VARCHAR(32),
  IN p_quantity INT
)
BEGIN
  DECLARE v_price DECIMAL(18,2);
  DECLARE v_rate  DECIMAL(7,6);
  DECLARE v_fee   DECIMAL(18,2);
  DECLARE v_total DECIMAL(18,2);
  DECLARE v_pno   BIGINT;

  IF p_quantity <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Quantity must be > 0';
  END IF;

  SELECT product_no INTO v_pno FROM like_list WHERE sn = p_sn;
  IF v_pno IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Like item not found';
  END IF;

  SELECT price, fee_rate INTO v_price, v_rate FROM products WHERE no = v_pno;

  SET v_fee   = ROUND(v_price * p_quantity * v_rate, 2);
  SET v_total = ROUND(v_price * p_quantity + v_fee, 2);

  START TRANSACTION;
    UPDATE like_list
    SET account = p_account,
        quantity = p_quantity,
        total_fee = v_fee,
        total_amount = v_total
    WHERE sn = p_sn;
  COMMIT;
END$$

-- 3) 刪除
CREATE PROCEDURE sp_delete_like_item (IN p_sn BIGINT)
BEGIN
  START TRANSACTION;
    DELETE FROM like_list WHERE sn = p_sn;
  COMMIT;
END$$

-- 4) 查詢使用者清單
CREATE PROCEDURE sp_list_user_likes (IN p_user_id VARCHAR(20))
BEGIN
  SELECT ll.sn, ll.user_id, u.user_name, u.email,
         ll.account, ll.product_no, p.product_name, p.price, p.fee_rate,
         ll.quantity, ll.total_fee, ll.total_amount
  FROM like_list ll
  JOIN users u    ON u.user_id = ll.user_id
  JOIN products p ON p.no = ll.product_no
  WHERE ll.user_id = p_user_id
  ORDER BY ll.sn DESC;

  SELECT COALESCE(SUM(total_amount),0) AS sum_total_amount,
         COALESCE(SUM(total_fee),0)    AS sum_total_fee
  FROM like_list
  WHERE user_id = p_user_id;
END$$

DELIMITER ;
