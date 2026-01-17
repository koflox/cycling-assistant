package com.koflox.id

import java.util.UUID

interface IdGenerator {
    fun generate(): String
}

internal class UuidIdGenerator : IdGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}
