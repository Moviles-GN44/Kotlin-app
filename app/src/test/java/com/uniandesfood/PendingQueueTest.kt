package com.uniandesfood.data.local

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PendingQueueTest {

    // Without AppStorage.init() the queue works only in memory, which is enough for these tests
    private fun newQueue() = PendingQueue("test.json", UnconfinedTestDispatcher())

    private fun item(id: String) = JSONObject().put("id", id)

    @Test
    fun add_keepsTheItemsInOrder() = runTest {
        val queue = newQueue()
        queue.add(item("a"))
        queue.add(item("b"))

        assertEquals(listOf("a", "b"), queue.all().map { it.getString("id") })
    }

    @Test
    fun add_ignoresDuplicatedIds() = runTest {
        val queue = newQueue()
        queue.add(item("a"))
        queue.add(item("a"))

        assertEquals(1, queue.size())
    }

    @Test
    fun flush_sendsEverythingAndEmptiesTheQueue() = runTest {
        val queue = newQueue()
        queue.add(item("a"))
        queue.add(item("b"))
        val sent = mutableListOf<String>()

        queue.flush { sent.add(it.getString("id")); true }

        assertEquals(listOf("a", "b"), sent)
        assertEquals(0, queue.size())
    }

    @Test
    fun flush_stopsAtTheFirstFailureAndKeepsTheRest() = runTest {
        val queue = newQueue()
        queue.add(item("a"))
        queue.add(item("b"))
        queue.add(item("c"))

        queue.flush { it.getString("id") != "b" }

        // "a" was sent, "b" failed, "c" was never tried
        assertEquals(listOf("b", "c"), queue.all().map { it.getString("id") })
    }
}