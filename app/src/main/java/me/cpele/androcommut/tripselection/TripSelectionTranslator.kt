package me.cpele.androcommut.tripselection

interface TripSelectionTranslator {

    fun processTransferType(transferType: String?): String?
    fun processMode(mode: String?): String?
}
