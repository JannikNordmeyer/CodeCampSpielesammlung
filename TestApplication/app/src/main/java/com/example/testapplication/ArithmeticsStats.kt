import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.GameNames
import com.example.testapplication.MyApplication
import com.example.testapplication.StatisticsViewModel
import com.example.testapplication.databinding.FragmentArithmeticsStatsBinding
import com.example.testapplication.databinding.FragmentTicTacToeStatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class ArithmeticsStats : Fragment() {

    private lateinit var binding: FragmentArithmeticsStatsBinding
    lateinit var viewmodel: StatisticsViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentArithmeticsStatsBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(StatisticsViewModel::class.java)

        viewmodel.ArithmeticsData.observe(viewLifecycleOwner){
            load()
        }

        return view
    }
    fun load(){

        if(viewmodel.ArithmeticsData.value == null){
            return
        }
        binding.gamesPlayedText.text =viewmodel.ArithmeticsGamesPlayed.toString()
        binding.winPercentageText.text = viewmodel.ArithmeticsWinPercentage.toString()

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.ArithmeticsData.value)

        binding.winGraph.getViewport().setScalable(true)
        binding.winGraph.getViewport().setScrollable(true)
        binding.winGraph.getViewport().setScalableY(true)
        binding.winGraph.getViewport().setScrollableY(true)
        binding.winGraph.getViewport().setXAxisBoundsManual(true)
        binding.winGraph.getViewport().setMaxX(viewmodel.ArithmeticsWinCount.toDouble())

        binding.winGraph.addSeries(series)
        binding.winGraph.title = "Win Percentage:"
        binding.winGraph.graphContentHeight

        val series2: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.ArithmeticsHighScore.value)

        binding.scoreGraph.getViewport().setScalable(true)
        binding.scoreGraph.getViewport().setScrollable(true)
        binding.scoreGraph.getViewport().setScalableY(true)
        binding.scoreGraph.getViewport().setScrollableY(true)
        binding.scoreGraph.getViewport().setXAxisBoundsManual(true)
        binding.scoreGraph.getViewport().setMaxX(viewmodel.ArithmeticsWinCount.toDouble())

        binding.scoreGraph.addSeries(series2)
        binding.scoreGraph.title = "High Score Development:"
        binding.scoreGraph.graphContentHeight
    }

}