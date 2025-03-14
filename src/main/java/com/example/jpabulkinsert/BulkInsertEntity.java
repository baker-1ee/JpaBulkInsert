package com.example.jpabulkinsert;

import org.springframework.data.domain.Persistable;

import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class BulkInsertEntity<T> implements Persistable<Long> {

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    private void markIsNotNew() {
        isNew = false;
    }

}
