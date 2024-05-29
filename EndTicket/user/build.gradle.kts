dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation(files("../grpc.jar"))
    implementation(project(":common:grpc"))
    implementation(project(":common:web"))
    implementation(project(":common:security"))
    implementation(project(":common:jpa"))
    implementation(project(":common:zipkin"))
}