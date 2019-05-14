-- liquibase formatted sql

-- changeset moda:1557719974992-1
CREATE TABLE ti_cheng (id BIGINT NOT NULL, bei_zhu VARCHAR(255) NULL, jine DECIMAL(19, 2) NULL, ri_qi date NULL, yong_hu_id BIGINT NOT NULL, CONSTRAINT PK_TI_CHENG PRIMARY KEY (id));

-- changeset moda:1557719974992-2
CREATE TABLE xiang_mu_ti_cheng_biao_zhuns (xiang_mu_id BIGINT NOT NULL, kai_shi date NULL, xiao_shi_ti_cheng DECIMAL(19, 2) NULL, yong_hu_id BIGINT NULL);

-- changeset moda:1557719974992-3
ALTER TABLE yong_hu ADD jie_suan_ri date NULL;

-- changeset moda:1557719974992-4
ALTER TABLE yong_hu ADD xiao_shi_ti_cheng DECIMAL(19, 2) NULL;

-- changeset moda:1557719974992-5
CREATE INDEX FKg8ycrwkpac4qlrhpiqpeimya4 ON xiang_mu_ti_cheng_biao_zhuns(yong_hu_id);

-- changeset moda:1557719974992-6
CREATE INDEX FKiylkxxucdkj9ld2eaf3606u16 ON ti_cheng(yong_hu_id);

-- changeset moda:1557719974992-7
CREATE INDEX FKpy0x5v3ad7k7m8891birfr1ad ON xiang_mu_ti_cheng_biao_zhuns(xiang_mu_id);

-- changeset moda:1557719974992-8
ALTER TABLE xiang_mu_ti_cheng_biao_zhuns ADD CONSTRAINT FKg8ycrwkpac4qlrhpiqpeimya4 FOREIGN KEY (yong_hu_id) REFERENCES yong_hu (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset moda:1557719974992-9
ALTER TABLE ti_cheng ADD CONSTRAINT FKiylkxxucdkj9ld2eaf3606u16 FOREIGN KEY (yong_hu_id) REFERENCES yong_hu (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset moda:1557719974992-10
ALTER TABLE xiang_mu_ti_cheng_biao_zhuns ADD CONSTRAINT FKpy0x5v3ad7k7m8891birfr1ad FOREIGN KEY (xiang_mu_id) REFERENCES xiang_mu (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

