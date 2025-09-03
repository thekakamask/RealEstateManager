package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity

object FakePoiEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poi1 = PoiEntity(1L, "Franprix République", "Supermarché", isSynced = true, DEFAULT_TIMESTAMP + 1)
    val poi2 = PoiEntity(2L, "École Primaire Jean Jaurès", "École",false,DEFAULT_TIMESTAMP + 2)
    val poi3 = PoiEntity(3L, "Pharmacie République", "Pharmacie",false,DEFAULT_TIMESTAMP + 3)
    val poi4 = PoiEntity(4L, "Lycée Voltaire", "École",false,DEFAULT_TIMESTAMP + 4)
    val poi5 = PoiEntity(5L, "Carrefour Express", "Supermarché",false,DEFAULT_TIMESTAMP + 5)

    val poi6 = PoiEntity(6L, "Intermarché Montmartre", "Supermarché",false,DEFAULT_TIMESTAMP + 6)
    val poi7 = PoiEntity(7L, "École Jules Ferry", "École",false,DEFAULT_TIMESTAMP + 7)
    val poi8 = PoiEntity(8L, "Pharmacie Montmartre", "Pharmacie",false,DEFAULT_TIMESTAMP + 8)
    val poi9 = PoiEntity(9L, "Lycée Renoir", "École",false,DEFAULT_TIMESTAMP + 9)
    val poi10 = PoiEntity(10L, "Monoprix Pigalle", "Supermarché",false,DEFAULT_TIMESTAMP + 10)

    val poi11 = PoiEntity(11L, "Casino Market Mouffetard", "Supermarché",false,DEFAULT_TIMESTAMP + 11)
    val poi12 = PoiEntity(12L, "École des Mines", "École",false,DEFAULT_TIMESTAMP + 12)
    val poi13 = PoiEntity(13L, "Pharmacie Mouffetard", "Pharmacie",false,DEFAULT_TIMESTAMP + 13)
    val poi14 = PoiEntity(14L, "Lycée Henri-IV", "École",false,DEFAULT_TIMESTAMP + 14)
    val poi15 = PoiEntity(15L, "Franprix Rue Monge", "Supermarché",false,DEFAULT_TIMESTAMP + 15)

    val poi16 = PoiEntity(16L, "Franprix Bastille", "Supermarché",false,DEFAULT_TIMESTAMP + 16)
    val poi17 = PoiEntity(17L, "École Maternelle Roquette", "École",false,DEFAULT_TIMESTAMP + 17)
    val poi18 = PoiEntity(18L, "Pharmacie Roquette", "Pharmacie",false,DEFAULT_TIMESTAMP + 18)
    val poi19 = PoiEntity(19L, "Lycée Charlemagne", "École",false,DEFAULT_TIMESTAMP + 19)
    val poi20 = PoiEntity(20L, "Monoprix Bastille", "Supermarché",false,DEFAULT_TIMESTAMP + 20)

    val poiEntityList = listOf(
        poi1, poi2, poi3, poi4, poi5,
        poi6, poi7, poi8, poi9, poi10,
        poi11, poi12, poi13, poi14, poi15,
        poi16, poi17, poi18, poi19, poi20
    )
}