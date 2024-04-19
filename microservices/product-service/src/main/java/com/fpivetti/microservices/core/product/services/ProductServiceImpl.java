package com.fpivetti.microservices.core.product.services;

import com.fpivetti.api.core.product.ProductDto;
import com.fpivetti.api.core.product.ProductService;
import com.fpivetti.api.exceptions.InvalidInputException;
import com.fpivetti.api.exceptions.NotFoundException;
import com.fpivetti.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public ProductDto getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        if (productId == 13) {
            throw new NotFoundException("No product found for productId: " + productId);
        }
        return new ProductDto(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }
}
