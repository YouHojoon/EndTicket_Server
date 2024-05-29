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
    }
}



