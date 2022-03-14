package com.example.testapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentTicTacToeStatsBinding
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class TicTacToeStats : Fragment() {

    private lateinit var binding: FragmentTicTacToeStatsBinding
    lateinit var viewmodel: StatisticsViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTicTacToeStatsBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(StatisticsViewModel::class.java)

        viewmodel.TicTacToeData.observe(viewLifecycleOwner){
            load()
        }

        return view
    }
    fun load(){

        if(viewmodel.TicTacToeData.value == null){
            return
        }
        binding.gamesPlayedText.text =viewmodel.TicTacToeGamesPlayed.toString()
        binding.winPercentageText.text = viewmodel.TicTacToeWinPercentage.toString()

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.TicTacToeData.value)

        binding.winGraph.getViewport().setScalable(true)
        binding.winGraph.getViewport().setScrollable(true)
        binding.winGraph.getViewport().setScalableY(true)
        binding.winGraph.getViewport().setScrollableY(true)
        binding.winGraph.getViewport().setXAxisBoundsManual(true)
        binding.winGraph.getViewport().setMaxX(viewmodel.TicTacToeWinCount.toDouble())

        binding.winGraph.addSeries(series)
        binding.winGraph.title = "Win Percentage:"
        binding.winGraph.graphContentHeight
    }

}