package skaro.pokedex.sdk.worker.command.validation;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
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
	public Object executeFilterChain(ProceedingJoinPoint joinPoint, ValidationFilterChain filterChain, WorkRequest workRequest) throws Throwable {
		LOG.info("Intercepted request {}", workRequest.getCommmand());
		Mono<AnsweredWorkRequest> currentAnswer = Stream.of(filterChain.value())
				.map(this::getFilterBean)
				.map(filter -> Mono.defer(() -> filter.filter(workRequest)))
				.reduce(Mono.empty(), (partialChain, nextFilter) -> partialChain.switchIfEmpty(nextFilter));
		
		return currentAnswer.switchIfEmpty(proceedWithCommand(joinPoint));
	}
	
	@Pointcut("@within(filterChain)")
	private void classAnnotatedWithValidationFilterChain(ValidationFilterChain filterChain) {}
	
	@Pointcut("args(workRequest,..)")
	private void methodHasWorkRequestArgument(WorkRequest workRequest) {}
	
	private CommandFilter getFilterBean(Filter filter) {
		Class<? extends CommandFilter> filterCls = filter.value();
		return beanFactory.getBean(filterCls);
	}
	
	@SuppressWarnings("unchecked")
	private Mono<AnsweredWorkRequest> proceedWithCommand(ProceedingJoinPoint joinPoint) throws Throwable {
		return Mono.defer( () -> {
			try {
				return (Mono<AnsweredWorkRequest>) joinPoint.proceed(joinPoint.getArgs());
			} catch (Throwable e) {
				throw new RuntimeException();
			}
		});
	}
	
}
