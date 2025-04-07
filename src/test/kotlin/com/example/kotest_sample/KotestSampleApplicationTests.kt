package com.example.kotest_sample

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.r2dbc.core.DatabaseClient
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KotestSampleApplicationTests(
	private val testRestTemplate: TestRestTemplate,
	private val databaseClient: DatabaseClient
): StringSpec({
	"プロダクトを作成することができる" {
		val response = testRestTemplate.postForEntity(
			"/products",
			HttpEntity(
				"{\"name\": \"pen\", \"price\": 100}",
				HttpHeaders().apply {
					add("Content-Type", "application/json")
				}
			),
			Product::class.java
		)

		response.statusCode.value() shouldBe 200
		val count = databaseClient.sql("SELECT count(*) FROM products WHERE name = 'pen' and  price = 100")
			.map { row -> row.get(0, Long::class.java) }
			.first()
			.block()
		count shouldBe 1L
	}

	"指定したIDのプロダクトを取得することができる" {
		// 事前にデータを挿入
		val testProductId = UUID.randomUUID()
		val testProductName = "water"
		val testProductPrice = 200
		databaseClient.sql("INSERT INTO products (product_id, name, price) VALUES ('$testProductId', '$testProductName', $testProductPrice)")
			.fetch()
			.rowsUpdated()
			.block()

		val response = testRestTemplate.getForEntity(
			"/products/$testProductId",
			Product::class.java
		)
		response.statusCode.value() shouldBe 200
		val body = response.body!!
		body.productId shouldBe testProductId
		body.name shouldBe testProductName
		body.price shouldBe testProductPrice

	}
})
