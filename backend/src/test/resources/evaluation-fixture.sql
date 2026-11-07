INSERT INTO disputes(customer_id,category,description,disputed_amount,currency,status,created_at,due_at,resolved_at,version)
SELECT (SELECT id FROM app_users WHERE email='customer@demo.local'),'DUPLICATE_CHARGE','Synthetic evaluation record',100,'INR',v.status,v.created::timestamptz,v.created::timestamptz+interval '72 hours',v.resolved::timestamptz,0
FROM (VALUES
('OPEN','2026-11-02 00:00:00+00',NULL),
('OPEN','2026-10-31 00:00:00+00',NULL),
('OPEN','2026-10-30 12:00:00+00',NULL),
('OPEN','2026-10-30 11:59:59+00',NULL),
('RESOLVED_APPROVED','2026-10-30 00:00:00+00','2026-11-02 00:00:00+00'),
('RESOLVED_REJECTED','2026-10-30 00:00:00+00','2026-11-02 02:00:00+00'),
('RESOLVED_REJECTED','2026-10-29 00:00:00+00','2026-11-01 00:00:00+00')
) AS v(status,created,resolved);
INSERT INTO dispute_decisions(dispute_id,manager_id,outcome,approved_refund_amount,explanation,created_at)
SELECT id,(SELECT id FROM app_users WHERE email='manager@demo.local'),CASE WHEN status='RESOLVED_APPROVED' THEN 'APPROVED' ELSE 'REJECTED' END,
CASE WHEN status='RESOLVED_APPROVED' THEN 25.50 ELSE 0 END,'Synthetic fixture decision',resolved_at FROM disputes WHERE resolved_at IS NOT NULL;
