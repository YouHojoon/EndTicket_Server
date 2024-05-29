import com.google.protobuf.gradle.id

plugins {
    id("com.google.protobuf") version "0.9.4"
}

ext["grpcStarterVersion"] = "2.15.0.RELEASE"
ext["grpcVersion"] = "3.25.3"
ext["protobufVersion"] = "1.62.2"
ext["protobufKotlinVersion"] = "1.4.1"

dependencies{
    api("io.grpc:grpc-kotlin-stub:${property("protobufKotlinVersion")}")
    api("io.grpc:grpc-protobuf:${property("protobufVersion")}")
    api("com.google.protobuf:protobuf-kotlin:${property("grpcVersion")}")
    api ("net.devh:grpc-spring-boot-starter:${property("grpcStarterVersion")}")
    testImplementation("io.grpc:grpc-testing:${property("protobufVersion")}")
    testImplementation("io.grpc:grpc-inprocess:${property("protobufVersion")}")
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
