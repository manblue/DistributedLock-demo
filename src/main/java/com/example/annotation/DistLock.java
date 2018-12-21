package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DistributedLock
 * */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistLock {

	String value() default "";
	/**锁超时时间 3s*/
	long lockExpire() default  3000;
	/**请求超时时间 3s*/
	long acquireTimeout()  default  3000;
}
