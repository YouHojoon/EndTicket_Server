dependencies {
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")
    api(project(":common:web")){
        exclude("org.springframework.boot","spring-boot-starter-web")
        exclude("org.springdoc","springdoc-openapi-starter-webmvc-ui")
    }
}
