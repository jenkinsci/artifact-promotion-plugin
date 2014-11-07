package org.jenkinsci.plugins.artifactpromotion.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.aether.artifact.Artifact;
import org.jenkinsci.plugins.artifactpromotion.exception.ArtifactResolveException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Helper class for extracting module names and packaging information from artifact's pom.xml
 * 
 * @author Timo "timii" Paananen
 *
 */
public class PomHelper {

	public List<String> getModuleNames(Artifact pom)
			throws ArtifactResolveException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expression = null;
		try {
			expression = xpath.compile("normalize-space(/project/modules)");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		String modulesAsString = evaluateExpression(pom, expression);

		if (modulesAsString == null || modulesAsString.isEmpty()) {
			throw new ArtifactResolveException("No modules found form pom "
					+ pom.getGroupId() + ":" + pom.getArtifactId() + ":"
					+ pom.getVersion());
		}

		StringTokenizer modules = new StringTokenizer(modulesAsString);
		List<String> moduleList = new ArrayList<String>();
		while (modules.hasMoreTokens()) {
			moduleList.add(modules.nextToken());
		}
		return moduleList;
	}

	public String getModulesPackaging(Artifact pom)
			throws ArtifactResolveException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			XPathExpression expression = xpath.compile("normalize-space(/project/packaging)");
			String extension = evaluateExpression(pom, expression);
			//TODO: Mapping table for packinging formats and actual extensions
			return "ejb".equalsIgnoreCase(extension) ? "jar" : extension;
		} catch (XPathExpressionException e) {
			return "jar";
		}

	}
	
	private String evaluateExpression(Artifact pom, XPathExpression expression)
			throws ArtifactResolveException {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document docu;
			docu = builder.parse(pom.getFile());
			return expression.evaluate(docu);
		} catch (ParserConfigurationException e) {
			throw new ArtifactResolveException(e);
		} catch (IOException e) {
			throw new ArtifactResolveException("Could not read main pom.xml", e);
		} catch (SAXException e) {
			throw new ArtifactResolveException("Could not parse main pom.xml",
					e);
		} catch (XPathExpressionException e) {
			throw new ArtifactResolveException("Invalid xpath selector", e);
		}

	}
}
