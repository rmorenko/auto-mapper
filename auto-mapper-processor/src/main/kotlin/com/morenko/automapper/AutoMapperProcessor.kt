package com.morenko.automapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class AutoMapperProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {

    private val processedClasses = mutableSetOf<String>() // ✅ Храним обработанные классы
    private val generatedMappings = mutableMapOf<String, String>() // ✅ Запоминаем созданные функции маппинга

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(AutoMapper::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        // ✅ Логируем найденные классы
        println("Обнаружены аннотированные классы: ${symbols.map { it.simpleName.asString() }}")

        // ✅ Сначала генерируем вложенные классы
        symbols.forEach { symbol ->
            symbol.getAllProperties().forEach { prop ->
                val propType = prop.type.resolve()
                val isAutoMapped = propType.declaration.annotations.any {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == AutoMapper::class.qualifiedName
                }
                if (isAutoMapped) {
                    generateMappingFunction(propType.declaration as KSClassDeclaration)
                }
            }
        }

        // ✅ Затем генерируем мапперы для основных DTO
        symbols.forEach { generateMappingFunction(it) }

        return emptyList()
    }

    private fun generateMappingFunction(classDeclaration: KSClassDeclaration) {
        val dtoName = classDeclaration.simpleName.asString()

        // ✅ Проверяем, обрабатывался ли уже этот класс
        if (processedClasses.contains(dtoName)) return
        processedClasses.add(dtoName)

        // ✅ Получаем правильный `Entity` для `Dto`
        val entityType = classDeclaration.annotations
            .firstOrNull { it.shortName.asString() == "AutoMapper" }
            ?.arguments?.firstOrNull()?.value as? KSType
            ?: return

        val entityName = entityType.declaration.simpleName.asString()
        val entityPackage = entityType.declaration.packageName.asString()

        val functionName = "map${dtoName}To${entityName}"
        val fileName = "${dtoName}_Mapper"

        val functionBuilder = FunSpec.builder(functionName)
            .receiver(classDeclaration.toClassName())
            .returns(ClassName(entityPackage, entityName))

        // ✅ Проверяем, есть ли вложенные объекты с `@AutoMapper`
        val propertyMappings = classDeclaration.getAllProperties().joinToString { prop ->
            val propName = prop.simpleName.asString()
            val propType = prop.type.resolve()

            // ✅ Проверяем, является ли тип примитивным
            val isPrimitive = propType.declaration.qualifiedName?.asString() in listOf(
                "kotlin.Int", "kotlin.String", "kotlin.Boolean", "kotlin.Long", "kotlin.Double"
            )

            val targetEntityName = (propType.declaration as? KSClassDeclaration)?.let { classDecl ->
                val autoMapperAnnotation = classDecl.annotations.firstOrNull { it.shortName.asString() == "AutoMapper" }
                val mappedType = autoMapperAnnotation?.arguments?.firstOrNull()?.value as? KSType
                mappedType?.declaration?.simpleName?.asString()
            } ?: "UNKNOWN"

            // ✅ Если тип примитивный, просто присваиваем значение
            if (isPrimitive) {
                "$propName = this.$propName"
            } else run {
                "$propName = this.$propName.map${propType.toClassName().simpleName}To$targetEntityName()"
            }
        }

        functionBuilder.addStatement("return $entityName($propertyMappings)")

        // ✅ Создаём файл для маппера
        val file = FileSpec.builder("generated.mapper", fileName)
            .addFunction(functionBuilder.build())
            .build()

        file.writeTo(codeGenerator, Dependencies.ALL_FILES)

        // ✅ Запоминаем сгенерированную функцию
        generatedMappings[dtoName] = functionName
    }
}
