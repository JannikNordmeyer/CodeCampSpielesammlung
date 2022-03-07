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
import com.example.testapplication.databinding.FragmentChallengeStatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class ChallengeStats : Fragment() {

    private lateinit var binding: FragmentChallengeStatsBinding
    lateinit var viewmodel: StatisticsViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChallengeStatsBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(StatisticsViewModel::class.java)

        viewmodel.ChallengeData.observe(viewLifecycleOwner){
            load()
        }

        return view
    }
    fun load(){

        if(viewmodel.ChallengeData.value == null){
            return
        }
        binding.gamesPlayedText.text =viewmodel.ChallengeGamesPlayed.toString()
        binding.winPercentageText.text = viewmodel.ChallengeWinPercentage.toString()

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.ChallengeData.value)

        binding.winGraph.getViewport().setScalable(true)
        binding.winGraph.getViewport().setScrollable(true)
        binding.winGraph.getViewport().setScalableY(true)
        binding.winGraph.getViewport().setScrollableY(true)
        binding.winGraph.getViewport().setXAxisBoundsManual(true)
        binding.winGraph.getViewport().setMaxX(viewmodel.ChallengeWinCount.toDouble())

        binding.winGraph.addSeries(series)
        binding.winGraph.title = "Win Percentage:"
        binding.winGraph.graphContentHeight

        val series2: LineGraphSeries<DataPoint> = LineGraphSeries(viewmodel.ChallengeHighScore.value)

        binding.scoreGraph.getViewport().setScalable(true)
        binding.scoreGraph.getViewport().setScrollable(true)
        binding.scoreGraph.getViewport().setScalableY(true)
        binding.scoreGraph.getViewport().setScrollableY(true)
        binding.scoreGraph.getViewport().setXAxisBoundsManual(true)
        binding.scoreGraph.getViewport().setMaxX(viewmodel.ChallengeWinCount.toDouble())

        binding.scoreGraph.addSeries(series2)
        binding.scoreGraph.title = "High Score Development:"
        binding.scoreGraph.graphContentHeight
    }

}