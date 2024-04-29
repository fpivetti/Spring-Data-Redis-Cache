package com.fpivetti.microservices.composite.product.services;

import com.fpivetti.api.composite.product.*;
import com.fpivetti.api.core.product.ProductDto;
import com.fpivetti.api.core.recommendation.RecommendationDto;
import com.fpivetti.api.core.review.ReviewDto;
import com.fpivetti.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    @Cacheable(cacheNames = "products", key = "#productId")
    public ProductAggregateDto getProduct(int productId) {
        try {
            LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);
            ProductDto productDto = integration.getProduct(productId);
            List<RecommendationDto> recommendations = integration.getRecommendations(productId);
            List<ReviewDto> reviews = integration.getReviews(productId);
            LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);
            return createProductAggregate(productDto, recommendations, reviews, serviceUtil.getServiceAddress());
        } catch (Exception e) {
            LOG.warn("getCompositeProduct failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void createProduct(ProductAggregateDto body) {
        try {
            LOG.debug("createCompositeProduct: create a new composite entity for productId: {}", body.getProductId());
            ProductDto productDto = new ProductDto(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(productDto);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    RecommendationDto recommendationDto = new RecommendationDto(body.getProductId(),
                            r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendationDto);
                });
            }
            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    ReviewDto reviewDto = new ReviewDto(body.getProductId(), r.getReviewId(), r.getAuthor(),
                            r.getSubject(), r.getContent(), null);
                    integration.createReview(reviewDto);
                });
            }
            LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

        } catch (Exception e) {
            LOG.warn("createCompositeProduct failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @CacheEvict(cacheNames = "products", key = "#productId")
    public void deleteProduct(int productId) {
        try {
            LOG.debug("deleteCompositeProduct: deletes a product aggregate for productId: {}", productId);
            integration.deleteProduct(productId);
            integration.deleteRecommendations(productId);
            integration.deleteReviews(productId);
            LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
        } catch (Exception e) {
            LOG.warn("deleteCompositeProduct failed: {}", e.getMessage());
            LOG.debug("deleteCompositeProduct: deletes any recommendations or reviews still left and then throws the exception");
            integration.deleteRecommendations(productId);
            integration.deleteReviews(productId);
            throw e;
        }
    }

    private ProductAggregateDto createProductAggregate(ProductDto productDto, List<RecommendationDto> recommendations, List<ReviewDto> reviews, String serviceAddress) {
        // 1. Setup product info
        int productId = productDto.getProductId();
        String name = productDto.getName();
        int weight = productDto.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummaryDto> recommendationSummaries = (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummaryDto(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummaryDto> reviewSummaries = (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummaryDto(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = productDto.getServiceAddress();
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.getFirst().getServiceAddress() : "";
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.getFirst().getServiceAddress() : "";
        ServiceAddressesDto serviceAddresses = new ServiceAddressesDto(serviceAddress, productAddress, recommendationAddress, reviewAddress);

        return new ProductAggregateDto(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
