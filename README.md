# Artifact Promotion Plugin

This is a simple plugin to *promote* artifacts. This is done on the artifact repository server and due to this the promotion process is specific to the used repository server.

In the first step, this plugin will support Sonatype Nexus OSS. 

## Artifact Promotion in Sonatype Nexus OSS
Nexus OSS doesn't support staging repositories like Nexus Pro. And, in addition, it does't support custom metadata.
Due to this, an artifact promotion could only be handled by moving an artifact from a staging repository into a 'release' repository (... or however you want to call your stage).

Some guys say this is an anti pattern like in this blog (http://www.alwaysagileconsulting.com/pipeline-antipattern-mutable-binary-location/) but there is no chance to do it the right way with Nexus OSS.

# Usage 
The plugin is in development and should not be used currently for production environments as many parts are subject to change. Especially the support for multiple repository servers will change the GUI and result in some internal refactorings.

# Contributions
Please feel free to contribute for other repository servers like
* Nexus Pro
* Artifactory and Aertifactory Pro
* Apache Archiva

Don't hesitate to come up with your suggestions.

# Useful links
* Plugin Wiki page: https://wiki.jenkins-ci.org/display/JENKINS/ArtifactPromotionPlugin
