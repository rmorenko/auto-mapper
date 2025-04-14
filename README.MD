# AutoMapper Project

Welcome to the documentation for the AutoMapper project. This project is designed to automatically generate mapping 
functions for data classes.

## What is it?
AutoMapper is a code generator that simplifies the implementation of mappings between Kotlin data classes based on a
convention over configuration approach. The generated mapping code uses function invocations and thus is fast, 
type-safe and easy to understand.

## Why?
Multi-layered applications often require to map between different object models (e.g. entities and DTOs). 
Writing such mapping code is a tedious and error-prone task. AutoMapper aims at simplifying this work by 
automating it as much as possible. AutoMapper generates mappings functions with Kotlin Symbol Processing (KSP)
at compile-time which ensures a high performance, allows for fast developer feedback and thorough error checking.

## How?
AutoMapper is an KSP processor which is plugged into the Kotlin compiler and can be used in command-line builds
(Maven, Gradle etc.)
AutoMapper uses sensible defaults but steps out of your way when it comes to configuring or implementing special behavior.

## Installation

### Gradle
If you’re using Gradle to build your project add the following to your_build.gradle.kts_ to use AutoMapper:

```kotlin
dependencies {
    implementation("com.morenko.automapper:auto-mapper-processor:$version")
    ksp("com.morenko.automapper:auto-mapper-processor:$version")
}
```

or to  _build.gradle_

```groovy
dependencies {
implementation "com.morenko.automapper:auto-mapper-processor:$version"
 ksp "com.morenko.automapper:auto-mapper-processor:$version"
}
```

### Maven
If you’re using Maven to build your project add the following to your _pom.xml_
```xml
<dependencies>
    <dependency>
        <groupId>com.morenko.automapper</groupId>
        <artifactId>auto-mapper-processor</artifactId>
        <version>$version</version>
    </dependency>
</dependencies>
```


### Defining a mapper
#### Simple case
For generating mapping function just add annotation @AutoMapper to source class and set to target 
property class of target class. If classes have the same properties it will be enough.

```kotlin
package com.example.entity

import com.example.dto.Dto
import com.morenko.automapper.annotations.AutoMapper

@AutoMapper(target = Dto::class)
data class Entity(
val name: String
)
```
```kotlin
package com.example.dto

data class Dto(
val name: String
)
```
After you compile code 
```shell 
gradle compileKotlin
```
or 
```shell 
mvn compile
```
you will find in _build/generated/ksp/main_ folder package _com.example.entity_ with 
_EntityMapper.kt_ file:

```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
name = this.name
)
```
#### Exclude properties
Let imagine situation - you have same properties in target and source class and 
properties in target class nullable with null values by default. 
You can exclude target properties from mapping function use _exclude_ property in 
_AutoMapper_ annotation:

```kotlin
package com.example.entity

import com.example.dto.Dto
import com.morenko.automapper.annotations.AutoMapper

@AutoMapper(target = Dto::class, exclude = ["title", "age"])
data class Entity(
    val name: String,
    val title: String,
    val age: Int
)
```

```kotlin
package com.example.dto

data class Dto(
    val name: String,
    val title: String? = null,
    val age: Int? = null
)
```
Generated _EntityMapper.kt_ file will be 

```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
name = this.name
)
```
#### Add imports
You can add additional import in generated mapping file. It can be usefully when
you want to set custom code in mapping functions. This cases describe in sections below.

```kotlin
package com.example.entity

import com.example.dto.Dto
import com.morenko.automapper.annotations.AutoMapper

@AutoMapper(target = Dto::class, imports = ["kotlin.also"])
data class Entity(
val name: String
)
```
```kotlin
package com.example.dto

data class Dto(
val name: String
)
```
Generated _EntityMapper.kt_ file will be
```kotlin
package com.example.entity

import com.example.dto.Dto
import kotlin.also

public fun Entity.mapEntityToDto(): Dto = Dto(
name = this.name
)
```
#### Use defaults
If you have properties that not present in source class you can set values for such properties
using custom code execution. For achieve such behaviour you can use _defaults_ property with
_Default_ annotations. The value _defaults_ property in AutoMapper can contain list of Default
declarations. The _Default_ contains property _target_ for declare the name of property in target
class. For this property will set value that declared in _code_ property:

```kotlin
package com.example.entity

import com.example.dto.Dto
import com.morenko.automapper.annotations.AutoMapper
import com.morenko.automapper.annotations.Default

@AutoMapper(target = Dto::class, defaults = [
Default("title", code = """
this.name + "_title"
""")
])
data class Entity(
val name: String
)
```
```kotlin
package com.example.dto

data class Dto(
val name: String,
val title: String
)
```
Generated _EntityMapper.kt_ file will be
```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
name = this.name,
title = this.name + "_title"
)
```
## API documentation 
[KDoc ](https://rmorenko.github.io/auto-mapper/)