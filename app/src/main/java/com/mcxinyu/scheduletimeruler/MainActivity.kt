package com.mcxinyu.scheduletimeruler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mcxinyu.scheduletimeruler.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inflate.root)
    }
}