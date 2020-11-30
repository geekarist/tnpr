package me.cpele.androcommut.tripselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.CustomApp
import me.cpele.androcommut.R
import me.cpele.androcommut.tripselection.TripSelectionViewModel.Intention

class TripSelectionFragment : Fragment() {

    companion object {
        fun newInstance() = TripSelectionFragment()
    }

    private val viewModel: TripSelectionViewModel by viewModels {
        ViewModelFactory {
            TripSelectionViewModel(
                CustomApp.instance.navitiaService
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trip_selection_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val (origin, destination) = arguments
            ?.let { TripSelectionFragmentArgs.fromBundle(it) }
            ?.apply { origin to destination }
            ?: throw IllegalArgumentException("Arguments must not be null")

        viewModel.dispatch(Intention.Load(origin, destination))
    }
}