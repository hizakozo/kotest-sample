package com.example.kotest_sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotestSampleApplication

fun main(args: Array<String>) {
	runApplication<KotestSampleApplication>(*args)
}
