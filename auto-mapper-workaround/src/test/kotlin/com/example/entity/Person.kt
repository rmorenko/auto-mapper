package com.example.entity

import com.example.address.Address
import io.github.rmorenko.automapper.annotations.AddTargetProperty
import io.github.rmorenko.automapper.annotations.GenerateTarget
import io.github.rmorenko.automapper.annotations.GenerateTargetPropertyType
import io.github.rmorenko.automapper.annotations.TargetPropertyExclude

@GenerateTarget(pkg = "com.example", name = "PersonDto",
    props = [AddTargetProperty(name = "add", pkg = "kotlin", className = "String")])
data class Person(val name: String,
                  @TargetPropertyExclude
                  val age: Int,
                  @GenerateTargetPropertyType(pkg = "com.example.address", className = "AddressDto")
                  val address: Address
)
