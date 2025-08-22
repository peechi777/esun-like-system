USE esun_like;

-- User
INSERT INTO users (user_id, user_name, email, account) VALUES
('A1236456789', '王○明', 'test@email.com', '1111999666');

-- Product
INSERT INTO products (product_name, price, fee_rate) VALUES
('台灣基金A', 1000.00, 0.10),
('全球債券B', 1500.00, 0.01),
('高收益C',   2000.00, 0.05);
