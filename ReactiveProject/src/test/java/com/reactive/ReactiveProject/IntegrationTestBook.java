package com.reactive.ReactiveProject;

import com.reactive.ReactiveProject.controller.BookController;
import com.reactive.ReactiveProject.entities.Book;
import com.reactive.ReactiveProject.services.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@WebFluxTest(BookController.class)
class IntegrationTestBook {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("Add Books")
    public void addBookTest() {
        Book book = new Book(1, "Book 1", "Description 1", "Publisher 1", "Author 1");
        Mono<Book> bookMono = Mono.just(book);
        when(bookService.create(any(Book.class))).thenReturn(bookMono);

        webTestClient.post().uri("/books")
                .body(Mono.just(book), Book.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("RetrieveAllBooks")
    public void getBooksTest() {
        Book book1 = new Book(1, "Book 1", "Description 1", "Publisher 1", "Author 1");
        Book book2 = new Book(2, "Book 2", "Description 2", "Publisher 2", "Author 2");
        Flux<Book> bookFlux = Flux.just(book1, book2);
        when(bookService.getAll()).thenReturn(bookFlux);

        Flux<Book> responseBody = webTestClient.get().uri("/books")
                .exchange()
                .expectStatus().isOk()
                .returnResult(Book.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectSubscription()
                .consumeNextWith(book -> {
                    assertEquals(book1.getBookId(), book.getBookId());
                    assertEquals(book1.getName(), book.getName());
                    assertEquals(book1.getDescription(), book.getDescription());
                    assertEquals(book1.getPublisher(), book.getPublisher());
                    assertEquals(book1.getAuthor(), book.getAuthor());
                })
                .consumeNextWith(book -> {
                    assertEquals(book2.getBookId(), book.getBookId());
                    assertEquals(book2.getName(), book.getName());
                    assertEquals(book2.getDescription(), book.getDescription());
                    assertEquals(book2.getPublisher(), book.getPublisher());
                    assertEquals(book2.getAuthor(), book.getAuthor());
                })
                .verifyComplete();
    }


    @Test
    @DisplayName("RetrieveBooksByID")
    public void getBookTest() {
        Book expectedBook = new Book(1, "Book 1", "Description 1", "Publisher 1", "Author 1");
        Mono<Book> bookMono = Mono.just(expectedBook);
        when(bookService.get(any(Integer.class))).thenReturn(bookMono);

        Flux<Book> responseBody = webTestClient.get().uri("/books/{bookId}", 1)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Book.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectSubscription()
                .consumeNextWith(book -> {
                    assertEquals(expectedBook.getBookId(), book.getBookId());
                    assertEquals(expectedBook.getName(), book.getName());
                    assertEquals(expectedBook.getDescription(), book.getDescription());
                    assertEquals(expectedBook.getPublisher(), book.getPublisher());
                    assertEquals(expectedBook.getAuthor(), book.getAuthor());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("UpdateBooksByID")
    public void updateBookTest() {
        Book updatedBook = new Book(1, "Updated Book 1", "Updated Description 1", "Updated Publisher 1", "Updated Author 1");
        Mono<Book> bookMono = Mono.just(updatedBook);
        when(bookService.update(any(Book.class), any(Integer.class))).thenReturn(bookMono);

        webTestClient.put().uri("/books/{bookId}", 1)
                .body(Mono.just(updatedBook), Book.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("DeleteBook")
    public void deleteBookTest() {
        given(bookService.delete(any(Integer.class))).willReturn(Mono.empty());

        webTestClient.delete().uri("/books/{bookId}", 1)
                .exchange()
                .expectStatus().isOk();
    }
}