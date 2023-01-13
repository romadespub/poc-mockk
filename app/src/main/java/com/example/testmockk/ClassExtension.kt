package com.example.testmockk

class ClassExtension {
  fun Item.reverseProperties(): Item = Item(adCategory, userId)
}