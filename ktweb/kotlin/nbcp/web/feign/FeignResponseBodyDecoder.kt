package nbcp.web.feign

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeignResponseBodyDecoder(val value: String = "")