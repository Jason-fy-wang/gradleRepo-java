package org.example.app.dsl

import java.util.regex.Pattern


Pattern SSH_URL = ~/^(([^:@]+):([^@]+)?@)?([^:]+)(:([0-9]+))?$/


def url = "user:password@host:22"
def url1 = "scott:loongson@192.168.20.21:22"
def matcher = SSH_URL.matcher(url1)

if (matcher.matches()) {
    (1..matcher.groupCount()).each { i ->
        println "Group $i: ${matcher.group(i)}"
    }
}