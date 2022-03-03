import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.testapplication.GameNames
import com.example.testapplication.MyApplication
import com.example.testapplication.databinding.FragmentGeoportalStatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class GeoportalStats : Fragment() {

    private lateinit var binding: FragmentGeoportalStatsBinding
    var gamesPlayed = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGeoportalStatsBinding.inflate(inflater,container,false)
        val view = binding.root

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(
            GameNames.COMPASS.toString()).child("GamesPlayed").get().addOnSuccessListener {
            if(it != null){
                binding.gamesPlayedText.setText(it.value.toString())
                gamesPlayed = it.value.toString().toInt()
            }
        }

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(
            GameNames.COMPASS.toString()).child("Win%").get().addOnSuccessListener {
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



        return view
    }

}