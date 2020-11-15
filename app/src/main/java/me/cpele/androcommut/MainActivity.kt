package me.cpele.androcommut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.cpele.androcommut.origdest.OriginDestinationFragment
import me.cpele.androcommut.origdest.OriginDestinationFragmentDirections

class MainActivity : AppCompatActivity(), OriginDestinationFragment.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }

    override fun openAutosuggestOrigin(fragment: Fragment) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationFragmentToAutosuggestFragment()
        )

    override fun openAutosuggestDestination(fragment: Fragment) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationFragmentToAutosuggestFragment()
        )
}