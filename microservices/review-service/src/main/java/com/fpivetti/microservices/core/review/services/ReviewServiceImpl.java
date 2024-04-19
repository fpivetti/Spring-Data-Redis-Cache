package com.fpivetti.microservices.core.review.services;

import com.fpivetti.api.core.review.ReviewDto;
import com.fpivetti.api.core.review.ReviewService;
import com.fpivetti.api.exceptions.InvalidInputException;
import com.fpivetti.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<ReviewDto> getReviews(int productId) {
        LOG.debug("/review return the found reviews for productId={}", productId);
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        if (productId == 213) {
            LOG.debug("No reviews found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<ReviewDto> list = new ArrayList<>();
        list.add(new ReviewDto(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
        list.add(new ReviewDto(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
        list.add(new ReviewDto(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));
        LOG.debug("/review response size: {}", list.size());
        return list;
    }
}
