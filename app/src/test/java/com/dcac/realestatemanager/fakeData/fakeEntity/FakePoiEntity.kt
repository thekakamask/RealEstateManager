package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity

object FakePoiEntity {

    val poi1 = PoiEntity(1L, "Franprix République", "Supermarché", true)
    val poi2 = PoiEntity(2L, "École Primaire Jean Jaurès", "École")
    val poi3 = PoiEntity(3L, "Pharmacie République", "Pharmacie")
    val poi4 = PoiEntity(4L, "Lycée Voltaire", "École")
    val poi5 = PoiEntity(5L, "Carrefour Express", "Supermarché")

    val poi6 = PoiEntity(6L, "Intermarché Montmartre", "Supermarché")
    val poi7 = PoiEntity(7L, "École Jules Ferry", "École")
    val poi8 = PoiEntity(8L, "Pharmacie Montmartre", "Pharmacie")
    val poi9 = PoiEntity(9L, "Lycée Renoir", "École")
    val poi10 = PoiEntity(10L, "Monoprix Pigalle", "Supermarché")

    val poi11 = PoiEntity(11L, "Casino Market Mouffetard", "Supermarché")
    val poi12 = PoiEntity(12L, "École des Mines", "École")
    val poi13 = PoiEntity(13L, "Pharmacie Mouffetard", "Pharmacie")
    val poi14 = PoiEntity(14L, "Lycée Henri-IV", "École")
    val poi15 = PoiEntity(15L, "Franprix Rue Monge", "Supermarché")

    val poi16 = PoiEntity(16L, "Franprix Bastille", "Supermarché")
    val poi17 = PoiEntity(17L, "École Maternelle Roquette", "École")
    val poi18 = PoiEntity(18L, "Pharmacie Roquette", "Pharmacie")
    val poi19 = PoiEntity(19L, "Lycée Charlemagne", "École")
    val poi20 = PoiEntity(20L, "Monoprix Bastille", "Supermarché")

    val poiEntityList = listOf(
        poi1, poi2, poi3, poi4, poi5,
        poi6, poi7, poi8, poi9, poi10,
        poi11, poi12, poi13, poi14, poi15,
        poi16, poi17, poi18, poi19, poi20
    )
}