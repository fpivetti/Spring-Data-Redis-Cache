package com.fpivetti.microservices.core.recommendation.services;

import com.fpivetti.api.core.recommendation.RecommendationDto;
import com.fpivetti.api.core.recommendation.RecommendationService;
import com.fpivetti.api.exceptions.InvalidInputException;
import com.fpivetti.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<RecommendationDto> getRecommendations(int productId) {
        LOG.debug("/recommendation return the found recommendations for productId={}", productId);
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        if (productId == 113) {
            LOG.debug("No recommendations found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<RecommendationDto> list = new ArrayList<>();
        list.add(new RecommendationDto(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        list.add(new RecommendationDto(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        list.add(new RecommendationDto(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
        LOG.debug("/recommendation response size: {}", list.size());
        return list;
    }
}
