package com.avik.personal_library

import com.avik.personal_library.repository.BookRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.MessageSource
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@WebMvcTest
class BookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var bookRepository: BookRepository

    @MockitoBean
    private lateinit var messageSource: MessageSource

    @Test
    fun `getAllBooks should return 200 OK with a list of books`() {
        val mockBooks = listOf(
            Book(id = 1, title = "Title 1", author = "Author 1", isbn = "1234567890", publicationYear = 2020),
            Book(id = 2, title = "Title 2", author = "Author 2", isbn = "0987654321", publicationYear = 2022)
        )

        `when`(bookRepository.findAll()).thenReturn(mockBooks)

        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[0].title").value("Title 1"))
            .andExpect(jsonPath("$[1].title").value("Title 2"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `getALlBooks should return 204 NO CONTENT when no books are found`() {
        `when`(bookRepository.findAll()).thenReturn(emptyList())
        `when`(messageSource.getMessage(eq("book.retrieval_failed"), isNull(), any())).thenReturn("Failed to retrieve books")

        mockMvc.perform(get("/api/books"))
            .andExpect(status().isNoContent)
            .andDo(MockMvcResultHandlers.print())

    }

    @Test
    fun `getAllBooks should return 500 INTERNAL SERVER ERROR on database error`() {
        // Arrange
        val errorMessage = "Failed to retrieve books"
        `when`(bookRepository.findAll()).thenThrow(IncorrectResultSizeDataAccessException("Simulated database error", 1))
        `when`(messageSource.getMessage(eq("book.retrieval_failed"), isNull(), any())).thenReturn(errorMessage)

        // Act & Assert
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().string(""))
//            .andExpect(jsonPath("$.message").value(errorMessage))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `deleteBook should return 204 NO CONTENT if book is deleted successfully`() {
        // Arrange
        `when`(bookRepository.existsById(1L)).thenReturn(true)

        // Act & Assert
        mockMvc.perform(delete("/api/books/1"))
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))
            .andDo(MockMvcResultHandlers.print())
    }

}