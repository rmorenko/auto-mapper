package com.morenko.automapper


import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class AutoMapperProcessorTest : FeatureSpec({
    //kotlin-compile-testing-ksp not support kotlin 2 compiler
    xfeature("should generate mapping function") {
        val source = SourceFile.kotlin(
            "TestDto.kt", """
            package com.example

            import com.morenko.automapper.annotations.AutoMapper

            @AutoMapper(TestEntity::class)
            data class TestDto(val id: String, val name: String)

            data class TestEntity(val id: String, val name: String)
            """
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            symbolProcessorProviders = listOf(AutoMapperProcessorProvider())
            inheritClassPath = true
            messageOutputStream = System.out // Для отладки
        }

        val result = compilation.compile()


        result.exitCode shouldBe KotlinCompilation.ExitCode.OK


        val generatedFile = compilation.kspSourcesDir.resolve("generated/mapper/TestDto_Mapper.kt")
        generatedFile.shouldExist()

        val content = generatedFile.readText()
        content.shouldContain("fun TestDto.mapTestDtoToTestEntity(): TestEntity")
    }
})
