package org.example.app

def infoDisplay (msg) {
    println("==="*20+"$msg"+"==="*20)
}

class Book {
    String name
    String author
}

// extend dynamic method
Book.metaClass.getHello = {
    -> "hello from extend method"
}

//extend dynamic properties
Book.metaClass.fullName {"$name" + " " + "$author"}

// append constructs
Book.metaClass.constructors << {
    String name -> new Book(name: name)
} << {
    String name, String author -> new Book(name: name, author: author)
} << {
    -> new Book()
}

// static methos
Book.metaClass.static.sayHi = {
    -> println "Hi from static method"
}

// properties
Book.metaClass.nickName = "Groovy"

book = new Book()

book.with {
    name = "Groovy in Action"
    author = "Dierk König"
}

infoDisplay("methods")
// get all methods
book.metaClass.methods.each {
    println it
}

infoDisplay("properties")
// get all properties
book.metaClass.properties.each {
    println(it)
}

println(book.hello)
println(book.fullName())

infoDisplay("static methos")
Book.sayHi()



