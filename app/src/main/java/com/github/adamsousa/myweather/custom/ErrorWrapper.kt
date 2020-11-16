package com.github.adamsousa.myweather.custom

class ErrorWrapper<T : Enum<T>> (
    val exception: Exception,
    val errorType: T)