#!/usr/bin/env bash

BOOT_VERSION="3.2.4"
JAVA_VERSION="21"

delete_unwanted() {
	# remove useless file for multi-project build
	find "$1" -depth -name "gradle" -exec rm -rfv "{}" \;
	find "$1" -depth -name "gradlew*" -exec rm -fv "{}" \;

	find "$1" -depth -name ".mvn" -exec rm -rfv "{}" \;
	find "$1" -depth -name "mvnw*" -exec rm -fv "{}" \;

	find "$1" -depth -name "*.md" -exec rm -fv "{}" \;
}

delete_tests() {
  # remove useless file from shared libraries
  find "$1" -depth -name "test" -exec rm -rfv "{}" \;
  find "$1" -depth -name "resources" -exec rm -rfv "{}" \;
}

spring init \
--boot-version="$BOOT_VERSION" \
--type=maven-project \
--java-version="$JAVA_VERSION" \
--packaging=jar \
--name=api \
--package-name=com.fpivetti.api \
--groupId=com.fpivetti.api \
--dependencies=webflux \
--version=1.0.0-SNAPSHOT \
api

spring init \
--boot-version="$BOOT_VERSION" \
--type=maven-project \
--java-version="$JAVA_VERSION" \
--packaging=jar \
--name=util \
--package-name=com.fpivetti.util \
--groupId=com.fpivetti.util \
--dependencies=webflux \
--version=1.0.0-SNAPSHOT \
util

mkdir microservices
cd microservices || exit

spring init \
--boot-version="$BOOT_VERSION" \
--type=maven-project \
--java-version="$JAVA_VERSION" \
--packaging=jar \
--name=product-service \
--package-name=com.fpivetti.microservices.core.product \
--groupId=com.fpivetti.microservices.core.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-service

spring init \
--boot-version="$BOOT_VERSION" \
--type=maven-project \
--java-version="$JAVA_VERSION" \
--packaging=jar \
--name=review-service \
--package-name=com.fpivetti.microservices.core.review \
--groupId=com.fpivetti.microservices.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
review-service

spring init \
--boot-version="$BOOT_VERSION" \
--type=maven-project \
--java-version="$JAVA_VERSION" \
--packaging=jar \
--name=recommendation-service \
--package-name=com.fpivetti.microservices.core.recommendation \
--groupId=com.fpivetti.microservices.core.recommendation \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
recommendation-service

spring init \
--boot-version="$BOOT_VERSION" \
--type=maven-project \
--java-version="$JAVA_VERSION" \
--packaging=jar \
--name=product-composite-service \
--package-name=com.fpivetti.microservices.composite.product \
--groupId=com.fpivetti.microservices.composite.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-composite-service

cd ..
cp -r microservices/product-service/.mvn .
cp microservices/product-service/.gitignore .
cp microservices/product-service/mvnw.cmd .
cp microservices/product-service/mvnw .
delete_unwanted microservices
delete_unwanted api
delete_unwanted util
delete_tests api
delete_tests util
exit 0
