package org.xteam.cs.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

public abstract class FileResource extends ProjectResource {

	private File file;
	private boolean isDirty;

	public FileResource(Project project, File file) {
		super(project);
		this.file = file;
	}
	
	@Override
	public String getName() {
		return file.getName();
	}

	public File getFile() {
		return file;
	}
	
	@Override
	public String getPath() {
		return file.getAbsolutePath();
	}
	
	@Override
	public boolean isDirty() {
		return isDirty;
	}
	
	@Override
	public String getContents() {
		isDirty = false;
		return read();
	}
	
	@Override
	public void save(String contents) {
		if (isDirty) {
			try {
				Writer str = new FileWriter(file);
				str.write(contents);
				str.close();
				markDirty(false);
			} catch (IOException e) {
			}
		}
	}
	
	private String read() {
		try {
			InputStream str = new FileInputStream(file);
			byte[] data = new byte[str.available()];
			str.read(data);
			str.close();
			return new String(data);
		} catch (IOException e) {
			return "";
		}
	}
	
	@Override
	public void markDirty(boolean isDirty) {
		if (this.isDirty != isDirty) {
			this.isDirty = isDirty;
			fireEvent(new ResourceEvent(ResourceEvent.CHANGE_STATE, this));
		}
	}
	
	@Override
	public String toString() {
		return file.getName();
	}
}
