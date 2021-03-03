package org.sidiff.common.ui.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

/**
 * A read-only editor input which contains a string.
 * @author rmueller
 */
public class StringEditorInput implements IStorageEditorInput {

	private final String name;
	private final String input;

	public StringEditorInput(String name, String input) {
		this.name = name;
		this.input = input;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return name;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public IStorage getStorage() throws CoreException {
		return new StringStorage();
	}

	private class StringStorage implements IStorage {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public IPath getFullPath() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}
	}
}
