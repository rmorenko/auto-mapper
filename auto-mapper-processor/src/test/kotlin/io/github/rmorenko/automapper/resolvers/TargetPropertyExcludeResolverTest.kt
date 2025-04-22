package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.TargetPropertyExclude
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk


class TargetPropertyExcludeResolverTest : StringSpec({
    val logger = mockk<KSPLogger>(relaxed = true)
    val targetPropertyExcludeResolver = TargetPropertyExcludeResolver(logger)

    "resolve should return false if no any annotation on property" {
        val ksPropertyDeclaration = mockk<KSPropertyDeclaration>() {
            every {
                simpleName.asString()
            } returns "property"
            every {
                annotations
            } returns emptyList<KSAnnotation>().asSequence()
        }
        targetPropertyExcludeResolver.resolve(ksPropertyDeclaration) shouldBe false
    }

    "resolve should return false if no TargetPropertyExclude annotation on property" {
        val ksPropertyDeclaration = mockk<KSPropertyDeclaration>() {
            every {
                simpleName.asString()
            } returns "property"
            every {
                annotations
            } returns listOf(
                mockk<KSAnnotation> {
                    every { shortName.asString() } returns AutoMapper::class.simpleName.toString()
                    every { annotationType.resolve().declaration.qualifiedName?.asString()
                    } returns AutoMapper::class.qualifiedName.toString()
                }
            ).asSequence()
        }
        targetPropertyExcludeResolver.resolve(ksPropertyDeclaration) shouldBe false
    }

    "resolve should return true if TargetPropertyExclude annotation on property presents" {
        val ksPropertyDeclaration = mockk<KSPropertyDeclaration>() {
            every {
                simpleName.asString()
            } returns "property"
            every {
                annotations
            } returns listOf(
                mockk<KSAnnotation> {
                   every { shortName.asString() } returns TargetPropertyExclude::class.simpleName.toString()
                   every { annotationType.resolve().declaration.qualifiedName?.asString()
                   } returns TargetPropertyExclude::class.qualifiedName.toString()
                }
            ).asSequence()
        }
        targetPropertyExcludeResolver.resolve(ksPropertyDeclaration) shouldBe true
    }
})
