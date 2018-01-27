package org.dreamscale.templates.tasks

import org.dreamscale.templates.DreamScaleTemplatesPlugin
import org.gradle.api.tasks.TaskAction

class CreateLibraryProjectTask extends AbstractTemplateTask {

    CreateLibraryProjectTask() {
        super("Create a library project (options: -PrepoName=?, [-Pclean])")
        group = DreamScaleTemplatesPlugin.GROUP
    }

    @TaskAction
    void createLibraryProject() {
        boolean clean = projectProps.isPropertyDefined("clean")
        BasicProject basicProject = createBasicProject(clean)

        basicProject.initGradleProject()
    }

}
