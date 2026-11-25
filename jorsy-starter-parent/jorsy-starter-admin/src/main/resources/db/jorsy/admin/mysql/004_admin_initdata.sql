-- Set @admin_code and @admin_password_hash in this MySQL client session before sourcing this file.
DROP PROCEDURE IF EXISTS jorsy_init_admin_004;
DELIMITER //
CREATE PROCEDURE jorsy_init_admin_004()
BEGIN
    DECLARE org_id CHAR(32) DEFAULT MD5('jorsy:root-org');
    DECLARE role_id CHAR(32) DEFAULT MD5('jorsy:administrator');
    DECLARE user_id CHAR(32);
    DECLARE identity_id CHAR(32);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;

    START TRANSACTION;
    IF @admin_code IS NULL OR CHAR_LENGTH(TRIM(@admin_code)) NOT BETWEEN 1 AND 50
        OR @admin_password_hash IS NULL OR NOT REGEXP_LIKE(@admin_password_hash,
            '^[{]bcrypt[}][$]2[aby][$](0[4-9]|[12][0-9]|3[01])[$][./A-Za-z0-9]{53}$', 'c') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Set admin_code and a valid {bcrypt} password hash';
    END IF;
    SET user_id = MD5(CONCAT('jorsy:admin:', LOWER(TRIM(@admin_code))));
    SET identity_id = MD5(CONCAT('jorsy:identity:', user_id));
    IF EXISTS (SELECT 1 FROM sys_user WHERE sys_user_code=TRIM(@admin_code) AND deleted=0
               AND (sys_user_id<>user_id OR NOT (sys_data <=> b'1'))) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Administrator code already belongs to another account';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM sys_org WHERE sys_org_id=org_id AND sys_org_code='0001' AND sys_data=1 AND deleted=0) THEN
        INSERT INTO sys_org (sys_org_id, sys_org_code, sys_org_name, sys_org_type, sort_no, sys_data, create_time, update_time)
        VALUES (org_id, '0001', '根组织', 'C', 1, 1, NOW(3), NOW(3));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM sys_role WHERE sys_role_id=role_id AND sys_role_code='administrator' AND sys_data=1 AND deleted=0) THEN
        INSERT INTO sys_role (sys_role_id, sys_role_code, sys_role_name, sys_data, create_time, update_time)
        VALUES (role_id, 'administrator', '超级管理员', 1, NOW(3), NOW(3));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM sys_user WHERE sys_user_id=user_id AND sys_user_code=TRIM(@admin_code) AND sys_data=1 AND deleted=0) THEN
        INSERT INTO sys_user (sys_user_id, sys_user_code, sys_user_name, sys_user_pwd, sys_user_status, sys_data, create_time, update_time)
        VALUES (user_id, TRIM(@admin_code), '管理员', @admin_password_hash, 10, 1, NOW(3), NOW(3));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM sys_rights WHERE sys_rights_id=identity_id AND sys_user_id=user_id
                   AND sys_role_id=role_id AND sys_org_id=org_id AND sys_data=1 AND deleted=0) THEN
        INSERT INTO sys_rights (sys_rights_id, sys_user_id, sys_user_code, sys_org_id, sys_org_code, sys_role_id,
                                sys_role_code, data_scope, enabled, defaulted, sys_data, create_time, update_time)
        VALUES (identity_id, user_id, TRIM(@admin_code), org_id, '0001', role_id, 'administrator', '9', 1, 1, 1, NOW(3), NOW(3));
    END IF;
    COMMIT;
END //
DELIMITER ;
CALL jorsy_init_admin_004();
DROP PROCEDURE jorsy_init_admin_004;
