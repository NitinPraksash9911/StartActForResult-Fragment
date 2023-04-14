package com.example.startactforresult_fragment

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

interface MyInterface {
    fun testMyMethod()
}

class MyClass : MyInterface {
    override fun testMyMethod() {
        println("asdasdaada called")
    }
}

class MyInvocationHandler(private val obj: Any) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        // Call your method here
        println("Before ${method.name} is called")
        return method.invoke(obj, *args.orEmpty())
    }
}

fun main() {
    val myClass:MyInterface = MyClass()

//    val proxy = Proxy.newProxyInstance(
//        MyInterface::class.java.classLoader,
//        arrayOf(MyInterface::class.java),
//        MyInvocationHandler(myClass)
//    ) as MyInterface

    val target:MyInterface = MyClass()
    val proxy = Proxy.newProxyInstance(
        MyInterface::class.java.classLoader,
        arrayOf(MyInterface::class.java),
        MyInvocationHandler(target)
    ) as MyInterface

    // Call the method on the proxy object
    proxy.testMyMethod()
}
