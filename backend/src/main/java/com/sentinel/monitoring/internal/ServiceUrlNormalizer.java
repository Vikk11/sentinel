package com.sentinel.monitoring.internal;

import java.net.URI;
import java.util.Locale;

final class ServiceUrlNormalizer {

    private ServiceUrlNormalizer() {
    }

    static String normalize(String url) {
        URI uri = URI.create(url);

        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        String host = uri.getHost();

        if (host == null) {
            throw new InvalidServiceUrlException();
        }

        //URI implementations may return an IPv6 host with brackets
        host = host.replace("[", "").replace("]", "")
                .toLowerCase(Locale.ROOT);

        int port = uri.getPort();

        //Default HTTP/HTTPS ports are equivalent to omitted ports
        if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
            port = -1;
        }

        //An empty HTTP path is equivalent to "/"
        String path = uri.getRawPath();

        if (path == null || path.isEmpty()) {
            path = "/";
        }

        StringBuilder result = new StringBuilder()
                .append(scheme)
                .append("://");

        if (host.contains(":")) {
            result.append('[').append(host).append(']');
        } else {
            result.append(host);
        }

        if (port != -1) {
            result.append(':').append(port);
        }

        result.append(path);

        if (uri.getRawQuery() != null) {
            result.append('?').append(uri.getRawQuery());
        }

        return result.toString();
    }
}