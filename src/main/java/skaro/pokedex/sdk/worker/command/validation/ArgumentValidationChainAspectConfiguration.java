package skaro.pokedex.sdk.worker.command.validation;

import java.lang.invoke.MethodHandles;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Configuration;

import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

@Aspect
@Configuration
public class ArgumentValidationChainAspectConfiguration {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private BeanFactory beanFactory;
	
	public ArgumentValidationChainAspectConfiguration(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Around("classAnnotatedWithValidationFilterChain(filterChain) && methodHasWorkRequestArgument(workRequest)")
	public Object executeFilterChain(ProceedingJoinPoint joinPoint, OrderedArgumentValidationFilterChain filterChain, WorkRequest workRequest) throws Throwable {
		LOG.info("here");
		return joinPoint.proceed(joinPoint.getArgs());
	}
	
	@Pointcut("@within(filterChain)")
	private void classAnnotatedWithValidationFilterChain(OrderedArgumentValidationFilterChain filterChain) {}
	
	@Pointcut("args(workRequest,..)")
	private void methodHasWorkRequestArgument(WorkRequest workRequest) {}
	
}
