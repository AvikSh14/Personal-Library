package com.avik.personal_library

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

@Entity
@Table(name = "books")
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    @Column(nullable = false)
    val title: String,

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must be less than 255 characters")
    @Column(nullable = false)
    val author: String,

    @NotBlank(message = "ISBN is required")
    @Size(min = 10, max = 13, message = "ISBN must be 10 or 13 characters")
    @Column(nullable = false, unique = true)
    val isbn: String,

    @Positive(message = "Publication year must be a positive number")
    @Column(nullable = false)
    val publicationYear: Int,

    @Size(max = 1000, message = "Publisher must be less than 1000 characters")
    @Column(nullable = true) // Publisher is optional
    val publisher: String? = null
)
