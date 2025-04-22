package io.github.rmorenko.automapper.generators

import com.google.devtools.ksp.processing.KSPLogger
import io.github.rmorenko.automapper.model.MappingInfo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class PropertyExpressionGeneratorTest : StringSpec({

    val logger = mockk<KSPLogger>(relaxed = true)
    val generator = PropertyExpressionGenerator(logger)

    "generate expression with targetEntityName, functionName and code" {
        val mappingInfo = MappingInfo(target="test", functionName = "mapFunction", code = "customCode")
        val result = generator.generate("propertyName", "PropertyClass",
            "TargetEntity", mappingInfo)
        result shouldBe "mapFunction({ customCode }.invoke())"
    }

    "generate expression with targetEntityName, functionName and empty code" {
        val mappingInfo = MappingInfo(target="test", functionName = "mapFunction")
        val result = generator.generate("propertyName", "PropertyClass",
            "TargetEntity", mappingInfo)
        result shouldBe "mapFunction(this.propertyName.mapPropertyClassToTargetEntity())"
    }

    "generate expression with targetEntityName and empty code and functionName " {
        val mappingInfo = MappingInfo(target="test")
        val result = generator.generate("propertyName", "PropertyClass",
            "TargetEntity", mappingInfo)
        result shouldBe "this.propertyName.mapPropertyClassToTargetEntity()"
    }

    "generate expression with targetEntityName and empty target, code and functionName " {
        val mappingInfo = MappingInfo()
        val result = generator.generate("propertyName", "PropertyClass",
            "TargetEntity", mappingInfo)
        result shouldBe "this.propertyName.mapPropertyClassToTargetEntity()"
    }

    "generate expression with targetEntityName and null mappingInfo" {
        val result = generator.generate("propertyName", "PropertyClass",
            "TargetEntity", null)
        result shouldBe "this.propertyName.mapPropertyClassToTargetEntity()"
    }

    "generate expression with null targetEntityName and not empty functionName and code" {
        val mappingInfo = MappingInfo(target="test", functionName = "mapFunction", code = "customCode")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "mapFunction({ customCode }.invoke())"
    }

    "generate expression with null targetEntityName, not empty functionName and empty code" {
        val mappingInfo = MappingInfo(target="test", functionName = "mapFunction")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "mapFunction(this.propertyName)"
    }

    "generate expression with null targetEntityName and empty code and functionName " {
        val mappingInfo = MappingInfo(target="test")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "this.propertyName"
    }

    "generate expression with null targetEntityName and empty target, code and functionName " {
        val mappingInfo = MappingInfo()
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "this.propertyName"
    }

    "generate expression with null targetEntityName and null mappingInfo" {
        val result = generator.generate("propertyName", "PropertyClass",
            null, null)
        result shouldBe "this.propertyName"
    }


    "generate expression with blank targetEntityName and not empty functionName and code" {
        val mappingInfo = MappingInfo(target="test", functionName = "mapFunction", code = "customCode")
        val result = generator.generate("propertyName", "PropertyClass",
            "  ", mappingInfo)
        result shouldBe "mapFunction({ customCode }.invoke())"
    }

    "generate expression with blank targetEntityName, not empty functionName and empty code" {
        val mappingInfo = MappingInfo(target="test", functionName = "mapFunction")
        val result = generator.generate("propertyName", "PropertyClass",
            "  ", mappingInfo)
        result shouldBe "mapFunction(this.propertyName)"
    }

    "generate expression with blank targetEntityName and empty code and functionName " {
        val mappingInfo = MappingInfo(target="test")
        val result = generator.generate("propertyName", "PropertyClass",
            "  ", mappingInfo)
        result shouldBe "this.propertyName"
    }

    "generate expression with blank targetEntityName and empty target, code and functionName " {
        val mappingInfo = MappingInfo()
        val result = generator.generate("propertyName", "PropertyClass",
            "  ", mappingInfo)
        result shouldBe "this.propertyName"
    }

    "generate expression with blank targetEntityName and null mappingInfo" {
        val result = generator.generate("propertyName", "PropertyClass",
            "  ", null)
        result shouldBe "this.propertyName"
    }


})
