package org.example.app

import groovy.util.logging.Slf4j


@Slf4j
class Performance implements GroovyInterceptable {

    def invokeMethod(String name, Object args) {
        def start = System.currentTimeMillis()

        def metaMethod = this.metaClass.getMetaMethod(name, args)
        if (metaMethod) {
            def result= metaMethod.invoke(this, args)
            def end = System.currentTimeMillis()
            log.info("spend time is ${end - start} ms")
            result
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }
}

@Slf4j
class SlowService extends Performance {
    def slowMethod() {
        log.info("slow method called")
        Thread.sleep(1000)
    }

    static rnd(){
        Math.abs(new Random().nextInt() % 5000 + 1000)
    }
}

def slow = new SlowService()

(1..4).each {
    slow.slowMethod()
}