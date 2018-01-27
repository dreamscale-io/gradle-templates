package org.dreamscale.templates

import org.dreamscale.templates.tasks.BasicProject

class BasicProjectBuilder {

    File repoDir;
    String name;
    File gradleUserHome;
    String dreamscaleGradleVersion;
    boolean clean = false;

    private BasicProjectBuilder() {}

    static BasicProjectBuilder getInstance() {
        new BasicProjectBuilder()
    }

    BasicProjectBuilder repoDir(File repoDir) {
        this.repoDir = repoDir
        this
    }

    BasicProjectBuilder dreamscaleGradleVersion(String dreamscaleGradleVersion) {
        this.dreamscaleGradleVersion = dreamscaleGradleVersion
        this
    }

    BasicProjectBuilder gradleUserHome(File gradleUserHome) {
        this.gradleUserHome = gradleUserHome
        this
    }

    BasicProjectBuilder name(String name) {
        this.name = name
        this
    }

    BasicProjectBuilder clean() {
        this.clean = true
        this
    }

    BasicProject build() {
        GitRepo gitRepo = GitRepo.initGitHubRepo(name, repoDir)
        BasicProject basicProject = new BasicProject(dreamscaleGradleVersion, gitRepo, gradleUserHome)
        basicProject.initGradleProject()
        basicProject
    }

}
