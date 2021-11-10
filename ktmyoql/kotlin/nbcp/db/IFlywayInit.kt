package nbcp.db

interface IFlywayInit {
    val version:Int;
    fun init();
}