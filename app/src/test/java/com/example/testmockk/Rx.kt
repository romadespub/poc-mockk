package com.example.testmockk

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.internal.operators.observable.ObservableFromArray
import org.junit.Assert
import org.junit.Test

class Rx {

  private val item = Item("userId", "adCategory")
  private val draft = Draft("adId")
  private val emitions: List<Draft> = listOf(Draft("0"), Draft("1"), Draft("2"))

  @Test
  fun `basic Single mock`() {
    val expected = draft
    val service = mockk<RxService>() {
      every { postItem(any()) } returns Single.just(expected)
    }

    // Optionally, when using Single:
    // val result = service.postItem(item).blockingGet()
    val result = service.postItem(item)
      .test()
      .values()
      .first()

    Assert.assertEquals(expected, result)
  }

  @Test
  fun `basic Observable mock`() {
    val expected = item
    val service = mockk<RxService>() {
      every { putItem(any()) } returns Observable.just(item)
    }

    // Optionally, when using Observable:
    // val result = service.putItem(item).blockingIterable().first()
    val result = service.putItem(item)
      .test()
      .values()
      .first()

    Assert.assertEquals(expected, result)
  }

  @Test
  fun `catch several emitions`() {
    val expected = emitions
    val service = mockk<RxService>() {
      every { sync() } returns ObservableFromArray(emitions.toTypedArray())
    }

    val result = service.sync()
      .test()
      .values()

    Assert.assertEquals(expected, result)
  }

  @Test
  fun `catch last emitted`() {
    val expected = emitions.last()
    val service = mockk<RxService>() {
      every { sync() } returns ObservableFromArray(emitions.toTypedArray())
    }

    val result = service.sync()
      .test()
      .values()
      .last()

    Assert.assertEquals(expected, result)
  }

  @Test(expected = RuntimeException::class)
  fun `check exception`() {
    val service = mockk<RxService>() {
      every { putItem(any()) } throws RuntimeException()
    }

    service.putItem(item)
      .test()
  }

  @Test
  fun `check error`() {
    val expected = RuntimeException("putItem failed message")
    val service = mockk<RxService>() {
      every { putItem(any()) } returns Observable.error(expected)
    }

    service.putItem(item)
      .test()
      .assertError(expected)
  }

  @Test
  fun `check completion`() {
    val service = mockk<RxService>() {
      every { sync() } returns ObservableFromArray(emitions.toTypedArray())
    }

    service.sync()
      .test()
      .assertComplete()
  }

}
