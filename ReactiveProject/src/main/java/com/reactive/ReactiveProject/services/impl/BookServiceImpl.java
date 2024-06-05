package com.reactive.ReactiveProject.services.impl;

import com.reactive.ReactiveProject.entities.Book;
import com.reactive.ReactiveProject.repositories.BookRepository;
import com.reactive.ReactiveProject.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;
    @Override
    public Mono<Book> create(Book book) {
        Mono<Book> createdBook = bookRepository.save(book);
        return createdBook;

    }

    @Override
    public Flux<Book> getAll() {
        return bookRepository.findAll();
    }

    @Override
    public Mono<Book> get(int bookId) {
        Mono<Book> item = bookRepository.findById(bookId);
        return item;
    }

    @Override
    public Mono<Book> update(Book book, int bookId) {
        Mono<Book> oldBook = bookRepository.findById(bookId);
        return oldBook.flatMap(book1 -> {
            book1.setName(book.getName());
            book1.setPublisher(book.getPublisher());
            book1.setAuthor(book.getAuthor());
            book1.setDescription(book.getDescription());
            return bookRepository.save(book1);
        });
    }

    @Override
    public Mono<Void> delete(int bookId) {
        return bookRepository.findById(bookId)
                .flatMap(bookRepository::delete);
    }

    @Override
    public Flux<Book> search(String query) {
        return null;
    }
    public Flux<Book> findByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }
    public Flux<Book> findByNameAndAuthor(String name, String author) {
        return bookRepository.findByNameAndAuthor(name, author);
    }
    public Mono<Book> partialUpdate(Book book, int bookId) {
        return bookRepository.findById(bookId)
                .flatMap(existingBook -> {
                    if (book.getName() != null) {
                        existingBook.setName(book.getName());
                    }
                    if (book.getDescription() != null) {
                        existingBook.setDescription(book.getDescription());
                    }
                    if (book.getPublisher() != null) {
                        existingBook.setPublisher(book.getPublisher());
                    }
                    if (book.getAuthor() != null) {
                        existingBook.setAuthor(book.getAuthor());
                    }
                    return bookRepository.save(existingBook);
                })
                .switchIfEmpty(Mono.empty());
    }
    }

