package com.fpivetti.microservices.core.recommendation;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fpivetti.api.core.recommendation.RecommendationDto;
import com.fpivetti.microservices.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoDbTestBase {
	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 113;
	private static final int PRODUCT_ID_INVALID = -1;

	@Autowired
	private WebTestClient client;

	@Autowired
	private RecommendationRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getRecommendationsByProductId() {
		assertEquals(0, repository.findByProductId(PRODUCT_ID_OK).size());
		postAndVerifyRecommendation(PRODUCT_ID_OK, 1, OK);
		postAndVerifyRecommendation(PRODUCT_ID_OK, 2, OK);
		postAndVerifyRecommendation(PRODUCT_ID_OK, 3, OK);

		assertEquals(3, repository.findByProductId(PRODUCT_ID_OK).size());

		getAndVerifyRecommendationsByProductId(PRODUCT_ID_OK, OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[0].productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$[0].recommendationId").isEqualTo(1);
	}

	@Test
	void getRecommendationsNotFound() {
		getAndVerifyRecommendationsByProductId(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
				.jsonPath("$.message").isEqualTo("No recommendations found for productId: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getRecommendationsInvalidParameterNegativeValue() {
		getAndVerifyRecommendationsByProductId(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + PRODUCT_ID_INVALID);
	}

	@Test
	void getRecommendationsInvalidParameter() {
		getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getRecommendationsMissingParameter() {
		getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	@Test
	void duplicateError() {
		int recommendationId = 1;
		assertEquals(0, repository.count());
		postAndVerifyRecommendation(PRODUCT_ID_OK, recommendationId, OK)
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendationId").isEqualTo(recommendationId);

		assertEquals(1, repository.count());

		postAndVerifyRecommendation(PRODUCT_ID_OK, recommendationId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id: 1");

		assertEquals(1, repository.count());
	}

	@Test
	void deleteRecommendations() {
		int recommendationId = 1;
		postAndVerifyRecommendation(PRODUCT_ID_OK, recommendationId, OK);
		assertEquals(1, repository.findByProductId(PRODUCT_ID_OK).size());

		deleteAndVerifyRecommendationsByProductId(PRODUCT_ID_OK, OK);
		assertEquals(0, repository.findByProductId(PRODUCT_ID_OK).size());

		deleteAndVerifyRecommendationsByProductId(PRODUCT_ID_OK, OK);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
				.uri("/recommendation" + productIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
		RecommendationDto recommendationDto = new RecommendationDto(productId, recommendationId, "Author " + recommendationId,
				recommendationId, "Content " + recommendationId, "SA");
		return client.post()
				.uri("/recommendation")
				.body(just(recommendationDto), RecommendationDto.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/recommendation?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}
}
