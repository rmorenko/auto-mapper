package io.github.rmorenko.automapper.generators

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.model.AutoMapperInfo
import io.github.rmorenko.automapper.setPrivateField
import com.squareup.kotlinpoet.ksp.toClassName
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.mockk.declaringKotlinFile
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify

class PropertyMappingsGeneratorTest : StringSpec({

    val logger = mockk<KSPLogger>(relaxed = true)

    "should generate code when properties are provided without Mapping annotations" {

        val targetPropertyDeclarationOne = mockk<KSPropertyDeclaration> {
            every { simpleName.asString() } returns "propertyOne"
        }
        val targetPropertyDeclarationTwo = mockk<KSPropertyDeclaration>{
            every { simpleName.asString() } returns "propertyTwo"
        }

        val targetPropertyDeclarationFour = mockk<KSPropertyDeclaration> {
            every { simpleName.asString() } returns "propertyFour"
        }

        val targetClassDeclaration = mockk<KSClassDeclaration> {
            every { getAllProperties() } returns listOf(targetPropertyDeclarationOne,
                targetPropertyDeclarationTwo, targetPropertyDeclarationFour
            ).asSequence()
        }

        val ksType = mockk<KSType>{
            every {
                declaration
            } returns targetClassDeclaration
        }

        val argument = mockk<KSValueArgument>{
            every { value } returns ksType
            every { name?.asString() } returns "target"
        }

        val annotation = mockk<KSAnnotation>{
            every { shortName.asString() } returns AutoMapper::class.simpleName.toString()
            every { arguments } returns listOf(argument)
        }


        val ksTypeOneDeclaration = mockk<KSClassDeclaration>{
            every { annotations } returns listOf(
                mockk<KSAnnotation>{
                    every { shortName.asString() } returns AutoMapper::class.simpleName.toString()
                    every { arguments } returns listOf(
                        mockk<KSValueArgument>{
                            every { name?.getShortName() } returns "target"
                            every { value } returns mockk<KSType>{
                                every { declaration.simpleName.asString() } returns "NestedTargetClass"
                            }
                        }
                    )
                }
            ).asSequence()
        }

        val ksTypeOne = mockk<KSType>{
            every { declaration } returns ksTypeOneDeclaration
        }

        mockkStatic(KSType::toClassName.declaringKotlinFile.qualifiedName!!)
        every {
            ksTypeOne.toClassName().simpleName
        } returns "NestedSourceClass"

        val typeReferenceOne = mockk<KSTypeReference>{
            every { resolve() } returns ksTypeOne
        }


        val sourcePropertyDeclarationOne = mockk<KSPropertyDeclaration>{
            every { simpleName.asString() } returns "propertyOne"
            every {
                type
            } returns typeReferenceOne
        }

        val ksTypeTwoDeclaration = mockk<KSClassDeclaration>{
            every { annotations } returns emptyList<KSAnnotation>().asSequence()
        }

        val ksTypeTwo = mockk<KSType>{
            every { declaration } returns ksTypeTwoDeclaration
        }

        every {
            ksTypeTwo.toClassName().simpleName
        } returns "String"

        val typeReferenceTwo = mockk<KSTypeReference>{
            every { resolve() } returns ksTypeTwo
        }

        val sourcePropertyDeclarationTwo = mockk<KSPropertyDeclaration>{
            every { simpleName.asString() } returns "propertyTwo"
            every {
                type
            } returns typeReferenceTwo
        }

        val ksTypeThreeDeclaration = mockk<KSClassDeclaration>{
            every { annotations } returns emptyList<KSAnnotation>().asSequence()
        }

        val ksTypeThree = mockk<KSType>{
            every { declaration } returns ksTypeThreeDeclaration
        }

        every {
            ksTypeThree.toClassName().simpleName
        } returns "String"

        val typeReferenceThree = mockk<KSTypeReference>{
            every { resolve() } returns ksTypeThree
        }

        val sourcePropertyDeclarationThree = mockk<KSPropertyDeclaration>{
            every { simpleName.asString() } returns "propertyThree"
            every {
                type
            } returns typeReferenceThree
        }


        val ksTypeFourDeclaration = mockk<KSClassDeclaration> {
            every { annotations } returns emptyList<KSAnnotation>().asSequence()
        }

        val ksTypeFour = mockk<KSType> {
            every { declaration } returns ksTypeFourDeclaration
        }


        val typeReferenceFour = mockk<KSTypeReference> {
            every { resolve() } returns ksTypeFour
        }

        val sourcePropertyDeclarationFour = mockk<KSPropertyDeclaration> {
            every { simpleName.asString() } returns "propertyFour"
            every {
                type
            } returns typeReferenceFour
        }

        val ksClassDeclaration = mockk<KSClassDeclaration>{
            every {
                packageName.asString()
            } returns "com.example"
            every {
                simpleName.asString()
            } returns "SourceClass"

            every {
                getAllProperties()
            } returns listOf(
                sourcePropertyDeclarationOne,
                sourcePropertyDeclarationTwo,
                sourcePropertyDeclarationThree,
                sourcePropertyDeclarationFour
            ).asSequence()

            every {
                annotations
            } returns listOf(annotation).asSequence()
        }
        val autoMapperInfo = AutoMapperInfo(
            "com.target", "TargetClass",
            excludes = setOf("propertyFour")
        )
        val propertyMappingsGenerator = PropertyMappingsGenerator(logger)
        val propertyExpressionGenerator = mockk<PropertyExpressionGenerator> {
            every {
                generate(
                    "propertyOne", "NestedSourceClass",
                    "NestedTargetClass", null
                )
            } returns "generated expression one"
            every {
                generate(
                    "propertyTwo", "String",
                    null, null
                )
            } returns "generated expression two"

            every {
                generate(
                    "propertyThree", "String",
                    null, null
                )
            } returns "generated expression three"

            every {
                generate(
                    "propertyFour", "String",
                    null, null
                )
            } returns "generated expression four"
        }
        propertyMappingsGenerator.setPrivateField("propertyExpressionGenerator", propertyExpressionGenerator)

        propertyMappingsGenerator.generate(
            ksClassDeclaration,
            emptyMap(),
            autoMapperInfo
        ) shouldBe """    propertyOne = generated expression one,
    propertyTwo = generated expression two"""

        verify(exactly = 1) {
            propertyExpressionGenerator.generate(
                "propertyOne", "NestedSourceClass",
                "NestedTargetClass", null
            )

            propertyExpressionGenerator.generate(
                "propertyTwo", "String",
                null, null
            )


        }

        verify(exactly = 0) {

            propertyExpressionGenerator.generate(
                "propertyThree", "String",
                null, null
            )

            propertyExpressionGenerator.generate(
                "propertyFour", "String",
                null, null
            )
        }
    }

    "should not generate code when no properties are provided" {

        val ksClassDeclaration = mockk<KSClassDeclaration>{
            every {
                packageName.asString()
            } returns "com.example"
            every {
                simpleName.asString()
            } returns "SourceClass"

            every {
                getAllProperties()
            } returns emptyList<KSPropertyDeclaration>().asSequence()

            every {
                annotations
            } returns emptyList<KSAnnotation>().asSequence()
        }
        val autoMapperInfo = AutoMapperInfo("com.target", "TargetClass")
        val propertyMappingsGenerator = PropertyMappingsGenerator(logger)
        propertyMappingsGenerator.generate(ksClassDeclaration, emptyMap(), autoMapperInfo).shouldBeEmpty()
    }
})
