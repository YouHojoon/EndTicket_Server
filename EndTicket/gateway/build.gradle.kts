dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation(project(":common:security"))
    implementation(project(":common:zipkin"))
    implementation(project(":common:grpc")){
        exclude("io.grpc", "grpc-netty-shaded")
        exclude("net.devh", "grpc-server-spring-boot-starter")
    }
    implementation(files("../grpc.jar"))
    implementation("io.grpc:grpc-netty:1.58.0")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")
}
