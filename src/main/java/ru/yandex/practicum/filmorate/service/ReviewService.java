package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final EventService eventService;

    public ReviewDto create(ReviewDto reviewDto) {
        validateUserExists(reviewDto.getUserId());
        validateFilmExists(reviewDto.getFilmId());

        Review review = ReviewMapper.toReview(reviewDto);
        Review createdReview = reviewRepository.create(review);
        eventService.record(createdReview.getUserId(), EventType.REVIEW, Operation.ADD, createdReview.getReviewId());
        return ReviewMapper.toReviewDto(createdReview);
    }

    public ReviewDto update(ReviewDto reviewDto) {
        validateReviewId(reviewDto.getReviewId());
        validateReviewExists(reviewDto.getReviewId());

        boolean updated = reviewRepository.update(ReviewMapper.toReview(reviewDto));

        if (!updated) {
            throw new NotFoundException("Отзыв с ID " + reviewDto.getReviewId() + " не найден");
        }

        Review review = reviewRepository.getById(reviewDto.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + reviewDto.getReviewId() + " не найден"));

        eventService.record(review.getUserId(), EventType.REVIEW, Operation.UPDATE, review.getReviewId());
        return ReviewMapper.toReviewDto(review);
    }

    public void deleteById(Long reviewId) {
        Review review = reviewRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + reviewId + " не найден"));

        boolean deleted = reviewRepository.deleteById(reviewId);

        if (!deleted) {
            throw new NotFoundException("Отзыв с ID " + reviewId + " не найден");
        }

        eventService.record(review.getUserId(), EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public ReviewDto getById(Long reviewId) {
        return reviewRepository.getById(reviewId)
                .map(ReviewMapper::toReviewDto)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + reviewId + " не найден"));
    }

    public List<ReviewDto> getReviews(Long filmId, Integer count) {
        if (count == null || count <= 0) {
            throw new ValidationException("Количество отзывов должно быть положительным");
        }
        if (filmId != null) {
            validateFilmExists(filmId);
        }
        return reviewRepository.getReviews(filmId, count)
                .stream()
                .map(ReviewMapper::toReviewDto)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
        reviewRepository.addReaction(reviewId, userId, true);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
        reviewRepository.addReaction(reviewId, userId, false);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
        reviewRepository.deleteReaction(reviewId, userId, true);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
        reviewRepository.deleteReaction(reviewId, userId, false);
    }

    private void validateReviewId(Long reviewId) {
        if (reviewId == null) {
            throw new ValidationException("ID отзыва не может быть пустым");
        }
    }

    private void validateReviewExists(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new NotFoundException("Отзыв с ID " + reviewId + " не найден");
        }
    }

    private void validateUserExists(Long userId) {
        userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateFilmExists(Long filmId) {
        Film film = filmRepository.getById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }
}
