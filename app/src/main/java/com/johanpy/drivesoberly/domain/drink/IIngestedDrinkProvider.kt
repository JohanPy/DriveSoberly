package com.johanpy.drivesoberly.domain.drink

interface IIngestedDrinkProvider {
    fun getDrinks(): List<IIngestedDrink>
}
