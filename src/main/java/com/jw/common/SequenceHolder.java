package com.jw.common;

import java.util.UUID;

public class SequenceHolder {
    public static Long nextValue() {
        return UUID.randomUUID().getLeastSignificantBits();
    }
}
