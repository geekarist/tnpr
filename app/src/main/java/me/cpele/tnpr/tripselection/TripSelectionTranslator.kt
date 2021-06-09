package me.cpele.tnpr.tripselection

interface TripSelectionTranslator {

    fun processTransferType(transferType: String?): String?
    fun processMode(mode: String?): String?
}
