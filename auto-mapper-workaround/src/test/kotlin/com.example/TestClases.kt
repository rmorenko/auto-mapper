package com.example

import com.morenko.automapper.AutoMapper

@AutoMapper(Entity::class)
data class Dto(
    val id: Int,
    val name: String,
    val status: String,
    val nested: NestedDto // ✅ Вложенный объект автоматически преобразуется
)

@AutoMapper(NestedEntity::class)
data class NestedDto(val code: String)

data class Entity(val id: Int, val name: String, val status: String, val nested: NestedEntity)
data class NestedEntity(val code: String)
