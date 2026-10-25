UPDATE tb_technologies
SET category = 'VERSION_CONTROL'
WHERE LOWER(name) = 'git'
  AND category = 'OTHER';

WITH new_technologies(name, category) AS (
    VALUES
        ('Go', 'LANGUAGE'),
        ('Rust', 'LANGUAGE'),
        ('C#', 'LANGUAGE'),
        ('C++', 'LANGUAGE'),
        ('PHP', 'LANGUAGE'),
        ('Ruby', 'LANGUAGE'),
        ('Scala', 'LANGUAGE'),
        ('Elixir', 'LANGUAGE'),
        ('TypeScript', 'LANGUAGE'),
        ('Node.js', 'LANGUAGE'),
        ('Quarkus', 'FRAMEWORK'),
        ('Micronaut', 'FRAMEWORK'),
        ('Spring Cloud', 'FRAMEWORK'),
        ('NestJS', 'FRAMEWORK'),
        ('Express', 'FRAMEWORK'),
        ('Fastify', 'FRAMEWORK'),
        ('Django', 'FRAMEWORK'),
        ('Flask', 'FRAMEWORK'),
        ('FastAPI', 'FRAMEWORK'),
        ('ASP.NET Core', 'FRAMEWORK'),
        ('Laravel', 'FRAMEWORK'),
        ('Ruby on Rails', 'FRAMEWORK'),
        ('Ktor', 'FRAMEWORK'),
        ('Gin', 'FRAMEWORK'),
        ('Actix Web', 'FRAMEWORK'),
        ('Phoenix', 'FRAMEWORK'),
        ('Microsoft Azure', 'CLOUD_PROVIDER'),
        ('Google Cloud Platform', 'CLOUD_PROVIDER'),
        ('Oracle Cloud Infrastructure', 'CLOUD_PROVIDER'),
        ('IBM Cloud', 'CLOUD_PROVIDER'),
        ('DigitalOcean', 'CLOUD_PROVIDER'),
        ('Alibaba Cloud', 'CLOUD_PROVIDER'),
        ('GitHub', 'VERSION_CONTROL'),
        ('GitLab', 'VERSION_CONTROL'),
        ('Bitbucket', 'VERSION_CONTROL'),
        ('Apache Subversion', 'VERSION_CONTROL'),
        ('Mercurial', 'VERSION_CONTROL'),
        ('Perforce', 'VERSION_CONTROL')
)
INSERT INTO tb_technologies (name, category, parent_id)
SELECT new_technologies.name, new_technologies.category, NULL
FROM new_technologies
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_technologies existing
    WHERE LOWER(existing.name) = LOWER(new_technologies.name)
      AND existing.parent_id IS NULL
);

WITH cloud_services(name, category, parent_name) AS (
    VALUES
        ('Azure Functions', 'SERVERLESS', 'Microsoft Azure'),
        ('Azure Kubernetes Service', 'DEVOPS_INFRA', 'Microsoft Azure'),
        ('Azure Blob Storage', 'STORAGE', 'Microsoft Azure'),
        ('Azure SQL Database', 'DATABASE', 'Microsoft Azure'),
        ('Azure Service Bus', 'SERVERLESS', 'Microsoft Azure'),
        ('Cloud Run', 'SERVERLESS', 'Google Cloud Platform'),
        ('Google Kubernetes Engine', 'DEVOPS_INFRA', 'Google Cloud Platform'),
        ('Google Cloud Storage', 'STORAGE', 'Google Cloud Platform'),
        ('BigQuery', 'DATABASE', 'Google Cloud Platform'),
        ('Google Pub/Sub', 'SERVERLESS', 'Google Cloud Platform'),
        ('Cloud SQL', 'DATABASE', 'Google Cloud Platform'),
        ('OCI Functions', 'SERVERLESS', 'Oracle Cloud Infrastructure'),
        ('OCI Object Storage', 'STORAGE', 'Oracle Cloud Infrastructure'),
        ('OCI Autonomous Database', 'DATABASE', 'Oracle Cloud Infrastructure')
)
INSERT INTO tb_technologies (name, category, parent_id)
SELECT cloud_services.name, cloud_services.category, parent.id
FROM cloud_services
JOIN tb_technologies parent
    ON LOWER(parent.name) = LOWER(cloud_services.parent_name)
   AND parent.parent_id IS NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_technologies existing
    WHERE LOWER(existing.name) = LOWER(cloud_services.name)
      AND existing.parent_id = parent.id
);
