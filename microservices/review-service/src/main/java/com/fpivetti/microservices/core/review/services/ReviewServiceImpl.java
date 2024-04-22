package com.fpivetti.microservices.core.review.services;

import com.fpivetti.api.core.review.ReviewDto;
import com.fpivetti.api.core.review.ReviewService;
import com.fpivetti.api.exceptions.InvalidInputException;
import com.fpivetti.microservices.core.review.persistence.ReviewEntity;
import com.fpivetti.microservices.core.review.persistence.ReviewRepository;
import com.fpivetti.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<ReviewDto> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<ReviewDto> reviewDtoList = mapper.entityListToApiList(entityList);
        reviewDtoList.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getReviews: response size: {}", reviewDtoList.size());
        return reviewDtoList;
    }

    @Override
    public ReviewDto createReview(ReviewDto body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id: " + body.getReviewId());
        }
    }

    @Override
    public void deleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
