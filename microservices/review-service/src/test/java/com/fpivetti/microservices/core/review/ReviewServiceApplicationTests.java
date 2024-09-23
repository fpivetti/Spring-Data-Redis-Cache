package com.fpivetti.microservices.core.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import com.fpivetti.api.core.review.ReviewDto;
import com.fpivetti.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTests extends PostgresTestBase {
	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 213;
	private static final int PRODUCT_ID_INVALID = -1;

	@Autowired
	private WebTestClient client;

	@Autowired
	private ReviewRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getReviewsByProductId() {
		assertEquals(0, repository.findByProductId(PRODUCT_ID_OK).size());
		postAndVerifyReview(PRODUCT_ID_OK, 0, OK);
		postAndVerifyReview(PRODUCT_ID_OK, 1, OK);
		postAndVerifyReview(PRODUCT_ID_OK, 2, OK);

		assertEquals(2, repository.findByProductId(PRODUCT_ID_OK).size());

		getAndVerifyReviewsByProductId(PRODUCT_ID_OK, OK)
				.jsonPath("$.length()").isEqualTo(2)
				.jsonPath("$[0].productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$[0].reviewId").isEqualTo(1);
	}

	@Test
	void getReviewsNotFound() {
		getAndVerifyReviewsByProductId(PRODUCT_ID_NOT_FOUND, OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getReviewsInvalidParameterNegativeValue() {
		getAndVerifyReviewsByProductId(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + PRODUCT_ID_INVALID);
	}

	@Test
	void getReviewsInvalidParameter() {
		getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getReviewsMissingParameter() {
		getAndVerifyReviewsByProductId("", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	@Test
	void createReviewInvalidParameterNegativeValue() {
		postAndVerifyReview(PRODUCT_ID_INVALID, 1, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + PRODUCT_ID_INVALID);
	}

	@Test
	void createReviewInvalidReviewIdNegativeValue() {
		postAndVerifyReview(PRODUCT_ID_OK, -1, OK);
		getAndVerifyReviewsByProductId(PRODUCT_ID_OK, OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void duplicateError() {
		int reviewId = 1;
		assertEquals(0, repository.count());
		postAndVerifyReview(PRODUCT_ID_OK, reviewId, OK)
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.reviewId").isEqualTo(reviewId);

		assertEquals(1, repository.count());

		postAndVerifyReview(PRODUCT_ID_OK, reviewId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id: 1");

		assertEquals(1, repository.count());
	}

	@Test
	void deleteReviews() {
		int reviewId = 1;
		postAndVerifyReview(PRODUCT_ID_OK, reviewId, OK);
		assertEquals(1, repository.findByProductId(PRODUCT_ID_OK).size());

		deleteAndVerifyReviewsByProductId(PRODUCT_ID_OK, OK);
		assertEquals(0, repository.findByProductId(PRODUCT_ID_OK).size());

		deleteAndVerifyReviewsByProductId(PRODUCT_ID_OK, OK);
	}

	@Test
	void deleteReviewsInvalidParameterNegativeValue() {
		deleteAndVerifyReviewsByProductId(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + PRODUCT_ID_INVALID);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
				.uri("/review" + productIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
		ReviewDto review = new ReviewDto(productId, reviewId, "Author " + reviewId, "Subject " + reviewId,
				"Content " + reviewId, "SA");
		return client.post()
				.uri("/review")
				.body(just(review), ReviewDto.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/review?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}
}
