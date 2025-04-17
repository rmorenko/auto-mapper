package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.github.rmorenko.automapper.annotations.TargetPropertyExclude

class TargetPropertyExcludeResolver(private val logger: KSPLogger) {

    fun resolve(ksPropertyDeclaration: KSPropertyDeclaration): Boolean {
        return ksPropertyDeclaration.annotations.any { ksAnnotation ->
            ksAnnotation.shortName.asString() == TargetPropertyExclude::class.simpleName &&
                    ksAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() ==
                    TargetPropertyExclude::class.qualifiedName
        }.also {
            logger.info("Target property ${ksPropertyDeclaration.simpleName.asString()} excluded: $it")
        }
    }
}
