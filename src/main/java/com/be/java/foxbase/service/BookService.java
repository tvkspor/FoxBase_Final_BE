package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.FavoriteBook;
import com.be.java.foxbase.db.entity.PublishedBook;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookId;
import com.be.java.foxbase.dto.request.BookCreationRequest;
import com.be.java.foxbase.dto.response.BookResponse;
import com.be.java.foxbase.dto.response.InFavoriteResponse;
import com.be.java.foxbase.dto.response.PaginatedResponse;
import com.be.java.foxbase.dto.response.ToggleFavoriteResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.mapper.BookMapper;
import com.be.java.foxbase.repository.*;
import com.be.java.foxbase.utils.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map; // <-- để dùng Map<String, Object>
import java.io.IOException; // <-- để catch IOException
import org.springframework.web.multipart.MultipartFile; // nếu chưa có
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    BookRepository bookRepository;

    @Autowired
    PublishedBookRepository publishedBookRepository;

    @Autowired
    FavoriteBookRepository favoriteBookRepository;

    @Autowired
    PurchasedBookRepository purchasedBookRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookMapper bookMapper;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void validateBookFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        // PDF, EPUB, MOBI are allowed
        List<String> allowedExtensions = List.of("pdf", "epub", "mobi");
        List<String> allowedMimeTypes = List.of(
                "application/pdf",
                "application/epub+zip",
                "application/x-mobipocket-ebook"
        );

        // Get file extension
        String extension = "";
        if (filename != null && filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }

        boolean isExtensionValid = allowedExtensions.contains(extension);
        boolean isMimeValid = allowedMimeTypes.contains(contentType);

        if (!isExtensionValid || !isMimeValid) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    public BookResponse publish(BookCreationRequest request){
        var book = bookMapper.toBook(request);

        User publisher = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        var publishedBook = new PublishedBook(new UserBookId(getCurrentUsername(), book.getBookId()), publisher, book);


        bookRepository.save(book);
        publishedBookRepository.save(publishedBook);

        return bookMapper.toBookResponse(book);
    }

    public Boolean removeWork(Long bookId){
        try {
            publishedBookRepository.deleteById(new UserBookId(getCurrentUsername(), bookId));
            bookRepository.deleteById(bookId);
            return true;
        } catch (AppException e) {
            return false;
        }
    }

    public BookResponse getBookById(Long id){
        var book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
        var response = bookMapper.toBookResponse(book);
        var avgRating = ratingRepository.findBookAverageRating(book.getBookId());
        response.setAverageRating(avgRating != null ? avgRating : 0);
        book.setAverageRating(avgRating);
        bookRepository.save(book);
        return response;
    }

    private PaginatedResponse<BookResponse> buildPaginatedBookResponse(Page<Book> books) {
        var bookResponses = books.map(bookMapper::toBookResponse);
        return toPaginatedResponse(books, bookResponses);
    }

    private PaginatedResponse<BookResponse> buildPaginatedBookResponseWithRating(Page<Book> books) {
        var bookResponses = books.map(book -> {
            var response = bookMapper.toBookResponse(book);
            var avgRating = ratingRepository.findBookAverageRating(book.getBookId());
            response.setAverageRating(avgRating);
            book.setAverageRating(avgRating);
            bookRepository.save(book);
            return response;
        });
        return toPaginatedResponse(books, bookResponses);
    }

    private <T> PaginatedResponse<T> toPaginatedResponse(Page<?> sourcePage, Page<T> mappedPage) {
        return PaginatedResponse.<T>builder()
                .content(mappedPage.toList())
                .totalPages(sourcePage.getTotalPages())
                .totalElements(sourcePage.getTotalElements())
                .size(sourcePage.getSize())
                .page(sourcePage.getNumber())
                .build();
    }

    public List<BookResponse> getMyBooks() {
        var books = publishedBookRepository.findByUser_Username(getCurrentUsername());
        return books.stream().map(item -> bookMapper.toBookResponse(item.getBook())).collect(Collectors.toList());
    }

    public List<BookResponse> getFavoriteBooks() {
        var books = favoriteBookRepository.findByUser_Username(getCurrentUsername());
        return books.stream().map(item -> bookMapper.toBookResponse(item.getBook())).collect(Collectors.toList());
    }

    public InFavoriteResponse checkInFavorite(Long bookId) {
        FavoriteBook favoriteBook = favoriteBookRepository.findById(new UserBookId(getCurrentUsername(), bookId)).orElse(null);
        return InFavoriteResponse.builder()
                .isAdded(favoriteBook != null)
                .build();
    }

    public ToggleFavoriteResponse toggleAddToFavoriteBooks(Long bookId) {
        FavoriteBook favoriteBook = favoriteBookRepository.findById(new UserBookId(getCurrentUsername(), bookId)).orElse(null);

        if (favoriteBook != null) {
            favoriteBookRepository.delete(favoriteBook);
            return ToggleFavoriteResponse.builder()
                    .username(getCurrentUsername())
                    .bookId(bookId)
                    .isAdded(false)
                    .message("Item has been removed from your favorite collection")
                    .build();
        }

        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        FavoriteBook favBook = new FavoriteBook(new UserBookId(getCurrentUsername(), bookId), user, book);
        favoriteBookRepository.save(favBook);

        return ToggleFavoriteResponse.builder()
                .username(getCurrentUsername())
                .bookId(bookId)
                .isAdded(true)
                .message("Item has been added to your favorite collection")
                .build();
    }

    public List<Long> getPurchasedBookIds() {
        var books = purchasedBookRepository.findByUser_Username(getCurrentUsername());
        var bookResponses = books.stream().map(item -> item.getId().getBookId());
        return bookResponses.toList();
    }

    public List<BookResponse> get6FreeBooks(){
        var books = bookRepository.findTop6ByPriceEquals(0L);
        return books.stream().map(bookMapper::toBookResponse).toList();
    }

    public List<BookResponse> get6CostBooks(){
        var books = bookRepository.findTop6ByPriceGreaterThan(0L);
        return books.stream().map(bookMapper::toBookResponse).toList();
    }

    public List<BookResponse> get6CommunityBooks(){
        var books = publishedBookRepository.findTop6By();
        var actualBooks = books.stream().map(PublishedBook::getBook).toList();
        return actualBooks.stream().map(bookMapper::toBookResponse).toList();
    }

    private List<Book> getFreeBooks(List<Book> input){
        return input.stream().filter(book -> book.getPrice() == 0).collect(Collectors.toList());
    }

    private List<Book> getCostBooks(List<Book> input){
        return input.stream().filter(book -> book.getPrice() > 0).collect(Collectors.toList());
    }

    private List<Book> getByTitleContaining(List<Book> input, String title){
        if (title == null) return input;
        return input.stream().filter(book ->
                book.getTitle().toLowerCase()
                        .contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<Book> getByAuthorContaining(List<Book> input, String author){
        if (author == null) return input;
        return input.stream().filter(book ->
                book.getAuthor().toLowerCase()
                        .contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<Book> getByGenreContaining(List<Book> input, String genre){
        if (genre == null) return input;
        return input.stream().filter(book ->
                book.getGenre().toLowerCase()
                        .contains(genre.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<Book> applyFilter(List<Book> books, Filter filter, String opt) {
        return switch (filter) {
            case TITLE -> getByTitleContaining(books, opt);
            case AUTHOR -> getByAuthorContaining(books, opt);
            case GENRE -> getByGenreContaining(books, opt);
            case FREE -> getFreeBooks(books);
            case COST -> getCostBooks(books);
            default -> books;
        };
    }

    public Page<Book> convertListToPage(List<Book> books, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), books.size());

        List<Book> subList = books.subList(start, end);
        return new PageImpl<>(subList, pageable, books.size());
    }

    public PaginatedResponse<BookResponse> getBooksByFilterList(Filter[] filters, String input, Pageable pageable) {
        boolean outsourceApplied = Arrays.stream(filters).anyMatch(filter -> filter == Filter.OUTSOURCE);
        filters = Arrays.stream(filters).filter(Objects::nonNull).toArray(Filter[]::new);

        if (outsourceApplied) {
            var books = publishedBookRepository
                    .findAll().stream()
                    .map(PublishedBook::getBook)
                    .collect(Collectors.toList());
            for (Filter filter : filters) {
                books = applyFilter(books, filter, input);
            }
            var responses = books.stream().map(bookMapper::toBookResponse).toList();
            return buildPaginatedBookResponse(convertListToPage(books, pageable));
        } else {
            var books = bookRepository.findAll();
            for (Filter filter : filters) {
                books = applyFilter(books, filter, input);
            }
            var responses = books.stream().map(bookMapper::toBookResponse).toList();
            return buildPaginatedBookResponse(convertListToPage(books, pageable));
        }
    }


  public BookResponse uploadAndPublish(BookCreationRequest meta, MultipartFile pdf, MultipartFile cover) {
    try {
      // Check null
      if (pdf == null || pdf.isEmpty()) {
          throw new AppException(ErrorCode.MISSING_FILE_PDF);
      }
      if (cover == null) {
          throw new AppException(ErrorCode.MISSING_FILE_COVER);
      }

      // Check file type
      validateBookFileType(pdf);

      // Check empty
      if (pdf.isEmpty()) {
          throw new AppException(ErrorCode.MISSING_FILE_PDF);
      }
      if (cover.isEmpty()) {
          throw new AppException(ErrorCode.MISSING_FILE_COVER);
      }

      Map<String, Object> pdfRes = cloudinaryService.uploadPdf(pdf);
      String pdfUrl = (String) pdfRes.get("secure_url");

      Map<String, Object> coverRes = cloudinaryService.uploadImage(cover);
      String coverUrl = (String) coverRes.get("secure_url");

      meta.setContentUrl(pdfUrl);
      meta.setImageUrl(coverUrl);

      return publish(meta);
    } catch (IOException e) {
      throw new RuntimeException("Upload failed: " + e.getMessage(), e);
    }
  }
}