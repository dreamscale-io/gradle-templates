package org.dreamscale.templates

import org.dreamscale.templates.tasks.AddJpaObjectTask
import org.dreamscale.templates.tasks.AddKafkaMessageTask
import org.dreamscale.templates.tasks.AddRestApiObjectTask
import org.dreamscale.templates.tasks.CreateBasicResourceTask
import org.dreamscale.templates.tasks.CreateIntegrationTestProjectTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.dreamscale.templates.tasks.AddKafkaContainerTask
import org.dreamscale.templates.tasks.AddPostgresContainerTask
import org.dreamscale.templates.tasks.CreateLibraryProjectTask
import org.dreamscale.templates.tasks.CreateDeployableProjectTask
import org.dreamscale.templates.tasks.CreateCrudResourceTask

/**
 * The core of the templates plugin.
 */
class DreamScaleTemplatesPlugin implements Plugin<Project> {

    static final String GROUP = 'Template'

    void apply(Project project) {
        ProjectProps customProps = new ProjectProps(project)

        if (customProps.isThisProjectGradleTemplates()) {
            customProps.applyCustomPropertiesFile()

            project.task 'createLibraryProject', type: CreateLibraryProjectTask
            project.task 'createDeployableProject', type: CreateDeployableProjectTask
            project.task 'createIntegrationTestProject', type: CreateIntegrationTestProjectTask
        }

        project.task 'createCrudResource', type: CreateCrudResourceTask
        project.task 'createBasicResource', type: CreateBasicResourceTask
        project.task 'addPostgresContainer', type: AddPostgresContainerTask
        project.task 'addKafkaContainer', type: AddKafkaContainerTask
        project.task 'addKafkaMessage', type: AddKafkaMessageTask
        project.task 'addRestApiObject', type: AddRestApiObjectTask
        project.task 'addJpaObject', type: AddJpaObjectTask
    }

}
