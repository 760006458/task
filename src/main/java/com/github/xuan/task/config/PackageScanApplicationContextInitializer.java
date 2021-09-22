package com.github.xuan.task.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.Ordered;

/**
 * spring.factories配置了该类，spring会实例化该类(ApplicationContextInitializer的实现类)，并调用initialize方法
 *
 * @author xuan
 * @create 2021-05-06 17:54
 **/
public class PackageScanApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private final String[] packages;

    public PackageScanApplicationContextInitializer() {
        this.packages = new String[]{"com.github.xuan.task"};
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (context instanceof BeanDefinitionRegistry) {
            ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner((BeanDefinitionRegistry) context);
            scanner.scan(packages);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 9999;
    }
}
