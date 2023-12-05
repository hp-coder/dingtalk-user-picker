plugins {
    java
    `maven-publish`
    // A Gradle plugin that provides Maven-like dependency management and exclusions
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.luban"
version = "1.0.0-sp2-SNAPSHOT"
var artifactId = "dingtalk-user-picker"


java {
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("DingTalkUserPicker") {
                groupId = "${project.group}"
                artifactId = "${artifactId}"
                version = "${project.version}"
                from(components["java"])

                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }

                pom {
                    name = "${artifactId}"
                    description = "A concise description of my library"
                    packaging = "jar"
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "hp"
                            name = "Hu Peng"
                            email = "hup_dev@outlook.com"
                        }
                    }
                }
            }
        }
    }
}

dependencyManagement {
    // Avoid being overwritten by dependencies.
    overriddenByDependencies(false)
    // Disable cache since SNAPSHOT dependencies have been used.
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }
    // Define a bom.
    imports {
        mavenBom("com.luban:common-bom:1.0.0-sp2-SNAPSHOT")
    }
    dependencies {
        // Fix bug
        dependency("log4j:log4j:1.2.17")

        // Project related dependencies
        dependency("com.luban:luban-dingtalk:1.0.4-sp2-SNAPSHOT")
        dependency("com.belerweb:pinyin4j:2.5.1")
        dependency("com.alibaba:fastjson:1.2.83")
    }
}

dependencies {
    // Project dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.luban:luban-dingtalk")
    implementation("com.belerweb:pinyin4j")
    implementation("com.alibaba:fastjson")

    // Project test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    compileOnly("org.projectlombok:lombok")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor")
}

tasks.compileJava {
    options.isIncremental = true
    options.isFork = true
    options.isFailOnError = false
}
