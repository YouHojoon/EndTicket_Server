rootProject.name = "EndTicket"
include("user")
include("auth")
include("eureka")
include("gateway")
include("common")
include("ticket")
include("common:jpa")
findProject(":common:jpa")?.name = "jpa"
