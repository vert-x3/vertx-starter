-- Evolution of a dependency over time
SELECT strftime('%Y-%m', date(projects.created_on)) as month, count(*) as count
FROM projects, json_each(projects.vertx_dependencies)
WHERE value = 'vertx-pg-client'
GROUP BY month
ORDER BY month;

-- Evolution of JDK versions over time
SELECT strftime('%Y-%m', date(projects.created_on)) as month, projects.jdk_version, count(*) as count
FROM projects
GROUP BY month, jdk_version
ORDER BY month, jdk_version DESC;

-- Number of projects created per year
SELECT strftime('%Y', date(projects.created_on)) as year, count(*) as count
FROM projects
GROUP BY year
ORDER BY year;
