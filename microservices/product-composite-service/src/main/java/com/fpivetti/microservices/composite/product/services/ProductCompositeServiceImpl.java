package com.fpivetti.microservices.composite.product.services;

import com.fpivetti.api.composite.product.*;
import com.fpivetti.api.core.product.ProductDto;
import com.fpivetti.api.core.recommendation.RecommendationDto;
import com.fpivetti.api.core.review.ReviewDto;
import com.fpivetti.api.exceptions.NotFoundException;
import com.fpivetti.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
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
    @CacheResult(cacheName = "products")
    public ProductAggregateDto getProduct(@CacheKey int productId) {
        LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);
        ProductDto productDto = integration.getProduct(productId);
        if (productDto == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<RecommendationDto> recommendations = integration.getRecommendations(productId);
        List<ReviewDto> reviews = integration.getReviews(productId);
        LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);
        return createProductAggregate(productDto, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void createProduct(ProductAggregateDto body) {
        try {
            LOG.debug("createCompositeProduct: create a new composite entity for productId: {}", body.getProductId());
            ProductDto productDto = new ProductDto(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(productDto);

            if(body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    RecommendationDto recommendationDto = new RecommendationDto(body.getProductId(),
                            r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendationDto);
                });
            }
            if(body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    ReviewDto reviewDto = new ReviewDto(body.getProductId(), r.getReviewId(), r.getAuthor(),
                            r.getSubject(), r.getContent(), null);
                    integration.createReview(reviewDto);
                });
            }
            LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    @CacheRemove(cacheName = "products")
    public void deleteProduct(@CacheKey int productId) {
        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);
        integration.deleteProduct(productId);
        integration.deleteRecommendations(productId);
        integration.deleteReviews(productId);
        LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
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
