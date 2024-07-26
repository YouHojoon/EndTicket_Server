ext["JWT_VERSION"] = "0.12.5"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    // jwt
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
    implementation("io.jsonwebtoken:jjwt-api:${property("JWT_VERSION")}")
    implementation("io.jsonwebtoken:jjwt-impl:${property("JWT_VERSION")}")
    implementation("io.jsonwebtoken:jjwt-jackson:${property("JWT_VERSION")}")

    implementation(files("../grpc.jar"))
    implementation(project(":common:web"))
    implementation(project(":common:redis"))
    implementation(project(":common:security"))
    implementation(project(":common:grpc"))
    implementation(project(":common:zipkin"))
    implementation(project(":common:kafka"))

    testImplementation("org.springframework.security:spring-security-test")
}

tasks.test {
    finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport {
    reports {
        html.required = true
    }
    finalizedBy("jacocoTestCoverageVerification")

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/infra/*",
                        "**/*Application*",
                        "**/domain/*",
                        "**/response/*",
                        "**/request/*",
                        "**/config/*",
                    )
                }
            },
        ),
    )
}

tasks.jacocoTestCoverageVerification {
    enabled = true

    violationRules {
        rule {
            excludes =
                listOf(
                    "**.infra.**",
                    "*Application*",
                    "**.domain.*",
                    "**.response.*",
                    "**.request.*",
                    "**.config.*",
                )

            element = "CLASS"

            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = 0.80.toBigDecimal()
            }

            // 라인 커버리지를 최소한 80% 만족시켜야 합니다.
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = 0.80.toBigDecimal()
            }

            // 빈 줄을 제외한 코드의 라인수를 최대 200라인으로 제한합니다.
            limit {
                counter = "LINE"
                value = "TOTALCOUNT"
                maximum = 200.toBigDecimal()
            }
        }
    }
}
