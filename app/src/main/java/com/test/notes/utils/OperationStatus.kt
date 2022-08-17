package com.test.notes.utils

sealed class OperationStatus<T>(val data: T? = null, val message: String? = null) {
    class SUCCESS<T>(data: T?) : OperationStatus<T>(data)
    class LOADING<T>() : OperationStatus<T>()
}