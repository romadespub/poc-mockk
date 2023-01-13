package com.example.testmockk

import io.mockk.checkUnnecessaryStub
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class Advanced {

  private val item = Item("userId", "adCategory")
  private val draft = Draft("adId")

  @Before
  fun setup() {
  }

  @Test
  fun `partially mock`() {
    val expected = listOf(
      Item("id", "id"),
      item.toDraft(),
    )
    val spy = spyk(BlockingService())
    every { spy.putItem(any()) } answers { expected[0] as Item }

    val result = listOf(
      spy.putItem(item),
      spy.postItem(item),
    )

    verify { spy.putItem(item) }
    assertEquals(expected, result)
  }

  @Test
  fun `hierarchical mocking`() {
    val expected = Container(Item("user", "category"), Draft("ad"))
    val container: Container = mockk() {
      every { item } returns mockk() {
        every { userId } returns "user"
        every { adCategory } returns "category"
      }
      every { draft } returns mockk() {
        every { adId } returns "ad"
      }
    }

    assertEquals(expected, container)
  }

  @Test
  @Ignore("remove ignore and will fail due to unnecessary stub")
  fun `check unnecessary stubbing`() {
    val expected = draft
    val service: BlockingService = mockk() {
      every { postItem(any()) } returns expected
    }
    checkUnnecessaryStub(service)
  }

  // Handle with care!!!
  @Test
  fun `mock private functions`() {
    val expected = Item(item.userId, "zaca boom!!!!")
    val spy = spyk<BlockingService>(recordPrivateCalls = true)
    every { spy["boom"]() } returns expected.adCategory

    val result = spy.reset(item)

    assertEquals(result, expected)
    verify {
      spy.reset(item)
      spy["boom"]
    }
  }

}