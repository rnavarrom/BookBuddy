package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.bookbuddy.databinding.FragmentProfileBinding
import java.util.*

class ProfileFragment : Fragment(), TextToSpeech.OnInitListener {
    lateinit var binding: FragmentProfileBinding
    private var tts: TextToSpeech? = null
    private var btn: Button? = null
    private var et: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentProfileBinding.inflate(layoutInflater, container, false)
        btn = this.btn
        et = this.et
        btn = binding.btnText
        et = binding.Text
        tts = TextToSpeech(context, this)

        btn!!.setOnClickListener {
            Speak()
        }
        return binding.root
    }

    override fun onDestroy() {
        if(tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
    private fun Speak(){
        val text = et!!.text.toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    override fun onInit(p0: Int) {
        if(p0 == TextToSpeech.SUCCESS){
            val locSpanish = Locale("es", "ES")
            tts!!.language = locSpanish

            var output = tts!!.setLanguage(locSpanish) //Locale.UK

            if(output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED){

            }
        }
    }
}