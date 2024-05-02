package com.fpivetti.microservices.composite.product;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import com.fpivetti.api.composite.product.ProductAggregateDto;
import com.fpivetti.api.composite.product.RecommendationSummaryDto;
import com.fpivetti.api.composite.product.ReviewSummaryDto;
import com.fpivetti.api.core.product.ProductDto;
import com.fpivetti.api.core.recommendation.RecommendationDto;
import com.fpivetti.api.core.review.ReviewDto;
import com.fpivetti.api.exceptions.InvalidInputException;
import com.fpivetti.api.exceptions.NotFoundException;
import com.fpivetti.microservices.composite.product.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests extends RedisCacheTestBase {
	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = -1;

	@Autowired
	private WebTestClient client;

	@Autowired
	private CacheManager cacheManager;

	@MockBean
	private ProductCompositeIntegration compositeIntegration;

	@BeforeEach
	void setUp() {
		when(compositeIntegration.getProduct(PRODUCT_ID_OK))
				.thenReturn(new ProductDto(PRODUCT_ID_OK, "name", 1, "mock-address"));
		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(singletonList(new RecommendationDto(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
		when(compositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(singletonList(new ReviewDto(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")));

		when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

		when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));

		// Before each test we clear the cache removing all saved entities
		cacheManager.getCacheNames().forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
	}

	@Test
	void createCompositeProduct1() {
		ProductAggregateDto compositeProduct = new ProductAggregateDto(1, "name", 1, null, null, null);
		postAndVerifyProduct(compositeProduct, OK);
	}

	@Test
	void createCompositeProduct2() {
		ProductAggregateDto compositeProduct = new ProductAggregateDto(1, "name", 1,
				singletonList(new RecommendationSummaryDto(1, "a", 1, "c")),
				singletonList(new ReviewSummaryDto(1, "a", "s", "c")), null);
		postAndVerifyProduct(compositeProduct, OK);
	}

	@Test
	void deleteCompositeProduct() {
		ProductAggregateDto compositeProduct = new ProductAggregateDto(1, "name", 1,
				singletonList(new RecommendationSummaryDto(1, "a", 1, "c")),
				singletonList(new ReviewSummaryDto(1, "a", "s", "c")), null);
		postAndVerifyProduct(compositeProduct, OK);
		deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
	}

	@Test
	void getProductById() {
		getAndVerifyProduct(PRODUCT_ID_OK, OK)
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	void getProductNotFound() {
		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getProductInvalidInput() {
		getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
	}

	@Test
	void getProductByIdFromCache() {
		getAndVerifyProduct(PRODUCT_ID_OK, OK);
		// Verify that the second time we call the GET operation, it will retrieve the result from the cache
		getAndVerifyProduct(PRODUCT_ID_OK, OK);
		verify(compositeIntegration, times(1)).getProduct(PRODUCT_ID_OK);
	}

	@Test
	void deleteCompositeProductFromCache() {
		getAndVerifyProduct(PRODUCT_ID_OK, OK);
		deleteAndVerifyProduct(PRODUCT_ID_OK, OK);
		// Verify that if we call the GET operation, it will invoke the getProduct method since no entries are found in the cache
		getAndVerifyProduct(PRODUCT_ID_OK, OK);
		verify(compositeIntegration, times(2)).getProduct(PRODUCT_ID_OK);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.get()
				.uri("/product-composite/" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void postAndVerifyProduct(ProductAggregateDto compositeProduct, HttpStatus expectedStatus) {
		client.post()
				.uri("/product-composite")
				.body(just(compositeProduct), ProductAggregateDto.class)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}

	private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		client.delete()
				.uri("/product-composite/" + productId)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}
}
