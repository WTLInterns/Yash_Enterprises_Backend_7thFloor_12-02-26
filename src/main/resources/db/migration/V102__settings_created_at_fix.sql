-- Safely add created_at to settings for MySQL 8
-- MySQL does NOT support: ADD COLUMN IF NOT EXISTS

-- Save current sql_mode and temporarily relax strict/zero-date checks
SET @OLD_SQL_MODE := @@SESSION.sql_mode;
SET SESSION sql_mode = REPLACE(@@SESSION.sql_mode, 'NO_ZERO_DATE', '');
SET SESSION sql_mode = REPLACE(@@SESSION.sql_mode, 'STRICT_TRANS_TABLES', '');

-- 1) Add created_at column ONLY if it does not exist (nullable first)
SET @col_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'settings'
    AND COLUMN_NAME = 'created_at'
);

SET @sql_add_created_at := IF(
  @col_exists = 0,
  'ALTER TABLE settings ADD COLUMN created_at DATETIME NULL',
  'SELECT 1'
);

PREPARE stmt_add_created_at FROM @sql_add_created_at;
EXECUTE stmt_add_created_at;
DEALLOCATE PREPARE stmt_add_created_at;

-- 2) Backfill any zero/invalid values with current timestamp
UPDATE settings
SET created_at = NOW()
WHERE created_at IS NULL OR created_at = '0000-00-00 00:00:00';

-- 3) Make column NOT NULL (safe after backfill)
ALTER TABLE settings
  MODIFY COLUMN created_at DATETIME NOT NULL;

-- Restore original sql_mode
SET SESSION sql_mode = @OLD_SQL_MODE;
