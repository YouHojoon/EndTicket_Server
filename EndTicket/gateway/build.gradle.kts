dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("net.devh:grpc-client-spring-boot-starter:${property("grpcStarterVersion")}"){
        exclude("io.grpc", "grpc-netty-shaded")
    }
    implementation(files("../grpc.jar"))
    implementation("io.grpc:grpc-netty:1.58.0")
    implementation(project(":common"))

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.4.0")
}
