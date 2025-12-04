package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.RatingRequest;
import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.PaginatedResponse;
import com.be.java.foxbase.dto.response.RatingResponse;
import com.be.java.foxbase.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratings")
public class RatingController {
    @Autowired
    private RatingService ratingService;

    @GetMapping("/{bookId}")
    ApiResponse<PaginatedResponse<RatingResponse>> getRatings(
            @PathVariable("bookId") Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PaginatedResponse<RatingResponse>>builder()
                .data(ratingService.getBookRatings(bookId, pageable))
                .build();
    }

    @GetMapping("/my-rating")
    ApiResponse<RatingResponse> getMyRating(
            @RequestParam Long bookId
    ) {
        return ApiResponse.<RatingResponse>builder()
                .data(ratingService.getMyRating(bookId))
                .build();
    }

    @PostMapping("/rate")
    ApiResponse<RatingResponse> createRating(@RequestBody RatingRequest ratingRequest){
        return ApiResponse.<RatingResponse>builder()
                .data(ratingService.createRating(ratingRequest))
                .build();
    }

    @GetMapping("/count")
    ApiResponse<Long> getCount(@RequestParam Long bookId){
        return ApiResponse.<Long>builder()
                .data(ratingService.countingBookRating(bookId))
                .build();
    }

}
