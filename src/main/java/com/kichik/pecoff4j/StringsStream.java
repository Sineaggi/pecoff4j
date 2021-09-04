package com.kichik.pecoff4j;

import com.kichik.pecoff4j.io.DataReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

public class StringsStream {
    private final byte[] data;
    private final Map<Integer, String> strings;

    public StringsStream(byte[] data) throws IOException {
        this.data = data;
        DataReader dr = new DataReader(data);
        Map<Integer, String> strings = new HashMap<>();
        for (int i = dr.getPosition(); i < data.length; i = dr.getPosition()) {
            String str = dr.readUtf();
            strings.put(i, str);
        }
        this.strings = strings;
    }

    public String get(int offset) throws IOException {
        try {
            return strings.computeIfAbsent(offset, (i) -> {
                DataReader dr = new DataReader(data);
                try {
                    dr.jumpTo(i);
                    return dr.readUtf();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
