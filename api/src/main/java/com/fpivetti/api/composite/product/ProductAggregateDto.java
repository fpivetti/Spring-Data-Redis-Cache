package com.fpivetti.api.composite.product;

import java.util.List;

public class ProductAggregateDto {
    private final int productId;
    private final String name;
    private final int weight;
    private final List<RecommendationSummaryDto> recommendations;
    private final List<ReviewSummaryDto> reviews;
    private final ServiceAddressesDto serviceAddresses;

    public ProductAggregateDto() {
        productId = 0;
        name = null;
        weight = 0;
        recommendations = null;
        reviews = null;
        serviceAddresses = null;
    }

    public ProductAggregateDto(int productId, String name, int weight, List<RecommendationSummaryDto> recommendations, List<ReviewSummaryDto> reviews, ServiceAddressesDto serviceAddresses) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.recommendations = recommendations;
        this.reviews = reviews;
        this.serviceAddresses = serviceAddresses;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public List<RecommendationSummaryDto> getRecommendations() {
        return recommendations;
    }

    public List<ReviewSummaryDto> getReviews() {
        return reviews;
    }

    public ServiceAddressesDto getServiceAddresses() {
        return serviceAddresses;
    }
}
