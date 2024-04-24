package com.fpivetti.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpivetti.api.core.product.ProductDto;
import com.fpivetti.api.core.product.ProductService;
import com.fpivetti.api.core.recommendation.RecommendationDto;
import com.fpivetti.api.core.recommendation.RecommendationService;
import com.fpivetti.api.core.review.ReviewDto;
import com.fpivetti.api.core.review.ReviewService;
import com.fpivetti.api.exceptions.BadRequestException;
import com.fpivetti.api.exceptions.InvalidInputException;
import com.fpivetti.api.exceptions.NotFoundException;
import com.fpivetti.util.http.HttpErrorInfo;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(RestTemplate restTemplate, ObjectMapper mapper,
                                       @Value("${app.product-service.host}") String productServiceHost,
                                       @Value("${app.product-service.port}") int productServicePort,
                                       @Value("${app.recommendation-service.host}") String recommendationServiceHost,
                                       @Value("${app.recommendation-service.port}") int recommendationServicePort,
                                       @Value("${app.review-service.host}") String reviewServiceHost,
                                       @Value("${app.review-service.port}") int reviewServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    @Override
    public ProductDto getProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call the getProduct API on URL: {}", url);
            ProductDto productDto = restTemplate.getForObject(url, ProductDto.class);

            assert productDto != null;
            LOG.debug("Found a product with id: {}", productDto.getProductId());
            return productDto;

        }catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public ProductDto createProduct(ProductDto body) {
        try {
            LOG.debug("Will post a new product to URL: {}", productServiceUrl);
            ProductDto productDto = restTemplate.postForObject(productServiceUrl, body, ProductDto.class);

            assert productDto != null;
            LOG.debug("Created a product with id: {}", productDto.getProductId());
            return productDto;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call the deleteProduct API on URL: {}", url);
            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<RecommendationDto> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the getRecommendations API on URL: {}", url);
            List<RecommendationDto> recommendations = restTemplate
                    .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<RecommendationDto>>(){})
                    .getBody();

            assert recommendations != null;
            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public RecommendationDto createRecommendation(RecommendationDto body) {
        try {
            LOG.debug("Will post a new recommendation to URL: {}", recommendationServiceUrl);
            RecommendationDto recommendationDto = restTemplate.postForObject(recommendationServiceUrl, body, RecommendationDto.class);
            assert recommendationDto != null;
            LOG.debug("Created a recommendation with id: {}", recommendationDto.getProductId());
            return recommendationDto;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the deleteRecommendations API on URL: {}", url);
            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<ReviewDto> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the getReviews API on URL: {}", url);
            List<ReviewDto> reviews = restTemplate
                    .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ReviewDto>>(){})
                    .getBody();

            assert reviews != null;
            LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ReviewDto createReview(ReviewDto body) {
        try {
            LOG.debug("Will post a new review to URL: {}", reviewServiceUrl);
            ReviewDto reviewDto = restTemplate.postForObject(reviewServiceUrl, body, ReviewDto.class);
            assert reviewDto != null;
            LOG.debug("Created a review with id: {}", reviewDto.getProductId());
            return reviewDto;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the deleteReviews API on URL: {}", url);
            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value()))) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(ex));
            case BAD_REQUEST:
                return new BadRequestException(getErrorMessage(ex));
            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioException) {
            return ex.getMessage();
        }
    }
}
