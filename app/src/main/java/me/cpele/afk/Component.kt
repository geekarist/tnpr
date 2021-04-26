package me.cpele.afk

import androidx.lifecycle.LiveData

/**
 * Business logic component.
 *
 * A [Component] processes [ActionT]s.
 * It applies some logic to it.
 * It holds and exposes a [StateT], and produces [ConsequenceT]s.
 * The [ConsequenceT]s can be consumed via [Event].
 *
 * @param ActionT Action
 * @param StateT Component state
 * @param ConsequenceT Consequence for the caller (payload of a consumable [Event]
 *
 * The naming follows [github.com/.../Decompose](https://github.com/arkivanov/Decompose).
 */
// TODO: Move to afk module
interface Component<ActionT, StateT, ConsequenceT> {
    fun dispatch(action: ActionT)
    val stateLive: LiveData<StateT>
    val eventLive: LiveData<Event<ConsequenceT>>
}
