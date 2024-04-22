dependencies {
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation(files("../grpc.jar"))
    implementation(project(":common"))
    // https://mvnrepository.com/artifact/net.devh/grpc-server-spring-boot-starter
    implementation("net.devh:grpc-server-spring-boot-starter:${property("grpcStarterVersion")}")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("io.grpc:grpc-testing:1.62.2")
    testImplementation("io.grpc:grpc-inprocess:1.62.2")



}