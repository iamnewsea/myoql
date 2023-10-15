package nbcp.base.json

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module

class EnumModule : Module() {
    override fun getModuleName(): String {
        return "BaseEnumModule"
    }

    override fun version(): Version {
        return Version.unknownVersion()
    }

    override fun setupModule(context: SetupContext) {
        context.addDeserializers(BaseEnumDeserializers())
    }
}