package me.cpele.tnpr.autosuggest

sealed class SuggestAnswerUiModel {

    data class Some(val places: List<PlaceUiModel>) : SuggestAnswerUiModel()
    object None : SuggestAnswerUiModel()
    data class Fail(val message: String, val throwable: Throwable) : SuggestAnswerUiModel()
}
