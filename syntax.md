# Example code
```
class Person {
    var name
    var age

    constructor(name, age) {
        local.name = name
        local.age = age
    }

    fn greet() {
        native.println("My name is" + local.name + " and i'm " + local.age + " years old)
    }
}

fn factorial(n) {
    // You can also do
    // if n == 0 || n == 1 return 1
    
    if n == 0 || n == 1 {
        return 1
    } else {
        return n * factorial(n - 1)
    }
}

fn main() {

    var number = 5

    var fact = factorial(5)
    
    native.println("Factorial of " + number + " equals to " + fact)
}
```
# Easter egg
Very easy to use simple and abstract public method.
```
public protected private static transient volatile const let def func fn implements extends assert boolean byte case switch catch char default do while for syncronized strictfp goto requires static throws try super package new module long interface int instanceof import if float finally final exports enum double continue byte break void main(String[] args) {
  native.println("hello world")
}
```