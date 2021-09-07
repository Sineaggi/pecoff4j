package com.kichik.pecoff4j;

import com.kichik.pecoff4j.io.DataReader;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class StringsStream2 {
    private final NavigableMap<Integer, String> strings;

    public StringsStream2(DataReader dr, int length) throws IOException {
        NavigableMap<Integer, String> strings = new TreeMap<>();
        int startingPos = dr.getPosition();
        for (int i = startingPos; i < startingPos + length; i = dr.getPosition()) {
            String str = dr.readUtf();
            strings.put(i - startingPos, str);
        }
        this.strings = strings;
    }

    public String get(int offset) {
        return strings.computeIfAbsent(offset, (i) -> {
            Map.Entry<Integer, String> low = strings.floorEntry(offset);
            return low.getValue().substring(offset - low.getKey());
        });
    }

    public Map.Entry<Integer, String> get(String key) {
        for (Map.Entry<Integer, String> integerStringEntry : strings.entrySet()) {
            if (key.equals(integerStringEntry.getValue())) {
                return integerStringEntry;
            }
        }
        return null;
    }
}
