plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.liquibase.gradle") version "2.2.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE")

	// test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	//kotest
	val kotestVersion = "5.9.1"
	testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
	testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

	// testcontainers
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")

	//liquibase
	implementation("org.liquibase:liquibase-core:4.19.0")
	implementation("org.liquibase:liquibase-groovy-dsl:3.0.2")
	liquibaseRuntime("org.postgresql:postgresql:42.3.1")
	liquibaseRuntime("org.liquibase:liquibase-core:4.19.0")
	liquibaseRuntime("org.liquibase:liquibase-groovy-dsl:3.0.2")
	liquibaseRuntime("info.picocli:picocli:4.6.1")
	testImplementation("org.postgresql:postgresql")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

liquibase {
	activities.register("main") {
		val db_url = System.getenv("DB_URL") ?: "localhost:5432/sample"
		val db_user = System.getenv("DB_USER") ?: "docker"
		val db_password = System.getenv("DB_PASS") ?: "docker"

		this.arguments = mapOf(
			"logLevel" to "info",
			"changeLogFile" to "src/main/resources/liquibase/xml/db.changelog.xml",
			"url" to "jdbc:postgresql://$db_url",
			"username" to db_user,
			"password" to db_password,
		)
	}
	runList = "main"
}