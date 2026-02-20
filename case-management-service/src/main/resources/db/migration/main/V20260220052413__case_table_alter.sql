ALTER TABLE eg_lm_case
ADD COLUMN id VARCHAR(64) NOT NULL default '7777777';

ALTER TABLE eg_lm_case
DROP COLUMN workflowid;
