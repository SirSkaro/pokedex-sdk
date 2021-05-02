package skaro.pokedex.sdk.worker.command.validation;

import java.lang.annotation.Annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;

@ExtendWith(SpringExtension.class)
public class ArgumentValidationChainAspectTest {

	@Mock
	private BeanFactory beanFactory;
	
	private ArgumentValidationChainAspectConfiguration aspect;
	
	@BeforeEach
	public void setup() {
		aspect = new ArgumentValidationChainAspectConfiguration(beanFactory);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testExecuteFilterChain_argumentValid() throws Throwable {
		String filter2BeanName = "filter 2 bean";
		WorkRequest workRequest = new WorkRequest();
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		ValidationFilterChain filterChain = Mockito.mock(ValidationFilterChain.class);
		Filter filter1 = createMockFilter(null);
		Filter filter2 = createMockFilter(filter2BeanName);
		ValidationFilter validationFilter1 = Mockito.mock(ValidationFilter.class);
		ValidationFilter validationFilter2 = Mockito.mock(ValidationFilter.class);
		
		ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Object[] joinPointArguments = new Object[] {workRequest};
		
		Mockito.when(joinPoint.getArgs())
			.thenReturn(joinPointArguments);
		Mockito.when(joinPoint.proceed(joinPointArguments))
			.thenReturn(Mono.just(answer));
		Mockito.when(filterChain.value())
			.thenReturn(Arrays.array(filter1, filter2));
		Mockito.when(beanFactory.getBean(ValidationFilter.class))
			.thenReturn(validationFilter1);
		Mockito.when(beanFactory.getBean(filter2BeanName, ValidationFilter.class))
			.thenReturn(validationFilter2);
		Mockito.when(validationFilter1.filter(workRequest))
			.thenReturn(Mono.empty());
		Mockito.when(validationFilter2.filter(workRequest))
			.thenReturn(Mono.empty());
		
		Mono<AnsweredWorkRequest> result = (Mono<AnsweredWorkRequest>) aspect.executeFilterChain(joinPoint, filterChain, workRequest);
		
		StepVerifier.create(result)
			.expectNext(answer)
			.expectComplete()
			.verify();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteFilterChain_emptyChain() throws Throwable {
		WorkRequest workRequest = new WorkRequest();
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		ValidationFilterChain filterChain = Mockito.mock(ValidationFilterChain.class);
		
		ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Object[] joinPointArguments = new Object[] {workRequest};
		
		Mockito.when(joinPoint.getArgs())
			.thenReturn(joinPointArguments);
		Mockito.when(joinPoint.proceed(joinPointArguments))
			.thenReturn(Mono.just(answer));
		Mockito.when(filterChain.value())
			.thenReturn(Arrays.array());
		
		Mono<AnsweredWorkRequest> result = (Mono<AnsweredWorkRequest>) aspect.executeFilterChain(joinPoint, filterChain, workRequest);
		
		StepVerifier.create(result)
			.expectNext(answer)
			.expectComplete()
			.verify();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteFilterChain_invalidBeanSpecified() throws Throwable {
		WorkRequest workRequest = new WorkRequest();
		AnsweredWorkRequest validationViolationAnswer = new AnsweredWorkRequest();
		ValidationFilterChain filterChain = Mockito.mock(ValidationFilterChain.class);
		Filter filter1 = createMockFilter(null);
		ValidationFilter validationFilter1 = Mockito.mock(ValidationFilter.class);
		
		ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Object[] joinPointArguments = new Object[] {workRequest};
		
		Mockito.when(joinPoint.getArgs())
			.thenReturn(joinPointArguments);
		Mockito.when(joinPoint.proceed(joinPointArguments))
			.thenReturn(Mono.empty());
		Mockito.when(filterChain.value())
			.thenReturn(Arrays.array(filter1));
		Mockito.when(beanFactory.getBean(ValidationFilter.class))
			.thenThrow(new NoSuchBeanDefinitionException(ValidationFilter.class));
		Mockito.when(validationFilter1.filter(workRequest))
			.thenReturn(Mono.just(validationViolationAnswer));
		
		Mono<AnsweredWorkRequest> result = (Mono<AnsweredWorkRequest>) aspect.executeFilterChain(joinPoint, filterChain, workRequest);
		
		StepVerifier.create(result)
			.expectError(NoSuchBeanDefinitionException.class)
			.verify();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testExecuteFilterChain_validationFails() throws Throwable {
		WorkRequest workRequest = new WorkRequest();
		AnsweredWorkRequest validationViolationAnswer = new AnsweredWorkRequest();
		ValidationFilterChain filterChain = Mockito.mock(ValidationFilterChain.class);
		Filter filter1 = createMockFilter(null);
		ValidationFilter validationFilter1 = Mockito.mock(ValidationFilter.class);
		
		ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Object[] joinPointArguments = new Object[] {workRequest};
		
		Mockito.when(joinPoint.getArgs())
			.thenReturn(joinPointArguments);
		Mockito.when(joinPoint.proceed(joinPointArguments))
			.thenReturn(Mono.empty());
		Mockito.when(filterChain.value())
			.thenReturn(Arrays.array(filter1));
		Mockito.when(beanFactory.getBean(ValidationFilter.class))
			.thenReturn(validationFilter1);
		Mockito.when(validationFilter1.filter(workRequest))
			.thenReturn(Mono.just(validationViolationAnswer));
		
		Mono<AnsweredWorkRequest> result = (Mono<AnsweredWorkRequest>) aspect.executeFilterChain(joinPoint, filterChain, workRequest);
		
		StepVerifier.create(result)
			.expectNext(validationViolationAnswer)
			.expectComplete()
			.verify();
	}
	
	private Filter createMockFilter(String beanName) {
		return new Filter() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Filter.class;
			}

			@Override
			public Class<? extends ValidationFilter> value() {
				return ValidationFilter.class;
			}

			@Override
			public String beanName() {
				return beanName;
			}};
	}
	
}
