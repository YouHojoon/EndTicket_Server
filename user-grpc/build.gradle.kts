import com.google.protobuf.gradle.id

ext["grpcVersion"] = "3.25.3"
ext["protobufVersion"] = "1.62.2"
ext["protobufKotlinVersion"] = "1.4.1"

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.google.protobuf") version "0.9.4"
}

group = "ac.kr.smu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-kotlin-stub:${property("protobufKotlinVersion")}")
    implementation("io.grpc:grpc-protobuf:${property("protobufVersion")}")
    implementation("com.google.protobuf:protobuf-kotlin:${property("grpcVersion")}")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

sourceSets{
    getByName("main"){
        java{
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/kotlin"
            )
        }
    }
}

protobuf{
    protoc{
        artifact = "com.google.protobuf:protoc:${property("grpcVersion")}"
    }
    plugins{
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${property("protobufVersion")}"
        }
        id("grpckt"){
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${property("protobufKotlinVersion")}:jdk8@jar"
        }
    }
    generateProtoTasks{
        all().forEach{
            it.plugins{
                id("grpc")
                id("grpckt")
            }
            it.builtins { id("kotlin") }
        }
    }
}