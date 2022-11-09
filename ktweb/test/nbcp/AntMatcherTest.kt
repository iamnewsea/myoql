package nbcp

import nbcp.base.TestBase
import org.junit.jupiter.api.Test
import org.springframework.util.AntPathMatcher

class AntMatcherTest : TestBase() {


    @Test
    fun test() {
        var matcher = AntPathMatcher()
        println(matcher.match("/a/{type}/c", "/a/b/c"))  // true
        println(matcher.matchStart("a/{type}/c", "/a/b/c"))  //true
        println(matcher.matchStart("a/*/c", "/a/b/c"))  // true


        matcher = AntPathMatcher(".")
        println(matcher.match(".a.{type}.c", ".a.b.c"))  // true
        println(matcher.matchStart("a.{type}.c", ".a.b.c"))  // true
        println(matcher.matchStart("a.*.c", "a.b.c"))   // true
    }
}