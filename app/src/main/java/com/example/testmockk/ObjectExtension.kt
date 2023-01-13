package com.example.testmockk

object ObjectExtension {
  fun Item.reverseProperties(): Item = Item(adCategory, userId)
}