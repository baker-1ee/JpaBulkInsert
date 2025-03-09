package com.example.bookstore.domain.book.entity

import com.example.bookstore.common.jpa.entity.BulkInsertEntityJ
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Books")
class Book private constructor(
    bookSeq: Long,
    title: String,
    author: String,
    publicationDate: LocalDateTime? = null,
    price: BigDecimal? = null
) : BulkInsertEntityJ() {

    @Id
    @Column
    var bookSeq: Long = bookSeq
        protected set

    @Column
    var title: String = title
        protected set

    @Column
    var author: String = author
        protected set

    @Column
    var publicationDate: LocalDateTime? = publicationDate
        protected set

    @Column
    var price: BigDecimal? = price
        protected set

    override fun getId(): Long {
        return bookSeq
    }

    fun updatePrice(price: BigDecimal) {
        this.price = price
    }

    companion object {
        fun create(title: String, author: String, price: BigDecimal) {
            
        }
    }

}
