package ru.alzhanov.drinkkollect.models

import drinkollect.v1.DrinkollectOuterClass.S3Resource
import kotlinx.datetime.Instant


data class OwnDrinkPost(
    override val name: String,
    override val description: String,
    override val image: S3Resource,
    override val location: String,
    override val author: String,
    override val timestamp: Instant,
    val likes: Long,
) : DrinkPost {
}