package com.jarly.domain

enum class Zone { FRIDGE, FREEZER, CUPBOARD }

enum class UnitKind(val suffix: String) {
    PIECES("pc"),
    GRAMS("g"),
    KILOGRAMS("kg"),
    MILLILITRES("ml"),
    LITRES("l"),
    PACKS("pack");
}
