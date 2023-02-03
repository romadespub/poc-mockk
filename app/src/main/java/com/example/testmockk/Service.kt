package com.example.testmockk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
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
      var counter = 0
      while(counter < 3) {
        emit(Draft(counter.toString()))
        delay(REFRESH_DELAY_IN_MS)
        counter += 1
      }
    }.onStart { delay(DELAY_IN_MS) }
}

/*interface RxService {
  fun postItem(item: Item): Single<Draft>
}*/

data class Item(val userId: String, val adCategory: String)
data class Draft(val adId: String)
data class Container(val item: Item, val draft: Draft)
