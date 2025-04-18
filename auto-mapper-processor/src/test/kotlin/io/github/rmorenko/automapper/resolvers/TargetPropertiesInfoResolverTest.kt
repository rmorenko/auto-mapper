package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import io.github.rmorenko.automapper.annotations.GenerateTargetPropertyType
import io.github.rmorenko.automapper.setPrivateField
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class TargetPropertiesInfoResolverTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)
    val targetPropertyExcludeResolver = mockk<TargetPropertyExcludeResolver>()
    val targetPropertiesInfoResolver = TargetPropertiesInfoResolver(logger)

    "resolve should return empty list if no properties in class declaration" {
        val klassDeclaration = mockk<KSClassDeclaration> {
            every {
                getAllProperties()
            } returns emptyList<KSPropertyDeclaration>().asSequence()
        }
        targetPropertiesInfoResolver.setPrivateField("excludeResolver", targetPropertyExcludeResolver)
        targetPropertiesInfoResolver.resolve(klassDeclaration)
    }

    "resolve should return correct list if properties without annotations" {
        val propertyOne = mockk<KSPropertyDeclaration> {
            every {
                simpleName.asString()
            } returns "propertyOne"

            every {
                type.resolve().declaration.packageName.asString()
            } returns "com.example.one"

            every {
                type.resolve().declaration.simpleName.asString()
            } returns "PropertyOne"

            every {
                annotations
            } returns emptyList<KSAnnotation>().asSequence()
        }
        val propertyTwo = mockk<KSPropertyDeclaration> {
            every {
                simpleName.asString()
            } returns "propertyTwo"
            every {
                type.resolve().declaration.packageName.asString()
            } returns "com.example.two"

            every {
                type.resolve().declaration.simpleName.asString()
            } returns "PropertyTwo"

            every {
                annotations
            } returns emptyList<KSAnnotation>().asSequence()
        }
        val klassDeclaration = mockk<KSClassDeclaration> {
            every {
                getAllProperties()
            } returns listOf(
                propertyOne,
                propertyTwo
            ).asSequence()
        }
        every {
            targetPropertyExcludeResolver.resolve(propertyOne)
        } returns false

        every {
            targetPropertyExcludeResolver.resolve(propertyTwo)
        } returns true

        targetPropertiesInfoResolver.setPrivateField("excludeResolver", targetPropertyExcludeResolver)
        targetPropertiesInfoResolver.resolve(klassDeclaration).size shouldBe 1
        val targetPropertyInfo = targetPropertiesInfoResolver.resolve(klassDeclaration).first()
        targetPropertyInfo.name shouldBe "propertyOne"
        targetPropertyInfo.pkg shouldBe "com.example.one"
        targetPropertyInfo.className shouldBe "PropertyOne"
    }


    "resolve return correct list if property with GeneratedPropertyType annotation" {
        val propertyOne = mockk<KSPropertyDeclaration> {
            every {
                simpleName.asString()
            } returns "propertyOne"

            every {
                type.resolve().declaration.packageName.asString()
            } returns "com.example.one"

            every {
                type.resolve().declaration.simpleName.asString()
            } returns "PropertyOne"

            every {
                annotations
            } returns emptyList<KSAnnotation>().asSequence()
        }

        val generatedPropertyTypeAnnotation = mockk<KSAnnotation> {
            every {
                shortName.asString()
            } returns GenerateTargetPropertyType::class.simpleName.toString()
            every {
                annotationType.resolve().declaration.qualifiedName?.asString()
            } returns GenerateTargetPropertyType::class.qualifiedName.toString()

            every {
                arguments
            } returns listOf(
                mockk<KSValueArgument> {
                    every {
                        name?.getShortName()
                    } returns "pkg"

                    every {
                        value
                    } returns "com.example.other"


                },
                mockk<KSValueArgument> {
                    every {
                        name?.getShortName()
                    } returns "className"

                    every {
                        value
                    } returns "OtherProperty"
                }
            )
        }

        val propertyTwo = mockk<KSPropertyDeclaration> {
            every {
                simpleName.asString()
            } returns "propertyOne"
            every {
                type.resolve().declaration.packageName.asString()
            } returns "com.example.two"

            every {
                type.resolve().declaration.simpleName.asString()
            } returns "PropertyTwo"

            every {
                annotations
            } returns listOf(generatedPropertyTypeAnnotation).asSequence()
        }
        val klassDeclaration = mockk<KSClassDeclaration> {
            every {
                getAllProperties()
            } returns listOf(
                propertyOne,
                propertyTwo
            ).asSequence()
        }
        every {
            targetPropertyExcludeResolver.resolve(propertyOne)
        } returns false

        every {
            targetPropertyExcludeResolver.resolve(propertyTwo)
        } returns false

        targetPropertiesInfoResolver.setPrivateField("excludeResolver", targetPropertyExcludeResolver)
        targetPropertiesInfoResolver.resolve(klassDeclaration).size shouldBe 2
        var targetPropertyInfo = targetPropertiesInfoResolver.resolve(klassDeclaration).first()
        targetPropertyInfo.name shouldBe "propertyOne"
        targetPropertyInfo.pkg shouldBe "com.example.one"
        targetPropertyInfo.className shouldBe "PropertyOne"

        targetPropertyInfo = targetPropertiesInfoResolver.resolve(klassDeclaration)[1]
        targetPropertyInfo.name shouldBe "propertyOne"
        targetPropertyInfo.pkg shouldBe "com.example.other"
        targetPropertyInfo.className shouldBe "OtherProperty"
    }

})
