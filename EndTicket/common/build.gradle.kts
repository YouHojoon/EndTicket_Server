plugins{
    id("java")
}

dependencies{
    implementation("org.springframework.boot:spring-boot-starter-test")
}

subprojects{
    apply(plugin = "java")

    dependencies{
        api(project(":common"))
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")
    }
}



