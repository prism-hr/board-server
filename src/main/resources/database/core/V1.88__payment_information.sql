ALTER TABLE resource
  ADD COLUMN payment_customer_id VARCHAR(255) AFTER dead_timestamp,
  ADD COLUMN payment_subscription_id VARCHAR(255) AFTER payment_customer_id,
  ADD UNIQUE INDEX(payment_customer_id),
  ADD UNIQUE INDEX(payment_subscription_id);
