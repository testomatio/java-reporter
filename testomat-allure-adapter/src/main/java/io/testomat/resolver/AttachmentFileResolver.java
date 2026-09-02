package io.testomat.resolver;

import java.io.IOException;

public interface AttachmentFileResolver {
    String find(String uuid) throws IOException;
}
