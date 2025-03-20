package org.example.app.intercept

import groovy.util.logging.Slf4j


@Slf4j
class PerformanceInterceptor implements Interceptor {

    def start

    @Override
    Object beforeInvoke(Object o, String s, Object[] objects) {
        log.info("start invoke")
        start = System.currentTimeMillis()
    }

    @Override
    Object afterInvoke(Object o, String method, Object[] args, Object result) {
        long spend  = System.currentTimeMillis() - start
        log.info("spend time is ${spend} ms")
        return result
    }

    @Override
    boolean doInvoke() {
        return true
    }
}



@Slf4j
class InterceptorClz {

    def test(param) {
        System.out.println("test method called: ${param}")
        log.info("test method called: ${param}")
        Thread.sleep(1000)
    }
}

class ProxyTest {
    static void useInterceptor (Class theClass, Interceptor interceptorInstance, Closure code){
            def proxy = ProxyMetaClass.getInstance(theClass)
            proxy.interceptor = interceptorInstance
            proxy.use {code()}

    }

    static void methodInterceptorByProxy() {
        useInterceptor(InterceptorClz, new PerformanceInterceptor()) {
            def ic = new InterceptorClz()
            ic.test("test1")
        }
    }
}

ProxyTest.methodInterceptorByProxy()


//def proxy = ProxyMetaClass.getInstance(InterceptorClz)
//def interceptor = new PerformanceInterceptor()
//proxy.interceptor = interceptor
//
//proxy.use {
//    def clz = new InterceptorClz()
//    clz.test("1")
//}