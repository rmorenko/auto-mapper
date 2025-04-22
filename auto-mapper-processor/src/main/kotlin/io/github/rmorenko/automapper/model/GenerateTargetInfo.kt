package io.github.rmorenko.automapper.model

data class GenerateTargetInfo (
    val properties: List<TargetPropertyInfo>,
    val packageName: String,
    val name: String
)
