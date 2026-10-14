package com.jbncode.anotadordomino.ui.util

object AdGate {
    /** Actívalo justo antes de abrir cámara o galería. */
    @Volatile var suppressNextAppOpen = false
}