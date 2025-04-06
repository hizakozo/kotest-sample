package com.example.kotest_sample

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.*
import java.util.*


@Configuration
class Router {
    @Bean
    fun healthCheckRoutes(controller: ProductController) = coRouter {
        GET("/products/{id}", controller::getProduct)
        POST("/products", controller::createProduct)
    }
}

@Controller
class ProductController(private val service: ProductService) {
    data class CreateProductRequest(val name: String, val price: Int)

    suspend fun createProduct(request: ServerRequest): ServerResponse =
        request.awaitBody<CreateProductRequest>().let {
            service.createProduct(it.name, it.price)
        }.let { ServerResponse.ok().bodyValueAndAwait(it) }

    suspend fun getProduct(request: ServerRequest): ServerResponse =
        service.getProduct(
            id = UUID.fromString(request.pathVariable("id"))
        ).let { ServerResponse.ok().bodyValueAndAwait(it) }
}

@Service
class ProductService(private val repository: ProductRepository) {
    suspend fun createProduct(
        name: String, price: Int
    ): Product = repository
        .save(Product(name = name, price = price))
        .awaitSingle()

    suspend fun getProduct(id: UUID): Product =
        repository
            .findById(id)
            .awaitSingle()
}

@Table("products")
data class Product(
    @Id
    @Column("product_id")
    val productId: UUID? = null,
    val name: String,
    val price: Int
)
interface ProductRepository : ReactiveCrudRepository<Product, UUID>