package com.reactive.ReactiveProject;
import com.reactive.ReactiveProject.controller.BookController;
import com.reactive.ReactiveProject.entities.Book;
import com.reactive.ReactiveProject.services.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private Book book;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        book = new Book(1, "Advanced spring stuff part 2", "this is spring boot stuff for advanced", "DEF publisher", "Narender Misra");
    }

    @Test
    @DisplayName("Create Book")
    void testCreateBook() {
        when(bookService.create(book)).thenReturn(Mono.just(book));

        StepVerifier.create(bookController.create(book))
                .expectNext(book)
                .verifyComplete();
    }

    @Test
    void testGetAllBooks() {
        when(bookService.getAll()).thenReturn(Flux.just(book));

        StepVerifier.create(bookController.getAll())
                .expectNext(book)
                .verifyComplete();
    }

    @Test
    void testGetBookById() {
        int bookId = 1;
        when(bookService.get(bookId)).thenReturn(Mono.just(book));

        StepVerifier.create(bookController.get(bookId))
                .expectNext(book)
                .verifyComplete();
    }

    @Test
    void testUpdateBook() {
        int bookId = 1;
        Book updatedBook = new Book(1, "Updated Title", "Updated Author", "Updated Publisher", "Updated Description");
        when(bookService.update(updatedBook, bookId)).thenReturn(Mono.just(updatedBook));

        StepVerifier.create(bookController.update(updatedBook, bookId))
                .expectNext(updatedBook)
                .verifyComplete();
    }

    @Test
    void testDeleteBook() {
        int bookId = 1;
        when(bookService.delete(bookId)).thenReturn(Mono.empty());

        StepVerifier.create(bookController.delete(bookId))
                .verifyComplete();
    }
}