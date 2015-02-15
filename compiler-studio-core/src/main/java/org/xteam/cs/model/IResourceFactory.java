/**
 * 
 */
package org.xteam.cs.model;

import java.io.File;

public interface IResourceFactory {

	ProjectResource create(Project project, File file);

	String getExtension();
	
}