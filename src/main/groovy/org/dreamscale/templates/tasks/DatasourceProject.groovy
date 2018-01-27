package org.dreamscale.templates.tasks

class DatasourceProject {

    private RestProject restProject
    private BasicProject basicProject

    DatasourceProject(RestProject restProject) {
        this.restProject = restProject
        this.basicProject = restProject.basicProject
    }

    String getServiceName() {
        restProject.serviceName
    }

    String getServiceId() {
        restProject.serviceId
    }

    String getServicePackage() {
        restProject.servicePackage
    }

    String getServicePackagePath() {
        restProject.servicePackagePath
    }

    void initPostgres() {
        basicProject.applyPlugin("postgres")

        applyPostgresCompileDependencies()
        applyApplicationProperties()
        applyTestCleanupSql()
        basicProject.appendServiceToAppDescriptor("postgres-shared")
    }

    private void applyTestCleanupSql() {
        basicProject.getProjectFile("src/sharedTest/resources/db/test_cleanup.sql") << ""

        basicProject.applyTemplate("src/sharedTest/groovy/${servicePackagePath}") {
            "PersistenceTest.java" template: "/templates/springboot/rest/persistence-test-annotation.java.tmpl",
                                   packageName: servicePackage
        }

        applyPersistenceTestAnnotationIfFileExists("ComponentTest.java")
        applyPersistenceTestAnnotationIfFileExists("IntegrationTest.java")
    }

    private void applyPersistenceTestAnnotationIfFileExists(String fileName) {
        File testFile = basicProject.findOptionalFile(fileName)
        if (testFile != null) {
            FileUtils.appendAfterLine(testFile, /@Retention/,
                                      "@PersistenceTest"
            )
        }
    }

    private void applyApplicationProperties() {
        File componentTestPropertiesFile = basicProject.getProjectFile("src/sharedTest/resources/application-test.properties")
        componentTestPropertiesFile.append("""
spring.datasource.url=jdbc:postgresql://docker.localhost:5432/\${spring.application.name}-test
""")

        File applicationPropertiesFile = basicProject.getProjectFile("src/main/resources/application-local.properties")
        applicationPropertiesFile.append("""
spring.datasource.url=jdbc:postgresql://docker.localhost:5432/\${spring.application.name}
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.test-on-borrow=true
spring.datasource.validation-interval=30000
spring.datasource.validation-query=SELECT 1;
""")
    }

    private applyPostgresCompileDependencies() {
        FileUtils.appendAfterLine(basicProject.getProjectFile("build.gradle"), 'compile "org.dreamscale:common-spring-boot-rest:', """\
    compile "org.springframework.boot:spring-boot-starter-data-jpa"
    compile "postgresql:postgresql:9.0-801.jdbc4"
    compile "org.liquibase:liquibase-core\""""
        )
    }

    private createLiquibaseChangeLog() {
        getLiquibaseChangeLog().parentFile.mkdirs()
        getLiquibaseChangeLog().text = """databaseChangeLog:
"""
    }

    private File getLiquibaseChangeLog() {
        basicProject.getProjectFile("src/main/resources/db/changelog/db.changelog-master.yaml")
    }

    void addCreateTableScript(String tableName) {
        basicProject.applyTemplate("src/main/resources/db") {
            "create_${tableName}.sql" template: "/templates/liquibase/create-table.sql.tmpl",
                    tableName: tableName
        }

        File liquibaseChangelog = getLiquibaseChangeLog()
        if (liquibaseChangelog.exists() == false) {
            createLiquibaseChangeLog()
        }
        liquibaseChangelog.append("""
  - include:
      file: db/create_${tableName}.sql
""")
    }

}
