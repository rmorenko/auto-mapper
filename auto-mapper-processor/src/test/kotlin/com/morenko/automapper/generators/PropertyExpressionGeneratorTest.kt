package com.morenko.automapper.generators

import com.google.devtools.ksp.processing.KSPLogger
import com.morenko.automapper.model.MappingInfo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class PropertyExpressionGeneratorTest : StringSpec({

    val logger = mockk<KSPLogger>(relaxed = true)
    val generator = PropertyExpressionGenerator(logger)

    "should generate expression with mapping info and target with targetEntityName " {
        val mappingInfo = MappingInfo(mapFn = "mapFunction", invokeFn = "invokeFunction",
            mapFirst = true, code = "customCode")
        val result = generator.generate("propertyName", "PropertyClass",
            "TargetEntity", mappingInfo)
        result shouldBe "mapFunction(customCode).invokeFunction()"
    }

    "should generate expression with mapping info and target with targetEntityName and empty code " {
        val mappingInfo = MappingInfo(mapFn = "mapFunction", invokeFn = "invokeFunction",
            mapFirst = true, code = "")
        val result = generator.generate("propertyName", "PropertyClass",
            "TargetEntity", mappingInfo)
        result shouldBe "mapFunction(this.propertyName.mapPropertyClassToTargetEntity()).invokeFunction()"
    }

    "should generate expression without target entity name" {
        val mappingInfo = MappingInfo(mapFn = "mapFunction", invokeFn = "invokeFunction", mapFirst = false, code = "")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "mapFunction(this.propertyName.invokeFunction())"
    }

    "should generate expression with custom code only" {
        val mappingInfo = MappingInfo(mapFn = "", invokeFn = "", mapFirst = false, code = "customCode")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "customCode"
    }

    "should generate expression with custom code and mapFN" {
        val mappingInfo = MappingInfo(mapFn = "mapFunction", invokeFn = "", mapFirst = false, code = "customCode")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "mapFunction(customCode)"
    }

    "should generate expression with custom code and invokeFn" {
        val mappingInfo = MappingInfo(mapFn = "", invokeFn = "invokeFn", mapFirst = false, code = "customCode")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "customCode.invokeFn()"
    }

    "should generate expression with mapFunction and  invokeFn and mapFirst true " {
        val mappingInfo = MappingInfo(mapFn = "mapFunction", invokeFn = "invokeFn", mapFirst = true, code = "")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "mapFunction(this.propertyName).invokeFn()"
    }

    "should generate expression with mapFunction and invokeFn and mapFirst false " {
        val mappingInfo = MappingInfo(mapFn = "mapFunction", invokeFn = "invokeFn", mapFirst = false, code = "")
        val result = generator.generate("propertyName", "PropertyClass",
            null, mappingInfo)
        result shouldBe "mapFunction(this.propertyName.invokeFn())"
    }

    "should generate simple expression without mapping info" {
        val result = generator.generate("propertyName", "PropertyClass",
            null, null)
        result shouldBe "this.propertyName"
    }
})
