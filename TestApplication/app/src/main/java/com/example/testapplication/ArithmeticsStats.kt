import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.StatisticsViewModel
import com.example.testapplication.databinding.FragmentArithmeticsStatsBinding
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class ArithmeticsStats : Fragment() {

    private lateinit var binding: FragmentArithmeticsStatsBinding
    lateinit var viewmodel: StatisticsViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentArithmeticsStatsBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(StatisticsViewModel::class.java)

        viewmodel.ArithmeticsData.observe(viewLifecycleOwner){
            load()
        }

        return view
    }
    //Läd Daten aus dem Viewmodel
    private fun load(){

        if(viewmodel.ArithmeticsData.value == null){
            return
        }
        binding.gamesPlayedText.text =viewmodel.ArithmeticsGamesPlayed.toString()
        binding.winPercentageText.text = viewmodel.ArithmeticsWinPercentage.toString()

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.ArithmeticsData.value)

        binding.winGraph.viewport.isScalable = true
        binding.winGraph.viewport.isScrollable = true
        binding.winGraph.viewport.setScalableY(true)
        binding.winGraph.viewport.setScrollableY(true)
        binding.winGraph.viewport.isXAxisBoundsManual = true
        binding.winGraph.viewport.setMaxX(viewmodel.ArithmeticsWinCount.toDouble())

        binding.winGraph.addSeries(series)
        binding.winGraph.title = "Win Percentage:"
        binding.winGraph.graphContentHeight

        val series2: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.ArithmeticsHighScore.value)

        binding.scoreGraph.viewport.isScalable = true
        binding.scoreGraph.viewport.isScrollable = true
        binding.scoreGraph.viewport.setScalableY(true)
        binding.scoreGraph.viewport.setScrollableY(true)
        binding.scoreGraph.viewport.isXAxisBoundsManual = true
        binding.scoreGraph.viewport.setMaxX(viewmodel.ArithmeticsWinCount.toDouble())

        binding.scoreGraph.addSeries(series2)
        binding.scoreGraph.title = "High Score Development:"
        binding.scoreGraph.graphContentHeight
    }

}