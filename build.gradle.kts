plugins {
   id("java")
   id("application")
   id("org.jetbrains.kotlin.jvm").version("1.3.10")
}

application {
   mainClassName = "nisui.app.NisuiApp"
}

allprojects {
    repositories {
        jcenter()
        maven(url = "https://jitpack.io")
    }

    apply(plugin = "java")
    apply(plugin = "kotlin")
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.slf4j:slf4j-api:1.7.21")
        testImplementation("junit:junit:4.12")
        testImplementation("org.assertj:assertj-core:3.8.0")
    }
    tasks.test {
        outputs.upToDateWhen { false }
        testLogging.showStandardStreams = true
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(arrayOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }
    /* compileTestJava { */
        /* options.compilerArgs << "-Xlint:unchecked" << "-Xlint:deprecation" */
    /* } */
}

/* tasks.run { */
val run by tasks.getting(JavaExec::class) {
   standardInput = System.`in`
   enableAssertions = true
   /* (findProperty("mainClass") as? String)?.let { */
      /* main = it */
   /* } */
   /* (findProperty("args") as? String)?.let { */
      /* println("Got args $it") */
      /* args?.addAll(it.split(' ')) */
   /* } */
}

tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Implementation-Title"] = "Nisui"
        attributes["Main-Class"] = "nisui.app.NisuiApp"
    }
    baseName = "Nisui"
    destinationDir = file("build")
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
}

subprojects {
    dependencies {
        testImplementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.6.2")
        testImplementation(group = "org.slf4j", name = "slf4j-simple", version = "1.7.25")
    }
}

project(":core") {
   dependencies {
      implementation("com.github.petitparser.java-petitparser:petitparser-core:2.1.0")
   }
}

project(":java_runner") {
    dependencies {
        implementation(project(":core"))
    }
}

project(":h2_store") {
    dependencies {
       implementation(group = "com.h2database", name = "h2", version = "1.4.196")
       implementation(project(":core"))
    }
}

project(":cli") {
    dependencies {
       implementation(project(":core"))
       implementation("info.picocli:picocli:2.2.1")
       implementation(project(":simple_reactor"))

       testImplementation("org.assertj:assertj-core:3.8.0")

       testImplementation(project(":java_runner"))
       testImplementation(project(":h2_store"))

    }
}

project(":simple_reactor") {
   dependencies {
       implementation(project(":core"))
       testImplementation(project(":java_runner"))
       testImplementation(project(":h2_store"))
   }
}

project(":gui") {
   dependencies {
      implementation(project(":core"))
      implementation(kotlin("reflect"))
      testImplementation("org.assertj:assertj-swing-junit:3.8.0")

      testImplementation(project(":h2_store"))
      testImplementation(project(":java_runner"))
   }
}


// App project"s settings

dependencies {
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.6.2")
    implementation(group = "org.slf4j", name = "slf4j-simple", version = "1.7.25")
    implementation("com.esotericsoftware.yamlbeans:yamlbeans:1.13")

    implementation(project(":core"))
    implementation(project(":java_runner"))
    implementation(project(":cli"))
    implementation(project(":h2_store"))
    implementation(project(":gui"))
}

tasks.test {
    testLogging.showStandardStreams = true
}

tasks.register<Javadoc>("javadocAll") {
   for (subproject in getSubprojects()) {
      val main = subproject.sourceSets.main.get()
      source = source.plus(main.getAllSource())
      classpath = classpath.plus(main.compileClasspath)
   }
   source = source.matching {
      include("**/*.java")
      // TODO: add dokka to document Kotlin code
   }
   destinationDir = File(buildDir, "javadoc")
}
