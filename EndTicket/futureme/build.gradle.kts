dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation(project(":common:kafka"))
    implementation(project(":common:web"))
    implementation(project(":common:security"))
    implementation(project(":common:jpa"))
    implementation(project(":common:zipkin"))
    testRuntimeOnly("com.h2database:h2")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test{
    finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport{
    reports{
        html.required = true
    }
    finalizedBy("jacocoTestCoverageVerification")

    classDirectories.setFrom(
        files(classDirectories.files.map {
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
        })
    )
}


tasks.jacocoTestCoverageVerification {
    enabled = true

    violationRules {
        rule {
            excludes = listOf(
                "**.infra.**",
                "*Application*",
                "**.domain.*",
                "**.response.*",
                "**.request.*",
                "**.config.**",
            )

            element = "CLASS"

            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = 0.90.toBigDecimal()
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