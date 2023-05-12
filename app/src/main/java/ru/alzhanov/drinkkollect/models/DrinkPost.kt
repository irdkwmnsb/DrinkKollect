package ru.alzhanov.drinkkollect.models

import drinkollect.v1.DrinkollectOuterClass.S3Resource
import kotlinx.datetime.Instant


interface DrinkPost {
    val name: String
    val description: String
    val image: S3Resource
    val location: String
    val author: String
    val timestamp: Instant
    val id: Long
}