package skaro.pokedex.sdk.worker.command.validation;

import static skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration.ARGUMENT_VALIDATION_ASPECT_ORDER;

import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.micrometer.core.instrument.util.StringUtils;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

@Aspect
@Configuration
@Order(ARGUMENT_VALIDATION_ASPECT_ORDER)
public class ArgumentValidationChainAspectConfiguration {
	
	private BeanFactory beanFactory;
	
	public ArgumentValidationChainAspectConfiguration(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Around("classAnnotatedWithValidationFilterChain(filterChain) && methodHasWorkRequestArgument(workRequest)")
	public Object executeFilterChain(ProceedingJoinPoint joinPoint, ValidationFilterChain filterChain, WorkRequest workRequest) {
		try {
			return constructChain(filterChain, workRequest)
					.switchIfEmpty(proceedWithCommand(joinPoint));
		} catch(BeansException e) {
			return Mono.error(e);
		}
	}
	
	@Pointcut("@within(filterChain)")
	private void classAnnotatedWithValidationFilterChain(ValidationFilterChain filterChain) {}
	
	@Pointcut("args(workRequest,..)")
	private void methodHasWorkRequestArgument(WorkRequest workRequest) {}

	private Mono<AnsweredWorkRequest> constructChain(ValidationFilterChain filterChain, WorkRequest workRequest) {
		return Stream.of(filterChain.value())
				.map(this::getFilterBean)
				.map(filter -> Mono.defer(() -> filter.filter(workRequest)))
				.reduce(Mono.empty(), (partialChain, nextFilter) -> partialChain.switchIfEmpty(nextFilter));
	}
	
	private ValidationFilter getFilterBean(Filter filter) {
		Class<? extends ValidationFilter> filterCls = filter.value();
		
		if(StringUtils.isBlank(filter.beanName())) {
			return beanFactory.getBean(filterCls);
		}
		return beanFactory.getBean(filter.beanName(), filterCls);
	}
	
	@SuppressWarnings("unchecked")
	private Mono<AnsweredWorkRequest> proceedWithCommand(ProceedingJoinPoint joinPoint) {
		return Mono.defer( () -> {
			try {
				return (Mono<AnsweredWorkRequest>) joinPoint.proceed(joinPoint.getArgs());
			} catch (Throwable e) {
				return Mono.error(e);
			}
		});
	}
	
}
