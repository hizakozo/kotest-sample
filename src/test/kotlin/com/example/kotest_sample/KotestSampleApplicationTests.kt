package com.example.kotest_sample

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.resource.ClassLoaderResourceAccessor
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager
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

@TestConfiguration
class TestConfiguration: AbstractProjectConfig() {
	override fun extensions() = listOf(SpringExtension)

	override suspend fun beforeProject() {
		// テストコンテナーの起動
		val postgresContainer = PostgreSQLContainer<Nothing>("postgres:latest")
		postgresContainer.start()
		System.setProperty("DB_USER", postgresContainer.username)
		System.setProperty("DB_PASSWORD", postgresContainer.password)
		System.setProperty("DB_NAME", postgresContainer.databaseName)
		System.setProperty("DB_PORT", postgresContainer.firstMappedPort.toString())

		val connection = DriverManager.getConnection(
			postgresContainer.jdbcUrl,
			postgresContainer.username,
			postgresContainer.password
		)

		// Liquibaseの初期化
		Liquibase(
			ClassPathResource("liquibase/xml/db.changelog.xml").path,
			ClassLoaderResourceAccessor(),
			DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
				liquibase.database.jvm.JdbcConnection(
					connection
				)
			)
		).update("")
	}
}