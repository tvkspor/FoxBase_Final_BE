package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.BookCreationRequest;
import com.be.java.foxbase.dto.request.BookFilterRequest;
import com.be.java.foxbase.dto.response.*;
import com.be.java.foxbase.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;                 // <-- thêm
import org.springframework.web.multipart.MultipartFile;  // <-- thêm
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @PostMapping("/publish")
    ApiResponse<BookResponse> publish(@RequestBody BookCreationRequest request){
        return ApiResponse.<BookResponse>builder()
                .data(bookService.publish(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBookById(@PathVariable Long id) {
        BookResponse book = bookService.getBookById(id);
        return ApiResponse.<BookResponse>builder()
                .statusCode(0)
                .message("Success")
                .data(book)
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Boolean> delete(@PathVariable Long id){
        return ApiResponse.<Boolean>builder()
                .data(bookService.removeWork(id))
                .build();
    }

    @PostMapping("/filter")
    ApiResponse<PaginatedResponse<BookResponse>> getByFilter(
            @RequestBody BookFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .data(bookService.getBooksByFilterList(request.getFilters(), request.getKeyword(), pageable))
                .build();
    }

    @GetMapping("/collection")
    ApiResponse<List<BookResponse>> getMyCollection(){
        return ApiResponse.<List<BookResponse>>builder()
                .data(bookService.getMyBooks())
                .build();
    }

    @GetMapping("/favorites")
    ApiResponse<List<BookResponse>> getMyFavorites(){
        return ApiResponse.<List<BookResponse>>builder()
                .data(bookService.getFavoriteBooks())
                .build();
    }

    @PostMapping("/favorites/toggle/{bookId}")
    ApiResponse<ToggleFavoriteResponse> toggleFavorite(
            @PathVariable Long bookId
    ){
        return ApiResponse.<ToggleFavoriteResponse>builder()
                .data(bookService.toggleAddToFavoriteBooks(bookId))
                .build();
    }

    @GetMapping("/favorites/check")
    ApiResponse<InFavoriteResponse> check(
            @RequestParam Long bookId
    ){
        return ApiResponse.<InFavoriteResponse>builder()
                .data(bookService.checkInFavorite(bookId))
                .build();
    }

    @GetMapping("/purchased")
    ApiResponse<List<Long>> getPurchasedBookIds(){
        return ApiResponse.<List<Long>>builder()
                .data(bookService.getPurchasedBookIds())
                .build();
    }

    @GetMapping("/free6")
    ApiResponse<List<BookResponse>> getFree6Books(){
        return ApiResponse.<List<BookResponse>>builder()
                .data(bookService.get6FreeBooks())
                .build();
    }

    @GetMapping("/cost6")
    ApiResponse<List<BookResponse>> getCost6Books(){
        return ApiResponse.<List<BookResponse>>builder()
                .data(bookService.get6CostBooks())
                .build();
    }

    @GetMapping("/comm6")
    ApiResponse<List<BookResponse>> getComm6Books(){
        return ApiResponse.<List<BookResponse>>builder()
                .data(bookService.get6CommunityBooks())
                .build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BookResponse> uploadBook(
            @RequestPart("meta") String metaJson,
            @RequestPart(value = "pdf", required = false) MultipartFile pdfFile,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BookCreationRequest meta = mapper.readValue(metaJson, BookCreationRequest.class);

        return ApiResponse.<BookResponse>builder()
                .data(bookService.uploadAndPublish(meta, pdfFile, coverFile))
                .build();
    }
}