package nbcp

import java.lang.instrument.Instrumentation

object AgentMain {

    @JvmStatic
    fun premain(agentArgs: String, inst: Instrumentation) {


        work(inst);
    }

    private fun work(inst: Instrumentation) {
        inst.addTransformer(MyTransform())

        inst.allLoadedClasses.map {
            return@map it.name
        }.apply {
            println(this.joinToString(", "))
        }
    }
}