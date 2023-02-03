package com.example.testmockk

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FlowServiceTest {

  private val item = Item("userId", "adCategory")
  private val draft = Draft("adId")
  private val emitions = listOf(Draft("0"), Draft("1"), Draft("2"))

  @Before
  fun setup() {
  }

  @Test
  fun `basic mock`() {
    val expected = draft
    val service = mockk<FlowService>() {
      every { postItem(any()) } returns flow { emit(expected) }
    }

    val result = runBlocking {
      service.postItem(item)
        .first()
    }

    Assert.assertEquals(expected, result)
  }

  @Test
  fun `verify call function with args`() {
    val service = mockk<FlowService>() {
      every { postItem(any()) } returns flow { emit(draft) }
    }

    runBlocking {
      service.postItem(item)
        .first()
    }

    verify { service.postItem(item) }
  }

  @Test
  fun `catch several emitions`() {
    val expected = emitions
    val service = mockk<FlowService>() {
      every { postItem(any()) } returns FlowService().sync()
    }

    val result = runBlocking {
      service.postItem(item)
        .toList()
    }

    Assert.assertEquals(expected, result)
  }

  @Test
  fun `catch last emitted`() {
    val expected = emitions.last()
    val service = mockk<FlowService>() {
      every { postItem(any()) } returns FlowService().sync()
    }

    val result = runBlocking {
      service.postItem(item)
        .toList()
        .last()
    }

    Assert.assertEquals(expected, result)
  }

  @Test
  fun `check completion`() {
    val expected = draft
    var result: Throwable? = Throwable("wrong exception")
    val service = mockk<FlowService>() {
      every { postItem(any()) } returns flow { emit(expected) }
    }

    runBlocking {
      service.postItem(item)
        .onCompletion { cause -> result = cause }
        .collect()
    }

    assertNull(result)
  }

  @Test
  fun `check exception`() {
    val expected = RuntimeException("failed message")
    var result: Throwable? = Throwable("wrong exception")
    val service = mockk<FlowService>() {
      every { putItem(any()) } returns flow { throw expected }
    }

    runBlocking {
      service.putItem(item)
        .catch { cause -> result = cause }
        .collect()
    }

    Assert.assertEquals(expected, result)
  }
}
