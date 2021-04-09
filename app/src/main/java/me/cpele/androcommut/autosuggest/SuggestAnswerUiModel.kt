package me.cpele.androcommut.autosuggest

sealed class SuggestAnswerUiModel {

    data class Some(val places: List<PlaceUiModel>) : SuggestAnswerUiModel()
}
