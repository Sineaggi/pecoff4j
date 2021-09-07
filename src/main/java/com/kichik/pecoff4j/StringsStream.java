package com.kichik.pecoff4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

public class StringsStream {
    private final byte[] data;
    private final Map<Integer, String> strings = new HashMap<>();

    public StringsStream(byte[] data) {
        this.data = data;
    }

    private String readUtf(int offset) {
        StringBuilder sb = new StringBuilder();
        int c;
        int i = 0;
        while ((c = data[offset + i++]) != 0) {
            sb.append((char) c);
        }
        return sb.toString();
    }

    public String get(int offset) {
        return strings.computeIfAbsent(offset, this::readUtf);
    }
}
