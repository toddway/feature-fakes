tasks.create("buildReadme") {
    doLast {
        copyImg()
        kotlinToMarkdown(
            inputFile = File("/Users/tway/AndroidStudioProjects/Sandbox/app/src/main/java/com/example/Readme.kt"),
            outputFile = File("${rootDir}/README.md"),
            imgPrefix = ""
        )
    }
}

fun copyImg() {
    listOf(
        "com.example.BasicThermostatApp.png",
        "com.example.FakeThermostatApp.png",
        "com.example.ThermostatAppWithFlags.png",
        "com.example.ThermostatApp.png",
        "com.example.BigThermostatApp.png"
    ).forEach {
        File("${rootDir}/app/build/generated/source/kapt/debug/scabbard/$it")
            .copyTo(File("${rootDir}/img/$it"), true)
    }
}

fun kotlinToMarkdown(
    inputFile : File,
    outputFile : File,
    imgPrefix : String = ""
) {
    val newLines = mutableListOf<String>()
    var isInCode = true
    inputFile.readLines().forEach {
        when {
            it.trim().startsWith("/*") -> {
                if (newLines.isNotEmpty()) newLines += it.replace("/*", "```\n")
                newLines += ""
                isInCode = false
            }
            it.trim().endsWith("*/") -> {
                newLines += it.replace("*/","\n```kotlin")
                isInCode = true
            }
            !isInCode && it.isBlank() -> newLines += "\n"
            !isInCode -> {
                var newValue = it.trim()
                if (newLines.last().trim().isNotEmpty()) {
                    newValue = " $newValue"
                }
                newLines[newLines.lastIndex] += newValue
            }
            newLines.isNotEmpty() -> newLines += it
        }
    }
    newLines.removeLast()
    outputFile.delete()
    newLines.forEach {
        val line = it.replace("src=\"", "src=\"$imgPrefix")
        outputFile.appendText("$line\n")
    }
}