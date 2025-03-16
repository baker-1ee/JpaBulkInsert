package com.jw.common.enums;

public interface BaseEnum {
    String getCode();

    static <E extends Enum<E> & BaseEnum> E fromCode(Class<E> enumClass, String code) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
