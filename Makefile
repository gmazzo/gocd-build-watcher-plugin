build:
	docker-compose run gradle ./gradlew --no-daemon build

shell:
	docker-compose run gradle sh
