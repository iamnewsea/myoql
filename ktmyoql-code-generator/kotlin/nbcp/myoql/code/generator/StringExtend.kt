package nbcp.myoql.code.generator


fun String.removeQuoteContent():String{
    return Regex("""\([^)]*\)""").replace(this, "")
}