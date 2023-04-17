# springboot cloud 版本关系：

参考:
> https://spring.io/projects/spring-cloud/
> https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E

最准备的方法,还是查看源码:
> https://github.com/spring-cloud/spring-cloud-kubernetes.git
> https://github.com/spring-cloud/spring-cloud-release.git

查看 Spring-Cloud 源码，看它依赖的 SpringBoot版本。 
SpringBoot版本为标准列


| SpringBoot 版本 | SpringCloud 版本 | SpringCloudCommons 版本 | spring-cloud-kubernetes 版本 | Spring Cloud Alibaba Version |
|---------------|----------------|-----------------------|----------------------------|------------------------------|
| 1.5.x         | Dalston        |                       |                            |                              |
| 1.5.x         | Edgware        |                       |                            | 1.5.1.RELEASE                |
| 2.0.x         | Finchley       |                       |                            | 2.0.4.RELEASE                |
| 2.1.x         | Greenwich      | 2.1.1.RELEASE         | 1.0.1.RELEASE              | 2.1.4.RELEASE                |
| 2.2.x         | Hoxton         |                       |                            | 2.2.1.RELEASE                |
| 2.3.x         | Hoxton         |                       |                            | 2.2.7.RELEASE                |
| 2.4.x         | 2020.0.x       | 3.0.5                 | `2.0.5`                    |                              |
| 2.5.x         | 2020.0.x       |                       |                            |                              |
| 2.6.x         | 2021.0.x       | 3.1.x                 | `2.1.1`                    | 2021.0.1.0                   |
| 3.x           | 2022.0.2       | 4.0.2                 |                            | 2022.0.0.0-RC1               |



