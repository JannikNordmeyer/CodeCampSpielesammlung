import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.testapplication.GameNames
import com.example.testapplication.MyApplication
import com.example.testapplication.databinding.FragmentChallengeStatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class ChallengeStats : Fragment() {

    private lateinit var binding: FragmentChallengeStatsBinding
    var gamesPlayed = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChallengeStatsBinding.inflate(inflater,container,false)
        val view = binding.root

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(
            GameNames.SCHRITTZAEHLER.toString()).child("GamesPlayed").get().addOnSuccessListener {
            if(it != null){
                binding.gamesPlayedText.setText(it.value.toString())
                gamesPlayed = it.value.toString().toInt()
            }
        }

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(
            GameNames.SCHRITTZAEHLER.toString()).child("Win%").get().addOnSuccessListener {
            if(it != null){

                var winCount = 0.0
                var winPer = 0.0

                var data = arrayOf<DataPoint>()

                it.children.forEach(){

                    if(it.value.toString().toInt() > 0){

                        winCount += 1
                        winPer = winCount / it.value.toString().toFloat()
                        data = data.plus(DataPoint(winCount, winPer))
                    }

                }
                binding.winPercentageText.setText(winPer.toString())
                val series: LineGraphSeries<DataPoint> = LineGraphSeries(data)

                binding.winGraph.getViewport().setScalable(true)
                binding.winGraph.getViewport().setScrollable(true)
                binding.winGraph.getViewport().setScalableY(true)
                binding.winGraph.getViewport().setScrollableY(true)
                binding.winGraph.getViewport().setXAxisBoundsManual(true)
                binding.winGraph.getViewport().setMinX(1.0)
                binding.winGraph.getViewport().setMaxX(winCount)

                binding.winGraph.addSeries(series)
                binding.winGraph.title = "Win Percentage:"
                binding.winGraph.graphContentHeight
            }
        }

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(
            GameNames.SCHRITTZAEHLER.toString()).child("HighScore").get().addOnSuccessListener {
            if(it != null){

                var data = arrayOf<DataPoint>()

                var counter = 0.0
                it.children.forEach(){

                    data = data.plus(DataPoint(counter, it.value.toString().toDouble()))
                    counter += 1


                }
                val series: LineGraphSeries<DataPoint> = LineGraphSeries(data)

                binding.scoreGraph.getViewport().setScalable(true)
                binding.scoreGraph.getViewport().setScrollable(true)
                binding.scoreGraph.getViewport().setScalableY(true)
                binding.scoreGraph.getViewport().setScrollableY(true)
                binding.scoreGraph.getViewport().setXAxisBoundsManual(true)
                binding.scoreGraph.getViewport().setMinX(0.0)
                binding.scoreGraph.getViewport().setMaxX(counter-1)

                binding.scoreGraph.addSeries(series)
                binding.scoreGraph.title = "High Scores:"
                binding.scoreGraph.graphContentHeight
            }
        }



        return view
    }

}