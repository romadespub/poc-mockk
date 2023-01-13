package com.example.testmockk

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Tips:
 * mock suspended func: coEvery
 * unordered verify suspended func: coVerify
 * mock with lambda suspended func: coAnswers
 *
 * run suspended func: runBlocking
 */

class Coroutine {

  private val item = Item("userId", "adCategory")
  private val draft = Draft("adId")

  @Before
  fun setup() {
  }

  @Test
  fun `basic mock`() {
    val expected = draft
    val service = mockk<BlockingService>() {
      coEvery { postItem(any()) } returns expected
    }

    val result = runBlocking { service.postItem(item) }

    assertEquals(expected, result)
  }

  @Test
  fun `mock returns lambda (of suspended function) using input values`() {
    val expected = item.toDraft()
    val service = mockk<BlockingService>() {
      coEvery { postItem(any()) } coAnswers { CoroutineService().postItem(arg(0)) }
    }

    val result = runBlocking { service.postItem(item) }

    assertEquals(expected, result)
  }

  @Test
  fun `verify call function with args`() {
    val service = mockk<BlockingService>() {
      every { postItem(any()) } returns draft
    }

    runBlocking { service.postItem(item) }

    coVerify {
      service.postItem(item)
    }
  }
}
