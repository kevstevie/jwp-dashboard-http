package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

public class ResponseWriter {

    private static final String STATIC = "static";
    private static final String CSS = "/css";

    private final OutputStream outputStream;
    private final RequestReader requestReader;

    public ResponseWriter(OutputStream outputStream, RequestReader requestReader) {
        this.outputStream = outputStream;
        this.requestReader = requestReader;
    }

    public void createResponse() throws IOException {
        String contentType = requestReader.getContentType();
        if (contentType.contains(ContentType.CSS.getType())) {
            createCssResponse();
            return;
        }
        if (contentType.contains(ContentType.ALL.getType()) || contentType.contains(ContentType.HTML.getType())) {
            createHtmlResponse();
            return;
        }
    }

    private void createHtmlResponse() throws IOException {
        String url = requestReader.getRequestUrl();
        String path = getClass().getClassLoader().getResource(STATIC + url).getPath();
        String responseBody = createResponseBody(path);

        final var response = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: " + responseBody.getBytes().length + " ",
                "",
                responseBody);
        try (outputStream) {
            outputStream.write(response.getBytes());
            outputStream.flush();
        }
    }

    private void createCssResponse() throws IOException {
        String url = requestReader.getRequestUrl();
        String path = getClass().getClassLoader().getResource(STATIC + url).getPath();
        String responseBody = createResponseBody(path);

        final var response = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/css;charset=utf-8 ",
                "Content-Length: " + responseBody.getBytes().length + " ",
                "",
                responseBody);
        try (outputStream) {
            outputStream.write(response.getBytes());
            outputStream.flush();
        }
    }

    private String createResponseBody(String path) throws IOException {
        File file = new File(path);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        }
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public RequestReader getRequestReader() {
        return requestReader;
    }
}
