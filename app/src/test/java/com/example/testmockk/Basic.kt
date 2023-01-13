package com.example.testmockk

import io.mockk.Called
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Tips:
 *
 * mock: every
 * unordered verify: verify
 * ordered verify: verifySequence
 * ordered (but with other execution in between) verify: verifyOrder
 * argument captor: slot
 *
 * annotations (penalizes performance): @MockK, @SpyK, @RelaxedMockK
 *
 * mock several values: andThen, returnMany
 * mock Unit return: just
 * mock with lambda: answers
 *
 * suspended func: coEvery, coVerify, coAnswers
 */

class Basic {

  private val item = Item("userId", "adCategory")
  private val draft = Draft("adId")

  @Before
  fun setup() {
  }

  @Test
  fun `happy path`() {
    val expected = item.toDraft()
    val service = BlockingService()

    val result = service.postItem(item)

    assertEquals(expected, result)
  }

  @Test
  fun `basic mock`() {
    val expected = draft
    val service: BlockingService = mockk() {
      every { postItem(any()) } returns expected
    }

    val result = service.postItem(item)

    assertEquals(expected, result)
  }

  @Test
  fun `mock returns one different value on each request`() {
    val expected = listOf(
      draft,
      item.toDraft(),
    )
    val service: BlockingService = mockk() {
      every { postItem(any()) } returns draft andThen item.toDraft()
    }

    val result = listOf(
      service.postItem(item),
      service.postItem(item),
    )

    assertEquals(expected, result)
  }

  @Test
  fun `mock returns one different value on each request, other way`() {
    val expected = listOf(
      draft,
      item.toDraft(),
    )
    val service: BlockingService = mockk() {
      every { postItem(any()) } returnsMany expected
    }

    val result = listOf(
      service.postItem(item),
      service.postItem(item),
    )

    assertEquals(expected, result)
  }

  @Test
  fun `mock returns lambda using input values`() {
    val expected = item.toDraft()
    val service: BlockingService = mockk() {
      every { postItem(any()) } answers { arg<Item>(0).toDraft() }
    }

    val result = service.postItem(item)

    assertEquals(expected, result)
  }

  @Test
  fun `capture arguments`() {
    val expected = item.toDraft().copy(adId = "fake")
    val slot = slot<Item>()
    val service: BlockingService = mockk() {
      every { postItem(capture(slot)) } answers { slot.captured.toDraft().copy(adId = "fake") }
    }

    val result = service.postItem(item)

    assertEquals(expected, result)
  }

  @Test
  fun `verify call function with args`() {
    val service: BlockingService = mockk() {
      every { postItem(any()) } returns draft
    }

    service.postItem(item)

    verify {
      service.postItem(item)
    }
  }

  @Test
  fun `verify execution order, not strictly`() {
    val newItem = Item("userId2", "adCategory2")
    val service: BlockingService = mockk() {
      every { postItem(any()) } returns draft
      every { deleteItem(any()) } just Runs
    }

    service.postItem(item)
    service.deleteItem(item)
    service.postItem(newItem)

    verifyOrder {
      service.postItem(item)
      service.postItem(newItem)
    }
  }

  @Test
  fun `verify execution order, strictly`() {
    val newItem = Item("userId2", "adCategory2")
    val service: BlockingService = mockk() {
      every { postItem(any()) } returns draft
      every { deleteItem(any()) } just Runs
    }

    service.postItem(item)
    service.deleteItem(item)
    service.postItem(newItem)

    verifySequence {
      service.postItem(item)
      service.deleteItem(item)
      service.postItem(newItem)
    }
  }

  @Test
  fun `avoid to mock all behaviours`() {
    val service: BlockingService = mockk(relaxed = true)

    service.postItem(item)

    verify {
      service.postItem(item)
    }
  }

  @Test(expected = RuntimeException::class)
  fun `mock an exception`(): Unit {
    val service: BlockingService = mockk() {
      every { postItem(any()) } throws RuntimeException()
    }
    service.postItem(item)
  }

  @Test(expected = RuntimeException::class)
  fun `failed path`(): Unit {
    val service = BlockingService()
    service.putItem(item)
  }

  @Test
  fun `returning unit happy path`() {
    val service = BlockingService()

    val result = service.deleteItem(item)

    assertNotNull(result)
  }

  @Test
  fun `mock Unit`() {
    val service: BlockingService = mockk() {
      every { deleteItem(any()) } just Runs
    }

    val result = service.deleteItem(item)

    assertNotNull(result)
  }

  @Test
  fun `dependency was not called at all`() {
    val service: BlockingService = mockk()

    verify {
      service wasNot Called
    }
  }

}