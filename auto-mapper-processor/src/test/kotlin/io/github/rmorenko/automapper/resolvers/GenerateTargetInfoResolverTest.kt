package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import io.github.rmorenko.automapper.annotations.GenerateTarget
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.github.rmorenko.automapper.model.TargetPropertyInfo
import io.github.rmorenko.automapper.setPrivateField
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class GenerateTargetInfoResolverTest : StringSpec({

    val logger = mockk<KSPLogger>(relaxed = true)

    "resolve should failed when GenerateTarget no annotations not present" {
        val classDeclaration = mockk<KSClassDeclaration> {
            every {
                annotations
            } returns emptyList<KSAnnotation>().asSequence()
        }
        val generateTargetInfoResolver = GenerateTargetInfoResolver(logger)
        shouldThrow<AnnotationNotPresent> {
            generateTargetInfoResolver.resolve(classDeclaration)
        }
    }

    "resolve should failed when multiply GenerateTarget no annotations" {
        val classDeclaration = mockk<KSClassDeclaration> {
            every {
                annotations
            } returns listOf(
                mockk<KSAnnotation> {
                    every {
                        shortName.asString()
                    } returns GenerateTarget::class.simpleName.toString()

                    every {
                        annotationType.resolve().declaration.qualifiedName?.asString()
                    } returns GenerateTarget::class.qualifiedName.toString()
                },
                mockk<KSAnnotation> {
                    every {
                        shortName.asString()
                    } returns GenerateTarget::class.simpleName.toString()

                    every {
                        annotationType.resolve().declaration.qualifiedName?.asString()
                    } returns GenerateTarget::class.qualifiedName.toString()
                }
            ).asSequence()
        }
        val generateTargetInfoResolver = GenerateTargetInfoResolver(logger)
        shouldThrow<MultiplyAnnotationException> {
            generateTargetInfoResolver.resolve(classDeclaration)
        }
    }

    "resolve should return correct result" {
        val classDeclaration = mockk<KSClassDeclaration> {
            every {
                annotations
            } returns listOf(
                mockk<KSAnnotation> {
                    every {
                        shortName.asString()
                    } returns GenerateTarget::class.simpleName.toString()

                    every {
                        annotationType.resolve().declaration.qualifiedName?.asString()
                    } returns GenerateTarget::class.qualifiedName.toString()

                    every {
                        arguments
                    } returns listOf(
                        mockk <KSValueArgument> {
                            every {
                                name?.getShortName()
                            } returns "pkg"

                            every {
                                value as String
                            } returns "com.example.target"

                        },
                        mockk <KSValueArgument> {
                            every {
                                name?.getShortName()
                            } returns "name"

                            every {
                                value as String
                            } returns "TargetClass"

                        },
                        mockk <KSValueArgument> {
                            every {
                                name?.getShortName()
                            } returns "name"

                            every {
                                value as String
                            } returns "TargetClass"
                        }
                    )
                }
            ).asSequence()
        }


        val targetPropertiesInfoResolver = mockk<TargetPropertiesInfoResolver>{
            every {
                resolve(classDeclaration)
            } returns listOf(
                TargetPropertyInfo("targetProperty",
                    "com.example.target.property", "TargetProperty")
            )
        }
        val generateTargetInfoResolver = GenerateTargetInfoResolver(logger)
        generateTargetInfoResolver.setPrivateField("targetPropertiesInfoResolver", targetPropertiesInfoResolver)
        val generateTargetInfo = generateTargetInfoResolver.resolve(classDeclaration)
        generateTargetInfo.name shouldBe "TargetClass"
        generateTargetInfo.packageName shouldBe "com.example.target"
        generateTargetInfo.properties.size shouldBe 1
        val targetPropertyInfo = generateTargetInfo.properties.first()
        targetPropertyInfo.name shouldBe "targetProperty"
        targetPropertyInfo.pkg shouldBe "com.example.target.property"
        targetPropertyInfo.className shouldBe "TargetProperty"

    }

    "resolve should return correct result wit empty pkg value in GenerateTarget annotation" {
        val classDeclaration = mockk<KSClassDeclaration> {
            every {
                packageName.asString()
            } returns "com.example.source"
            every {
                annotations
            } returns listOf(
                mockk<KSAnnotation> {
                    every {
                        shortName.asString()
                    } returns GenerateTarget::class.simpleName.toString()

                    every {
                        annotationType.resolve().declaration.qualifiedName?.asString()
                    } returns GenerateTarget::class.qualifiedName.toString()

                    every {
                        arguments
                    } returns listOf(
                        mockk <KSValueArgument> {
                            every {
                                name?.getShortName()
                            } returns "pkg"

                            every {
                                value as String
                            } returns ""

                        },
                        mockk <KSValueArgument> {
                            every {
                                name?.getShortName()
                            } returns "name"

                            every {
                                value as String
                            } returns "TargetClass"

                        },
                        mockk <KSValueArgument> {
                            every {
                                name?.getShortName()
                            } returns "name"

                            every {
                                value as String
                            } returns "TargetClass"
                        }
                    )
                }
            ).asSequence()
        }


        val targetPropertiesInfoResolver = mockk<TargetPropertiesInfoResolver>{
            every {
                resolve(classDeclaration)
            } returns listOf(
                TargetPropertyInfo("targetProperty",
                    "com.example.target.property", "TargetProperty")
            )
        }
        val generateTargetInfoResolver = GenerateTargetInfoResolver(logger)
        generateTargetInfoResolver.setPrivateField("targetPropertiesInfoResolver", targetPropertiesInfoResolver)
        val generateTargetInfo = generateTargetInfoResolver.resolve(classDeclaration)
        generateTargetInfo.name shouldBe "TargetClass"
        generateTargetInfo.packageName shouldBe "com.example.source"
        generateTargetInfo.properties.size shouldBe 1
        val targetPropertyInfo = generateTargetInfo.properties.first()
        targetPropertyInfo.name shouldBe "targetProperty"
        targetPropertyInfo.pkg shouldBe "com.example.target.property"
        targetPropertyInfo.className shouldBe "TargetProperty"

    }


})
