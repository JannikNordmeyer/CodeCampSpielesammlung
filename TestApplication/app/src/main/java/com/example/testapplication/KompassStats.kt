import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.StatisticsViewModel
import com.example.testapplication.databinding.FragmentGeoportalStatsBinding
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class KompassStats : Fragment() {

    private lateinit var binding: FragmentGeoportalStatsBinding
    lateinit var viewmodel: StatisticsViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGeoportalStatsBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(StatisticsViewModel::class.java)

        viewmodel.CompassData.observe(viewLifecycleOwner){
            load()
        }

        return view
    }
    //Läd Daten aus dem Viewmodel
    private fun load(){

        if(viewmodel.CompassData.value == null){
            return
        }
        binding.gamesPlayedText.text =viewmodel.CompassGamesPlayed.toString()
        var winP = (viewmodel.CompassWinPercentage * 100).toString()
        if(winP.length > 6){ binding.winPercentageText.text = winP.substring(0,7)+"%" }
        else{ binding.winPercentageText.text = winP+"%" }

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.CompassData.value)

        binding.winGraph.viewport.isScalable = true
        binding.winGraph.viewport.isScrollable = true
        binding.winGraph.viewport.setScalableY(true)
        binding.winGraph.viewport.setScrollableY(true)
        binding.winGraph.viewport.isXAxisBoundsManual = true
        binding.winGraph.viewport.setMaxX(viewmodel.CompassWinCount.toDouble())

        binding.winGraph.addSeries(series)
        binding.winGraph.title = "Win Percentage:"
        binding.winGraph.graphContentHeight
    }

}