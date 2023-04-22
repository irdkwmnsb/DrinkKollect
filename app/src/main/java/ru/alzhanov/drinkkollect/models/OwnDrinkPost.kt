package ru.alzhanov.drinkkollect.models

import kotlinx.datetime.Instant


data class OwnDrinkPost(
    override val name: String,
    override val description: String,
    override val image: Int,
    override val location: String,
    override val author: String,
    override val timestamp: Instant,
    val likes: Int,
) : DrinkPost