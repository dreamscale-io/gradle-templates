package org.dreamscale.templates.tasks

import org.gradle.api.tasks.TaskAction

class CreateDeployableProjectTask extends AbstractTemplateTask {

    CreateDeployableProjectTask() {
        super("Create a SpringBoot REST project (options: -PrepoName=?, [-Pclean, -Ppostgres, -PserviceName=<app name>, -PservicePackageName=<package name>])")
    }

    @TaskAction
    void createDeployableProject() {
        boolean clean = projectProps.isPropertyDefined("clean")
        BasicProject basicProject = createBasicProject(clean)
        RestProject restProject = new RestProject(basicProject)
        restProject.initRestProject()
        if (projectProps.isPropertyDefined("postgres")) {
            restProject.initPostgres()
        }
    }

}
