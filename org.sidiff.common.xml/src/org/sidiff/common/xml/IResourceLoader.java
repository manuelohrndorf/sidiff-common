package org.sidiff.common.xml;

import java.io.IOException;
import java.io.InputStream;

public interface IResourceLoader {
	
	InputStream loadResourceAsStream(String path) throws IOException;

}
