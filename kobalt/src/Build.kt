import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.project

val p = project {
    name = "kystrix-core"
    group = "se.haleby.kystrix"
    artifactId = name
    version = "0.1.0"
    val kotlinVersion = "1.2.61"

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-runtime:$kotlinVersion")
        compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        compile("com.netflix.hystrix:hystrix-core:1.5.12")
    }

    dependenciesTest {
        compile("junit:junit:4.12")
        compile("io.rest-assured:rest-assured:3.1.1")
        compile("org.assertj:assertj-core:3.11.1")
        compile("org.asynchttpclient:async-http-client:2.5.3")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")
        compile("io.javalin:javalin:2.1.1")
        compile("org.slf4j:slf4j-simple:1.7.25")
    }

    assemble {
        jar {
        }
    }
}
