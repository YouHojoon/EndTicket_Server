dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation(project(":common:kafka"))
    implementation(project(":common:web"))
    implementation(project(":common:security"))
    implementation(project(":common:jpa"))
    implementation(project(":common:redis"))
    implementation(project(":common:zipkin"))
    testRuntimeOnly("com.h2database:h2")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
