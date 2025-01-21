package com.example.datarecoverynew.views.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.example.datarecoverynew.R
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivitySelectLanguagesBinding
import com.example.datarecoverynew.models.LanguageModel
import com.example.datarecoverynew.views.adapters.LanguagesAdapter
import com.example.diabetes.helper.ads.isNetworkAvailable
import com.example.recoverydata.interfaces.DataListener

class SelectLanguagesActivity : BaseActivity() {
    lateinit var binding: ActivitySelectLanguagesBinding
    lateinit var adapter: LanguagesAdapter
    val languages = ArrayList<LanguageModel>()
    val allLanguages = ArrayList<LanguageModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLanguagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        clickListener()
    }

    private fun clickListener() {
        binding.searchIV.setOnClickListener {
            var searchValue = binding.searchED.text.toString().trim()
            if (searchValue.isEmpty()) {
                binding.searchED.setError(getString(R.string.enter_value))
            } else {
                languages.clear()
                val list = allLanguages.filter {
                    it.name.contains(searchValue, true)
                }
                languages.addAll(list)
                if (languages.isNotEmpty()) {
                    binding.noTV.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.notifyDataSetChanged()
                    binding.noTV.visibility = View.VISIBLE
                }
            }
        }
        binding.searchED.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchValue = s.toString().trim()
                if (searchValue.isEmpty()) {
                    languages.clear()
                    languages.addAll(allLanguages)
                    adapter.notifyDataSetChanged()
                } else {
                    languages.clear()
                    val list = allLanguages.filter {
                        it.name.contains(searchValue, true)
                    }
                    languages.addAll(list)
                    if (languages.isNotEmpty()) {
                        binding.noTV.visibility = View.GONE
                        adapter.notifyDataSetChanged()
                    } else {
                        adapter.notifyDataSetChanged()
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.backIV.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {

        languages.add(LanguageModel(R.drawable.ic_english, "English"))
        languages.add(LanguageModel(R.drawable.ic_afrikaans, "Afrikaans"))
        languages.add(LanguageModel(R.drawable.ic_arabic, "Arabic"))
        languages.add(LanguageModel(R.drawable.ic_afrikaans, "Afrikaans"))
        languages.add(LanguageModel(R.drawable.ic_china, "Chinese"))
        languages.add(LanguageModel(R.drawable.ic_danish, "Danish"))
        languages.add(LanguageModel(R.drawable.ic_dutch, "Dutch"))
        languages.add(LanguageModel(R.drawable.ic_french, "French"))
        languages.add(LanguageModel(R.drawable.ic_germen, "German"))
        languages.add(LanguageModel(R.drawable.ic_hindi, "Hindi"))
        languages.add(LanguageModel(R.drawable.ic_italian, "Italian"))
        languages.add(LanguageModel(R.drawable.ic_japnees, "Japanese"))
        languages.add(LanguageModel(R.drawable.ic_korean, "Korean"))
        languages.add(LanguageModel(R.drawable.ic_malay, "Malay"))
        languages.add(LanguageModel(R.drawable.ic_norwegian, "Norwegian"))
        languages.add(LanguageModel(R.drawable.ic_portuguese, "Portuguese"))
        languages.add(LanguageModel(R.drawable.ic_russian, "Russian"))
        languages.add(LanguageModel(R.drawable.ic_spanish, "Spanish"))
        languages.add(LanguageModel(R.drawable.ic_swedish, "Swedish"))
        languages.add(LanguageModel(R.drawable.ic_thai, "Thai"))
        languages.add(LanguageModel(R.drawable.ic_turkish, "Turkish"))
        languages.add(LanguageModel(R.drawable.ic_ukrainian, "Ukrainian"))
//        languages.add(LanguageModel(R.drawable.ic_vietnamese,"Vietnamese"))

        allLanguages.addAll(languages)
        adapter = LanguagesAdapter(
            this,
            this@SelectLanguagesActivity,
            0,
            languages,
            object : DataListener {
                override fun onRecieve(any: Any) {
                    var position = any as Int
                    position += 1;

                    if (position != -1) {
                        if (position == 1) {
                            setLanguage("en")
                        } else if (position == 2) {
                            setLanguage("af")
                        } else if (position == 3) {
                            setLanguage("ar")
                        } else if (position == 4) {
                            setLanguage("zh")
                        } else if (position == 5) {
                            setLanguage("da")
                        } else if (position == 6) {
                            setLanguage("nl")
                        } else if (position == 7) {
                            setLanguage("fr")
                        } else if (position == 8) {
                            setLanguage("de")
                        } else if (position == 9) {
                            setLanguage("hi")
                        } else if (position == 10) {
                            setLanguage("it")
                        } else if (position == 11) {
                            setLanguage("ja")
                        } else if (position == 12) {
                            setLanguage("ko")
                        } else if (position == 13) {
                            setLanguage("ms")
                        } else if (position == 14) {
                            setLanguage("no")
                        } else if (position == 15) {
                            setLanguage("pt")
                        } else if (position == 16) {
                            setLanguage("ru")
                        } else if (position == 17) {
                            setLanguage("es")
                        } else if (position == 18) {
                            setLanguage("sv")
                        } else if (position == 19) {
                            setLanguage("th")
                        } else if (position == 20) {
                            setLanguage("tr")
                        } else if (position == 21) {
                            setLanguage("uk")
                        } else if (position == 22) {
                            setLanguage("vi")
                        }
                        startActivity(
                            Intent(
                                this@SelectLanguagesActivity,
                                HowToUseActivity::class.java
                            )
                        )
                    }
                }
            }
        )
        binding.recylerView.adapter = adapter

    }
}


/* if(position != -1) {
     if (position == 1) {
         setLanguage("af")
     } else if (position == 2) {
         setLanguage("")
     } else if (position == 3) {
         setLanguage("zh")
     } else if (position == 4) {
         setLanguage("da")
     } else if (position == 5) {
         setLanguage("nl")
     } else if (position == 6) {
         setLanguage("en")
     } else if (position == 7) {
         setLanguage("fr")
     } else if (position == 8) {
         setLanguage("de")
     } else if (position == 9) {
         setLanguage("hi")
     } else if (position == 10) {
         setLanguage("it")
     } else if (position == 11) {
         setLanguage("ja")
     } else if (position == 12) {
         setLanguage("ko")
     } else if (position == 13) {
         setLanguage("ms")
     } else if (position == 14) {
         setLanguage("no")
     } else if (position == 15) {
         setLanguage("pt")
     } else if (position == 16) {
         setLanguage("ru")
     } else if (position == 17) {
         setLanguage("es")
     } else if (position == 18) {
         setLanguage("sv")
     } else if (position == 19) {
         setLanguage("th")
     } else if (position == 20) {
         setLanguage("tr")
     } else if (position == 21) {
         setLanguage("uk")
     } else if (position == 22) {
         setLanguage("vi")
     }
     startActivity(Intent(this@LanguageActivity,HowToUseActivity::class.java))

}*/