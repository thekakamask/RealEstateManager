package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.Poi

object FakePoiModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poi1 = Poi(1L, "Franprix République", "Supermarché", true, DEFAULT_TIMESTAMP + 1)
    val poi2 = Poi(2L, "École Primaire Jean Jaurès", "École", false, DEFAULT_TIMESTAMP + 2)
    val poi3 = Poi(3L, "Pharmacie République", "Pharmacie", false, DEFAULT_TIMESTAMP + 3)
    val poi4 = Poi(4L, "Lycée Voltaire", "École", false, DEFAULT_TIMESTAMP + 4)
    val poi5 = Poi(5L, "Carrefour Express", "Supermarché", false, DEFAULT_TIMESTAMP + 5)

    val poi6 = Poi(6L, "Intermarché Montmartre", "Supermarché", false, DEFAULT_TIMESTAMP + 6)
    val poi7 = Poi(7L, "École Jules Ferry", "École", false, DEFAULT_TIMESTAMP + 7)
    val poi8 = Poi(8L, "Pharmacie Montmartre", "Pharmacie", false, DEFAULT_TIMESTAMP + 8)
    val poi9 = Poi(9L, "Lycée Renoir", "École", false, DEFAULT_TIMESTAMP + 9)
    val poi10 = Poi(10L, "Monoprix Pigalle", "Supermarché", false, DEFAULT_TIMESTAMP + 10)

    val poi11 = Poi(11L, "Casino Market Mouffetard", "Supermarché", false, DEFAULT_TIMESTAMP + 11)
    val poi12 = Poi(12L, "École des Mines", "École", false, DEFAULT_TIMESTAMP + 12)
    val poi13 = Poi(13L, "Pharmacie Mouffetard", "Pharmacie", false, DEFAULT_TIMESTAMP + 13)
    val poi14 = Poi(14L, "Lycée Henri-IV", "École", false, DEFAULT_TIMESTAMP + 14)
    val poi15 = Poi(15L, "Franprix Rue Monge", "Supermarché", false, DEFAULT_TIMESTAMP + 15)

    val poi16 = Poi(16L, "Franprix Bastille", "Supermarché", false, DEFAULT_TIMESTAMP + 16)
    val poi17 = Poi(17L, "École Maternelle Roquette", "École", false, DEFAULT_TIMESTAMP + 17)
    val poi18 = Poi(18L, "Pharmacie Roquette", "Pharmacie", false, DEFAULT_TIMESTAMP + 18)
    val poi19 = Poi(19L, "Lycée Charlemagne", "École", false, DEFAULT_TIMESTAMP + 19)
    val poi20 = Poi(20L, "Monoprix Bastille", "Supermarché", false, DEFAULT_TIMESTAMP + 20)

    val poiModelList = listOf(
        poi1, poi2, poi3, poi4, poi5,
        poi6, poi7, poi8, poi9, poi10,
        poi11, poi12, poi13, poi14, poi15,
        poi16, poi17, poi18, poi19, poi20
    )
}
