package me.cpele.androcommut

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.cpele.androcommut.ui.od.OriginDestinationFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, OriginDestinationFragment.newInstance())
                    .commitNow()
        }
    }
}