# Clarity Programming Language

## Overview

Clarity is a high-level, object-oriented custom programming language designed to prioritize simplicity and clarity. It features a concise syntax and supports dynamic typing, making it easy to read and write. Clarity is suitable for both beginners and experienced developers who want to create clean, maintainable code.

## Table of Contents

1. [Features](#features)
2. [Basic Syntax](#basic-syntax)
   - [Variables](#variables)
   - [Functions](#functions)
   - [Classes](#classes)
   - [Control Structures](#control-structures)
3. [Native Libraries](#native-libraries)
4. [Examples](#examples)
   - [Simple Program](#simple-program)
   - [Using a Class](#using-a-class)
   - [Looping Over a List](#looping-over-a-list)
   - [Static Properties and Methods](#static-properties-and-methods)
5. [Conclusion](#conclusion)

## Features

- **Object-Oriented:** Support for classes, methods, and properties.
- **Concise Syntax:** Minimalist syntax that enhances readability.
- **Dynamic Typing:** Variables do not require explicit type declarations.
- **Native Libraries:** Provides a standard library with essential functions for I/O operations, mathematical calculations, and error handling.
- **Flexible Main Entry Point:** The program does not require a specific main function to run, similar to Python.

## Basic Syntax

### Variables

Variables are declared using the `var` keyword. They can be initialized at the time of declaration or assigned values later.

```clarity
var x
x = 5

var y = 10
```
## Functions

Functions are defined using the fn keyword. A function can return a value or nothing (void).

```clarity

fn add(a, b) {
    return a + b
}

fn displayMessage() {
    native.println("Hello from Clarity!")
}
```
## Classes

Classes in Clarity are defined using the class keyword and can have properties, a constructor, and methods.

```clarity

class Person {
    var name
    var age

    constructor(name, age) {
        local.name = name
        local.age = age
    }

    fn greet() {
        native.println("My name is " + local.getName() + " and I'm " + local.getAge() + " years old")
    }

    fn getName() {
        return name
    }

    fn getAge() {
        return age
    }
}
```
## Control Structures

Clarity supports common control structures like loops and conditionals.

For Loop:

```clarity

for i : [1, 2, 3] {
    native.println(i)
}

for var i = 1, i <= 100, i = i + 1 {
    native.print("Iteration: " + i + "\n")
}
```
## Native Libraries

Clarity includes a set of built-in libraries that provide essential functions for various operations. You can include these libraries using the include keyword.

For example:

```clarity

include system
include math
```
Common Native Functions
```clarity
    native.println(message): Prints a message with a newline.
    native.print(message): Prints a message without adding a newline.
    native.error.except(): Handles errors or exceptions.
```
## Examples
## Simple Program

A Clarity program does not require a specific main function to run. Here's a basic example:

```clarity

native.println("Hello, World!")
```
## Using a Class

```clarity
var john = new Person("John", 16)
john.greet()
```
## Looping Over a List
```clarity

for i : [1, 2, 3] {
    native.println(i)
}
```
## Static Properties and Methods

```clarity

class Test {
    static const var name = "Clarity"

    static fn getName() {
        return Test.name
    }
}

for var i = 1, i <= 100, i = i + 1 {
    native.print(Test.getName() + " loves you " + i + " times\n")
}
```
## Conclusion

Clarity is designed to be straightforward and easy to learn, offering both powerful features and a simple syntax. With its dynamic typing, flexible structure, and access to native libraries, Clarity is an ideal language for developing various types of applications while maintaining clean and readable code.
