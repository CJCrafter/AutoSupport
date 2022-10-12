group = "me.cjcrafter"
version = "1.0.0"

plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.18")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}