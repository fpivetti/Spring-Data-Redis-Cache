package com.fpivetti.microservices.core.recommendation.services;

import com.fpivetti.api.core.recommendation.RecommendationDto;
import com.fpivetti.api.core.recommendation.RecommendationService;
import com.fpivetti.api.exceptions.InvalidInputException;
import com.fpivetti.microservices.core.recommendation.persistence.RecommendationEntity;
import com.fpivetti.microservices.core.recommendation.persistence.RecommendationRepository;
import com.fpivetti.util.http.ServiceUtil;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationRepository repository, RecommendationMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository =repository;
        this.mapper = mapper;
    }

    @Override
    public List<RecommendationDto> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<RecommendationDto> recommendationDtoList = mapper.entityListToApiList(entityList);
        recommendationDtoList.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getRecommendations response size: {}", recommendationDtoList.size());
        return recommendationDtoList;
    }

    @Override
    public RecommendationDto createRecommendation(RecommendationDto body) {
        try {
            if (body.getProductId() < 1) {
                throw new InvalidInputException("Invalid productId: " + body.getProductId());
            }
            if (body.getRecommendationId() < 1) {
                LOG.warn("Invalid recommendationId: {}, skipping to the next entity", body.getRecommendationId());
                return new RecommendationDto();
            }
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);

            LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id: " + body.getRecommendationId());
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        LOG.debug("deleteRecommendations: tries to delete recommendations for product with id: {}", productId);
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        repository.deleteAll(repository.findByProductId(productId));
        LOG.debug("deleteRecommendations: recommendations deleted for product with id: {}", productId);
    }
}
