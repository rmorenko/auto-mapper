package com.example.entity

import com.example.dto.DocumentDto
import io.github.rmorenko.automapper.annotations.AutoMapper

@AutoMapper(target = DocumentDto::class)
data class Document(
    val name: String,
    val description: String
)
