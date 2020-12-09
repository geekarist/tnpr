package me.cpele.androcommut

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.FlowPreview
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.autosuggest.AutosuggestFragment
import me.cpele.androcommut.autosuggest.AutosuggestFragmentDirections
import me.cpele.androcommut.autosuggest.AutosuggestTrigger
import me.cpele.androcommut.origdest.OriginDestinationFragment
import me.cpele.androcommut.origdest.OriginDestinationFragmentDirections

@FlowPreview
class MainActivity : AppCompatActivity(),
    OriginDestinationFragment.Listener,
    AutosuggestFragment.Listener {

    private val viewModel: MainViewModel by viewModels { ViewModelFactory { MainViewModel() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        viewModel.eventLive.observe(this) { event ->
            event.consume { effect ->
                when (effect) {
                    is MainViewModel.Effect.SuggestionIdentified -> {
                        val fragment = supportFragmentManager.findFragmentById(effect.fragmentId)
                        fragment?.findNavController()?.navigate(
                            AutosuggestFragmentDirections.actionAutosuggestFragmentToOriginDestinationFragment(
                                effect.originId,
                                effect.originLabel,
                                effect.destinationId,
                                effect.destinationLabel
                            )
                        )
                    }
                }
            }
        }
    }

    override fun openAutosuggestOrigin(fragment: Fragment) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationToAutosuggest(
                AutosuggestTrigger.ORIGIN
            )
        )

    override fun openAutosuggestDestination(fragment: Fragment) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationToAutosuggest(
                AutosuggestTrigger.DESTINATION
            )
        )

    override fun openTrip(
        fragment: Fragment,
        originId: String,
        originLabel: String,
        destinationId: String,
        destinationLabel: String
    ) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationToTripSelection(
                originId,
                originLabel,
                destinationId,
                destinationLabel
            )
        )

    override fun takeAutosuggestion(
        fragment: Fragment,
        trigger: AutosuggestTrigger,
        id: String,
        label: String
    ) = viewModel.dispatch(MainViewModel.Intention.Suggestion(fragment.id, trigger, id, label))
}