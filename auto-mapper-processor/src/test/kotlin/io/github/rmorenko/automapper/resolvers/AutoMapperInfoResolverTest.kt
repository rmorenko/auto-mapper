package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.Mapping
import io.github.rmorenko.automapper.exceptions.AnnotationNotPresent
import io.github.rmorenko.automapper.exceptions.MultiplyAnnotationException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.Serial

private val annotationSimpleName =  AutoMapper::class.simpleName.toString()
private val annotationFullName =  AutoMapper::class.qualifiedName.toString()

class AutoMapperInfoResolverTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)


    "resolve should failed without AutoMapperAnnotation" {
        val autoMapperInfoResolver = AutoMapperInfoResolver(logger)
        val classDeclaration = mockk<KSClassDeclaration>()
        val someAnnotation = mockk<KSAnnotation>()
        val someAnnotationName = mockk<KSName>()

        every {
            someAnnotationName.asString()
        } returns Serial::class.simpleName.toString()

        every {
            someAnnotation.shortName
        } returns someAnnotationName

        every {
            classDeclaration.annotations
        } returns listOf(someAnnotation).asSequence()
        shouldThrowExactly<AnnotationNotPresent> {
            autoMapperInfoResolver.resolve(classDeclaration)
        }
    }

    "resolve should failed with multiply AutoMapperAnnotation" {
        val autoMapperInfoResolver = AutoMapperInfoResolver(logger)
        val classDeclaration = mockk<KSClassDeclaration> {
            every {
                simpleName.asString()
            } returns "TargetClass"
            every {
                qualifiedName?.asString()
            } returns "com.example.TargetClass"
        }
        val mappingAnnotationOne = mockk<KSAnnotation>()
        val mappingAnnotationTwo = mockk<KSAnnotation>()
        val annotationKSName = mockk<KSName>()
        val fullAnnotationKSName = mockk<KSName>()

        every {
            annotationKSName.asString()
        } returns annotationSimpleName

        every {
            fullAnnotationKSName.asString()
        } returns annotationFullName

        every {
            mappingAnnotationOne.shortName
            mappingAnnotationTwo.shortName
        } returns annotationKSName

        every {
            mappingAnnotationOne.annotationType.resolve().declaration.qualifiedName
            mappingAnnotationTwo.annotationType.resolve().declaration.qualifiedName
        } returns fullAnnotationKSName

        every {
            classDeclaration.annotations
        } returns listOf(mappingAnnotationTwo, mappingAnnotationTwo).asSequence()
        shouldThrowExactly<MultiplyAnnotationException> {
            autoMapperInfoResolver.resolve(classDeclaration)
        }
    }

    "resolve should return correct AutoMapperInfo when only target argument present" {
        val autoMapperInfoResolver = AutoMapperInfoResolver(logger)

        val classDeclaration = mockk<KSClassDeclaration>()
        val autoMapperAnnotation = mockk<KSAnnotation>()
        val autoMapperAnnotationKSName = mockk<KSName>()
        val fullAutoMapperAnnotationKSName = mockk<KSName>()

        every {
            autoMapperAnnotation.shortName
        } returns autoMapperAnnotationKSName

        every {
            autoMapperAnnotation.annotationType.resolve().declaration.qualifiedName
        } returns fullAutoMapperAnnotationKSName

        every {
            fullAutoMapperAnnotationKSName.asString()
        } returns annotationFullName


        every {
            autoMapperAnnotationKSName.asString()
        } returns annotationSimpleName

        every {
            classDeclaration.qualifiedName?.asString()
        } returns "com.example.SourceClass"

        every {
            classDeclaration.annotations
        } returns listOf(autoMapperAnnotation).asSequence()

        val autoMapperTargetArgumentName = mockk<KSName>{
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }

        val autoMapperTargetKSType = mockk<KSType>{
            every { declaration.packageName.asString() } returns "com.example"
            every { declaration.simpleName.asString() } returns "TargetClass"
            every { declaration.qualifiedName?.asString() } returns "com.example.TargetClass"
        }

        val autoMapperAnnotationArguments = listOf(
            mockk<KSValueArgument>{
                every { name } returns autoMapperTargetArgumentName
                every { value } returns autoMapperTargetKSType
            }
        )

        every {
            autoMapperAnnotation.arguments
        } returns autoMapperAnnotationArguments


        val actualAdditionalInfo = autoMapperInfoResolver.resolve(classDeclaration)
        actualAdditionalInfo.targetPackage shouldBe "com.example"
        actualAdditionalInfo.targetName shouldBe "TargetClass"
        actualAdditionalInfo.excludes.size shouldBe 0
        actualAdditionalInfo.additionalImports.size shouldBe 0
        actualAdditionalInfo.defaults.size shouldBe 0
    }


    "resolve should return correct AutoMapperInfo" {
        val autoMapperInfoResolver = AutoMapperInfoResolver(logger)
        val resolver = mockk<Resolver>()
        val classDeclaration = mockk<KSClassDeclaration>()
        val autoMapperAnnotation = mockk<KSAnnotation>()
        val autoMapperAnnotationKSName = mockk<KSName>()
        val fullAutoMapperAnnotationKSName = mockk<KSName>()

        every {
            autoMapperAnnotation.shortName
        } returns autoMapperAnnotationKSName

        every {
            autoMapperAnnotationKSName.asString()
        } returns annotationSimpleName

        every {
            autoMapperAnnotation.annotationType.resolve().declaration.qualifiedName
        } returns fullAutoMapperAnnotationKSName

        every {
            fullAutoMapperAnnotationKSName.asString()
        } returns annotationFullName

        every {
            autoMapperAnnotation.annotationType.resolve().declaration.qualifiedName
        } returns fullAutoMapperAnnotationKSName

        every {
            fullAutoMapperAnnotationKSName.asString()
        } returns annotationFullName

        every {
            classDeclaration.qualifiedName?.asString()
        } returns "com.example.SourceClass"

        every {
            classDeclaration.annotations
        } returns listOf(autoMapperAnnotation).asSequence()

        val autoMapperTargetArgumentName = mockk<KSName>{
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }
        val autoMapperDefaultsArgumentName = mockk<KSName>{
            every { asString() } returns "defaults"
            every { getShortName() } returns "defaults"
        }

        val autoMapperImportsArgumentName = mockk<KSName>{
            every { asString() } returns "imports"
            every { getShortName() } returns "imports"
        }

        val autoMapperExcludeArgumentName = mockk<KSName>{
            every { asString() } returns "exclude"
            every { getShortName() } returns "exclude"
        }

        val autoMapperTargetKSType = mockk<KSType>{
            every { declaration.packageName.asString() } returns "com.example"
            every { declaration.simpleName.asString() } returns "TargetClass"
            every { declaration.qualifiedName?.asString() } returns "com.example.TargetClass"
        }

        val defaultKSName = mockk<KSName>{
            every { asString() } returns "defaults"
        }

        val defaultTargetArgumentName = mockk<KSName>{
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }

        val defaultCodeArgumentName = mockk<KSName>{
            every { asString() } returns "code"
            every { getShortName() } returns "code"
        }

        val defaultAnnotationsArguments = listOf(
            mockk<KSValueArgument>{
                every { name } returns defaultTargetArgumentName
                every { value } returns "otherProperty"
            },
            mockk<KSValueArgument>{
                every { name } returns defaultCodeArgumentName
                every { value } returns "default code"
            }
        )

        val defaultAnnotations = listOf(
            mockk<KSAnnotation>{
                every { shortName } returns defaultKSName
                every { arguments } returns defaultAnnotationsArguments
            }
        )

        val autoMapperAnnotationArguments = listOf(
            mockk<KSValueArgument>{
                every { name } returns autoMapperTargetArgumentName
                every { value } returns autoMapperTargetKSType
            },
            mockk<KSValueArgument>{
                every { name } returns autoMapperDefaultsArgumentName
                every { value } returns ArrayList(defaultAnnotations)
            },

            mockk<KSValueArgument>{
                every { name } returns autoMapperImportsArgumentName
                every { value } returns ArrayList(listOf("com.import.a", "com.import.b"))
            },

            mockk<KSValueArgument>{
                every { name } returns autoMapperExcludeArgumentName
                every { value } returns ArrayList(listOf("excludeA", "excludeB"))
            }
        )

        every {
            autoMapperAnnotation.arguments
        } returns autoMapperAnnotationArguments

        every {
            resolver.getSymbolsWithAnnotation(Mapping::class.simpleName.toString())
        } returns emptyList<KSClassDeclaration>().asSequence()

        val actualAdditionalInfo = autoMapperInfoResolver.resolve(classDeclaration)
        actualAdditionalInfo.targetPackage shouldBe "com.example"
        actualAdditionalInfo.targetName shouldBe "TargetClass"
        actualAdditionalInfo.excludes shouldBe listOf("excludeA", "excludeB")
        actualAdditionalInfo.additionalImports shouldBe listOf("com.import.a", "com.import.b")
        actualAdditionalInfo.defaults.size shouldBe 1
        actualAdditionalInfo.defaults.first().code shouldBe "default code"
        actualAdditionalInfo.defaults.first().target shouldBe "otherProperty"
    }


    "resolve should return correct AutoMapperInfo when code is empty in default" {
        val autoMapperInfoResolver = AutoMapperInfoResolver(logger)
        val resolver = mockk<Resolver>()
        val classDeclaration = mockk<KSClassDeclaration>()
        val autoMapperAnnotation = mockk<KSAnnotation>()
        val autoMapperAnnotationKSName = mockk<KSName>()
        val fullAutoMapperAnnotationKSName = mockk<KSName>()

        every {
            autoMapperAnnotation.shortName
        } returns autoMapperAnnotationKSName

        every {
            autoMapperAnnotationKSName.asString()
        } returns annotationSimpleName

        every {
            autoMapperAnnotation.annotationType.resolve().declaration.qualifiedName
        } returns fullAutoMapperAnnotationKSName

        every {
            fullAutoMapperAnnotationKSName.asString()
        } returns annotationFullName

        every {
            classDeclaration.qualifiedName?.asString()
        } returns "com.example.SourceClass"

        every {
            classDeclaration.annotations
        } returns listOf(autoMapperAnnotation).asSequence()

        val autoMapperTargetArgumentName = mockk<KSName>{
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }
        val autoMapperDefaultsArgumentName = mockk<KSName>{
            every { asString() } returns "defaults"
            every { getShortName() } returns "defaults"
        }

        val autoMapperTargetKSType = mockk<KSType>{
            every { declaration.packageName.asString() } returns "com.example"
            every { declaration.simpleName.asString() } returns "TargetClass"
            every { declaration.qualifiedName?.asString() } returns "com.example.TargetClass"
        }

        val defaultKSName = mockk<KSName>{
            every { asString() } returns "defaults"
        }

        val defaultTargetArgumentName = mockk<KSName>{
            every { asString() } returns "target"
            every { getShortName() } returns "target"
        }

        val defaultCodeArgumentName = mockk<KSName>{
            every { asString() } returns "code"
            every { getShortName() } returns "code"
        }

        val defaultAnnotationsArguments = listOf(
            mockk<KSValueArgument>{
                every { name } returns defaultTargetArgumentName
                every { value } returns "otherProperty"
            },
            mockk<KSValueArgument>{
                every { name } returns defaultCodeArgumentName
                every { value } returns ""
            }
        )

        val defaultAnnotations = listOf(
            mockk<KSAnnotation>{
                every { shortName } returns defaultKSName
                every { arguments } returns defaultAnnotationsArguments
            }
        )

        val autoMapperAnnotationArguments = listOf(
            mockk<KSValueArgument>{
                every { name } returns autoMapperTargetArgumentName
                every { value } returns autoMapperTargetKSType
            },
            mockk<KSValueArgument>{
                every { name } returns autoMapperDefaultsArgumentName
                every { value } returns ArrayList(defaultAnnotations)
            }
        )

        every {
            autoMapperAnnotation.arguments
        } returns autoMapperAnnotationArguments
        every {
            resolver.getSymbolsWithAnnotation(Mapping::class.simpleName.toString())
        } returns listOf(
            mockk<KSClassDeclaration> {
                every {
                    packageName.asString()
                } returns "com.example.target"

                every {
                    simpleName.asString()
                } returns "TargetProperty"
            }


        ).asSequence()
        val actualAdditionalInfo = autoMapperInfoResolver.resolve(classDeclaration)
        actualAdditionalInfo.targetPackage shouldBe "com.example"
        actualAdditionalInfo.targetName shouldBe "TargetClass"
        actualAdditionalInfo.defaults.size shouldBe 0
    }
})
