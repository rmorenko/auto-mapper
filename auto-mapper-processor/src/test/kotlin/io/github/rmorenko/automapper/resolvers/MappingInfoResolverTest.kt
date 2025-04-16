package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import io.github.rmorenko.automapper.annotations.Mapping
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.mockk.every
import io.mockk.mockk

private val annotationSimpleName =  Mapping::class.simpleName.toString()

class MappingInfoResolverTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)
    val resolver = MappingInfoResolver(logger)

    "resolve should return empty result without annotation" {
        val classDeclaration = mockk<KSClassDeclaration>()

        every {
            classDeclaration.annotations
        } returns listOf<KSAnnotation>().asSequence()


        val propertyDeclarations = listOf(
            mockk<KSPropertyDeclaration>{
                every { annotations } returns listOf<KSAnnotation>().asSequence()
            }
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val actualResult = resolver.resolve(classDeclaration)
        actualResult.size shouldBe 0

    }

    "resolve should return empty result with empty Mapping annotation" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()


        val propertyDeclarations = listOf(
            mockk<KSPropertyDeclaration>{
                every { annotations } returns listOf(mappingAnnotation).asSequence()
            }
        ).asSequence()

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns Mapping::class.toString()

        every {
            mappingAnnotation.shortName.asString()
        } returns annotationSimpleName


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        every {
            mappingAnnotation.arguments
        } returns emptyList()

        val actualResult = resolver.resolve(classDeclaration)
        actualResult.size shouldBe 0

    }

    "resolve should fail when duplicate Mapping annotation" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation, mappingAnnotation).asSequence()

        val propertyDeclaration = mockk<KSPropertyDeclaration>{
            every { annotations } returns listOf(mappingAnnotation, mappingAnnotation).asSequence()
            every { simpleName.asString() } returns "sourceProperty"
        }


        val propertyDeclarations = listOf(propertyDeclaration
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns annotationSimpleName

        every {
            mappingAnnotation.shortName.asString()
        } returns Mapping::class.simpleName.toString()


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        shouldThrowExactly<MultiplyAnnotationException> {
            resolver.resolve(classDeclaration)
        }
    }

    "resolve should return not empty result when empty annotation arguments values" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()

        val propertyDeclaration = mockk<KSPropertyDeclaration>{
            every { annotations } returns listOf(mappingAnnotation).asSequence()
            every { simpleName.asString() } returns "sourceProperty"
        }


        val propertyDeclarations = listOf(propertyDeclaration
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns annotationSimpleName

        every {
            mappingAnnotation.shortName.asString()
        } returns Mapping::class.simpleName.toString()


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        val arguments = listOf(
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "target"

                every {
                    value
                } returns ""
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "transform"

                every {
                    value
                } returns ""
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "code"

                every {
                    value
                } returns ""
            }

        )

        every {
            mappingAnnotation.arguments
        } returns arguments

        val actualResult = resolver.resolve(classDeclaration)
        val mappingInfo = actualResult["sourceProperty"]
        mappingInfo.shouldNotBeNull()
        mappingInfo.functionName.shouldBeEmpty()
        mappingInfo.isExtension shouldBe false
        mappingInfo.code.shouldBeEmpty()
    }


    "resolve should return correct answer with not empty target, transform, code" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()

        val propertyDeclaration = mockk<KSPropertyDeclaration>{
            every { annotations } returns listOf(mappingAnnotation).asSequence()
            every { simpleName.asString() } returns "sourceProperty"
        }


        val propertyDeclarations = listOf(propertyDeclaration
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns annotationSimpleName

        every {
            mappingAnnotation.shortName.asString()
        } returns Mapping::class.simpleName.toString()


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        val arguments = listOf(
            mockk<KSValueArgument>{
                 every {
                     name?.getShortName()
                 } returns "target"

                every {
                    value
                } returns "targetProperty"
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "transform"

                every {
                    value
                } returns "transformFunction"
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "code"

                every {
                    value
                } returns "some code"
            }
        )

        every {
            mappingAnnotation.arguments
        } returns arguments

        val actualResult = resolver.resolve(classDeclaration)
        val mappingInfo = actualResult["sourceProperty"]
        mappingInfo.shouldNotBeNull()
        mappingInfo.code shouldBe "some code"
        mappingInfo.isExtension shouldBe false
        mappingInfo.functionName shouldBe "transformFunction"
    }


    "resolve should return correct answer with not empty target, transform" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()

        val propertyDeclaration = mockk<KSPropertyDeclaration>{
            every { annotations } returns listOf(mappingAnnotation).asSequence()
            every { simpleName.asString() } returns "sourceProperty"
        }


        val propertyDeclarations = listOf(propertyDeclaration
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns annotationSimpleName

        every {
            mappingAnnotation.shortName.asString()
        } returns Mapping::class.simpleName.toString()


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        val arguments = listOf(
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "target"

                every {
                    value
                } returns "targetProperty"
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "transform"

                every {
                    value
                } returns "transformFunction"
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "code"

                every {
                    value
                } returns ""
            }
        )

        every {
            mappingAnnotation.arguments
        } returns arguments

        val actualResult = resolver.resolve(classDeclaration)
        val mappingInfo = actualResult["sourceProperty"]
        mappingInfo.shouldNotBeNull()
        mappingInfo.target shouldBe "targetProperty"
        mappingInfo.code.shouldBeEmpty()
        mappingInfo.isExtension shouldBe false
        mappingInfo.functionName shouldBe "transformFunction"
    }


    "resolve should return correct answer with not empty transform and code" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()

        val propertyDeclaration = mockk<KSPropertyDeclaration>{
            every { annotations } returns listOf(mappingAnnotation).asSequence()
            every { simpleName.asString() } returns "sourceProperty"
        }


        val propertyDeclarations = listOf(propertyDeclaration
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns annotationSimpleName

        every {
            mappingAnnotation.shortName.asString()
        } returns Mapping::class.simpleName.toString()


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        val arguments = listOf(
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "target"

                every {
                    value
                } returns ""
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "transform"

                every {
                    value
                } returns "transformFunction"
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "code"

                every {
                    value
                } returns "some code"
            }
        )

        every {
            mappingAnnotation.arguments
        } returns arguments

        val actualResult = resolver.resolve(classDeclaration)
        val mappingInfo = actualResult["sourceProperty"]
        mappingInfo.shouldNotBeNull()
        mappingInfo.target.shouldBeEmpty()
        mappingInfo.code shouldBe "some code"
        mappingInfo.isExtension shouldBe false
        mappingInfo.functionName shouldBe "transformFunction"
    }


    "resolve should return correct answer with not empty target and code" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()

        val propertyDeclaration = mockk<KSPropertyDeclaration>{
            every { annotations } returns listOf(mappingAnnotation).asSequence()
            every { simpleName.asString() } returns "sourceProperty"
        }


        val propertyDeclarations = listOf(propertyDeclaration
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns annotationSimpleName

        every {
            mappingAnnotation.shortName.asString()
        } returns Mapping::class.simpleName.toString()


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        val arguments = listOf(
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "target"

                every {
                    value
                } returns "targetProperty"
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "transform"

                every {
                    value
                } returns ""
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "code"

                every {
                    value
                } returns "some code"
            }
        )

        every {
            mappingAnnotation.arguments
        } returns arguments

        val actualResult = resolver.resolve(classDeclaration)
        val mappingInfo = actualResult["sourceProperty"]
        mappingInfo.shouldNotBeNull()
        mappingInfo.target shouldBe "targetProperty"
        mappingInfo.code shouldBe "some code"
        mappingInfo.isExtension shouldBe false
        mappingInfo.functionName.shouldBeEmpty()
    }


    "resolve should return correct answer with extenfion function as tranform" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val mappingAnnotation = mockk<KSAnnotation>()
        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotation).asSequence()

        val propertyDeclaration = mockk<KSPropertyDeclaration>{
            every { annotations } returns listOf(mappingAnnotation).asSequence()
            every { simpleName.asString() } returns "sourceProperty"
        }


        val propertyDeclarations = listOf(propertyDeclaration
        ).asSequence()

        every {
            classDeclaration.getAllProperties()
        } returns propertyDeclarations

        val mappingAnnotationName = mockk<KSName>()

        every {
            mappingAnnotationName.asString()
        } returns annotationSimpleName

        every {
            mappingAnnotation.shortName.asString()
        } returns Mapping::class.simpleName.toString()


        every {
            mappingAnnotation.shortName
        } returns mappingAnnotationName

        val arguments = listOf(
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "target"

                every {
                    value
                } returns "targetProperty"
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "transform"

                every {
                    value
                } returns "SomeClass.someFunction"
            },
            mockk<KSValueArgument>{
                every {
                    name?.getShortName()
                } returns "code"

                every {
                    value
                } returns "some code"
            }
        )

        every {
            mappingAnnotation.arguments
        } returns arguments

        val actualResult = resolver.resolve(classDeclaration)
        val mappingInfo = actualResult["sourceProperty"]
        mappingInfo.shouldNotBeNull()
        mappingInfo.target shouldBe "targetProperty"
        mappingInfo.code shouldBe "some code"
        mappingInfo.isExtension shouldBe true
        mappingInfo.functionName shouldBe "someFunction"
    }

})
