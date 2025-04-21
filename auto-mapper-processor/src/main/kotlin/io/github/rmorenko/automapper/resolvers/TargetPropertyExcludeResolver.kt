package io.github.rmorenko.automapper.resolvers

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.github.rmorenko.automapper.annotations.TargetPropertyExclude

/**
 * Class responsible for reading `@TargetPropertyExclude` annotations
 *
 * @property logger Logger for logging information during the resolution process.
 */
class TargetPropertyExcludeResolver(private val logger: KSPLogger) {

    /**
     * Checks that property is annotated with `@TargetPropertyExclude`
     * @param ksPropertyDeclaration - property declaration
     * @return true if property annotated with `@TargetPropertyExclude` annotation
     */
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
