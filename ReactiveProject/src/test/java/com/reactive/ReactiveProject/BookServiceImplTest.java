package com.reactive.ReactiveProject;

import com.reactive.ReactiveProject.entities.Book;
import com.reactive.ReactiveProject.repositories.BookRepository;
import com.reactive.ReactiveProject.services.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

    @SpringBootTest
    public class BookServiceImplTest {

        @Autowired
        private BookService bookService;

        @MockBean
        private BookRepository bookRepository;

        @Test
        void testCreateBook() {
            Book book = new Book(1, "Test Book", "Test Description", "Test Publisher", "Test Author");

            Mockito.when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(book));

            Mono<Book> createdBook = bookService.create(book);

            StepVerifier.create(createdBook)
                    .expectNext(book)
                    .verifyComplete();
        }

        @Test
        void testGetAllBooks() {
            Book book1 = new Book(1, "Test Book 1", "Test Description 1", "Test Publisher 1", "Test Author 1");
            Book book2 = new Book(2, "Test Book 2", "Test Description 2", "Test Publisher 2", "Test Author 2");

            Mockito.when(bookRepository.findAll()).thenReturn(Flux.just(book1, book2));

            Flux<Book> bookFlux = bookService.getAll();

            StepVerifier.create(bookFlux)
                    .expectNext(book1)
                    .expectNext(book2)
                    .verifyComplete();
        }

        @Test
        void testGetBookById() {
            Book book = new Book(1, "Test Book", "Test Description", "Test Publisher", "Test Author");

            Mockito.when(bookRepository.findById(anyInt())).thenReturn(Mono.just(book));

            Mono<Book> foundBook = bookService.get(1);

            StepVerifier.create(foundBook)
                    .expectNext(book)
                    .verifyComplete();
        }

        @Test
        void testUpdateBook() {
            Book existingBook = new Book(1, "Test Book", "Test Description", "Test Publisher", "Test Author");
            Book updatedBook = new Book(1, "Updated Book", "Updated Description", "Updated Publisher", "Updated Author");

            Mockito.when(bookRepository.findById(anyInt())).thenReturn(Mono.just(existingBook));
            Mockito.when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(updatedBook));

            Mono<Book> updatedMono = bookService.update(updatedBook, 1);

            StepVerifier.create(updatedMono)
                    .expectNext(updatedBook)
                    .verifyComplete();
        }

        @Test
        void testDeleteBook() {
            Book book = new Book(1, "Test Book", "Test Description", "Test Publisher", "Test Author");

            Mockito.when(bookRepository.findById(anyInt())).thenReturn(Mono.just(book));
            Mockito.when(bookRepository.delete(book)).thenReturn(Mono.empty());

            Mono<Void> deleted = bookService.delete(1);

            StepVerifier.create(deleted)
                    .verifyComplete();
        }
        @Test
        void testCreateBook_WithNullBook() {
            Mockito.when(bookRepository.save(Mockito.any()))
                    .thenReturn(Mono.empty());

            Mono<Book> createdBook = bookService.create(null);

            StepVerifier.create(createdBook)
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        void testGetBookById_InvalidId() {
            Mockito.when(bookRepository.findById(anyInt()))
                    .thenReturn(Mono.empty());

            Mono<Book> foundBook = bookService.get(-1);

            StepVerifier.create(foundBook)
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        void testUpdateBook_InvalidId() {
            Mockito.when(bookRepository.findById(anyInt()))
                    .thenReturn(Mono.empty());

            Mono<Book> updatedBook = bookService.update(new Book(1, "Title", "Desc", "Publisher", "Author"), -1);

            StepVerifier.create(updatedBook)
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        void testUpdateBook_WithNullBook() {
            Mockito.when(bookRepository.save(Mockito.any()))
                    .thenReturn(Mono.empty());

            Mono<Book> updatedBook = bookService.create(null);

            StepVerifier.create(updatedBook)
                    .expectNextCount(0)
                    .verifyComplete();
        }

    }


