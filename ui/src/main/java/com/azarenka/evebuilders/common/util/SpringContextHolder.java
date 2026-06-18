package com.azarenka.evebuilders.common.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext is not initialized");
        }
        return applicationContext.getBean(type);
    }
}
