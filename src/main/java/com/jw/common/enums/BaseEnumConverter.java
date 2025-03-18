package com.jw.common.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class BaseEnumConverter<E extends Enum<E> & BaseEnum> implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected BaseEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return (attribute != null) ? attribute.getCode() : null;
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return (dbData != null) ? BaseEnum.fromCode(enumClass, dbData) : null;
    }
}
