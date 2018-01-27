# DreamScale Project Templates

This project is used to bootstrap new projects as well as adding to existing projects.  

There are times during development where the easiest way forward is to copy a set of classes and then proceed 
renaming/deleting/etc.  This project exists b/c that process is error prone and just generally sucks.


## Initialization

This project requires a primary workspace directory to be configured - a default root directory for all subsequent operations.

The default workspace is `..`, the directory where you checked out the gradle-templates project.  This value is stored
in `gradle/custom.gradle` which is initialized when the plugin is first applied.  Edit this file if you want to change 
the workspace directory.


## Usage

There are two intended uses for this project - as a creator of new projects and augmentor of existing projects.

Run the `./gradlew tasks` command and look at the group `Template tasks` to see a list of the available tasks.


### Project Creation Tasks

The project creation tasks are only available when run from this project.  All tasks require the project property
`repoName` to be specified in order to identify the target directory.  

#### createLibraryProject

Creates a basic gradle project with a simple build.gradle file.

Supported task options:
* clean - if the target directory already exists, delete it

#### createDeployableProject

Creates a skeleton SpringBoot project (includes build.gradle, application class, and supporting classes)

Supported task options:
* clean - if the target directory already exists, delete it
* postgres - initializes the project with a postgres container and supporting files
* serviceName - the name of the SpringBoot application entrypoint, defaults to the repository name, converted to upper camel-case
* servicePackageName - the name of the core package (contains the application entrypoint), defaults to "org.dreamscale.${serviceName.toLowercase()}"

### Project Augmentation Tasks

The project augmentation tasks are available when run from this project but also when the `dreamscale-templates` plugin
is applied to another project like so...  

```
buildscript {
    dependencies {
        classpath "org.dreamscale:gradle-templates:1.+"
    }
}

apply plugin: "dreamscale-templates"
```

In this case, the target directory will be the project which applies the plugin and the project property `repoName` 
will be ignored.

#### createRestResource

Creates a resource in an existing SpringBoot REST project (includes the Resource, ResourceSpec, ResourceWireSpec, etc)
* You may need to update the package for application.properties swagger.resource.package if your resource does not live in org.dreamscale.<servicePackageName>.resources

#### addPostgresContainer

Adds a Postgres docker container configuration to an existing project


## Publishing to Bintray

Make sure you have a bintray account and are a member of the [DreamScale organization](https://bintray.com/dreamscale/organization/edit)

Open your [user profile](https://bintray.com/profile/edit/organizations) and retrieve your API Key

Execute bintray upload `gw bintrayUpload -Pbintray.user=<bintray user> -Pbintray.apiKey=<api key>`

Open the DreamScale [gradle-templates](https://bintray.com/dreamscale/maven-public/org.dreamscale%3Agradle-templates) package and
click the [Publish](https://bintray.com/dreamscale/maven-public/org.dreamscale%3Agradle-templates/publish) link