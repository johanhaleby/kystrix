import com.beust.kobalt.plugin.java.javaCompiler
import com.beust.kobalt.plugin.kotlin.kotlinCompiler
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.autoGitTag
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.Scm

const val KYSTRIX_GROUP_NAME = "se.haleby.kystrix"
const val KYSTRIX_VERSION = "0.1.0"
const val KOTLIN_VERSION = "1.2.61"
const val HYSTRIX_VERSION = "1.5.12"

val core = project {
    name = "kystrix-core"
    group = KYSTRIX_GROUP_NAME
    artifactId = name
    version = KYSTRIX_VERSION
    directory = "core"

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-runtime:$KOTLIN_VERSION")
        compile("org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION")
        compile("com.netflix.hystrix:hystrix-core:$HYSTRIX_VERSION")
    }

    dependenciesTest {
        compile("junit:junit:4.12")
        compile("io.javalin:javalin:2.1.1")
        compile("io.rest-assured:rest-assured:3.1.1")
        compile("org.asynchttpclient:async-http-client:2.5.3")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")
        compile("org.slf4j:slf4j-simple:1.7.25")
        compile("org.assertj:assertj-core:3.11.1")
    }

    // Create maven pom so that we can sync with Maven central
    assemble {
        mavenJars {
        }
    }

    // Enable Bintray integration
    bintray {
        publish = true
        sign = true
    }

    // Automatically create a git tag when publishing a release
    autoGitTag {
        enabled = true
        annotated = true
        tag = "$version"
        message = "Released $version"
    }

    pom = Model().apply {
        name = project.name
        description = "Kystrix is a small Kotlin DSL over Hystrix "
        url = "https://kystrix.haleby.se/"
        licenses = listOf(License().apply {
            name = "Apache 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0"
        })
        scm = Scm().apply {
            url = "https://github.com/johanhaleby/kystrix"
            connection = "https://github.com/johanhaleby/kystrix.git"
            developerConnection = "git@github.com:johanhaleby/kystrix.git"
        }
        developers = listOf(Developer().apply {
            name = "Johan Haleby"
        })
    }
}

val spring = project(core) {
    name = "kystrix-spring"
    group = KYSTRIX_GROUP_NAME
    artifactId = name
    version = KYSTRIX_VERSION
    directory = "spring"

    kotlinCompiler {
        args("-jvm-target", "1.8")
    }

    dependencies {
        compile("io.projectreactor:reactor-core:3.1.9.RELEASE")
        compile("io.reactivex:rxjava-reactive-streams:1.2.1")
        compile("org.jetbrains.kotlin:kotlin-runtime:$KOTLIN_VERSION")
        compile("org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION")
        compile("com.netflix.hystrix:hystrix-core:$HYSTRIX_VERSION")
    }

    dependenciesTest {
        compile("junit:junit:4.12")
        compile("org.assertj:assertj-core:3.11.1")
        compile("io.javalin:javalin:2.1.1")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")
        compile("io.projectreactor.ipc:reactor-netty:0.7.8.RELEASE")
        compile("org.springframework:spring-webflux:5.0.8.RELEASE")
    }

    // TODO Add assembly and maven stuff
}