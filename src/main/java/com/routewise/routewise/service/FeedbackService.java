package com.routewise.routewise.service;

import com.routewise.routewise.entity.Feedback;
import com.routewise.routewise.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback createFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id).orElse(null);
    }

    public Feedback updateFeedback(Long id, Feedback feedback) {
        Feedback existingFeedback =
                feedbackRepository.findById(id).orElse(null);

        if (existingFeedback != null) {
            existingFeedback.setRating(feedback.getRating());
            existingFeedback.setComment(feedback.getComment());
            existingFeedback.setCreatedAt(feedback.getCreatedAt());
            existingFeedback.setUser(feedback.getUser());
            existingFeedback.setTrip(feedback.getTrip());

            return feedbackRepository.save(existingFeedback);
        }

        return null;
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}
