package org.xml.sax;

import java.io.IOException;

public interface EntityResolver {
    
    public abstract InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException;
}
