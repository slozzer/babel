# Babel

> String-based internationalization (i18n) for Scala applications

## Features

- First class support for plurals
- Great fit for Scala.js clients, since `java.util.Locale` is not used
- Translation definitions in either code or a supported serialization format (e.g. JSON)
- Allows lifting translations into data classes: no more `String` key look-ups
- Creating translation subsets that are only suitable for a specific language, which can be useful for clients that request all translations for a single language 
- Open to arbitrary `String` formatting solutions (e.g. `String.format` or `java.util.MessageFormat`)
- Dependency-free `core` module

## Installation

Dependency-free core module, that contains all data class definitions, type classes and pre-defined locales

```scala
"io.taig" %%% "babel-core" % "x.y.z" 
```
JSON serialization and deserialization

```scala
"io.taig" %%% "babel-circe" % "x.y.z"
```

Reading serialized language definitions from resources (JVM only)

```scala
"io.taig" %% "babel-loader" % "x.y.z"
```

Codecs to populate custom translation data classes

```scala
"io.taig" %%% "babel-generic" % "x.y.z"
```

String formatting choices, pick one or role your own

> Please note that `java.util.MessageFormat` is not available for Scala.js, so the `printf` module is recommended

```scala
"io.taig" %%% "babel-formatter-printf" % "x.y.z"
"io.taig" %% "babel-formatter-message-format" % "x.y.z"
```

Default setup which is assumed in the documentation below

```scala
"io.taig" %% "babel-loader" % "x.y.z"
"io.taig" %%% "babel-circe" % "x.y.z"
"io.taig" %%% "babel-generic" % "x.y.z"
"io.taig" %%% "babel-formatter-printf" % "x.y.z"
```

## Overview

### Data classes

- `Locale`, `Language`, `Country`  
Basic data classes in the fashion of `java.util.Locale`. It is possible to convert between `java.util.Locale` and `io.taig.babel.Locale`.
- `Text`, `Quantity`  
`Text` holds the actual `String` value of a translation as well as a `Map` of `Quantity` to describe plural forms
- `Segments`, `Path`  
`Segments` is a tree structure that mimics nested case classes. A `Path` can be used to identify a `Node` or a `Leaf` in such a tree.
- `Dictionary`  
A collection of `Text`s that are namespaced in `Segments`, so basically only translations of a single language,
- `Translation`  
A collection of `Text`s with their respective `Locale`s. This is the translation of one particular phrase into several languages.
- `Babel`  
The collection of all translations: namespaced in `Segment`s with `Translation`s in the leaves.

### Type classes

- `Formatter`: `(String, Seq[Any]) => String`  
String formatting instances but no default is provided. Depend on either `babel-formatter-printf` or `babel-formatter-message-format` and import the instance explicitly.
- `Printer`: `A => String`
- `Parser`: `String => Either[Error, A]`  
- `Encoder`: `F[A] => Segments[A]`  
- `Decoder`: `Segments[A] => Either[Error, F[A]]`  

## Guide

### Defining translations in JSON files

For each language a separate file must be created in the `resources/babel` folder and the name of the `Locale` as filename.

`resources/en.json`

```json
{
  "greeting": "Good afternoon"
}
```