package com.example.testmockk

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

private const val DELAY_IN_MS = 500L
private const val REFRESH_DELAY_IN_MS = 1000L

class BlockingService {
  fun postItem(item: Item): Draft {
    return item.toDraft()
  }

  fun putItem(item: Item): Item {
    throw RuntimeException("putItem failed message")
  }

  fun deleteItem(item: Item): Unit {
  }

  fun reset(item: Item): Item {
    return Item(item.userId, boom())
  }

  private fun boom() = "no category!!"
}

/*interface RxService {
  fun postItem(item: Item): Single<Draft>
}*/

class CoroutineService {

  private val blockingService = BlockingService()

  suspend fun postItem(item: Item): Draft {
    delay(DELAY_IN_MS)
    return blockingService.postItem(item)
  }

  suspend fun putItem(item: Item): Item {
    delay(DELAY_IN_MS)
    return blockingService.putItem(item)
  }

  suspend fun deleteItem(item: Item): Unit {
    delay(DELAY_IN_MS)
    return blockingService.deleteItem(item)
  }
}

class FlowService {

  private val blockingService = BlockingService()

  fun postItem(item: Item): Flow<Draft> =
    flow {
      emit(blockingService.postItem(item))
    }.onStart { delay(DELAY_IN_MS) }

  fun putItem(item: Item): Flow<Item> =
    flow {
      emit(blockingService.putItem(item))
    }.onStart { delay(DELAY_IN_MS) }

  fun deleteItem(item: Item): Flow<Unit> =
    flow {
      emit(blockingService.deleteItem(item))
    }.onStart { delay(DELAY_IN_MS) }

  fun sync(): Flow<Draft> =
    flow {
      var counter = 0
      while(counter < 5) {
        emit(Draft(counter.toString()))
        delay(REFRESH_DELAY_IN_MS)
        counter += 1
      }
    }.onStart { delay(DELAY_IN_MS) }
}

data class Item(val userId: String, val adCategory: String)
data class Draft(val adId: String)
data class Container(val item: Item, val draft: Draft)
