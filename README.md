# Clarity Programming Language

## Overview

Clarity is a high-level, object-oriented programming language designed to prioritize simplicity and clarity. It features a concise syntax and supports dynamic typing, making it easy to read and write. Clarity is suitable for both beginners and experienced developers who want to create clean, maintainable code.

## Table of Contents
1. [Installation](#installation)
2. [Features](#features)
3. [Basic Syntax](#basic-syntax)
   - [Variables](#variables)
   - [Functions](#functions)
   - [Classes](#classes)
   - [Control Structures](#control-structures)
4. [Native Libraries](#native-libraries)
5. [Examples](#examples)
   - [Simple Program](#simple-program)
   - [Using a Class](#using-a-class)
   - [Looping Over a List](#looping-over-a-list)
   - [Static Properties and Methods](#static-properties-and-methods)

## Installation

To install Clarity, follow these steps based on your operating system:

##### download Required Files:
Windows: Download install_windows.bat
Mac: Download install_mac.sh
Linux: Download install_linux.sh
Also, download clarity.jar

##### Prepare for Installation:
Ensure that `install_windows.bat`, `install_mac.sh`, or `install_linux.sh`, along with `clarity.jar`, are located in the same directory.

##### Run the Installer:
Windows: Double-click `install_windows.bat` to run the installer.
Mac: Open a terminal, navigate to the directory containing `install_mac.sh` and clarity.jar, then run the bash file.
Linux: Open a terminal, navigate to the directory containing `install_linux.sh` and clarity.jar, then the bash file.

##### Complete Installation:
The installer will place Clarity on your system `%userpath%/Clarity` and then remove the installer files. You can safely delete clarity.jar and the installer script if they are not automatically removed.
(Java will automatically installed if version 8 or higher is not found)

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
// foreach
for i : [1, 2, 3] {
    native.println(i)
}

// for
for var i = 1, i <= 100, i = i + 1 {
    native.print("Iteration: " + i + "\n")
}
```

While Loop:

```clarity
var i = 0
var finished = false
while !finished {
    native.println("Not finished")
    i = i + 1
    finished = i > 100
}
```

Select Statement:

```clarity
var i = native.input("Write a number from 1 to 3: ")
select i {
    when 1 {
        native.println("You wrote 1")
    }
    when 1 {
        native.println("You wrote 2")
    }
    when 1 {
        native.println("You wrote 3")
    }
    default {
        native.println(i + " is not a number between 1 and 3")
    }
}
```

If Statement:

```clarity
var x = int(native.input("Write a number: ")) // asking for a number and converting it to integer
if x > 0 {
    native.println("Your number is greater than 0")
} else if x < 0 {
    native.println("Your number is less than 0")
} else {
    native.println("Your number is equal to 0")
}
```

Assert Statement:

```clarity
var x = int(native.input("Write a number from 1 to 3: "))
assert x >= 1 && x <= 3 else "The number " + x + " is not between i and 3!"
```

Is Check:

```clarity
class Inherited {
}

class Inheritor inherits Inherited {
}

var inheritor = new Inheritor()

assert inheritor is Inherited // does not give any exception since inheritor is indirectly Inherited

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
