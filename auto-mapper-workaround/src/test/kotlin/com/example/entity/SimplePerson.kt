package com.example.entity

import io.github.rmorenko.automapper.annotations.AddTargetProperty
import io.github.rmorenko.automapper.annotations.GenerateTarget
import io.github.rmorenko.automapper.annotations.TargetPropertyExclude

@GenerateTarget(pkg = "com.example.dto",
    props = [AddTargetProperty(name = "surname", pkg = "kotlin", className = "String")],
    name = "SimplePersonDto")

data class SimplePerson(
    val name: String,
    val age: Int,
    @TargetPropertyExclude
    val child: SimplePerson
)
