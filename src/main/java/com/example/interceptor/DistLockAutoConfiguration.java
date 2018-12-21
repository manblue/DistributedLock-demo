package com.example.interceptor;

import javax.annotation.PostConstruct;

import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import com.example.annotation.DistLock;
/**
 * 自动配置
 * */
public class DistLockAutoConfiguration extends AbstractPointcutAdvisor {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Advice advice;
	private Pointcut pointcut;
	
	@PostConstruct
	public void init() {
		logger.error("init DistLockAutoConfiguration start");
		this.pointcut = new AnnotationMatchingPointcut(null, DistLock.class);
		this.advice = new LockMethodInterceptor();
		logger.error("init DistLockAutoConfiguration end");
	}
	
	@Override
	public Pointcut getPointcut() {
		// TODO Auto-generated method stub
		return this.pointcut;
	}

	@Override
	public Advice getAdvice() {
		// TODO Auto-generated method stub
		return this.advice;
	}

}
