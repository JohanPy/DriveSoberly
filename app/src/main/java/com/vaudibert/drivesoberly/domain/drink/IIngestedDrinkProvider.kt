package com.vaudibert.drivesoberly.domain.drink

interface IIngestedDrinkProvider {
    fun getDrinks(): List<IIngestedDrink>
}
