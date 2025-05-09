# AutoMapper Project

Welcome to the documentation for the AutoMapper project. This project is designed to automatically generate mapping 
functions for Kotlin data classes. 

## Author
My [LinkedIn](https://www.linkedin.com/in/roman-morenko-5091b6258/) profile.
For comments, ideas and suggestions: [email me](mailto:morenko83@gmail.com) | [te.me](https://t.me/roman_morenko)

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
    implementation(group = "io.github.rmorenko",  name ="auto-mapper-processor", version  = "$version")
    ksp(group = "io.github.rmorenko",  name ="auto-mapper-processor",
        classifier = "jar-with-dependencies",
        version  = "$version")
}
```

or to  _build.gradle_

```groovy
dependencies {
    implementation(group: "io.github.rmorenko", name: "auto-mapper-processor", version: "$version")
    ksp(group: "io.github.rmorenko", name: "auto-mapper-processor", classifier: "jar-with-dependencies", version: "$version")
}
```

### Maven
If you’re using Maven to build your project add the following to your _pom.xml_
```xml
<dependencies>
    <dependency>
        <groupId>io.github.rmorenko</groupId>
        <artifactId>auto-mapper-processor</artifactId>
        <classifier>jar-with-dependencies</classifier>
        <version>$version</version>
    </dependency>
    <dependency>
        <groupId>com.dyescape</groupId>
        <artifactId>kotlin-maven-symbol-processing</artifactId>
        <version>${ksp.version}</version>
    </dependency>
</dependencies>
```
Then configure the kotlin-maven-plugin to use the ksp compiler plugin:

```xml
<configuration>
    <compilerPlugins>
        <compilerPlugin>ksp</compilerPlugin>
    </compilerPlugins>
</configuration>
```

### Defining a mapper
#### Simple case
For generating mapping function just add annotation @AutoMapper to source class and set to _target_
argument target class. If classes have the same properties it will be enough.

```kotlin
package com.example.entity

import com.example.dto.Dto
import io.github.rmorenko.automapper.annotations.AutoMapper

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
You will find in _build/generated/ksp/main_ folder package _com.example.entity_ with 
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
import io.github.rmorenko.automapper.annotations.AutoMapper

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
Generated _EntityMapper.kt_ file will be:

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
import io.github.rmorenko.automapper.annotations.AutoMapper

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
Generated _EntityMapper.kt_ file will be:
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
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.Default

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
Generated _EntityMapper.kt_ file will be:
```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
name = this.name,
title = this.name + "_title"
)
```
#### Mapping annotation for customize mapping logic
For customizing logic for mapping  individual properties you can use _Mapping_ 
annotation on properties in source class
##### Simple case
If you need to map properties with different names just add _Mapping_ annotation 
on property in source class. In _target_ property set name of property in target class:

```kotlin
package com.example.entity

import com.example.dto.Dto
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.Mapping

@AutoMapper(target = Dto::class)
data class Entity(
@Mapping(target = "nam")
val name: String
)
```
```kotlin
package com.example.dto

data class Dto(
val nam: String
)
```
Generated _EntityMapper.kt_ file will be:

```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
nam = this.name
)
```
##### Use transform property for applying functions
You can apply functions on property values in generated mapping code. Add _transform_
property to _Mapping_ with function name. You can define functions in same file as
target class defined or add import in _Mapping_ annotation. In example below we can
see standard extension function _uppercase_ for _kotlin.String_ and two defined 
functions. _String.addPrefix_ is extension function and _addSuffix_ is simple 
toplevel function.

```kotlin
package com.example.entity

import com.example.dto.Dto
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.Mapping

@AutoMapper(target = Dto::class)
data class Entity(
@Mapping(target = "nam", transform="String.uppercase")
val name: String,
@Mapping(transform="addSuffix")
val title: String,
@Mapping(transform="String.addPrefix")
val description: String
)

fun addSuffix(arg: String) = "${arg}_suffix"

fun String.addPrefix() = "prefix_${this}"
```
```kotlin
package com.example.dto

data class Dto(
val nam: String,
val title: String,
val description: String
)
```
Generated _EntityMapper.kt_ file will be:
```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
nam = this.name.uppercase(),
title = addSuffix(this.title),
description = this.description.addPrefix()
)
```
##### Use code property for execute custom code snippet
You can execute custom code blocks in _Mapping_ annotation. Add _code_ property 
with custom code. And result for this code map to target property value. 
In example below for property _name_defined simple expression, that concatenate
_name_ and _description_ values. For property _description_ we define more 
complex snippet, that contains two expressions:

```kotlin
package com.example.entity

import com.example.dto.Dto
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.Mapping


@AutoMapper(target = Dto::class)
data class Entity(
@Mapping(code = "this.name + this.description")
val name: String,
@Mapping(code = """
val a = 0;
this.description + a;
""")
val description: String
)
```
<code style="color : red">Important!! Before version _1.0.2_ if code contains more than one expression, 
you should wrap it with _{ ... }.invoke()_</code>


```kotlin
package com.example.entity

import com.example.dto.Dto
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.Mapping


@AutoMapper(target = Dto::class)
data class Entity(
@Mapping(code = "this.name + this.description")
val name: String,
@Mapping(code = """
val a = 0;
this.description + a;
}.invoke()
""")
val description: String
)
```


```kotlin
package com.example.dto

data class Dto(
val name: String,
val description: String
)
```

Generated _EntityMapper.kt_ file will be:
```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
name = this.name + this.description,
description = { val a = 0;
this.description + a;
}.invoke()
)
```
You can combine _transform_ and _code_ properties in _Mapping_ annotation;
Function that defined in _transform_ will apply to result of expression defined
in _code_:

```kotlin
package com.example.entity

import com.example.dto.Dto
import io.github.rmorenko.automapper.annotations.AutoMapper
import io.github.rmorenko.automapper.annotations.Mapping

@AutoMapper(target = Dto::class)
data class Entity(
val name: String,
@Mapping(code = """
{ val a = 0;
this.description + a;
}.invoke()
""", transform = "String.lowercase")
val description: String
)
```
```kotlin
package com.example.dto

data class Dto(
    val name: String,
    val description: String
)
```
Generated _EntityMapper.kt_ file will be:

```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
name = this.name,
description = { val a = 0;
this.description + a;
}.invoke().lowercase()
)
```
#### Mapping properties with custom types
For correct mapping properties with custom types you should also add _Mapping_
to all custom classes. In example below defined _Entity_ class with property
error with custom type Error.

```kotlin
package com.example.entity

import com.example.dto.Dto
import io.github.rmorenko.automapper.annotations.AutoMapper

@AutoMapper(target = Dto::class)
data class Entity(
val name: String,
val error: Error
)
```
```kotlin
package com.example.entity

import com.example.dto.ErrorDto
import io.github.rmorenko.automapper.annotations.AutoMapper

@AutoMapper(target = ErrorDto::class)
data class Error (
val code: String,
val message: String
)
```
Also Dto and ErrorDto defined:

```kotlin
package com.example.dto

data class Dto(
val name: String,
val error: ErrorDto
)
```
```kotlin
package com.example.dto

data class ErrorDto (
    val code: String,
    val message: String
)
```
In _com.example.entity_ package will generate two files
_EntityMapper.kt_ and _ErrorMapper.kt_:

```kotlin
package com.example.entity

import com.example.dto.Dto

public fun Entity.mapEntityToDto(): Dto = Dto(
name = this.name,
error = this.error.mapErrorToErrorDto()
)
```

```kotlin
package com.example.entity

import com.example.dto.ErrorDto

public fun Error.mapErrorToErrorDto(): ErrorDto = ErrorDto(
    code = this.code,
    message = this.message
)
```
In _ErrorMapper.kt_ generated nested mapping function

### Generating target class
In release _1.0.2_ was added new feature - generating target classes. It is useful when
you need generate class with mostly the same properties. In simple case just add _GenerateTarget_
annotation with name of generated class in _name_ argument:

```kotlin
package com.example.entity

import io.github.rmorenko.automapper.annotations.GenerateTarget

@GenerateTarget(name = "SimplePersonDto")
data class SimplePerson(
val name: String,
val age: Int
)
```
Generated class will be in same package source class
```kotlin
package com.example.entity

import kotlin.Int
import kotlin.String

public data class SimplePersonDto(
public val name: String,
public val age: Int,
)
```
#### GenerateTarget, set package name.
For declare package name you can use _pkg_ argument in _GenerateTarget_
```kotlin
package com.example.entity

import io.github.rmorenko.automapper.annotations.GenerateTarget

@GenerateTarget(pkg = "com.example.dto", name = "SimplePersonDto")
data class SimplePerson(
val name: String,
val age: Int
)
```
The result will be
```kotlin
package com.example.dto

import kotlin.Int
import kotlin.String

public data class SimplePersonDto(
  public val name: String,
  public val age: Int,
)
```

#### GenerateTarget, add additional properties.
To add additional properties to generated target class use _props_ argument in _GenerateTarget_
annotation. _Props_ argument is the list of _AddTargetProperty_ with three arguments
* name - name of property, required
* pkg - package of property type, not required (if pkg is not set takes package name from source class)
* className - class name of property type, required
```kotlin
package com.example.entity

import io.github.rmorenko.automapper.annotations.AddTargetProperty
import io.github.rmorenko.automapper.annotations.GenerateTarget

@GenerateTarget(pkg = "com.example.dto",
    props = [AddTargetProperty(name = "surname", pkg = "kotlin", className = "String")],
    name = "SimplePersonDto")

data class SimplePerson(
    val name: String,
    val age: Int
)

```
Result will be:
```kotlin
package com.example.dto

import kotlin.Int
import kotlin.String

public data class SimplePersonDto(
  public val name: String,
  public val age: Int,
  public val surname: String,
)
```
#### GenerateTarget, how to exclude properties in generated class
For excluding properties in generated target class you can use _TargetPropertyExclude_ 
annotation on property which you want to exclude.
```kotlin
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
```
Property _child_ will exclude from generated class:
```kotlin
package com.example.dto

import kotlin.Int
import kotlin.String

public data class SimplePersonDto(
  public val name: String,
  public val age: Int,
  public val surname: String,
)
```
#### GenerateTarget, how to customize property types in generated classes
For changing property type in generating class yoy can use _GenerateTargetPropertyType_
annotation on properties which type you want to set. Annotation _GenerateTargetPropertyType_ has
two arguments _pkg_ and _className_. Argument _pkg_ is not required, if it is not set package name 
will take from source class.

```kotlin
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
```
Nested class declaration: 
```kotlin
package com.example.address

import kotlin.String

public data class AddressDto(
public val city: String,
public val country: String,
)
```
Result:
```kotlin
package com.example

import com.example.address.AddressDto
import kotlin.String

public data class PersonDto(
  public val name: String,
  public val address: AddressDto,
  public val add: String,
)

```
## API documentation for last release
[KDoc ](https://rmorenko.github.io/auto-mapper/)