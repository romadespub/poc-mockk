package com.example.testmockk

import com.example.testmockk.ObjectExtension.reverseProperties
import io.mockk.checkUnnecessaryStub
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
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

  @After
  fun tearDown() {
    // Ensure you disable all static mocks to avoid issues
    unmockkStatic(Item::toDraft)
    unmockkStatic(Item::toDraft2)
    unmockkStatic("com.example.testmockk.ExtensionsKt")
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
  // If you need to use this feature you are doing something wrong probably
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

  @Test
  fun `mock class level extension function`() {
    val userId = "1234"
    val adCategory = "fake_category"
    val expected = Item(userId, adCategory)
    with (mockk<ClassExtension>()) {
      every { Item(userId, adCategory).reverseProperties() } returns expected

      // You have to check inside ClassExtension scope
      val result = Item(userId, adCategory).reverseProperties()

      assertEquals(expected, result)
    }
  }

  @Test
  fun `mock object level extension function`() {
    val userId = "1234"
    val adCategory = "fake_category"
    val expected = Item(userId, adCategory)
    mockkObject(ObjectExtension)
    with(ObjectExtension) {
      every { Item(userId, adCategory).reverseProperties() } returns expected
    }

    // As objects are single static instances you can check outside ObjectExtension scope
    val result = Item(userId, adCategory).reverseProperties()

    assertEquals(expected, result)
  }

  @Test
  fun `mock top level extension functions`() {
    // Declare full file extension function to enable mocking
    mockkStatic("com.example.testmockk.ExtensionsKt")

    val userId = "1234"
    val adCategory = "fake_category"
    val expected = "new_ad_id"
    val expected2 = "adId_${userId}_${adCategory}"
    val item = Item(userId, adCategory)
    every { any<Item>().toDraft() } returns Draft(expected)

    val result = item.toDraft().adId
    val result2 = item.toDraft2().adId

    assertEquals(expected, result)
    assertEquals(expected2, result2)

    // Ensure you disable all static mocks to avoid issues
  }

  @Test
  fun `mock top level extension functions, other way`() {
    val userId = "1234"
    val adCategory = "fake_category"
    val expected = "new_ad_id"
    val expected2 = "adId_${userId}_${adCategory}"
    val item = Item(userId, adCategory)

    // Declare any extension function to enable mocking on all file
    mockkStatic(Item::toDraft)
    every { any<Item>().toDraft() } returns Draft(expected)
    // uncomment below lne, will fail the second assert
    // every { any<Item>().toDraft2() } returns Draft(expected)

    val result = item.toDraft().adId // mocked extension
    val result2 = item.toDraft2().adId // not mocked extension

    assertEquals(expected, result)
    assertEquals(expected2, result2)

    // Ensure you disable all static mocks to avoid issues
  }

}