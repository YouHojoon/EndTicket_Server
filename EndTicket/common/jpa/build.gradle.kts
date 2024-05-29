plugins{
    kotlin("plugin.jpa") version "1.9.22"
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
    testRuntimeOnly("com.h2database:h2")
}
