package com.example.bookstore.common.jpa.entity

import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.hibernate.envers.Audited
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener::class)
abstract class BasicEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDatetime: LocalDateTime? = null
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    var updatedDatetime: LocalDateTime? = null
        protected set

    @CreatedBy
    var createdBy: Long? = null
        protected set

    @LastModifiedBy
    var updatedBy: Long? = null
        protected set

    // Lombok의 @ToString(callSuper = true) 대체
    override fun toString(): String {
        return ReflectionToStringBuilder.toString(this)
    }
}
