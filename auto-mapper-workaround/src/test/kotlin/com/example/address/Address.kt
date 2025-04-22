package com.example.address

import io.github.rmorenko.automapper.annotations.GenerateTarget

@GenerateTarget(name = "AddressDto")
data class Address(val city:String, val country:String)
