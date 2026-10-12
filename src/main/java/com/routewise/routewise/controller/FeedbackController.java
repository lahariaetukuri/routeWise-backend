package com.routewise.routewise.controller;

import com.routewise.routewise.entity.Feedback;
import com.routewise.routewise.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(
            @RequestBody Feedback feedback) {
        return ResponseEntity.ok(
                feedbackService.createFeedback(feedback)
        );
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(
                feedbackService.getAllFeedback()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(
            @PathVariable Long id) {

        Feedback feedback = feedbackService.getFeedbackById(id);

        if (feedback == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(feedback);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feedback> updateFeedback(
            @PathVariable Long id,
            @RequestBody Feedback feedback) {

        Feedback updatedFeedback =
                feedbackService.updateFeedback(id, feedback);

        if (updatedFeedback == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedFeedback);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(
            @PathVariable Long id) {

        feedbackService.deleteFeedback(id);

        return ResponseEntity.noContent().build();
    }
}
