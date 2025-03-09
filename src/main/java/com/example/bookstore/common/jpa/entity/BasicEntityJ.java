package com.example.bookstore.common.jpa.entity;

import lombok.Getter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@ToString
@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class BasicEntityJ {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdDatetime;

    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime updatedDatetime;

    @CreatedBy
    protected Long createdBy;

    @LastModifiedBy
    protected Long updatedBy;

}
