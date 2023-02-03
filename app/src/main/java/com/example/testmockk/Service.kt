package com.example.testmockk

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.internal.operators.observable.ObservableFromArray
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.TimeUnit
import kotlin.experimental.ExperimentalTypeInference

private const val DELAY_IN_MS = 500L
private const val REFRESH_DELAY_IN_MS = 1000L

internal class Service {
  fun postItem(item: Item): Draft {
    return item.toDraft()
  }

  fun putItem(@Suppress("UNUSED_PARAMETER")item: Item): Item {
    throw RuntimeException("putItem failed message")
  }

  fun deleteItem(item: Item): Unit {
  }

  fun reset(item: Item): Item {
    return Item(item.userId, boom())
  }

  private fun boom() = "no category!!"
}

class CoroutineService {

  private val service = Service()

  suspend fun postItem(item: Item): Draft {
    delay(DELAY_IN_MS)
    return service.postItem(item)
  }

  suspend fun putItem(item: Item): Item {
    delay(DELAY_IN_MS)
    return service.putItem(item)
  }

  suspend fun deleteItem(item: Item): Unit {
    delay(DELAY_IN_MS)
    return service.deleteItem(item)
  }
}

class FlowService {

  private val service = Service()

  fun postItem(item: Item): Flow<Draft> =
    flow {
      emit(service.postItem(item))
    }.onStart { delay(DELAY_IN_MS) }

  fun putItem(item: Item): Flow<Item> =
    flow {
      emit(service.putItem(item))
    }.onStart { delay(DELAY_IN_MS) }

  fun deleteItem(item: Item): Flow<Unit> =
    flow {
      emit(service.deleteItem(item))
    }.onStart { delay(DELAY_IN_MS) }

  fun sync(): Flow<Draft> =
    flow {
      emit(Draft("0"))
      delay(REFRESH_DELAY_IN_MS)
      emit(Draft("1"))
      delay(REFRESH_DELAY_IN_MS)
      emit(Draft("2"))
      delay(REFRESH_DELAY_IN_MS)
    }.onStart { delay(DELAY_IN_MS) }
}

class RxService {

  private val service = Service()

  fun postItem(item: Item): Single<Draft> =
    Single
      .just(service.postItem(item))
      .delay(DELAY_IN_MS, TimeUnit.MILLISECONDS)

  fun putItem(item: Item): Observable<Item> =
    Observable
      .just(service.putItem(item))
      .delay(DELAY_IN_MS, TimeUnit.MILLISECONDS)

  fun sync(): Observable<Draft> =
    ObservableFromArray(
      arrayOf(
        Draft("0"),
        Draft("1"),
        Draft("2")
      )
    ).delay(DELAY_IN_MS, TimeUnit.MILLISECONDS)
}

data class Item(val userId: String, val adCategory: String)
data class Draft(val adId: String)
data class Container(val item: Item, val draft: Draft)
