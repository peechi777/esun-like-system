DROP DATABASE IF EXISTS esun_like;
CREATE DATABASE esun_like CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE esun_like;

-- User
CREATE TABLE users (
  user_id   VARCHAR(20)  PRIMARY KEY,
  user_name VARCHAR(100) NOT NULL,
  email     VARCHAR(255) NOT NULL,
  account   VARCHAR(32)  NOT NULL
);

-- Product
CREATE TABLE products (
  no           BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_name VARCHAR(200) NOT NULL,
  price        DECIMAL(18,2) NOT NULL,
  fee_rate     DECIMAL(7,6)  NOT NULL  -- 0.1=10%、0.01=1%
);

-- Like List
CREATE TABLE like_list (
  sn           BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id      VARCHAR(20)  NOT NULL,
  product_no   BIGINT       NOT NULL,
  quantity     INT          NOT NULL,      
  account      VARCHAR(32)  NOT NULL,
  total_fee    DECIMAL(18,2) NOT NULL,
  total_amount DECIMAL(18,2) NOT NULL,
  FOREIGN KEY (user_id)    REFERENCES users(user_id),
  FOREIGN KEY (product_no) REFERENCES products(no)
);
