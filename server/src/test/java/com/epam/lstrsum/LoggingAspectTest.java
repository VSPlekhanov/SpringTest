package com.epam.lstrsum;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LoggingAspectTest {

    private LoggingAspect loggingAspect = new LoggingAspect();
    private ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

    {
        doReturn(new Object[0]).when(joinPoint).getArgs();
        Signature mock = mock(Signature.class);
        doReturn(mock).when(joinPoint).getSignature();
        doReturn("methodName").when(mock).getName();
    }

    @Test
    public void aspectReturnValueFromJoinPoint() throws Throwable {
        Object toBeReturned = new Object();
        doReturn(toBeReturned).when(joinPoint).proceed(anyVararg());

        assertThat(loggingAspect.logMethodExecution(joinPoint))
                .isEqualToComparingFieldByFieldRecursively(toBeReturned);

        verify(joinPoint, times(1)).proceed(anyVararg());
    }

    @Test
    public void aspectThrownException() throws Throwable {
        doThrow(RuntimeException.class).when(joinPoint).proceed(anyVararg());

        assertThatThrownBy(() -> loggingAspect.logMethodExecution(joinPoint))
                .isInstanceOf(RuntimeException.class);

        verify(joinPoint, times(1)).proceed(anyVararg());
    }

}