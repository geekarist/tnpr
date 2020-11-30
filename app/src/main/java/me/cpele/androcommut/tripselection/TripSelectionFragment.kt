package me.cpele.androcommut.tripselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.cpele.androcommut.R

class TripSelectionFragment : Fragment() {

    companion object {
        fun newInstance() = TripSelectionFragment()
    }

    private lateinit var viewModel: TripSelectionViewModel

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

        Toast.makeText(
            context,
            "Origin: $origin, destination: $destination",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripSelectionViewModel::class.java)
    }
}