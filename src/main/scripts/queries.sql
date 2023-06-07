-- Evolution of a dependency over time
SELECT STRFTIME('%Y-%m', DATE(created_on)) AS month, COUNT(*) AS count
FROM projects, JSON_EACH(vertx_dependencies)
WHERE value = 'vertx-pg-client'
GROUP BY month
ORDER BY month;

-- Evolution of JDK versions over time
SELECT STRFTIME('%Y-%m', DATE(created_on)) AS month, jdk_version, COUNT(*) AS count
FROM projects
GROUP BY month, jdk_version
ORDER BY month, jdk_version DESC;

-- Number of projects created per year
SELECT STRFTIME('%Y', DATE(created_on)) AS year, COUNT(*) AS count
FROM projects
GROUP BY year
ORDER BY year;

-- Number of projects created per year
SELECT STRFTIME('%Y', DATE(created_on)) AS year, COUNT(*) AS count
FROM projects
GROUP BY year
ORDER BY year;

-- Operating system distribution
SELECT operating_system, ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM projects), 2) AS percent
FROM projects
GROUP BY operating_system;

-- Language distribution
SELECT language, ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM projects), 2) AS percent
FROM projects
GROUP BY language;

-- Build tool distribution
SELECT build_tool, ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM projects), 2) AS percent
FROM projects
GROUP BY build_tool;

-- Vert.x version distribution over years
SELECT STRFTIME('%Y', DATE(created_on)) AS year,
       vertx_version,
       ROUND(COUNT(*) * 100.0 / totals.count,
             2)                         AS percent
FROM projects
       JOIN (SELECT STRFTIME('%Y', DATE(created_on)) AS year, COUNT(*) AS count
             FROM projects
             GROUP BY year
             ORDER BY year) AS totals
            ON STRFTIME('%Y', DATE(created_on)) = totals.year
GROUP BY STRFTIME('%Y', DATE(created_on)), vertx_version;

-- JDK version distribution over years
SELECT STRFTIME('%Y', DATE(created_on)) AS year,
       jdk_version,
       ROUND(COUNT(*) * 100.0 / totals.count,
             2)                         AS percent
FROM projects
       JOIN (SELECT STRFTIME('%Y', DATE(created_on)) AS year, COUNT(*) AS count
             FROM projects
             GROUP BY year
             ORDER BY year) AS totals
            ON STRFTIME('%Y', DATE(created_on)) = totals.year
GROUP BY STRFTIME('%Y', DATE(created_on)), jdk_version;
