package org.jenkinsci.plugins.artifactpromotion;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jenkinsci.plugins.artifactpromotion.exception.ArtifactResolveException;
import org.jenkinsci.plugins.artifactpromotion.helper.PomHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Timo "timii" Paananen
 *
 */
public class PomHelperTest {

	private PomHelper pomHelper;

	@Before
	public void setup() {
		pomHelper = new PomHelper();
	}

	@Test
	public void testGetModuleList() throws URISyntaxException,
			ArtifactResolveException {
		final List<String> EXPECTED = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			{
				add("module1");
				add("module2");
				add("module3");
			}
		};
		URL pomURL = this.getClass().getResource("/test-pom-multimodule.xml");
		File testPomFile = new File(pomURL.toURI());
		Artifact artifact = new DefaultArtifact("org.jenkins-ci.test", "test",
				"", "pom", "1.0.0", new HashMap<String, String>(), testPomFile);

		List<String> actual = pomHelper.getModuleNames(artifact);
		Assert.assertEquals(EXPECTED, actual);

	}

	@Test(expected = ArtifactResolveException.class)
	public void testGetModuleList_doesNotHaveModulesList()
			throws ArtifactResolveException, URISyntaxException {
		URL pomURL = this.getClass().getResource("/test-pom-no-modules.xml");
		File testPomFile = new File(pomURL.toURI());
		Artifact artifact = new DefaultArtifact("org.jenkins-ci.test", "test",
				"", "jar", "1.0.0", new HashMap<String, String>(), testPomFile);

		pomHelper.getModuleNames(artifact);
	}

}
