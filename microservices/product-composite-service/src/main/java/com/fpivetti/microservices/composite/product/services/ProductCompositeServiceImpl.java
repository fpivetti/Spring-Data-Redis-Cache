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
    public ProductAggregateDto getProduct(int productId) {
        LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);
        ProductDto product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<RecommendationDto> recommendations = integration.getRecommendations(productId);
        List<ReviewDto> reviews = integration.getReviews(productId);
        LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregateDto createProductAggregate(ProductDto product, List<RecommendationDto> recommendations, List<ReviewDto> reviews, String serviceAddress) {
        // 1. Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummaryDto> recommendationSummaries = (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummaryDto(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummaryDto> reviewSummaries = (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummaryDto(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.getFirst().getServiceAddress() : "";
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.getFirst().getServiceAddress() : "";
        ServiceAddressesDto serviceAddresses = new ServiceAddressesDto(serviceAddress, productAddress, recommendationAddress, reviewAddress);

        return new ProductAggregateDto(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
