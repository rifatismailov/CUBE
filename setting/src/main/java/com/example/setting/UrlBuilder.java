package com.example.setting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Клас UrlBuilder створює URL-адресу з різних частин.
 */
public class UrlBuilder {
    private final String protocol;
    private final String ip;
    private final String port;
    private final String directory;
    private final String fileName;

    /**
     * Приватний конструктор для ініціалізації об'єкта UrlBuilder через Builder.
     * @param builder об'єкт Builder, що містить параметри для URL
     */
    private UrlBuilder(Builder builder) {
        this.protocol = builder.protocol;
        this.ip = builder.ip;
        this.port = builder.port;
        this.directory = builder.directory;
        this.fileName = builder.fileName;
    }

    /**
     * Метод будує та повертає повний URL на основі наявних даних.
     * @return згенерований URL у вигляді рядка
     */
    public String buildUrl() {
        StringBuilder url = new StringBuilder();
        url.append(protocol).append("://").append(ip);

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

    /**
     * Клас Builder для гнучкого створення URL-адреси.
     */
    public static class Builder {
        private String protocol = "http"; // Значення за замовчуванням
        private String ip;
        private String port;
        private String directory;
        private String fileName;

        /**
         * Встановлює протокол URL.
         * @param protocol протокол (http або https)
         * @return поточний екземпляр Builder
         */
        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Встановлює IP-адресу сервера.
         * @param ip IP-адреса
         * @return поточний екземпляр Builder
         */
        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        /**
         * Встановлює порт сервера.
         * @param port номер порту
         * @return поточний екземпляр Builder
         */
        public Builder setPort(String port) {
            this.port = port;
            return this;
        }

        /**
         * Встановлює директорію.
         * @param directory шлях до директорії
         * @return поточний екземпляр Builder
         */
        public Builder setDirectory(String directory) {
            this.directory = directory;
            return this;
        }

        /**
         * Встановлює ім'я файлу.
         * @param fileName назва файлу
         * @return поточний екземпляр Builder
         */
        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        /**
         * Будує об'єкт UrlBuilder.
         * @return новий екземпляр UrlBuilder
         */
        public UrlBuilder build() {
            return new UrlBuilder(this);
        }
    }

    /**
     * Клас IPValidator для перевірки правильності введеної IP-адреси.
     */
    public static class IPValidator {
        private static final String IP_ADDRESS_PATTERN =
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        private final Pattern pattern;
        private Matcher matcher;

        /**
         * Конструктор ініціалізує регулярний вираз для перевірки IP-адреси.
         */
        public IPValidator() {
            pattern = Pattern.compile(IP_ADDRESS_PATTERN);
        }

        /**
         * Перевіряє, чи відповідає введений рядок формату IP-адреси.
         * @param ip IP-адреса у вигляді рядка
         * @return true, якщо IP-адреса коректна, false у протилежному випадку
         */
        public boolean validate(final String ip) {
            matcher = pattern.matcher(ip);
            return matcher.matches();
        }
    }

    /**
     * Клас PortValidator для перевірки правильності номера порту.
     */
    public static class PortValidator {
        private static final String PORT_PATTERN = "^([0-9]{1,5})$";

        private final Pattern pattern;
        private Matcher matcher;

        /**
         * Конструктор ініціалізує регулярний вираз для перевірки порту.
         */
        public PortValidator() {
            pattern = Pattern.compile(PORT_PATTERN);
        }

        /**
         * Перевіряє, чи є введений рядок коректним номером порту.
         * @param port номер порту у вигляді рядка
         * @return true, якщо номер порту валідний, false у протилежному випадку
         */
        public boolean validate(final String port) {
            matcher = pattern.matcher(port);
            if (matcher.matches()) {
                int portNumber = Integer.parseInt(port);
                return portNumber >= 0 && portNumber <= 65535;
            }
            return false;
        }
    }
}
