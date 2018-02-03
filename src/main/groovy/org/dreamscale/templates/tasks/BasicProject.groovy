package org.dreamscale.templates.tasks

import org.gradle.api.GradleException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.dreamscale.templates.GitRepo
import org.dreamscale.templates.ProjectProps
import org.dreamscale.templates.ProjectTemplate

import static com.google.common.base.CaseFormat.LOWER_CAMEL
import static com.google.common.base.CaseFormat.LOWER_HYPHEN
import static com.google.common.base.CaseFormat.UPPER_CAMEL

class BasicProject {

    @Delegate
    private GitRepo gitRepo
    private File targetDir
    String repoName
    private File gradleUserHomeDir
    private String dreamscaleGradleVersion
    private ProjectProps projectProps

    BasicProject(ProjectProps projectProps, GitRepo gitRepo) {
        this.gitRepo = gitRepo
        this.repoName = gitRepo.repoDir.name
        this.targetDir = gitRepo.repoDir
        this.projectProps = projectProps
    }

    BasicProject(String dreamscaleGradleVersion, GitRepo gitRepo, File gradleUserHomeDir) {
        this.gitRepo = gitRepo
        this.repoName = gitRepo.repoDir.name
        this.targetDir = gitRepo.repoDir
        this.dreamscaleGradleVersion = dreamscaleGradleVersion
        this.gradleUserHomeDir = gradleUserHomeDir
    }

    GitRepo getGitRepo() {
        gitRepo
    }

    ProjectProps getProjectProps() {
        projectProps
    }

    File getRepoDir() {
        gitRepo.repoDir
    }

    File getTargetDir() {
        targetDir
    }

    String getServiceName() {
        String defaultServiceName = LOWER_HYPHEN.to(UPPER_CAMEL, repoName).capitalize()
        if (projectProps != null) {
            projectProps.getOptionalProjectPropertyOrDefault("serviceName", defaultServiceName)
        } else {
            defaultServiceName
        }
    }

    String getServicePackage() {
        String defaultPackageName = "org.dreamscale.${serviceName.toLowerCase()}"
        if (projectProps != null) {
            projectProps.getOptionalProjectPropertyOrDefault("servicePackageName", defaultPackageName)
        } else {
            defaultPackageName
        }
    }

    String getServicePackagePath() {
        packageToPath(servicePackage)
    }

    private String packageToPath(String pkg) {
        pkg.replaceAll ("\\.", "/" )
    }

    void initGradleProject() {
        if (new File(repoDir, "build.gradle").exists() == false) {
            initDreamScaleGradleWrapper()

            initBasicGradleBuild()
            gitRepo.commitProjectFiles("added build.gradle")

            new File(repoDir, "src/main/java").mkdirs()
            new File(repoDir, "src/test/groovy").mkdirs()
        }
    }

    void initDreamScaleGradleWrapper() {
        if (new File(repoDir, "gradle/wrapper/gradle-wrapper.properties").exists() == false) {
            initGradleWrapper()
            initGitignore()
            gitRepo.commitProjectFiles("initial commit, gradle wrapper")

            replaceGradleWrapperDistributionUrl()
            gitRepo.commitProjectFiles("use dreamscale gradle")
        }
    }

    File getBuildFile() {
        getProjectFileOrFail("build.gradle")
    }

