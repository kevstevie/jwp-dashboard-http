package org.apache.coyote.http11;

import org.apache.catalina.ResourceHandler;
import org.apache.coyote.http11.request.HttpRequest;

public interface Adaptor {

    Controller findController(HttpRequest httpRequest);

    void addController(String url, Controller controller);

    void setResourceHandler(ResourceHandler resourceHandler);
}
