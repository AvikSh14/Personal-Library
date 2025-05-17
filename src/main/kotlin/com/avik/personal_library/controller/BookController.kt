package com.avik.personal_library.controller

import com.avik.personal_library.Book
import com.avik.personal_library.repository.BookRepository
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookRepository: BookRepository,
    private val messageSource: MessageSource
) {
    private val logger = LoggerFactory.getLogger(BookController::class.java)

    @GetMapping
    fun getAllBooks(): ResponseEntity<List<Book>> {
        logger.info("Fetching all books")
        return try {
            val books = bookRepository.findAll()
            if (books.isEmpty()) {
                logger.warn("No books found")
                ResponseEntity.status(HttpStatus.NO_CONTENT).body(emptyList())
            } else {
                logger.info("Successfully retrieved {} books", books.size)
                ResponseEntity.ok(books)
            }
        } catch (exception: DataAccessException) {
            logger.error("Database error while fetching all books", exception)
            val errorMessage = messageSource.getMessage("book.retrieval_failed", null, Locale.getDefault())
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, exception)
        }
    }

    @GetMapping("/{id}")
    fun getBookById(@PathVariable id: Long): ResponseEntity<Book> {
        logger.info("Fetching book with id {}", id)
        return try {
            bookRepository.findById(id)
                .map {
                    logger.info("Fetched book with id {}", id)
                    ResponseEntity.ok(it)
                }
                .orElseThrow {
                    logger.warn("Book not found with id {}", id)
                    val errorMessage = messageSource.getMessage("book.not_found", arrayOf(id), Locale.getDefault())
                    ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
                }
        } catch (exception: DataAccessException) {
            logger.error("Database error while fetch book with id {}", id)
            val errorMessage = messageSource.getMessage("book.retrieval_failed", null, Locale.getDefault())
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, exception)
        }
    }

    @PostMapping
    fun createBook(@Valid @RequestBody book: Book): ResponseEntity<Book> {
        logger.info("Creating a new book: {}", book)
        return try {
            val savedBook = bookRepository.save(book)
            logger.info("Book created with id: {}", savedBook.id)
            ResponseEntity.status(HttpStatus.CREATED).body(savedBook)
        } catch (e: DataIntegrityViolationException) {
            logger.error("ISBN is not unique", e)
            val errorMessage = messageSource.getMessage("book.isbn_unique", null, Locale.getDefault())
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage, e)
        } catch (e: DataAccessException) {
            logger.error("Database error while creating a book", e)
            val errorMessage = messageSource.getMessage("book.creation_failed", null, Locale.getDefault())
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, e)
        }
    }

    @PutMapping
    fun updateBook(@PathVariable id: Long, @Valid @RequestBody updatedBook: Book): ResponseEntity<Book> {
        logger.info("Updating book with id {}", id)
        return try {
            bookRepository.findById(id)
                .map { currentBook ->
                    val bookToUpdate = currentBook.copy(
                        title = updatedBook.title,
                        author = updatedBook.author,
                        isbn = updatedBook.isbn,
                        publicationYear = updatedBook.publicationYear,
                        publisher = updatedBook.publisher
                    )

                    val savedBook = bookRepository.save(bookToUpdate)
                    ResponseEntity.ok(savedBook)
                }
                .orElseThrow {
                    logger.warn("Book not found with id: {}", id)
                    val errorMessage = messageSource.getMessage("book.not_found", arrayOf(id), Locale.getDefault())
                    ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
                }
        } catch (e: DataIntegrityViolationException) {
            logger.error("ISBN is not unique", e)
            val errorMessage = messageSource.getMessage("book.isbn_unique", null, Locale.getDefault())
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage, e)
        } catch (e: DataAccessException) {
            logger.error("Database error while updating book with id: {}", id, e)
            val errorMessage = messageSource.getMessage("book.update_failed", null, Locale.getDefault())
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, e)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteBook(@PathVariable id: Long): ResponseEntity<Void> {
        logger.info("Deleting book with id: {}", id)
        return try {
            if (bookRepository.existsById(id)) {
                bookRepository.deleteById(id)
                logger.info("Book deleted with id: {}", id)
                ResponseEntity.noContent().build()
            } else {
                logger.warn("Book not found with id: {}", id)
                val errorMessage = messageSource.getMessage("book.not_found", arrayOf(id), Locale.getDefault())
                throw ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
            }
        } catch (e: DataAccessException) {
            logger.error("Database error while deleting book with id: {}", id, e)
            val errorMessage = messageSource.getMessage("book.deletion_failed", null, Locale.getDefault())
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, e)
        }
    }
}