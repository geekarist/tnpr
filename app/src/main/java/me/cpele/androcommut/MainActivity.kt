package me.cpele.androcommut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.FlowPreview
import me.cpele.androcommut.autosuggest.AutosuggestFragment
import me.cpele.androcommut.autosuggest.AutosuggestFragmentDirections
import me.cpele.androcommut.origdest.OriginDestinationFragment
import me.cpele.androcommut.origdest.OriginDestinationFragmentDirections

@FlowPreview
class MainActivity : AppCompatActivity(),
    OriginDestinationFragment.Listener,
    AutosuggestFragment.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }

    override fun openAutosuggestOrigin(fragment: Fragment) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationToAutosuggest(
                AutosuggestFragment.Trigger.ORIGIN
            )
        )

    override fun openAutosuggestDestination(fragment: Fragment) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationToAutosuggest(
                AutosuggestFragment.Trigger.DESTINATION
            )
        )

    override fun takeAutosuggestion(
        fragment: Fragment,
        trigger: AutosuggestFragment.Trigger?,
        label: String
    ) {
        fragment.findNavController().navigate(
            AutosuggestFragmentDirections.actionAutosuggestFragmentToOriginDestinationFragment(
                label.takeIf { trigger == AutosuggestFragment.Trigger.ORIGIN },
                label.takeIf { trigger == AutosuggestFragment.Trigger.DESTINATION }
            )
        )
    }
}