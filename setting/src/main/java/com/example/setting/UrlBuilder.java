package com.example.setting;


public class UrlBuilder {
    private String protocol;
    private String ip;
    private String port;
    private String directory;
    private String fileName;

    private UrlBuilder(Builder builder) {
        this.protocol = builder.protocol;
        this.ip = builder.ip;
        this.port = builder.port;
        this.directory = builder.directory;
        this.fileName = builder.fileName;
    }

    public String buildUrl() {
        StringBuilder url = new StringBuilder();
        url.append(protocol).append("://")
                .append(ip);

        if (port != null && !port.isEmpty()) {
            url.append(":").append(port);
        }

        if (directory != null && !directory.isEmpty()) {
            if (!directory.startsWith("/")) {
                url.append("/");
            }
            url.append(directory);
        }

        if (fileName != null && !fileName.isEmpty()) {
            if (!directory.endsWith("/")) {
                url.append("/");
            }
            url.append(fileName);
        }

        return url.toString();
    }

    public static class Builder {
        private String protocol = "http"; // Значення за замовчуванням
        private String ip;
        private String port;
        private String directory;
        private String fileName;

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder setPort(String port) {
            this.port = port;
            return this;
        }

        public Builder setDirectory(String directory) {
            this.directory = directory;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public UrlBuilder build() {
            return new UrlBuilder(this);
        }
    }

}
