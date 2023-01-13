package com.example.testmockk

fun Item.toDraft(): Draft = createDraft(this)
fun Item.toDraft2(): Draft = createDraft(this)

private fun createDraft(item: Item) =
  Draft("adId_${item.userId}_${item.adCategory}")