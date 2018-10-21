import com.beust.kobalt.api.Project
import com.beust.kobalt.plugin.kotlin.kotlinCompiler
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.autoGitTag
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.Scm

object Versions {
    // Java
    const val java = "1.8"

    // Kystrix
    const val kystrix = "0.1.5-SNAPSHOT"

    // Compile versions
    const val kotlin = "1.2.70"
    const val hystrix = "1.5.12"
    const val reactorCore = "3.1.9.RELEASE"
    const val reactiveStreams = "1.2.1"

    // Test versions
    const val assertj = "3.11.1"
    const val slf4j = "1.7.25"
    const val jackson = "2.9.6"
    const val restAssured = "3.1.1"
    const val asyncHttpClient = "2.5.3"
    const val javalin = "2.1.1"
    const val junit = "4.12"
    const val reactorNetty = "0.7.8.RELEASE"
    const val webflux = "5.0.8.RELEASE"
}

const val KYSTRIX_GROUP_NAME = "se.haleby.kystrix"

val core = project {
    name = "kystrix-core"
    group = KYSTRIX_GROUP_NAME
    artifactId = name
    version = Versions.kystrix
    directory = "core"

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-runtime:${Versions.kotlin}")
        compile("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
        compile("com.netflix.hystrix:hystrix-core:${Versions.hystrix}")
    }

    dependenciesTest {
        compile("junit:junit:${Versions.junit}")
        compile("io.javalin:javalin:${Versions.javalin}")
        compile("io.rest-assured:rest-assured:${Versions.restAssured}")
        compile("org.asynchttpclient:async-http-client:${Versions.asyncHttpClient}")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
        compile("org.slf4j:slf4j-simple:${Versions.slf4j}")
        compile("org.assertj:assertj-core:${Versions.assertj}")
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
        enabled = false
        annotated = true
        tag = "$version"
        message = "Released $version"
    }

    pom = kystrixPom("Kystrix is a small Kotlin DSL over Hystrix")
}

val spring = project(core) {
    name = "kystrix-spring"
    group = KYSTRIX_GROUP_NAME
    artifactId = name
    version = Versions.kystrix
    directory = "spring"

    kotlinCompiler {
        args("-jvm-target", Versions.java)
    }

    dependencies {
        compile("io.projectreactor:reactor-core:${Versions.reactorCore}")
        compile("io.reactivex:rxjava-reactive-streams:${Versions.reactiveStreams}")
        compile("org.jetbrains.kotlin:kotlin-runtime:${Versions.kotlin}")
        compile("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
        compile("com.netflix.hystrix:hystrix-core:${Versions.hystrix}")
    }

    dependenciesTest {
        compile("junit:junit:${Versions.junit}")
        compile("org.assertj:assertj-core:${Versions.assertj}")
        compile("io.javalin:javalin:${Versions.javalin}")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
        compile("io.projectreactor.ipc:reactor-netty:${Versions.reactorNetty}")
        compile("org.springframework:spring-webflux:${Versions.webflux}")
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
        enabled = false
        annotated = true
        tag = "$version"
        message = "Released $version"
    }

    pom = kystrixPom("Kystrix Spring adds Spring support for Kystrix")
}

private fun Project.kystrixPom(desc: String) = Model().apply {
    name = project.name
    description = desc
    url = "https://kystrix.haleby.se/"
    licenses = listOf(License().apply {
        name = "Apache-2.0"
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
