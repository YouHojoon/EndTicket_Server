dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("net.devh:grpc-client-spring-boot-starter:3.0.0.RELEASE")
    implementation(files("../grpc.jar"))
    implementation("io.grpc:grpc-netty:1.60.1")
}