    void applyPlugin(String pluginName) {
        FileUtils.appendAfterLine(getBuildFile(), /apply\s+plugin:\s+"org.dreamscale/, /apply plugin: "${pluginName}"/)
    }

    private void initBasicGradleBuild() {
        applyTemplate {
            'build.gradle' template: "/templates/basic/build.gradle.tmpl"
            'gradle.properties' template: "/templates/basic/gradle.properties.tmpl",
                    artifactId: repoName
        }
    }

    private void replaceGradleWrapperDistributionUrl() {
        File gradleWrapperProperties = new File(repoDir, "gradle/wrapper/gradle-wrapper.properties")
        String text = gradleWrapperProperties.text
        String dreamscaleGradleVersion = getDreamscaleGradleVersion()
        String distributionUrl = "https://dl.bintray.com/dreamscale/maven-public/org/dreamscale/dreamscale-gradle/${dreamscaleGradleVersion}/dreamscale-gradle-${dreamscaleGradleVersion}-bin.zip"
        gradleWrapperProperties.text = text.replaceFirst(/(?m)^distributionUrl=.*/, /distributionUrl=${distributionUrl}/)
    }

    private String getDreamscaleGradleVersion() {
        if (this.dreamscaleGradleVersion != null) {
            return this.dreamscaleGradleVersion
        } else if (this.projectProps != null) {
            return projectProps.getRequiredProjectProperty("dreamscaleGradleVersion")
        } else {
            throw new RuntimeException("Missing property dreamscaleGradleVersion!")
        }
    }

    private void initGradleWrapper() {
        GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(repoDir)

        if (gradleUserHomeDir != null) {
            connector.useGradleUserHomeDir(gradleUserHomeDir)
        }

        ProjectConnection connection = connector.connect()
        try {
            connection.newBuild().forTasks("wrapper").run()
        } finally {
            connection.close()
        }
    }

    private void initGitignore() {
        applyTemplate {
            '.gitignore' template: "/templates/git/gitignore.tmpl"
        }
    }

    void applyTemplate(Closure closure) {
        ProjectTemplate.fromRoot(targetDir, closure)
    }

    void applyTemplate(String relativePath, Closure closure) {
        ProjectTemplate.fromRoot(new File(targetDir, relativePath), closure)
    }

    File getProjectFile(String filePath) {
        File file = new File(targetDir, filePath)
        file.parentFile.mkdirs()
        file
    }

    File getProjectFileOrFail(String filePath) {
        File file = getProjectFile(filePath)
        if (file.exists() == false) {
            throw new GradleException("Failed to resolve ${file.name} at expected location=${file.absolutePath}")
        }
        file
    }

    File findFile(String fileName) {
        File matchingFile = findOptionalFile(fileName)
        if (matchingFile == null) {
            throw new RuntimeException("Failed to find file with name=${fileName} from baseDir=${targetDir.absolutePath}")
        }
        matchingFile
    }

    File findOptionalFile(String fileName) {
        File matchingFile = null
        targetDir.eachFileRecurse { File file ->
            if (file.name == fileName) {
                matchingFile = file
            }
        }
        matchingFile
    }

    void appendServiceToAppDescriptor(String service) {
        File appDescriptor = getProjectFileOrFail("src/deploy/cloudfoundry/app-descriptor.yml")
        if (appDescriptor.text.contains("services:")) {
            FileUtils.appendAfterLine(appDescriptor, "services:", """\
    - ${service}""")

        } else {
            FileUtils.appendAfterLine(appDescriptor, "type:", """\
  services:
    - ${service}""")

        }
    }

    void includeGradleSubmodule(String submoduleName) {
        File gradleSettings = getProjectFile("settings.gradle")
        if (gradleSettings.exists() == false) {
            gradleSettings.text = ""
        }

        if (gradleSettings.text !=~ /${submoduleName}/) {
            gradleSettings.text += "include \"${submoduleName}\"\n"
        }
    }

    void addClientSubmodule(String type) {
        File clientBuildFile = getProjectFile("${type}-client/build.gradle")
        if (clientBuildFile.exists() == false) {
            applyTemplate("${type}-client") {
                "build.gradle" template: "/templates/springboot/${type}/build.${type}-client.gradle.tmpl"
            }

            includeGradleSubmodule("${type}-client")

            File buildFile = getProjectFileOrFail("build.gradle")
            FileUtils.appendAfterLine(buildFile, /^dependencies \{/, "    compile project(\"${type}-client\")")
            FileUtils.appendBeforeLine(buildFile, /sharedTest/, "    sharedTestCompile project(path: \"${type}-client\", configuration: \"mainTestRuntime\")")
        }
    }

    void addApiObject(String type, String dtoName, boolean upperCamel) {
        addClientSubmodule(type)

        String apiPackage = "${servicePackage}.api"
        String apiPackagePath = packageToPath(apiPackage)
        String typeUpperCase = type.capitalize()

        String dtoNameLowerCamel = UPPER_CAMEL.to(LOWER_CAMEL, dtoName)

        applyTemplate("${type}-client/src/main/java/${apiPackagePath}") {
            "${dtoName}.java" template: "/templates/springboot/${type}/resource-api.java.tmpl",
                                   resourceName: dtoName, packageName: apiPackage,
                                   upperCamel: upperCamel
        }
        applyTemplate("${type}-client/src/mainTest/groovy/${apiPackagePath}") {
            "Random${dtoName}Builder.groovy" template: "/templates/test/random-client-builder.groovy.tmpl",
                                                  targetClass: dtoName, packageName: apiPackage,
                                                  qualifier: "${typeUpperCase}Client"
        }

        File randomClientBuilderSupport = getProjectFile("${type}-client/src/mainTest/groovy/${apiPackagePath}/${typeUpperCase}ClientRandomBuilderSupport.java")
        if (randomClientBuilderSupport.exists() == false) {
            applyTemplate("${type}-client/src/mainTest/groovy/${apiPackagePath}") {
                "${typeUpperCase}ClientARandom.java" template: "/templates/test/client-arandom.java.tmpl",
                        packageName: apiPackage, qualifier: "${typeUpperCase}Client"
                "${typeUpperCase}ClientRandomBuilderSupport.java" template: "/templates/test/random-builder-support.java.tmpl",
                        packageName: apiPackage, qualifier: "${typeUpperCase}Client"
            }

            File coreARandom = findFile("CoreARandom.java")
            FileUtils.appendAfterLine(coreARandom, "import", "import ${apiPackage}.${typeUpperCase}ClientRandomBuilderSupport;")
            FileUtils.appendAfterLine(coreARandom, /\s+private CoreRandomBuilderSupport.*/, """\
    @Delegate
    private ${typeUpperCase}ClientRandomBuilderSupport ${type}ClientRandomBuilderSupport = new ${typeUpperCase}ClientRandomBuilderSupport();"""
            )
        }
        FileUtils.appendToClass(randomClientBuilderSupport, """

    public Random${dtoName}Builder ${dtoNameLowerCamel}() {
        return new Random${dtoName}Builder();
    }
""")
    }

}