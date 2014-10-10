package org.jenkinsci.plugins.artifactpromotion.exception;

/**
 * 
 * @author Timo "timii" Paananen
 *
 */
public class ArtifactResolveException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ArtifactResolveException() {
		super();
	}
	
	public ArtifactResolveException(String message) {
		super(message);
	}
	
	public ArtifactResolveException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArtifactResolveException(Throwable e) {
		super(e);
	}
}
