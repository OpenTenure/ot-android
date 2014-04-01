SELECT SCRIPT FROM
(
/* LIST ALL AVAILABLE UPGRADE SCRIPTS HERE */
SELECT '1.0.0' as VERSION_FROM, '1.5.0' as VERSION_TO,'upgradedb_1.0.0_1.5.0.sql' as SCRIPT FROM DUAL
UNION
SELECT '1.5.0' as VERSION_FROM, '2.0.0' as VERSION_TO,'upgradedb_1.5.0_2.0.0.sql' as SCRIPT FROM DUAL
)
WHERE VERSION_FROM >= SELECT VALUE FROM CONFIGURATION CFG WHERE CFG.NAME='DBVERSION'
ORDER BY VERSION_FROM