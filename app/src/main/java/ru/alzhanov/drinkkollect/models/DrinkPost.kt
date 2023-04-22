package ru.alzhanov.drinkkollect.models

import kotlinx.datetime.Instant


interface DrinkPost {
    val name: String
    val description: String
    val image: Int
    val location: String
    val author: String
    val timestamp: Instant
}