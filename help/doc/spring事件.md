# Spring 事件顺序：

spring事件非常不准。

1. 继承 BeanDefinitionRegistryPostProcessor,ApplicationContextAware , 且带 @Order 注解的Bean

   执行顺序：
    1. setApplicationContext
    2. postProcessBeanDefinitionRegistry
    3. postProcessBeanFactory

2. 继承 BeanFactoryPostProcessor,ApplicationContextAware , 且带 @Order 注解的Bean

   执行顺序：
    1. setApplicationContext, 发出 开始事件
    2. postProcessBeanFactory

SpringUtil 使用第2种方式，继承 BeanFactoryPostProcessor

ContextClosedEvent（容器关闭时） ContextRefreshedEvent（容器刷新是） ContextStartedEvent（容器启动时候） ContextStoppedEvent（容器停止的时候）

> 定义方式： https://blog.csdn.net/ignorewho/article/details/80702827

> 执行顺序： https://blog.csdn.net/isea533/article/details/100146833