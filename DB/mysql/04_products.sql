USE esun_like;

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_add_product $$
CREATE PROCEDURE sp_add_product(
  IN  p_product_name VARCHAR(100),
  IN  p_price        DECIMAL(15,2),
  IN  p_fee_rate     DECIMAL(10,4),
  OUT p_product_no   BIGINT
)
BEGIN
  START TRANSACTION;
    INSERT INTO products(product_name, price, fee_rate)
    VALUES (p_product_name, p_price, p_fee_rate);
    SET p_product_no = LAST_INSERT_ID(); 
  COMMIT;
END $$

DROP PROCEDURE IF EXISTS sp_list_products $$
CREATE PROCEDURE sp_list_products()
BEGIN
  SELECT
    no           AS product_no,
    product_name,
    price,
    fee_rate
  FROM products
  ORDER BY no DESC;
END $$

DELIMITER ;
