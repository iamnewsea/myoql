package nbcp.agent

import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

class MyTransform : ClassFileTransformer {
    override fun transform(
        loader: ClassLoader,
        className: String,
        classBeingRedefined: Class<*>,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray
    ): ByteArray {
        println("正在加载: ${className}")
        return classfileBuffer;
    }
}