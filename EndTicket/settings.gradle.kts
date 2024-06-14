rootProject.name = "EndTicket"
include("user")
include("auth")
include("eureka")
include("gateway")
include("common")
include("ticket")
include("common:jpa")
findProject(":common:jpa")?.name = "jpa"
include("common:redis")
findProject(":common:redis")?.name = "redis"
include("common:web")
findProject(":common:web")?.name = "web"
include("common:security")
findProject(":common:security")?.name = "security"
include("common:grpc")
findProject(":common:grpc")?.name = "grpc"
include("common:kafka")
findProject(":common:kafka")?.name = "kafka"
include("common:zipkin")
findProject(":common:zipkin")?.name = "zipkin"
include("futureMe")
include("common:webflux")
findProject(":common:webflux")?.name = "webflux"
include("config")
