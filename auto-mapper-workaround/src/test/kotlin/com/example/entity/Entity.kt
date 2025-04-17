package com.example.entity

import com.example.Box

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
