package com.infinities.library.services.impl;

import com.infinities.library.datas.BookUpdateData;
import com.infinities.library.exception.BookAlreadyExistedException;
import com.infinities.library.exception.BookNotFoundException;
import com.infinities.library.models.BookModel;
import com.infinities.library.repo.BookRepository;
import com.infinities.library.services.BookService;
import com.infinities.library.validator.BookValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookValidator bookValidator;

    @Override
    public BookModel createBook(final BookModel bookModel) {

        // should not have isbn
        if (!getBookValidator().isAvailableForCreate(bookModel)) {
            log.error("bookModel include isbn attribute {}", bookModel.getIsbn());
            throw new IllegalArgumentException("Please remove ISBN value for creating the book");
        }

        final BookModel existedBook = getBookRepository().findByName(bookModel.getName());
        if (Objects.nonNull(existedBook)) {
            throw new BookAlreadyExistedException("Book with current Name already exited, please use update");
        }

        log.info("Saving the book with book name {}", bookModel.getName());
        return getBookRepository().save(bookModel);
    }

    @Override
    public BookModel updateBook(final BookUpdateData updateData) {

        final BookModel toUpdate = Objects.nonNull(updateData.getIsbn()) ?
                getBookRepository().findById(updateData.getIsbn())
                        .orElse(getBookRepository().findByName(updateData.getName())) :
                getBookRepository().findByName(updateData.getName());

        if (Objects.isNull(toUpdate)) {
            log.error("Could not update the book with isbn {} or name {}", updateData.getIsbn(), updateData.getName());
            throw new BookNotFoundException("There is no book with current name or ISBN");
        }

        // validator to verify
        if (getBookValidator().isAvailableForUpdate(updateData, toUpdate)) {
            log.info("Update the book with book {}", toUpdate.getIsbn());
            return getBookRepository().save(toUpdate);
        }

        throw new IllegalArgumentException("At least put one attribute to update");
    }

    @Override
    public boolean deleteBook(final Long isbn) {
        try {
            log.info("delete the book with book isbn {}", isbn);
            getBookRepository().deleteById(isbn);
            return true;
        } catch (EmptyResultDataAccessException e) {
            log.error("There is no book with isbn {}", isbn);
            throw new IllegalArgumentException("There is no book with given isbn");
        }


    }

    @Override
    public List<BookModel> getAllBooks() {
        return getBookRepository().findAll();
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public BookValidator getBookValidator() {
        return bookValidator;
    }
}
