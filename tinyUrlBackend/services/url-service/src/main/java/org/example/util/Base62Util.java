package org.example.util;

public class Base62Util {
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String idToBase62 (Long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            int remainder = (int) (id % 62);
            sb.append(BASE62.charAt(remainder));
            id /= 62;
        }
        return sb.reverse().toString();
    }

    public static Long base62ToId(String base62) {
        long id = 0;
        for (int i = 0; i < base62.length(); i++) {
            id = id * 62 + BASE62.indexOf(base62.charAt(i));
        }
        return id;
    }
}
