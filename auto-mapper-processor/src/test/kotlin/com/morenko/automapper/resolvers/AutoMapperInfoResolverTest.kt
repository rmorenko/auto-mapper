package com.morenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class AutoMapperInfoResolverTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)
    val resolver = AutoMapperInfoResolver(logger)

    "resolve should return correct AutoMapperInfo when only target argument present" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val autoMapperAnnotation = mockk<KSAnnotation>()
        val autoMapperAnnotationName = mockk<KSName>()

        every {
            autoMapperAnnotation.shortName
        } returns autoMapperAnnotationName

        every {
            autoMapperAnnotationName.asString()
        } returns "AutoMapper"

        every {
            classDeclaration.annotations
        } returns listOf(autoMapperAnnotation).asSequence()

        val autoMapperTargetArgumentName = mockk<KSName>().apply {
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }

        val autoMapperTargetKSType = mockk<KSType>().apply {
            every { declaration.packageName.asString() } returns "com.example"
            every { declaration.simpleName.asString() } returns "TargetClass"
        }

        val autoMapperAnnotationArguments = listOf(
            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperTargetArgumentName
                every { value } returns autoMapperTargetKSType
            }
        )

        every {
            autoMapperAnnotation.arguments
        } returns autoMapperAnnotationArguments


        val actualAdditionalInfo = resolver.resolve(classDeclaration)
        actualAdditionalInfo.targetPackage shouldBe "com.example"
        actualAdditionalInfo.targetName shouldBe "TargetClass"
        actualAdditionalInfo.excludes.size shouldBe 0
        actualAdditionalInfo.additionalImports.size shouldBe 0
        actualAdditionalInfo.defaults.size shouldBe 0
    }


    "resolve should return correct AutoMapperInfo" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val autoMapperAnnotation = mockk<KSAnnotation>()
        val autoMapperAnnotationName = mockk<KSName>()

        every {
            autoMapperAnnotation.shortName
        } returns autoMapperAnnotationName

        every {
            autoMapperAnnotationName.asString()
        } returns "AutoMapper"

        every {
            classDeclaration.annotations
        } returns listOf(autoMapperAnnotation).asSequence()

        val autoMapperTargetArgumentName = mockk<KSName>().apply {
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }
        val autoMapperDefaultsArgumentName = mockk<KSName>().apply {
            every { asString() } returns "defaults"
            every { getShortName() } returns "defaults"
        }

        val autoMapperImportsArgumentName = mockk<KSName>().apply {
            every { asString() } returns "imports"
            every { getShortName() } returns "imports"
        }

        val autoMapperExcludeArgumentName = mockk<KSName>().apply {
            every { asString() } returns "exclude"
            every { getShortName() } returns "exclude"
        }

        val autoMapperTargetKSType = mockk<KSType>().apply {
            every { declaration.packageName.asString() } returns "com.example"
            every { declaration.simpleName.asString() } returns "TargetClass"
        }

        val defaultKSName = mockk<KSName>().apply {
            every { asString() } returns "defaults"
        }

        val defaultTargetArgumentName = mockk<KSName>().apply {
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }

        val defaultCodeArgumentName = mockk<KSName>().apply {
            every { asString() } returns "code"
            every { getShortName() } returns "code"
        }

        val defaultAnnotationsArguments = listOf(
            mockk<KSValueArgument>().apply {
                every { name } returns defaultTargetArgumentName
                every { value } returns "otherProperty"
            },
            mockk<KSValueArgument>().apply {
                every { name } returns defaultCodeArgumentName
                every { value } returns "default code"
            }
        )

        val defaultAnnotations = listOf(
            mockk<KSAnnotation>().apply {
                every { shortName } returns defaultKSName
                every { arguments } returns defaultAnnotationsArguments
            }
        )

        val autoMapperAnnotationArguments = listOf(
            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperTargetArgumentName
                every { value } returns autoMapperTargetKSType
            },
            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperDefaultsArgumentName
                every { value } returns ArrayList(defaultAnnotations)
            },

            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperImportsArgumentName
                every { value } returns ArrayList(listOf("com.import.a", "com.import.b"))
            },

            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperExcludeArgumentName
                every { value } returns ArrayList(listOf("excludeA", "excludeB"))
            }
        )

        every {
            autoMapperAnnotation.arguments
        } returns autoMapperAnnotationArguments


        val actualAdditionalInfo = resolver.resolve(classDeclaration)
        actualAdditionalInfo.targetPackage shouldBe "com.example"
        actualAdditionalInfo.targetName shouldBe "TargetClass"
        actualAdditionalInfo.excludes shouldBe listOf("excludeA", "excludeB")
        actualAdditionalInfo.additionalImports shouldBe listOf("com.import.a", "com.import.b")
        actualAdditionalInfo.defaults.size shouldBe 1
        actualAdditionalInfo.defaults.first().code shouldBe "default code"
        actualAdditionalInfo.defaults.first().target shouldBe "otherProperty"
    }


    "resolve should return correct AutoMapperInfo when target is empty in default" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val autoMapperAnnotation = mockk<KSAnnotation>()
        val autoMapperAnnotationName = mockk<KSName>()

        every {
            autoMapperAnnotation.shortName
        } returns autoMapperAnnotationName

        every {
            autoMapperAnnotationName.asString()
        } returns "AutoMapper"

        every {
            classDeclaration.annotations
        } returns listOf(autoMapperAnnotation).asSequence()

        val autoMapperTargetArgumentName = mockk<KSName>().apply {
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }
        val autoMapperDefaultsArgumentName = mockk<KSName>().apply {
            every { asString() } returns "defaults"
            every { getShortName() } returns "defaults"
        }

        val autoMapperTargetKSType = mockk<KSType>().apply {
            every { declaration.packageName.asString() } returns "com.example"
            every { declaration.simpleName.asString() } returns "TargetClass"
        }

        val defaultKSName = mockk<KSName>().apply {
            every { asString() } returns "defaults"
        }

        val defaultTargetArgumentName = mockk<KSName>().apply {
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }

        val defaultCodeArgumentName = mockk<KSName>().apply {
            every { asString() } returns "code"
            every { getShortName() } returns "code"
        }

        val defaultAnnotationsArguments = listOf(
            mockk<KSValueArgument>().apply {
                every { name } returns defaultTargetArgumentName
                every { value } returns ""
            },
            mockk<KSValueArgument>().apply {
                every { name } returns defaultCodeArgumentName
                every { value } returns "default code"
            }
        )

        val defaultAnnotations = listOf(
            mockk<KSAnnotation>().apply {
                every { shortName } returns defaultKSName
                every { arguments } returns defaultAnnotationsArguments
            }
        )

        val autoMapperAnnotationArguments = listOf(
            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperTargetArgumentName
                every { value } returns autoMapperTargetKSType
            },
            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperDefaultsArgumentName
                every { value } returns ArrayList(defaultAnnotations)
            }
        )

        every {
            autoMapperAnnotation.arguments
        } returns autoMapperAnnotationArguments


        val actualAdditionalInfo = resolver.resolve(classDeclaration)
        actualAdditionalInfo.targetPackage shouldBe "com.example"
        actualAdditionalInfo.targetName shouldBe "TargetClass"
        actualAdditionalInfo.defaults.size shouldBe 0
    }

    "resolve should return correct AutoMapperInfo when code is empty in default" {
        val classDeclaration = mockk<KSClassDeclaration>()
        val autoMapperAnnotation = mockk<KSAnnotation>()
        val autoMapperAnnotationName = mockk<KSName>()

        every {
            autoMapperAnnotation.shortName
        } returns autoMapperAnnotationName

        every {
            autoMapperAnnotationName.asString()
        } returns "AutoMapper"

        every {
            classDeclaration.annotations
        } returns listOf(autoMapperAnnotation).asSequence()

        val autoMapperTargetArgumentName = mockk<KSName>().apply {
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }
        val autoMapperDefaultsArgumentName = mockk<KSName>().apply {
            every { asString() } returns "defaults"
            every { getShortName() } returns "defaults"
        }

        val autoMapperTargetKSType = mockk<KSType>().apply {
            every { declaration.packageName.asString() } returns "com.example"
            every { declaration.simpleName.asString() } returns "TargetClass"
        }

        val defaultKSName = mockk<KSName>().apply {
            every { asString() } returns "defaults"
        }

        val defaultTargetArgumentName = mockk<KSName>().apply {
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }

        val defaultCodeArgumentName = mockk<KSName>().apply {
            every { asString() } returns "code"
            every { getShortName() } returns "code"
        }

        val defaultAnnotationsArguments = listOf(
            mockk<KSValueArgument>().apply {
                every { name } returns defaultTargetArgumentName
                every { value } returns "otherProperty"
            },
            mockk<KSValueArgument>().apply {
                every { name } returns defaultCodeArgumentName
                every { value } returns ""
            }
        )

        val defaultAnnotations = listOf(
            mockk<KSAnnotation>().apply {
                every { shortName } returns defaultKSName
                every { arguments } returns defaultAnnotationsArguments
            }
        )

        val autoMapperAnnotationArguments = listOf(
            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperTargetArgumentName
                every { value } returns autoMapperTargetKSType
            },
            mockk<KSValueArgument>().apply {
                every { name } returns autoMapperDefaultsArgumentName
                every { value } returns ArrayList(defaultAnnotations)
            }
        )

        every {
            autoMapperAnnotation.arguments
        } returns autoMapperAnnotationArguments


        val actualAdditionalInfo = resolver.resolve(classDeclaration)
        actualAdditionalInfo.targetPackage shouldBe "com.example"
        actualAdditionalInfo.targetName shouldBe "TargetClass"
        actualAdditionalInfo.defaults.size shouldBe 0
    }
})
