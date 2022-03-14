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

class GeoportalStats : Fragment() {

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
    fun load(){

        if(viewmodel.CompassData.value == null){
            return
        }
        binding.gamesPlayedText.text =viewmodel.CompassGamesPlayed.toString()
        binding.winPercentageText.text = viewmodel.CompassWinPercentage.toString()

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.CompassData.value)

        binding.winGraph.getViewport().setScalable(true)
        binding.winGraph.getViewport().setScrollable(true)
        binding.winGraph.getViewport().setScalableY(true)
        binding.winGraph.getViewport().setScrollableY(true)
        binding.winGraph.getViewport().setXAxisBoundsManual(true)
        binding.winGraph.getViewport().setMaxX(viewmodel.CompassWinCount.toDouble())

        binding.winGraph.addSeries(series)
        binding.winGraph.title = "Win Percentage:"
        binding.winGraph.graphContentHeight
    }

}