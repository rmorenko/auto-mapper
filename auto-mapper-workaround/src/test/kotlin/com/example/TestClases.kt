package com.example

import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.Default
import io.github.rmorenko.automapper.annotations.Mapping

@AutoMapper(
    target = Entity::class,
    defaults = [Default("box", "Box(1, 2)")],
    imports = ["kotlin.collections.List", "kotlin.collections.Set"],
    exclude = ["summary"]
)
data class Dto(
    @Mapping(transform = "Int.multiply")
    val id: Int,
    @Mapping(transform = "addPrefix")
    val name: String,
    @Mapping(transform = "Int.toString", target = "someAge")
    val age: Int,
    @Mapping(code = "\"Prefix_\" + this.name")
    val status: String,
    val nested: NestedDto,
    val title: String,
    val summary: String,
    @Mapping(code = "", target = "desc")
    val description: String
)

@AutoMapper(NestedEntity::class)
data class NestedDto(
    @Mapping(code = "this.code + \"_postfix\"")
    val code: String
)

data class Entity(
    val id: Int,
    val name: String,
    val status: String,
    val nested: NestedEntity,
    val summary: String? = null,
    val someAge: String,
    val desc: String,
    val box: Box
)

data class NestedEntity(val code: String)

data class Box(val height: Int, val width: Int)

fun addPrefix(string: String): String {

    return "prefix_string$string"
}

fun Int.multiply() = this * 2

