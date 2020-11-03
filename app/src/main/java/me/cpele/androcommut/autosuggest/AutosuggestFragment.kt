package me.cpele.androcommut.autosuggest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.cpele.androcommut.R

class AutosuggestFragment : Fragment() {

    companion object {
        fun newInstance() = AutosuggestFragment()
    }

    private lateinit var viewModel: AutosuggestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.autosuggest_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AutosuggestViewModel::class.java)
        // TODO: Use the ViewModel
    }

}