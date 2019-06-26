# Artifact Promotion Plugin

This is a simple plugin to *promote* artifacts. This is done on the artifact repository server and due to this the promotion process is specific to the used repository server.

In the first step, this plugin will support Sonatype Nexus OSS. 

## Artifact Promotion in Sonatype Nexus OSS
Nexus OSS doesn't support staging repositories like Nexus Pro. And, in addition, it does't support custom metadata.
Due to this, an artifact promotion could only be handled by moving or copying an artifact from a staging repository into a 'release' repository (... or however you want to call your stage).

Some guys say this is an anti pattern like in [this blog](http://www.alwaysagileconsulting.com/articles/pipeline-antipattern-artifact-promotion/) but there is no chance to do it the right way with Nexus OSS.

# Usage 
The plugin is in development and you should consider that some parts are subject to change. Future changes can effect the GUI, pipeline code, the Job DSL interface and the configuration file, resulting in breaking changes while upgrading. We'll try to ensure to mark such changes, but you'll use the plugin at your own risk.

## Defining a Promotion Job using Job DSL
The plugin adds an extension to the Job DSL plugin to allow defining Artifact Promotion build steps in Job DSL scripts. The extension can be used with the Job DSL plugin version 1.69 or higher.

```
job {
	steps {
	    artifactPromotion {
	      groupId(String groupId)
	      artifactId(String artifactId)
	      classifier(String classifier)
	      version(String version)
	      extension(String extension = "jar")
	      stagingRepository(String url, String user, String password, boolean skipDeletion = true)
	      releaseRepository(String url, String user, String password)
	      debug(boolean debug)
	    }
	}
}
```

Creates a build step to promote an artifact from a staging to a release repository, unchecking *skipDeletion* which causes the deletion of the **whole** version with all files from the staging repository (this is disabled by default). 

```
job('example') {
	steps {
		// assumes default repo system (NexusOSS)
	    artifactPromotion {
	      groupId("com.example.test")
	      artifactId("my-artifact")
	      version("1.0.0")
	      extension("zip")
	      stagingRepository("http://nexus.myorg.com:8080/content/repositories/release-candidates", "foo", "s3cr3t", false)
	      releaseRepository("http://nexus.myorg.com:8080/content/repositories/releases", "foo", "s3cr3t")
	    }
	}
}
```

## Pipeline
For usage within Pipeline scripts use the snippet generator or see the example below:

### Scripted pipeline

```
stage('example') {
    artifactPromotion (
        promoterClass: 'org.jenkinsci.plugins.artifactpromotion.NexusOSSPromotor',
        groupId: 'com.example.test',
        artifactId: 'my-artifact',
        version: '1.0.0',
        extension: 'zip',
        stagingRepository: 'http://nexus.myorg.com:8080/content/repositories/release-candidates',
        stagingUser: 'foo',
        stagingPW: 's3cr3t',
        skipDeletion: true,
        releaseRepository: 'http://nexus.myorg.com:8080/content/repositories/releases',
        releaseUser: 'foo',
        releasePW: 's3cr3t'
    )
}
```

### Declarative pipeline

```
stage('example') {
    steps {
        artifactPromotion (
            promoterClass: 'org.jenkinsci.plugins.artifactpromotion.NexusOSSPromotor',
	    debug: false,
            groupId: 'com.example.test',
            artifactId: 'my-artifact',
            version: '1.0.0',
            extension: 'zip',
            stagingRepository: 'http://nexus.myorg.com:8080/content/repositories/release-candidates',
            stagingUser: 'foo',
            stagingPW: 's3cr3t',
            skipDeletion: true,
            releaseRepository: 'http://nexus.myorg.com:8080/content/repositories/releases',
            releaseUser: 'foo',
            releasePW: 's3cr3t'
        )
    }
}
```

## Artifact deletion
When you promote artifacts from the staging to the release repository you may want to remove the artifact from staging. If your artifact only has one associated file, the plugin works as expected.
Although if you're using classifiers, deletion removes all files associated with the artifact. The *Skip deletion* option preserves the files in the staging repository. 
Untick 'Skip deletion' only after you've promoted all the relevant files in previous steps. Use a promotion step for each classifier.

**ATTENTION:** Use the possibility to delete files very carefully!

By default, the option *Skip deletion* is enabled.

## Promoting POM artifacts

Starting with version 0.5.2 it is possible to promote POM artifacts, like parent POM or multi module project descriptions, specifying the POM to be promoted and indicating the extension `pom`.

```
stage('example') {
    artifactPromotion (
        promoterClass: 'org.jenkinsci.plugins.artifactpromotion.NexusOSSPromotor',
        groupId: 'com.example.test',
        artifactId: 'my-pom-artifact',
        version: '1.0.0',
        extension: 'pom',
        stagingRepository: 'http://nexus.myorg.com:8080/content/repositories/release-candidates',
        stagingUser: 'foo',
        stagingPW: 's3cr3t',
        skipDeletion: true,
        releaseRepository: 'http://nexus.myorg.com:8080/content/repositories/releases',
        releaseUser: 'foo',
        releasePW: 's3cr3t'
    )
}
```

# Contributions
Please feel free to contribute for other repository servers like

* Nexus Pro
* Artifactory and Aertifactory Pro
* Apache Archiva

Additionally, we've don't yet support the Jenkins Credentials Plugin.

Don't hesitate to come up with your suggestions. Pull requests are preferred as I'm limited in my time.

# History

* 0.5.2 - Allow promoting POM artifacts. Fix debug functionality in the delete function. 
* 0.5.1 - Support for Jenkins Pipelines and minor bug fixes; upgrade dependency to Job DSL 1.69, Upgrade used Aether version, fixes some FindBugs findings
* 0.4.0 - Support for Maven Classifiers
* 0.3.6 - Support for Job DSL Plugin 

# Known Issues
[ ] The plugin doesn't supports Jenkins Credentials plugin. Due to this, credentials are written and saved in plain text then using Job DSL oder Pipeline DSL. (Hint: I would very appreciate a pull request implementing this).

# Useful links
* Plugin Wiki page: [https://wiki.jenkins-ci.org/display/JENKINS/ArtifactPromotionPlugin](https://wiki.jenkins-ci.org/display/JENKINS/ArtifactPromotionPlugin)
