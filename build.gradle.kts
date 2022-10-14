import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask

group = "me.cjcrafter"
version = "1.0.1"

plugins {
    `java-library`
    `maven-publish`
    id("com.github.breadmoirai.github-release") version "2.4.1"
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
    withSourcesJar()
    withJavadocJar()
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

tasks.test {
    useJUnitPlatform()
}

tasks.register("releaseAutoSupport").configure {
    dependsOn(":publish")
    finalizedBy(":createGithubRelease")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/CJCrafter/AutoSupport")
            credentials {
                username = findProperty("user").toString()
                password = findProperty("pass").toString()
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
                groupId = "me.cjcrafter"
                artifactId = "autosupport" // MUST be lowercase

            from(components["java"])
        }
    }
}

tasks.register<GithubReleaseTask>("createGithubRelease").configure {
    // https://github.com/BreadMoirai/github-release-gradle-plugin

    owner.set("CJCrafter")
    repo.set("AutoSupport")
    authorization.set("Token ${findProperty("pass").toString()}")
    tagName.set("$version")
    targetCommitish.set("main")
    releaseName.set("v${version}")
    draft.set(true)
    prerelease.set(false)
    generateReleaseNotes.set(true)
    body.set("")
    overwrite.set(false)
    allowUploadToExisting.set(false)
    apiEndpoint.set("https://api.github.com")

    setReleaseAssets(file("/build/libs/AutoSupport-$version"))

    // If set to true, you can debug that this would do
    dryRun.set(false)

    doFirst {
        println("Creating GitHub release")
    }
}