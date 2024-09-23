package com.fpivetti.microservices.core.product;

import static org.junit.jupiter.api.Assertions.*;

import com.fpivetti.api.core.product.ProductDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import com.fpivetti.microservices.core.product.persistence.ProductEntity;
import com.fpivetti.microservices.core.product.services.ProductMapper;

class MapperTests {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);
        ProductDto api = new ProductDto(1, "n", 1, "sa");
        ProductEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getWeight(), entity.getWeight());

        ProductDto api2 = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getWeight(), api2.getWeight());
        assertNull(api2.getServiceAddress());
    }
}