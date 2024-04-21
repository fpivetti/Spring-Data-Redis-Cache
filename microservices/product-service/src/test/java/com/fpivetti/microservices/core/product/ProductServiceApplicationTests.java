package com.fpivetti.microservices.core.product;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fpivetti.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTests extends MongoDbTestBase{
	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 13;
	private static final int PRODUCT_ID_INVALID = -1;

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getProductById() {
		client.get()
				.uri("/product/" + PRODUCT_ID_OK)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK);
	}

	@Test
	void getProductNotFound() {
		client.get()
				.uri("/product/" + PRODUCT_ID_NOT_FOUND)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("No product found for productId: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getProductInvalidParameterNegativeValue() {
		client.get()
				.uri("/product/" + PRODUCT_ID_INVALID)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product/" + PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("Invalid productId: " + PRODUCT_ID_INVALID);
	}

	@Test
	void getProductInvalidParameterString() {
		client.get()
				.uri("/product/no-integer")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(BAD_REQUEST)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product/no-integer")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}
}
